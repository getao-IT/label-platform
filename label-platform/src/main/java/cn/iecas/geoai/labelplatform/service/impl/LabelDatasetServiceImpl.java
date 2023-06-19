package cn.iecas.geoai.labelplatform.service.impl;

import cn.aircas.utils.date.DateUtils;
import cn.aircas.utils.file.FileUtils;
import cn.aircas.utils.image.geo.GeoUtils;
import cn.iecas.geoai.labelplatform.dao.LabelDatasetFileMapper;
import cn.iecas.geoai.labelplatform.dao.LabelDatasetImageInfoMapper;
import cn.iecas.geoai.labelplatform.dao.LabelDatasetMapper;
import cn.iecas.geoai.labelplatform.dao.LabelProjectMapper;
import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.*;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import cn.iecas.geoai.labelplatform.entity.emun.*;
import cn.iecas.geoai.labelplatform.entity.fileFormat.LabelObject;
import cn.iecas.geoai.labelplatform.entity.fileFormat.XMLLabelObjectInfo;
import cn.iecas.geoai.labelplatform.service.*;
import cn.iecas.geoai.labelplatform.service.labelFileService.LabelFileService;
import cn.iecas.geoai.labelplatform.util.CollectionsUtils;
import cn.iecas.geoai.labelplatform.util.LabelPointTypeConvertor;
import cn.iecas.geoai.labelplatform.util.XMLUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author vanishrain
 */
@Slf4j
@Service
//@Transactional
public class LabelDatasetServiceImpl extends ServiceImpl<LabelDatasetMapper,LabelDataset> implements LabelDatasetService {

    @Value("${value.dir.rootDir}")
    private String rootDir;

    @Autowired
    private FileService fileService;

    @Autowired
    private LabelTaskService labelTaskService;

    @Autowired
    private SampleSetService sampleSetService;

    @Autowired
    private LabelProjectService labelProjectService;

    @Autowired
    private LabelDatasetFileService labelDatasetFileService;

    @Autowired
    private LabelDatasetFileMapper labelDatasetFileMapper;

    @Autowired
    private FavoriteCollectionService favoriteCollectionService;

    @Autowired
    private LabelProjectMapper labelProjectMapper;

    @Autowired
    private LabelDatasetMapper labelDatasetMapper;


    @Override
    public void copyLabelInfo(int datasetId, int userId, String labelPath) {
        File labelDirFile = FileUtils.getFile(labelPath);
        List<String> labelFileNameList = Arrays.asList(Objects.requireNonNull(labelDirFile.list()));
        Assert.notEmpty(labelFileNameList,"标注文件列表为空");

        /**
         * 创建标注项目
         */
        LabelDataset labelDataset = this.getById(datasetId);
        LabelProject labelProject = new LabelProject();
        labelProject.setKeywords("[{\"name\":\"dgfg\",\"color\":\"rgb(59,146,247)\"}]");
        labelProject.setUserId(userId);
        labelProject.setUseLabel(false);
        labelProject.setIsAiLabel(false);
        labelProject.setDefaultCheckCount(50);
        labelProject.setCategory("detection-oblique");
        labelProject.setDatasetType(DatasetType.IMAGE);
        labelProject.setLabelUserIds(String.valueOf(userId));
        labelProject.setCheckUserIds(String.valueOf(userId));
        labelProject.setDefaultLabelCount(labelDataset.getCount());
        labelProject.setProjectName(labelDataset.getDatasetName());
        labelProject.setRelatedDatasetId(String.valueOf(datasetId));
        this.labelProjectService.createLabelProject(labelProject,null);


        /**
         * 标注项目申领
         */
        QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
        labelTaskQueryWrapper.eq("label_project_id",labelProject.getId()).eq("task_type", LabelTaskType.LABEL);
        LabelTask labelTask = this.labelTaskService.getOne(labelTaskQueryWrapper);
        this.labelTaskService.applyForData(labelTask.getId());

        /**
         * 查询数据集下包含哪些影像的id，并获取image_name,id,height,coordinate_system_type字段
         */
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("file_id").eq("dataset_id",labelProject.getDatasetId());
        List<LabelDatasetFile> labelDatasetFileInfos = this.labelDatasetFileService.list(queryWrapper);
        List<Integer> fileIdList = labelDatasetFileInfos.stream().map(LabelDatasetFile::getFileId).collect(Collectors.toList());
        List<JSONObject> fileInfoList = this.fileService.listFileInfoByIdList(fileIdList,DatasetType.IMAGE,null);

        for (JSONObject fileInfo : fileInfoList) {
            String imageName = fileInfo.getString("imageName");
            String labelFileName = FilenameUtils.removeExtension(imageName) + ".xml";
            String imageFilePath = FileUtils.getStringPath(this.rootDir, fileInfo.getString("path"));
            String labelFilePath = FileUtils.getStringPath(labelPath,labelFileName);
            CoordinateSystemType originalCoordinateType = CoordinateSystemType.valueOf(fileInfo.getString("coordinateSystemType"));

            LabelObject labelObject = XMLUtils.parseXMLFromFile(XMLLabelObjectInfo.class,labelFilePath);
            LabelPointType labelPointType = LabelPointType.valueOf(labelObject.getCoordinate().toUpperCase());
            CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;


            if (LabelPointType.PIXEL == labelPointType){
                if (CoordinateSystemType.GEOGCS == originalCoordinateType)
                    coordinateConvertType = CoordinateConvertType.PIXEL_TO_LONLAT;
                if (CoordinateSystemType.PROJCS == originalCoordinateType)
                    coordinateConvertType = CoordinateConvertType.PIXEL_TO_PROJECTION;
                if (CoordinateSystemType.PIXELCS == originalCoordinateType)
                    coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;
            }
            if (LabelPointType.GEODEGREE == labelPointType){
                if (CoordinateSystemType.PROJCS == originalCoordinateType)
                    coordinateConvertType = CoordinateConvertType.PROJECTION_TO_LONLAT;
            }

            LabelPointTypeConvertor.convertLabelPointType(imageFilePath,labelObject,coordinateConvertType);
            String labelInfo = labelObject.toJSONObject().toString();
            //String labelInfo = xmlToJson(labelFilePath,imageFilePath, labelDatasetFileInfo);

            LabelCommitInfo labelCommitInfo = new LabelCommitInfo();
            labelCommitInfo.setStatus(LabelStatus.UNCHECK);
            labelCommitInfo.setLabelTaskId(labelTask.getId());
            labelCommitInfo.setLabelInfo(labelInfo);
            labelCommitInfo.setLabelFileId(fileInfo.getInteger("id"));
            this.labelTaskService.commitLabelInfo(labelCommitInfo);
        }

//        /**
//         * 影像标注信息入库
//         */
//        for (LabelDatasetFileInfo labelDatasetFileInfo : labelDatasetFileInfos) {
//            String imageName = labelDatasetFileInfo.getImageName();
//            String labelFileName = FilenameUtils.removeExtension(imageName) + ".xml";
//            String imageFileName = FileUtils.getStringPath(this.rootDir, labelDatasetFileInfo.getPath());
//            String labelFilePath = FileUtils.getStringPath(labelPath,labelFileName);
//            String labelInfo = xmlToJson(labelFilePath,imageFileName, labelDatasetFileInfo);
//
//            LabelCommitInfo labelCommitInfo = new LabelCommitInfo();
//            labelCommitInfo.setStatus(LabelStatus.UNCHECK);
//            labelCommitInfo.setLabelTaskId(labelTask.getId());
//            labelCommitInfo.setLabelInfo(labelInfo);
//            labelCommitInfo.setLabelFileId(labelDatasetFileInfo.getFileId());
//            this.labelTaskService.commitLabelInfo(labelCommitInfo);
//        }
    }

