package cn.iecas.geoai.labelplatform.service.impl;

import cn.aircas.utils.file.FileUtils;
import cn.aircas.utils.image.geo.GeoUtils;
import cn.iecas.geoai.labelplatform.dao.ImageMapper;
import cn.iecas.geoai.labelplatform.dao.LabelDatasetFileMapper;
import cn.iecas.geoai.labelplatform.dao.LabelProjectMapper;
import cn.iecas.geoai.labelplatform.dao.LabelTaskMapper;
import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.domain.*;
import cn.iecas.geoai.labelplatform.entity.dto.AIRequestParam;
import cn.iecas.geoai.labelplatform.entity.dto.ResourceInfo;
import cn.iecas.geoai.labelplatform.entity.emun.CoordinateSystemType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelProjectStatus;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import cn.iecas.geoai.labelplatform.entity.fileFormat.PZLabelObjectInfo;
import cn.iecas.geoai.labelplatform.service.AILabelService;
import cn.iecas.geoai.labelplatform.service.FileService;
import cn.iecas.geoai.labelplatform.service.LabelDatasetFileService;
import cn.iecas.geoai.labelplatform.service.LabelDatasetService;
import cn.iecas.geoai.labelplatform.util.XMLUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.cache.Cache;
import com.google.common.collect.Sets;
import com.sun.javafx.binding.SelectBinding;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class AILabelServiceImpl implements AILabelService {

    @Value("${value.dir.absolutePath}")
    private String absolutePath;

    @Value("${value.dir.labDir}")
    private String labDir;

    @Value("${value.dir.inoutDir}")
    private String inoutDir;

    @Value("${value.ai-label.service-detail}")
    private String serviceDetail;

    @Value("${value.ai-label.preprocessing}")
    private String imagePreprocess;

    @Autowired
    HttpServletRequest request;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LabelDatasetService labelDatasetService;

    @Autowired
    LabelDatasetFileService labelDatasetFileService;

    @Autowired
    FileService imageService;

    @Autowired
    ImageMapper imageMapper;

    @Autowired
    LabelProjectMapper labelProjectMapper;

    @Autowired
    LabelTaskMapper labelTaskMapper;

    @Autowired
    LabelDatasetFileMapper labelDatasetFileMapper;

    @Value(value = "${value.ai-label.service-info}")
    private String service_info;

    @Value(value = "${value.ai-label.call-service}")
    private String call_service;

    @Value(value = "${value.ai-label.task-status}")
    private String task_status;

    @Value(value = "${value.ai-label.task-result}")
    private String task_result;

    @Value(value = "${value.api.file-info}")
    private String imageInfoApi;

    @Value(value = "${value.dir.rootDir}")
    private String rootDir;

    private Map<String, String> filePaths = new HashMap<>();



    @Override
    public List<LabelCategory> listAIAlgorithms(String serviceName){
        URI uri = null;
        String token = request.getHeader("token");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        if (serviceName != null && !serviceName.equals("")) {
            String rules = "{\"partial_match_rules\":{\"service_name\":\""+serviceName+"\"}}";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(service_info).queryParam("rules", rules);
            uri = builder.build().encode().toUri();
        } else {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(service_info);
            uri = builder.build().encode().toUri();
        }
        JSONObject jsonObject = restTemplate.exchange(uri, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
        ArrayList<LabelCategory> arrayList = new ArrayList();
//        Map<String,JSONObject> map = new LinkedHashMap<>();
//        map = JSONObject.parseObject(String.valueOf(jsonObject),Map.class);
//        for(JSONObject value : map.values()){
//            LabelCategory labelCategory = new LabelCategory();
//            labelCategory.setId(value.getString("id"));
//            labelCategory.setCategoryName(value.getString("service_name"));
//            arrayList.add(labelCategory);
//        }
        JSONArray algorithmArrays = jsonObject.getJSONArray("data");
        if (algorithmArrays == null) {
            return arrayList;
        }
        for (int index = 0; index < algorithmArrays.size(); index++) {
            JSONObject algorithm = algorithmArrays.getJSONObject(index);
            LabelCategory labelCategory = new LabelCategory();
            labelCategory.setId(algorithm.getString("id"));
            labelCategory.setCategoryName(algorithm.getString("service_name"));
            labelCategory.setServiceId(algorithm.getString("id"));
            labelCategory.setVersionId(algorithm.getIntValue("version_id"));
            arrayList.add(labelCategory);
        }
        return arrayList;
    }

    @Async
    @Override
    public void callAIService(LabelProject labelProject,String token) {
        String serviceId = labelProject.getServiceId();
        int datasetId = Integer.parseInt(labelProject.getRelatedDatasetId());
        List<Integer> imageIdList = labelDatasetService.listImageIdByDatasetId(datasetId);
        if (imageIdList.size() != 0) {
            JSONObject result = new JSONObject();
            String imageIds = imageIdList.toString().replace("[", "").replace("]", "").replace(" ", "");

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("token", token);
            HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);

            try {
                result = restTemplate.exchange(imageInfoApi + imageIds, HttpMethod.GET, httpEntity, JSONObject.class).getBody();
            } catch (ResourceAccessException e) {
                log.error("访问影像服务接口api：{} 出错", imageInfoApi + imageIds);
                throw new ResourceAccessException("关联服务访问出错");
            }
            List<Image> imageList = JSONArray.parseArray(result.getJSONArray("data").toJSONString(), Image.class);

            List<String> inputFileList = new ArrayList<>();
            for (Image image : imageList) {
                String path = image.getPath();
                inputFileList.add(path);
            }
            List<String> outputFileList = new ArrayList<>();
            for (String inputFile : inputFileList) {
                String name = inputFile.substring(0, inputFile.indexOf("."));
                String outputFile = name + ".det" + ".xml";
                outputFileList.add(outputFile);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("service_id", serviceId);
            jsonObject.put("input_file", inputFileList);
            jsonObject.put("output_file", outputFileList);
            JSONObject object = restTemplate.postForEntity(call_service, jsonObject, JSONObject.class).getBody();
            System.out.println(object);
            String tokenId = object.getString("Token_ID");
            String RootUrl = task_status + tokenId;
            JSONObject rootObject = restTemplate.exchange(RootUrl, HttpMethod.POST,httpEntity,JSONObject.class).getBody();
            while (rootObject != null) {
                if (rootObject.getString("Error_Message").equals("WAITING")) {
                    try {
                        System.out.println(rootObject);
                        Thread.sleep(5000);
                        rootObject = restTemplate.exchange(RootUrl, HttpMethod.POST,httpEntity,JSONObject.class).getBody();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(rootObject);
                    break;
                }
            }
            JSONObject taskresult_object = restTemplate.exchange(task_result
                    + tokenId, HttpMethod.POST,httpEntity,JSONObject.class).getBody();
            log.info(taskresult_object.toString());


            for (int i = 0; i < imageIdList.size(); i++) {
                int imageId = imageIdList.get(i);
                Image imageInfo = imageList.stream().filter(image->image.getId()==imageId).findFirst().get();
                String imagePath = imageInfo.getPath();
                String outputFoderFile = imagePath.substring(0, imagePath.indexOf(".")) + ".det.xml";
                String outputFileName = outputFoderFile.substring(outputFoderFile.lastIndexOf("/")).replace("/", "");
                QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("image_id", imageId).eq("dataset_id", labelProject.getDatasetId());
                LabelDatasetFile labelDatasetFile = labelDatasetFileMapper.selectOne(queryWrapper);

                double point_1 = imageInfo.getMaxLat();
                double point_2 = imageInfo.getMaxLon();
                double point_3 = imageInfo.getMinLat();
                double point_4 = imageInfo.getMinLon();
                LabelPointType labelPointType;
                labelPointType = (point_1 == 0 && point_2 == 0 && point_3 == 0 && point_4 == 0)? LabelPointType.PIXEL : LabelPointType.GEODEGREE ;
                JSONObject perImageObject = taskresult_object.getJSONObject(outputFileName);
                labelDatasetFile.setAiLabel(dataProcess(perImageObject,labelPointType,imageInfo));
                labelDatasetFile.setLabel(labelDatasetFile.getAiLabel());
                System.out.println(labelDatasetFile.getAiLabel());
                labelDatasetFile.setStatus(LabelStatus.UNAPPLIED);
                labelDatasetFileMapper.updateById(labelDatasetFile);

            }
            labelProject.setStatus(LabelProjectStatus.LABELING);
            labelProjectMapper.updateById(labelProject);
        }
    }

    public String dataProcess(JSONObject perImageObject , LabelPointType labelPointType , Image image) {
        JSONObject object = ((perImageObject.getJSONObject("annotation"))).getJSONObject("objects");
        JSONArray jsonArray = object.getJSONArray("object");
        String coordinate = jsonArray.getJSONObject(0).getString("coordinate");
        // 算法问题使得coordinate坐标类型错误，临时处理 TODO getao
        //coordinate = coordinate == null ? "PIXEL":coordinate;
        //coordinate = labelPointType.getValue() == 0 ? "geodegree" : "pixel";
        //imagePath = rootDir + File.separator + "2.tiff";
        gdal.AllRegister();
        String imagePath = FileUtils.getStringPath(rootDir,image.getPath());
        Dataset dataset = gdal.Open(imagePath);
        List<JSONObject> objectList = new ArrayList<>();
        String labelType;
        if (labelPointType.getValue() == 0) {
            labelType = "geodegree";
        } else {
            labelType = "pixel";
        }

        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject perObject = jsonArray.getJSONObject(i);
            JSONObject perBox = perObject.getJSONObject("points");
            JSONArray pointArray = perBox.getJSONArray("point");
            List<String> pointList = new ArrayList<>();
            for (int j = 0; j < pointArray.size(); j++) {
                String point = pointArray.getString(j);
                double x = Double.parseDouble(point.split(",")[0].trim());
                double y = Double.parseDouble(point.split(",")[1].trim());
                double coordinates[] = new double[]{x,y};
                if (!coordinate.equals(labelType)) {
                    if (labelPointType.getValue() == 0) {
                        coordinates = image.getCoordinateSystemType() == CoordinateSystemType.PROJCS ?
                                GeoUtils.pixel2Coordinate(x, y, imagePath, GeoUtils.COORDINATE_PROJECTION):
                                GeoUtils.pixel2Coordinate(x, y, imagePath, GeoUtils.COORDINATE_LONLAT);
                    } else {
                        coordinates = GeoUtils.convertCoordinateToPixel(x, y, imagePath, GeoUtils.COORDINATE_LONLAT);
                        coordinates[1] = dataset.getRasterYSize() - coordinates[1];
                    }
                } else {
                    if ("geodegree".equalsIgnoreCase(coordinate)) {
                        if (image.getCoordinateSystemType() == CoordinateSystemType.PROJCS) {
                            coordinates = GeoUtils.coordinateConvertor(x, y, imagePath, GeoUtils.COORDINATE_PROJECTION);
                        }
                    }else{
                        coordinates[0] = x;
                        coordinates[1] = dataset.getRasterYSize() -y;
                    }
                }
                point = coordinates[0] + ", " + coordinates[1];
                pointList.add(point);
            }
            perBox.put("point", pointList);
            if (labelPointType.getValue() == 0) {
                perObject.put("coordinate", "geodegree");
            } else {
                perObject.put("coordinate", "pixel");
            }
            perObject.put("points", perBox);
            objectList.add(perObject);
        }
        object.put("object", objectList);
        return object.toString();
    }


    @Override
    public CommonResult<JSONObject> checkBackState(String tokenId) {
        String token = request.getHeader("token");
        String RootUrl = task_status + "?task_id="+tokenId;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        JSONObject rootObject = restTemplate.exchange(RootUrl, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
        log.info("++++++++++++++==============:{}", rootObject.toString());
        //String errorMessage = rootObject.getJSONObject("data").getString("Error_Message");
        CommonResult<JSONObject> result = new CommonResult<>();
        result.setCode((Integer) rootObject.get("code"));
        Map<String, Object> resultData = (Map<String, Object>) rootObject.get("data");
        Set<Map.Entry<String, Object>> entrySet = resultData.entrySet();
        JSONObject data = new JSONObject();
        for (Map.Entry<String, Object> entry : entrySet) {
            data.put(entry.getKey(), entry.getValue());
        }
        result.setData(data);
        result.setMessage((String) rootObject.get("message"));
        return result;
    }

    public void writeTxt(String txtPath , String content) {

        FileWriter fw = null;
        try {
            File file = new File(txtPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                }
            }
            fw = new FileWriter(txtPath);
            fw.write("{\"sentences\":[\"" + content +  "\"]}");
        }catch (Exception e) {
            log.error("ai文本写入错误");
        }finally {
            try {
                fw.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getTokenId(AIRequestParam params) {
        String fileName = "";
        List<String> outputFileList = new ArrayList<>();
        String txtPath = null;
        String outPutPath = null;
        if (params.getTaskType() == 2) {
            txtPath = rootDir + File.separator +  "txt" + File.separator + "in" + File.separator + System.currentTimeMillis() + ".txt";
            writeTxt(txtPath , params.getContent());
            fileName = txtPath.substring(txtPath.lastIndexOf("/") + 1);
            outPutPath = txtPath.replace("in" , "out");
            outPutPath = txtPath.replace("in" , "out");
            if (!new File(outPutPath).getParentFile().exists()) new File(outPutPath).getParentFile().mkdirs();

        }else {
            for (int index = 0; index < params.getImagePathList().size(); index++) {
                String sourcePath = params.getImagePathList().get(index);
                String imagePath = FileUtils.getStringPath(labDir, sourcePath).replace("\\", "/");
                params.getImagePathList().set(index, imagePath);
            }
            for (String imagePath : params.getImagePathList()) {
                String outputFile = imagePath.substring(0, imagePath.indexOf(".")) + ".det_" + System.currentTimeMillis() + ".xml";
                fileName = outputFile.substring(outputFile.lastIndexOf("/") + 1);
                outputFileList.add(outputFile);
            }
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", request.getHeader("token"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("service_id", params.getServiceId());
        jsonObject.put("version_id", params.getVersionId());
        if (params.getTaskType() == 2) {
            jsonObject.put("input_file", txtPath.replace(rootDir , labDir));
            jsonObject.put("output_file", outPutPath.replace(rootDir ,labDir));
        }else {
            jsonObject.put("input_file", params.getImagePathList());
            jsonObject.put("output_file", outputFileList);
        }
        jsonObject.put("source_service_id", params.getSource_service_id());
        jsonObject.put("working_type", params.getWorking_type());
        jsonObject.put("running_type", params.getRunning_type());
//        ResourceInfo resourceInfo = ResourceInfo.builder()
//                .cpu_count(params.getResource_info().getCpu_count())
//                .mem_size(params.getResource_info().getMem_size())
//                .gpu_dict(params.getResource_info().getGpu_dict())
//                .shm_size(params.getResource_info().getShm_size()).build();
//        jsonObject.put("resource_info", resourceInfo);
        HttpEntity<JSONObject> entity = new HttpEntity<>(jsonObject,httpHeaders);
        String tokenId = "";
        try {
            JSONObject object = restTemplate.exchange(call_service, HttpMethod.POST, entity, JSONObject.class).getBody();
            tokenId = object.getJSONObject("data").getString("task_id");
            filePaths.put(tokenId, fileName);
        } catch (Exception e) {
            throw new RuntimeException("调用获取tokenId三方接口报错");
        }
        return tokenId;
    }

    @Override
    public JSONObject getLabelMessage(int imageId, int taskId, String tokenId, LabelPointType labelPointType, HttpServletRequest request , int taskType) {
        try{
            List<Integer> imageIdList = new ArrayList<>();

            String token = request.getHeader("token");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("token",token);
            HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
            JSONObject aiLabelResult = restTemplate.exchange(task_result +"?task_id="+ tokenId, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
            String labelResult = null;
            JSONArray keywords = null;
            assert aiLabelResult != null;
            if(aiLabelResult.getJSONObject("data")!= null) {
                String txtPath = aiLabelResult.getJSONObject("data").getString("result_file_address");

                LabelTask labelTask = labelTaskMapper.selectById(taskId);
                int datasetId = labelTask.getLabelDatasetId();
                if (taskType == 2) {
                    LabelDatasetFile labelDatasetFile = labelDatasetFileMapper.selectById(imageId);
                    imageId = labelDatasetFile.getFileId();
                }
                QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("file_id", imageId).eq("dataset_id", datasetId);
                LabelDatasetFile labelDatasetFile = labelDatasetFileMapper.selectOne(queryWrapper);

                if (taskType == 1) {
                    imageIdList.add(imageId);
                    List<JSONObject> fileList = imageService.listFileInfoByIdList(imageIdList, DatasetType.IMAGE,null);
                    List<Image> imageList = fileList.stream().map(file -> JSONObject.parseObject(file.toJSONString(), Image.class)).collect(Collectors.toList());
                    String fileName = filePaths.get(tokenId);
                    aiLabelResult = aiLabelResult.getJSONObject("data").getJSONObject(fileName);
                    labelResult = dataProcess(aiLabelResult, labelPointType, imageList.get(0));
                }else if (taskType == 2) {
                    labelResult = txtToJson(txtPath.replace(labDir,rootDir));
                    JSONArray entities = JSONObject.parseObject(labelResult).getJSONObject("object").getJSONArray("entities");
                    JSONArray keyWordS = new JSONArray();
                    HashSet<String> set = Sets.newHashSet();
                    JSONArray jsonArray = JSONArray.parseArray(labelTask.getKeywords());
                    if (jsonArray.size() >0) {
                        for (Object o : jsonArray) {
                            JSONObject jsonObject = JSONObject.parseObject(o.toString());
                            String name = jsonObject.getString("name");
                            set.add(name);
                        }

                    }

                    for (Object entity : entities) {
                        JSONObject entityJson = JSONObject.parseObject(entity.toString());
                        String type = entityJson.getString("type");
                        set.add(type);

                    }
                    int i = 0;
                    for (String type : set) {

                        String[] rgb = { "rgb(59,146,247)", "rgb(124,186,79)", "rgb(174,119,187)", "rgb(230,219,85)", "rgb(52,30,151)",
                                "rgb(118,134,201)", "rgb(211,193,76)", "rgb(108,230,228)", "rgb(160,181,243)", "rgb(239,126,49)",
                                "rgb(191,193,86)", "rgb(114,212,183)", "rgb(167,57,216)", "rgb(141,149,90)", "rgb(226,133,229)",
                                "rgb(188,165,234)", "rgb(115,170,208)", "rgb(35,93,169)", "rgb(178,64,139)", "rgb(75,167,145)" };
                        JSONObject jsonObject = new JSONObject();
                        if (i >= 20) {
                            int r = new Random().nextInt(100) + 100;
                            int g = new Random().nextInt(100) + 100;
                            int b = new Random().nextInt(100) + 100;

                            jsonObject.put("color", String.format("rgb(%s, %s, %s)", r, g, b));
                        }else {
                            jsonObject.put("color" , rgb[i]);
                        }
                        jsonObject.put("name" , type);

                        keyWordS.add(jsonObject);

                        i += 1;

                    }
                    keywords = keyWordS;
                    labelTask.setKeywords(keyWordS.toString());
                    labelTaskMapper.updateById(labelTask);
                    int labelProjectId = labelTask.getLabelProjectId();
                    labelProjectMapper.updateKeyWords(labelProjectId , keyWordS.toString());
                }
                labelDatasetFile.setAiLabel(labelResult);
                labelDatasetFileMapper.updateById(labelDatasetFile);
            }
            JSONObject jsonObject = JSONObject.parseObject(labelResult);
            jsonObject.put("keywords" , keywords);
            return jsonObject;
        }catch (HttpServerErrorException e){
            return null;
        }
    }

    private String txtToJson(String txtPath) {
        File file = new File(txtPath);
        StringBuilder json = new StringBuilder();
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;

        try {
            fileInputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String read = null;
            while ((read = bufferedReader.readLine()) != null) {
                json.append(read);
            }

            String result = json.toString().replace("sentence", "content")
                    .replace("mentions", "entities");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("object", JSONArray.parseArray(result).getJSONObject(0));

            return jsonObject.toString();

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null ) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void updateLabelInfo(int imageId, int taskId) {
        LabelTask labelTask = labelTaskMapper.selectById(taskId);
        int datasetId = labelTask.getLabelDatasetId();
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_id", imageId).eq("dataset_id", datasetId);
        LabelDatasetFile labelDatasetFile = labelDatasetFileMapper.selectOne(queryWrapper);
        if (StringUtils.isNotBlank(labelDatasetFile.getLabel())){
            JSONObject labelInfo = JSONObject.parseObject(labelDatasetFile.getLabel());
            JSONObject aiLabelInfo = JSONObject.parseObject(labelDatasetFile.getAiLabel());

            //获取智能标注结果数组
            JSONArray aiLabelArray = aiLabelInfo.getJSONArray("object");

            //获取人工标注结果数组
            JSONArray labelArray = labelInfo.getJSONArray("object");

            //合并人工与智能标注结果
            labelArray.addAll(aiLabelArray);
            labelInfo.put("object",labelArray);
            labelDatasetFile.setLabel(labelInfo.toJSONString());
        }else {
            labelDatasetFile.setLabel(labelDatasetFile.getAiLabel());
        }

        labelDatasetFile.setAiLabel("");
        labelDatasetFileMapper.updateById(labelDatasetFile);
    }

    /**
     * 获取服务版本信息
     * @param serviceId
     * @return
     */
    @Override
    public List<LabelCategory> getServoceVersionById(int serviceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", request.getHeader("token"));
        String url = serviceDetail + "/" + serviceId;
        HttpEntity<JSONObject> entity = new HttpEntity<>(null, headers);
        JSONObject result = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class).getBody();
        List<LabelCategory> versionInfo = (List<LabelCategory>) JSONObject.parseObject(JSON.toJSON(result.get("data")).toString()).get("version");
        log.info(versionInfo.toString());
        return versionInfo;
    }

    /**
     * 创建标注项目时，设置影像文件的预处理结果路径
     * @param labelProject
     * @param token
     */
    @Async
    @Override
    public void setImagePretreatPath(LabelProject labelProject, String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", token);
        HttpEntity<Object> entity = new HttpEntity<>(null, httpHeaders);

        Set<Integer> unPretreatFileIds = new HashSet<>();
        int datasetId = labelProject.getDatasetId();
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<LabelDatasetFile>().eq("dataset_id", datasetId);
        List<LabelDatasetFile> labelDatasetFileList = this.labelDatasetFileMapper.selectList(queryWrapper);
        log.info("数据集文件数量为：{}",labelDatasetFileList.size());
        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("file_id", labelDatasetFile.getFileId()).ne("preprocess_path", null);
            if (this.labelDatasetFileMapper.selectCount(queryWrapper) == 0) {
                JSONObject fileInfoById = this.imageService.getFileInfoById(labelDatasetFile.getFileId(),token);
                URI url = UriComponentsBuilder.fromHttpUrl(imagePreprocess)
                        .queryParam("imgPath", fileInfoById.getString("path")).build().encode().toUri();
                unPretreatFileIds.add(labelDatasetFile.getId());
                log.info("正在进行智能辅助预处理：{}",fileInfoById.getString("path"));
                try {
                    String preprocessPath = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
                    log.info("智能辅助预处理{}结束：{}",fileInfoById.getString("path"),preprocessPath);
                    labelDatasetFile.setPreprocessPath(preprocessPath);
                    UpdateWrapper<LabelDatasetFile> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.set("preprocess_path",preprocessPath)
                            .set(!labelProject.isCooperate(), "status", LabelStatus.UNAPPLIED)
                            .set(labelProject.isCooperate(), "status", LabelStatus.LABELING)
                            .eq("id",labelDatasetFile.getId());
                    this.labelDatasetFileService.update(updateWrapper);
                }catch (RestClientException e){
                    log.error("智能辅助预处理{}失败",fileInfoById.getString("path"));
                }
            } else {
                log.info("数据集文件为 {} 的文件 {} 已存在预处理记录", labelDatasetFile.getId(), labelDatasetFile.getFileId());
            }
        }
    }

}