    /**
     * 创建数据集时，从文件管理接口分页获取文件信息，并判断文件是否在收藏夹中
     * @param fileSearchParam
     * @return
     */
    @Override
    public PageResult<JSONObject> getFileInfoList(FileSearchParam fileSearchParam) {
        PageResult<JSONObject> pageResult = fileService.getFileInfoByPage(fileSearchParam);
        List<JSONObject> fileInfoList = pageResult.getResult();
        if (fileInfoList == null || fileInfoList.isEmpty())
            return pageResult;
        for (JSONObject fileInfo : fileInfoList) {
            int fileId = fileInfo.getInteger("id");
            if (this.favoriteCollectionService.isInFavoriteCollection(fileSearchParam.getUserId(),fileId)){
                fileInfo.put("favorite","True");
            }else
                fileInfo.put("favorite","False");
        }
        pageResult.setResult(fileInfoList);
        return pageResult;

    }

    /**
     * 获取所有文件ID信息，用来创建数据集
     * @param fileSearchParam
     * @return
     */
    @Override
    public List<Integer> getAllFileIdList(FileSearchParam fileSearchParam) {
        return fileService.getAllFileIdList(fileSearchParam).toJavaList(Integer.class);
    }

    public  String xmlToJson(String filePath,String imagePath, LabelDatasetFileInfo labelDatasetFileInfo){
        Set<String> otherNameSet = new HashSet<>(Arrays.asList("未知","其他","真值结果","判读标注"));
        File file = new File(filePath);
        if (!file.exists())
            return null;

        CoordinateSystemType coordinateSystemType = labelDatasetFileInfo.getCoordinateSystemType();
        XMLSerializer xmlSerializer = new XMLSerializer();
        net.sf.json.JSONObject jsonLabel = net.sf.json.JSONObject.fromObject(xmlSerializer.readFromFile(file));
        net.sf.json.JSONObject finalJson = new net.sf.json.JSONObject();
        net.sf.json.JSONArray objects = new net.sf.json.JSONArray();
        if (jsonLabel.get("objects") instanceof net.sf.json.JSONObject){
            objects.add(jsonLabel.getJSONObject("objects"));
        }
        else if (jsonLabel.get("objects") instanceof net.sf.json.JSONArray){
            objects = jsonLabel.getJSONArray("objects");
        }
        Iterator<net.sf.json.JSONObject> iter = objects.iterator();
        while(iter.hasNext()){
            try {
                net.sf.json.JSONObject object = iter.next();
                String coordinate = object.getString("coordinate");
                if (object.get("possibleresult") instanceof net.sf.json.JSONObject){
                    net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
                    String name = ((net.sf.json.JSONObject) object.get("possibleresult")).getString("name").trim();

                    if (otherNameSet.contains(name))
                        ((net.sf.json.JSONObject) object.get("possibleresult")).put("name","其它");
                    if ("两栖舰艇".equalsIgnoreCase(name))
                        ((net.sf.json.JSONObject) object.get("possibleresult")).put("name","两栖舰");
                    if ("空中加油机".equalsIgnoreCase(name))
                        ((net.sf.json.JSONObject) object.get("possibleresult")).put("name","加油机");
                    if ("空中预警机".equalsIgnoreCase(name))
                        ((net.sf.json.JSONObject) object.get("possibleresult")).put("name","预警机与指挥机");

                    jsonArray.add(object.get("possibleresult"));
                    object.remove("possibleresult");
                    object.put("possibleresult",jsonArray);
                }else{
                    if (object.get("possibleresult") instanceof net.sf.json.JSONArray){
                        net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
                        String name = object.getJSONArray("possibleresult").getString(0).trim();
                        net.sf.json.JSONObject newPossibleresult = new net.sf.json.JSONObject();
                        newPossibleresult.put("name",name);
                        jsonArray.add(newPossibleresult);
                        object.remove("possibleresult");
                        object.put("possibleresult",jsonArray);
                    }
                }
                net.sf.json.JSONObject pointJson = new net.sf.json.JSONObject();
                net.sf.json.JSONArray pointsArray = object.getJSONArray("points");
                List<String> pointList = new ArrayList<>();
                for (int pointIndex = 0; pointIndex < pointsArray.size(); pointIndex++) {
                    String[] point = pointsArray.getString(pointIndex).split(",");
                    double[] coordination = new double[]{Double.parseDouble(point[0]),Double.parseDouble(point[1])};
                    if ("pixel".equalsIgnoreCase(coordinate) ){
                        //默认输入像素坐标为左上角，转换为左下角给标注使用
                        if (!CoordinateSystemType.PIXELCS.equals(coordinateSystemType)){
                            object.put("coordinate","geodegree");
                            String coordinateType  = coordinateSystemType.equals(CoordinateSystemType.PROJCS) ? GeoUtils.COORDINATE_PROJECTION : GeoUtils.COORDINATE_LONLAT;
                            coordination = GeoUtils.pixel2Coordinate(coordination[0],coordination[1],imagePath,coordinateType);
                        }else
                            coordination[1] = labelDatasetFileInfo.getHeight() - coordination[1];

                    }else {
                        if (CoordinateSystemType.PROJCS.equals(coordinateSystemType)){
                            coordination = GeoUtils.coordinateConvertor(coordination[0],coordination[1],imagePath,GeoUtils.COORDINATE_PROJECTION);
                        }
                    }
                    pointList.add(coordination[0]+","+coordination[1]);
                }
                pointJson.put("point",pointList);

                object.remove("points");
                object.put("points",pointJson);
            }catch (Exception e){
                iter.remove();
                log.info("{} 标注信息部分有误",filePath);
            }
        }

        finalJson.put("object",jsonLabel.get("objects"));
        return finalJson.toString();

    }

    /*
     * 通过datasetId查询出imageId,通过imageId查询出path
     * */
    @Override
    public List<Integer> listImageIdByDatasetId(int datasetId) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("image_id").eq("dataset_id",datasetId);
        List<Integer> imageIdList = new ArrayList<>();
        List<LabelDatasetFile> datasetList = labelDatasetFileMapper.selectList(queryWrapper);
        for(LabelDatasetFile dataset : datasetList){
            int imageId = dataset.getFileId();
            imageIdList.add(imageId);
        }
        return imageIdList;
    }



    /**
     * 按条件分页获取数据集的信息
     * @param labelDatasetsSearchRequest 数据集请求参数
     * @return 分页数据集信息
     */
    @Override
    public PageResult<LabelDataset> getLabelDatasetInfo(LabelDatasetsSearchRequest labelDatasetsSearchRequest) {
        String datasetName = labelDatasetsSearchRequest.getDatasetName();
        Page<LabelDataset> page = new Page<>(labelDatasetsSearchRequest.getPageNo(),labelDatasetsSearchRequest.getPageSize());
        QueryWrapper<LabelDataset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("visibility",true)
                .eq(labelDatasetsSearchRequest.getDatasetType() != null &&
                        labelDatasetsSearchRequest.getDatasetType() != "",
                        "dataset_type", labelDatasetsSearchRequest.getDatasetType())
                .eq(labelDatasetsSearchRequest.getCategory() != null &&
                                labelDatasetsSearchRequest.getCategory() != "",
                        "category", labelDatasetsSearchRequest.getCategory())
                .like("dataset_name",datasetName)
                .orderByDesc("create_time")
                .and(Wrapper->Wrapper.eq("user_id",labelDatasetsSearchRequest.getUserId()).or().eq("is_public",true));
        IPage<LabelDataset> iPage = this.page(page,queryWrapper);
        return new PageResult<>(iPage.getCurrent(), iPage.getTotal(), iPage.getRecords());
    }



    /**
     * 获取用户所有数据集的名称
     * @param userId 用户id
     * @return list<String> 该用户的数据集id列表</String></>
     */
    @Override
    public Map<Integer,String> getDatasetNameList(int userId, DatasetType datasetType) {
        Map<Integer,String> datasetNameMap = new HashMap<>();
        QueryWrapper<LabelDataset> queryWrapper = new QueryWrapper<>();
        queryWrapper = queryWrapper.select("id","dataset_name").eq("visibility",true)
                .eq(datasetType!=null,"dataset_type",datasetType)
                .and(Wrapper->Wrapper.eq("user_id",userId).or().eq("is_public",true));
        List<LabelDataset> labelDatasetList = this.list(queryWrapper);
        Assert.notEmpty(labelDatasetList,"没有属于该用户的数据集");
        labelDatasetList.forEach(labelDataset -> datasetNameMap.put(labelDataset.getId(),labelDataset.getDatasetName()));
        return datasetNameMap;
    }


    /**
     * 设置数据集的显隐性
     * @param datasetId 数据集id
     */
    @Override
    public void setDatasetVisible(int datasetId,boolean visible) {
        UpdateWrapper<LabelDataset> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("visibility",visible).set("create_time", DateUtils.nowDate()).eq("id",datasetId);
        this.update(updateWrapper);
    }

    /**
     * 下载manifest文件
     * @param datasetId
     * @param httpServletResponse
     * @throws IOException
     */
    @Override
    public void getManifest(int datasetId, HttpServletResponse httpServletResponse) throws IOException {
        LabelDataset labelDataset = this.getById(datasetId);
        Assert.notNull(labelDataset,"查找的数据集不存在");
        File file = FileUtils.getFile(this.rootDir,labelDataset.getDatasetPath(),"manifest.json");
        Assert.isTrue(file.exists(),"数据的manifest文件不存在");
        downloadFile(file,httpServletResponse);
        log.info("文件下载成功");
    }

    /**
     * 根据数据集创建样本集
     * @param sampleSetCreationInfo 样本集创建参数
     */
    @Override
    public void createSampleSet(SampleSetCreationInfo sampleSetCreationInfo) throws IOException {
        int datasetId = sampleSetCreationInfo.getDatasetId();
        LabelDataset labelDataset = this.getById(datasetId);
        Assert.notNull(labelDataset,"查找的数据集不存在");
        int userId = labelDataset.getUserId();
        String manifestPath = FileUtils.getStringPath(labelDataset.getDatasetPath(),"manifest.json");
        File manifestFile = FileUtils.getFile(this.rootDir,manifestPath);
        if (!manifestFile.exists())
            throw new IOException("数据集尚未生成,manifest文件不存在");

        List<LabelDatasetFile> labelDatasetFileList = this.labelDatasetFileService
                .list(new QueryWrapper<LabelDatasetFile>().select("file_id").eq("dataset_id",datasetId));
        Assert.notNull(labelDatasetFileList,"数据集下没有数据");
        sampleSetCreationInfo.setUserId(userId);
        sampleSetCreationInfo.setManifestPath(manifestPath);
        sampleSetCreationInfo.setSampleSetClassification(labelDataset.getDatasetType());

        if (StringUtils.isNotBlank(labelDataset.getProjectCategory())){
            String sampleSetCategory = labelDataset.getProjectCategory();
            if (sampleSetCategory.contains("detection-"))
                sampleSetCreationInfo.setSampleSetCategory(SampleSetCategory.DETECTION);
            else if (sampleSetCategory.contains("video-label"))
                sampleSetCreationInfo.setSampleSetCategory(SampleSetCategory.CLASSIFICATION);
            else
                sampleSetCreationInfo.setSampleSetCategory(SampleSetCategory.CHANGE_DETECTION);
        }

        sampleSetService.createSampleSet(sampleSetCreationInfo);
    }


    /**
     * 删除数据集
     * @param datasetIdList 数据集id
     * todo 根据情况判断数据集是否能够删除
     */
    @Override
    public void deleteLabelDataset(List<Integer> datasetIdList) {
        for(int datasetId : datasetIdList){
            LabelDataset labelDataset = this.getById(datasetId);
            if (labelDataset.getCategory().equals("任务生成")){
                labelDataset.setVisibility(false);
                this.updateById(labelDataset);
            }else {
                this.removeById(datasetId);
                this.labelDatasetFileService.removeAllByDatasetId(datasetId);
            }
        }

    }


    /**
     * 生成数据集的manifest文件
     * @param labelDataset 数据集信息
     * @return 生成的manifest文件路径
     */
    @Override
    public String createManifest(LabelDataset labelDataset) throws IOException, ResourceAccessException {
        int datasetId = labelDataset.getId();
        LabelProject labelProject = this.labelProjectService.getById(labelDataset.getProjectId());
        labelDataset.setProjectCategory(labelProject.getCategory());
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("file_id","label", "related_file_id").eq("dataset_id",datasetId).eq("status",LabelStatus.FINISH);
        List<LabelDatasetFile> labelDatasetFileList = this.labelDatasetFileService.list(queryWrapper);
        List<Integer> fileIdList = labelDatasetFileList.stream().map(LabelDatasetFile::getFileId).collect(Collectors.toList());
        List<JSONObject> fileInfoList = this.fileService.listFileInfoByIdList(fileIdList,labelDataset.getDatasetType(),null);
//        if (labelDataset.getDatasetType() == DatasetType.TEXT){
//            for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
//                JSONObject fileInfo = fileInfoList.get(0);
//                labelDatasetFile.setData(fileInfo);
//            }
//        }else {
//            for (JSONObject fileInfo : fileInfoList) {
//                int fileId = fileInfo.getInteger("id");
//                LabelDatasetFile labelDatasetFile = labelDatasetFileList.stream()
//                        .filter(labelDatasetFileTemp -> labelDatasetFileTemp.getFileId() == fileId).findFirst().get();
//                labelDatasetFile.setData(fileInfo);
//            }
//        }

        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            int fileId = labelDatasetFile.getFileId();
            Optional<JSONObject> fileInfoOptional = fileInfoList.stream().filter(fileInfoTemp->fileInfoTemp.getInteger("id") == fileId).findFirst();
            JSONObject fileInfo = fileInfoOptional.orElseGet(() -> fileInfoList.get(0));
            labelDatasetFile.setData(fileInfo);
        }

        LabelFileService labelFileService = labelDataset.getDatasetType().getLabelFileService();
        String manifest = labelFileService.createManifest(labelDataset,labelDatasetFileList);


        /*
        生成manifest文件
         */
        File manifestFile = FileUtils.getFile(this.rootDir,labelDataset.getDatasetPath(),"manifest.json");
        if (!manifestFile.getParentFile().exists())
            manifestFile.getParentFile().mkdirs();
        log.info("manifest文件路径为:{}",manifestFile.getAbsolutePath());
        FileWriter fileWriter = new FileWriter(manifestFile);
        fileWriter.write(StringEscapeUtils.unescapeEcmaScript(manifest));
        fileWriter.close();
        return manifestFile.getAbsolutePath();
    }

//    /**
//     * 生成数据集的manifest文件
//     * @param labelDataset 数据集信息
//     * @return 生成的manifest文件路径
//     */
//    @Override
//    public String createManifest(LabelDataset labelDataset) throws IOException, ResourceAccessException {
//        Manifest manifest = new Manifest();
//        manifest.setDatasetinfo(labelDataset);
//        int datasetId = labelDataset.getId();
//
//
//        /**
//         * 查询标注项目所关联的影像的信息
//         */
//        QueryWrapper<LabelDatasetFileInfo> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select("image_id","label","path","coordinate_system_type","source","image_name").eq("dataset_id",datasetId).eq("status",LabelStatus.FINISH);
//        List<LabelDatasetFileInfo> labelDatasetFileInfos = this.labelDatasetImageInfoMapper.selectList(queryWrapper);
//        Assert.notEmpty(labelDatasetFileInfos,"生成数据集内容为空");
//
//        //遍历影像信息，对影像label进行相应的处理
//        for (LabelDatasetFileInfo labelDatasetFileInfo : labelDatasetFileInfos) {
//            JSONObject labelJSON = JSONObject.parseObject(labelDatasetFileInfo.getLabel());
//            LabelObject labelObject = JSONObject.toJavaObject(labelJSON, XMLLabelObjectInfo.class);
//            CoordinateSystemType coordinateSystemType = labelDatasetFileInfo.getCoordinateSystemType();
//
//            CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;
//            if (coordinateSystemType == CoordinateSystemType.PROJCS)
//                coordinateConvertType = CoordinateConvertType.PROJECTION_TO_LONLAT;
//            if (coordinateSystemType == CoordinateSystemType.PIXELCS)
//                coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;
//
//            String imagePath = FileUtils.getStringPath(this.rootDir, labelDatasetFileInfo.getPath());
//            LabelPointTypeConvertor.convertLabelPointType(imagePath,labelObject,coordinateConvertType);
//
//            Image image = new Image();
//            image.setId(labelDatasetFileInfo.getFileId());
//            BeanUtils.copyProperties(labelDatasetFileInfo,image,"id");
//            manifest.addData(image,JSONObject.toJSONString(labelObject));
//        }
//
//        /*
//        生成manifest文件
//         */
//        File manifestFile = FileUtils.getFile(this.rootDir,labelDataset.getDatasetPath(),"manifest.json");
//        if (!manifestFile.getParentFile().exists())
//            manifestFile.getParentFile().mkdirs();
//        log.info("manifest文件路径为:{}",manifestFile.getAbsolutePath());
//        FileWriter fileWriter = new FileWriter(manifestFile);
//        fileWriter.write(StringEscapeUtils.unescapeEcmaScript(JSON.toJSONString(manifest, SerializerFeature.WriteMapNullValue, SerializerFeature.PrettyFormat)));
//        fileWriter.close();
//        return manifestFile.getAbsolutePath();
//    }


    @Override
    public Boolean isExistDataset(String datasetName, int userId) {
        QueryWrapper<LabelDataset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_name",datasetName).and(new Function<QueryWrapper<LabelDataset>, QueryWrapper<LabelDataset>>() {
            @Override
            public QueryWrapper<LabelDataset> apply(QueryWrapper<LabelDataset> qw) {
                return qw.eq("user_id", userId).or().eq("is_public", true);
            }
        });
        if (this.count(queryWrapper)!=0){
            return false;
        }
        return true;
    }


    /**
     * 获取指定数据集的所有文件内容
     * @param labelDatasetFileRequest 数据集影像请求参数
     * @return 符合条件的影像信息
     */
    @Override
    public PageResult<LabelDatasetOrProjectFileInfo> getFileInfoFromDataset(LabelDatasetFileRequest labelDatasetFileRequest) throws ResourceAccessException {
        int datasetId = labelDatasetFileRequest.getDatasetId();
        LabelDataset labelDataset = this.getById(datasetId);
        Assert.notNull(labelDataset,String.format("id为: %d 的数据集不能存在",datasetId));
        labelDatasetFileRequest.setDatasetType(labelDataset.getDatasetType());
        labelDatasetFileRequest.setCategory(labelDataset.getCategory());
        labelDatasetFileRequest.setVisibility(labelDataset.isVisibility());
        return this.labelDatasetFileService.getFileInfoFromDataset(labelDatasetFileRequest);
    }


    /**
     * 根据标注项目关联的数据集，生成其对应的数据集
     * @param labelProject 标注项目信息
     */
    @Override
    public int createDatasetFromProject(LabelProject labelProject, boolean useLabel) {
        List<Integer> datasetIdList = Arrays.asList(labelProject.getRelatedDatasetId().split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
        List<LabelDatasetFile> labelDatasetFileList = this.labelDatasetFileService
                .list(new QueryWrapper<LabelDatasetFile>().in("dataset_id",datasetIdList));
        LabelDataset labelDataset = LabelDataset.builder().createTime(DateUtils.nowDate()).category("任务生成").visibility(false)
                .projectId(labelProject.getId()).datasetName(labelProject.getProjectName() + "-" + DateUtils.nowDate("yyyyMMddHHmmss"))
                .userId(labelProject.getUserId()).count(labelDatasetFileList.size()).datasetType(labelProject.getDatasetType()).build();
        this.save(labelDataset);
        String datasetPath = FileUtils.getStringPath("user",labelProject.getUserId(),"dataset",labelDataset.getId());
        labelDataset.setDatasetPath(datasetPath);
        this.updateById(labelDataset);
        labelDatasetFileList.forEach(labelDatasetImage->{labelDatasetImage.setDatasetId(labelDataset.getId());
            labelDatasetImage.setFeedback(null);
            labelDatasetImage.setStatus(labelProject.getIsAiLabel()?LabelStatus.AILABELING :LabelStatus.UNAPPLIED);
            labelDatasetImage.setId(0);});

        if (!useLabel)
            labelDatasetFileList.forEach(labelDatasetImage -> {labelDatasetImage.setLabel(null);labelDatasetImage.setAiLabel(null);});
        if (labelDataset.getCategory().equals("任务生成"))
            labelDatasetFileList = labelDatasetFileList.stream().map(f -> {
                f.setAssignLabelTime(null);f.setAssignCheckTime(null);f.setFinishLabelTime(null);f.setFinishCheckTime(null);f.setCheckUserId(0);f.setLabelUserId(0);
                return f;
            }).collect(Collectors.toList());
        if (labelProject.isPreprocessing())
            labelDatasetFileList.forEach(f -> {
                f.setStatus(LabelStatus.AILABELING);
            });
        if (labelProject.isCooperate() && !labelProject.isPreprocessing())
            labelDatasetFileList.forEach(f -> f.setStatus(LabelStatus.LABELING));
        this.labelDatasetFileService.saveBatch(labelDatasetFileList);

        if (labelProject.isCooperate()) {
            List<LabelDatasetFile> datasetFileList = this.labelDatasetFileService.list(
                    new QueryWrapper<LabelDatasetFile>().eq("dataset_id", labelDataset.getId()));
            List<Integer> imageIds = datasetFileList.stream().map(LabelDatasetFile::getFileId).collect(Collectors.toList());
            labelProject.setImageIds(imageIds);
        }
        labelProject.setTotalCount(labelDatasetFileList.size());
        return labelDataset.getId();
    }

    /**
     * 创建用户数据集
     * @param labelDataset 数据集参数
     */
    @Override
    public void createLabelDatasetInfo(LabelDataset labelDataset) throws IOException {
        Set<Integer> fileIdList = new HashSet<>();

        if (labelDataset.isSelectAll() || labelDataset.getDatasetType() == DatasetType.TEXT)
            fileIdList.addAll(this.fileService.listFileIdBySearch(labelDataset));
        else
            fileIdList.addAll(labelDataset.getFileIdList());

        Assert.notEmpty(fileIdList,"请选择创建数据集需要的文件");
        labelDataset.setVisibility(true);
        labelDataset.setCreateTime(DateUtils.nowDate());
        labelDataset.setCount(fileIdList.size());
        labelDataset.setFinishCount(fileIdList.size());
        labelDataset.setCategory("用户创建");
        this.save(labelDataset);

        int datasetId = labelDataset.getId();
        List<LabelDatasetFile> labelDatasetFileList = new ArrayList<>();
        for (Integer fileId : fileIdList) {
            LabelDatasetFile labelDatasetFile = LabelDatasetFile.builder()
                    .datasetId(datasetId).fileId(fileId).status(LabelStatus.UNAPPLIED).build();
            labelDatasetFileList.add(labelDatasetFile);
        }

        if (labelDataset.isPublic())
            labelDataset.setDatasetPath(FileUtils.getStringPath("dataset",labelDataset.getDatasetName()+"-",datasetId));
        else
            labelDataset.setDatasetPath(FileUtils.getStringPath("user",labelDataset.getUserId(),"dataset",datasetId));
        this.updateById(labelDataset);

        //this.createManifest(labelDataset,labelDatasetImageList);
        labelDatasetFileService.saveBatch(labelDatasetFileList);
    }

    /**
     * 创建合并标注项目数据集
     * @param labelDataset
     * @param labelDatasetFileList
     * @param projectIds
   */
    @Override
    public void createMergeProjectDataset(LabelDataset labelDataset, List<LabelDatasetFile> labelDatasetFileList, List<Integer> projectIds) {
        Set<Integer> fileIdList = new HashSet<>();

        // 创建任务合并的数据集
        fileIdList.addAll(labelDataset.getFileIdList());

        labelDataset.setVisibility(true);
        labelDataset.setCreateTime(DateUtils.nowDate());
        labelDataset.setCount(fileIdList.size());
        labelDataset.setCategory("任务合并");
        this.save(labelDataset);

        int datasetId = labelDataset.getId();
        if (labelDataset.isPublic())
            labelDataset.setDatasetPath(FileUtils.getStringPath("dataset",labelDataset.getDatasetName()+"-",datasetId));
        else
            labelDataset.setDatasetPath(FileUtils.getStringPath("user",labelDataset.getUserId(),"dataset",datasetId));
        this.updateById(labelDataset);

        // 保存处理合并的数据集文件
        labelDatasetFileList.forEach(labelDatasetFile -> {
            labelDatasetFile.setDatasetId(datasetId);
            //labelDatasetFile.setStatus(LabelStatus.UNAPPLIED);
        });
        //this.createManifest(labelDataset,labelDatasetImageList);
        labelDatasetFileService.saveBatch(labelDatasetFileList);
    }

    /**
     * 获取合并标注项目的数据集
     * @param userInfo 当前操作用户信息
     * @param datasetIdList 合并数据集ID
     * @param labelDatasetFileList 数据集文件集合
     * @return
     */
    @Override
    public LabelDataset getMergeDataSet(UserInfo userInfo, List<Integer> datasetIdList, List<LabelDatasetFile> labelDatasetFileList) {

        // 获取合并任务对应数据集，进行数据集的重建
        List<LabelDataset> labelDatasetList = this.labelDatasetMapper.selectList(new QueryWrapper<LabelDataset>().in("id", datasetIdList));

        LabelDataset labelDataset = LabelDataset.builder().userId(userInfo.getId())
                .category("任务生成").datasetType(labelDatasetList.get(0).getDatasetType())
                .fileIdList(new ArrayList<>()).build();
        StringBuffer fileIdListBuffer = new StringBuffer();
        for (int i = 0; i < labelDatasetFileList.size(); i++) {
            fileIdListBuffer.append(labelDatasetFileList.get(i).getFileId());
            if (i != labelDatasetFileList.size()-1) {
                fileIdListBuffer.append(",");
            }
        }
        labelDataset.setFileIdList(fileIdListBuffer.toString());
        for (LabelDataset dataset : labelDatasetList) {
            // 数据集数量
            labelDataset.setCount(labelDataset.getCount() + dataset.getCount());
            // 数据集关键字
            if (labelDataset.getKeywords() == null || labelDataset.getKeywords().equals("")) {
                labelDataset.setKeywords(dataset.getKeywords());
            } else {
                labelDataset.setKeywords(labelDataset.getKeywords() + "," + dataset.getKeywords());
            }
            // 数据集名称
            if (labelDataset.getDatasetName() == null || labelDataset.getDatasetName().equals("")) {
                labelDataset.setDatasetName(dataset.getDatasetName());
                continue;
            } else {
                labelDataset.setDatasetName(labelDataset.getDatasetName() + "/" + dataset.getDatasetName());
            }
        }
        return labelDataset;
    }

    /**
     * 下载manifest文件
     * @param file manifest文件
     * @param response
     * @throws IOException
     */
    private void downloadFile(File file, HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        response.setHeader("content-type","application/octet-stream");
        response.setHeader("Content-Disposition","attachment;filename=manifest.json");
        byte[] buffer = new byte[1024];


        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            OutputStream outputStream = response.getOutputStream();
            int readCount = bufferedInputStream.read(buffer);
            while (readCount != -1) {
                outputStream.write(buffer, 0, readCount);
                readCount = bufferedInputStream.read(buffer);
            }
        }catch (IOException e){
            log.error("下载文件出错");
        }


    }

    /**
     * 将集合中的元素拼接为逗号分隔的字符串
     * @param list
     * @return
     */
    private String getStrFromList(List list) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            buffer.append(list.get(i));
            if (i < list.size() - 1) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

    /**
     * 获取标注项目数据集已通过审核的文件数量
     * @param labelDataset
     * @return
     */
    @Override
    public int getDatasetFileFinishCount(LabelDataset labelDataset) {
        int finishCount = this.labelDatasetFileService.count(new QueryWrapper<LabelDatasetFile>()
                .eq("dataset_id", labelDataset.getId()).eq("status", LabelStatus.FINISH));
        return finishCount;
    }

    @Override
    public List<String> getLabelDataPath(List<Integer> ids) {
        List<LabelDataset> labelDatasets = labelDatasetMapper.selectBatchIds(ids);
        return labelDatasets.stream().map(LabelDataset::getDatasetPath).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        String filePath = "d:\\1.xml";
        File file = new File(filePath);
        XMLSerializer xmlSerializer = new XMLSerializer();
        net.sf.json.JSONObject jsonLabel = net.sf.json.JSONObject.fromObject(xmlSerializer.readFromFile(file));
        net.sf.json.JSONObject finalJson = new net.sf.json.JSONObject();
        net.sf.json.JSONArray objects = new net.sf.json.JSONArray();
        if (jsonLabel.get("objects") instanceof net.sf.json.JSONObject){
            objects.add(jsonLabel.getJSONObject("objects"));
        }
        else if (jsonLabel.get("objects") instanceof net.sf.json.JSONArray){
            objects = jsonLabel.getJSONArray("objects");
        }
        Iterator<net.sf.json.JSONObject> iter = objects.iterator();
        while(iter.hasNext()){
            try {
                net.sf.json.JSONObject object = iter.next();
                String coordinate = object.getString("coordinate");
                if (object.get("possibleresult") instanceof net.sf.json.JSONObject){
                    net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
                    String name = ((net.sf.json.JSONObject) object.get("possibleresult")).getString("name").trim();


                    jsonArray.add(object.get("possibleresult"));
                    object.remove("possibleresult");
                    object.put("possibleresult",jsonArray);
                }else{
                    if (object.get("possibleresult") instanceof net.sf.json.JSONArray){
                        net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
                        String name = object.getJSONArray("possibleresult").getString(0).trim();
                        net.sf.json.JSONObject newPossibleresult = new net.sf.json.JSONObject();
                        newPossibleresult.put("name",name);
                        jsonArray.add(newPossibleresult);
                        object.remove("possibleresult");
                        object.put("possibleresult",jsonArray);
                    }
                }
                net.sf.json.JSONObject pointJson = new net.sf.json.JSONObject();
                net.sf.json.JSONArray pointsArray = object.getJSONArray("points");
                List<String> pointList = new ArrayList<>();
                for (int pointIndex = 0; pointIndex < pointsArray.size(); pointIndex++) {
                    String[] point = pointsArray.getString(pointIndex).split(",");
                    double[] coordination = new double[]{Double.parseDouble(point[0]),Double.parseDouble(point[1])};
//                    if ("pixel".equalsIgnoreCase(coordinate) ){
//                        //默认输入像素坐标为左上角，转换为左下角给标注使用
//                        if (!CoordinateSystemType.PIXELCS.equals(coordinateSystemType)){
//                            object.put("coordinate","geodegree");
//                            String coordinateType  = coordinateSystemType.equals(CoordinateSystemType.PROJCS) ? GeoUtils.COORDINATE_PROJECTION : GeoUtils.COORDINATE_LONLAT;
//                            coordination = GeoUtils.pixel2Coordinate(coordination[0],coordination[1],imagePath,coordinateType);
//                        }else
//                            coordination[1] = labelDatasetImageInfo.getHeight() - coordination[1];
//
//                    }else {
//                        if (CoordinateSystemType.PROJCS.equals(coordinateSystemType)){
//                            coordination = GeoUtils.coordinateConvertor(coordination[0],coordination[1],imagePath,GeoUtils.COORDINATE_PROJECTION);
//                        }
//                    }
                    pointList.add(coordination[0]+","+coordination[1]);
                }
                pointJson.put("point",pointList);

                object.remove("points");
                object.put("points",pointJson);
            }catch (Exception e){
                iter.remove();
                log.info("{} 标注信息部分有误",filePath);
            }
        }
//        for (int index = 0, size = objects.size(); index < size; index++) {
//
//        }
//        while(iter.hasNext()){
//            net.sf.json.JSONObject object = iter.next();
//        }
        finalJson.put("object",jsonLabel.get("objects"));
    }

}