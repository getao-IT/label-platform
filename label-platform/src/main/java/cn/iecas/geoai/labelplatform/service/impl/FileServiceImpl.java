package cn.iecas.geoai.labelplatform.service.impl;

import cn.aircas.utils.date.DateUtils;
import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.dto.FileSearchParam;
import cn.iecas.geoai.labelplatform.entity.dto.LabelDatasetFileRequest;
import cn.iecas.geoai.labelplatform.service.FileService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class FileServiceImpl implements FileService {


    @Value(value = "${value.api.file-info}")
    private String fileInfoApi;

    @Value(value = "${value.api.list-file-id-url}")
    private String listFileIdUrl;

    @Value(value = "${value.api.file-info-idList}")
    private String fileInfoByIdListApi;

    @Value(value = "${value.api.get-file-by-contentid}")
    private String getFileByContentIdUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest request;


    @Override
    public JSONObject getFileInfoById(int fileId,String token) {
        List<Integer> fileIdList = Collections.singletonList(fileId);
        List<JSONObject> fileList = this.listFileInfoByIdList(fileIdList,DatasetType.IMAGE,token);
        return fileList.get(0);
    }

    @Override
    public JSONObject getFileInfoById(int fileId, DatasetType datasetType,String token) {
        List<Integer> fileIdList = Collections.singletonList(fileId);
        List<JSONObject> fileList = this.listFileInfoByIdList(fileIdList,datasetType,token);
        return fileList.get(0);
    }

    @Override
    public List<Integer> listFileIdBySearch(LabelDataset labelDataset) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",request.getHeader("token"));
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("searchParam",labelDataset.getSearchParam());
        paramMap.put("startTime",labelDataset.getStartTime());
        paramMap.put("endTime",labelDataset.getEndTime());
        paramMap.put("fileType",labelDataset.getDatasetType().getValue());
        paramMap.put("content","false");
        if (labelDataset.getFileIdList()==null){
            paramMap.put("fileIdList","");
        }else {
            paramMap.put("fileIdList",labelDataset.getFileIdList().toString().replace("[","").replace("]",""));
        }
        if (labelDataset.getDatasetType() == DatasetType.TEXT){
            paramMap.put("content","true");
        }
        String requestUrl = jointGetParam(listFileIdUrl,paramMap);
        JSONObject result = null;
        try{
            result = restTemplate.exchange(requestUrl, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
        }catch (Exception e){
            postForWarning();
        }
        return result.getJSONArray("data").toJavaList(Integer.class);
    }

    @Override
    public JSONArray getAllFileIdList(FileSearchParam fileSearchParam) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",request.getHeader("token"));
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        Map<String, Object> paramMap = JSONObject.parseObject(JSONObject.toJSONString(fileSearchParam));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (paramMap.get("startTime") != null) {
            paramMap.put("startTime", format.format(paramMap.get("startTime")));
        }
        if (paramMap.get("endTime") != null) {
            paramMap.put("endTime", format.format(paramMap.get("endTime")));
        }

        JSONArray result = null;
        String requestUrl = jointGetParam(listFileIdUrl,paramMap);
        try{
            result = restTemplate.exchange(requestUrl, HttpMethod.GET,httpEntity,JSONObject.class).getBody().getJSONArray("data");
        }catch (Exception e){
            postForWarning();
        }
        return result!=null ? result : null;
    }

    @Override
    public PageResult<JSONObject> getFileInfoByPage(FileSearchParam fileSearchParam) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",request.getHeader("token"));
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        Map<String, Object> paramMap = JSONObject.parseObject(JSONObject.toJSONString(fileSearchParam));
//        paramMap.put("searchParam",fileSearchParam.getSearchParam());
//        paramMap.put("pageNo",fileSearchParam.getPageNo());
//        paramMap.put("content",fileSearchParam.isContent());
//        paramMap.put("pageSize",fileSearchParam.getPageSize());
//        paramMap.put("fileType",fileSearchParam.getFileType().getValue());
        if (fileSearchParam.getFileIdList() != null && !fileSearchParam.getFileIdList().isEmpty())
            paramMap.put("fileIdList",fileSearchParam.getFileIdList().toString().replace("[","").replace("]",""));
        paramMap.put("isFromDataset", fileSearchParam.isFromDataset());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (paramMap.get("startTime") != null) {
            paramMap.put("startTime", format.format(paramMap.get("startTime")));
        }
        if (paramMap.get("endTime") != null) {
            paramMap.put("endTime", format.format(paramMap.get("endTime")));
        }

        JSONObject result = null;
        String requestUrl = jointGetParam(fileInfoApi,paramMap);
        try{
            result = restTemplate.exchange(requestUrl, HttpMethod.GET,httpEntity,JSONObject.class).getBody().getJSONObject("data");
        }catch (Exception e){
            postForWarning();
        }
        return result!=null ? JSONObject.parseObject(result.toJSONString(),PageResult.class) : null;
    }

    @Override
    public  Map<Integer, JSONObject> getFileByContentId(String fileType, Set<Integer> fileIds) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", request.getHeader("token"));
        HttpEntity<JSONObject> httpEntity = new HttpEntity<JSONObject>(null, httpHeaders);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(getFileByContentIdUrl)
                .queryParam("contentIds", fileIds.toString().replace("[", "").replace("]","").replace(" ", ""))
                .queryParam("fileType", fileType).build();
        JSONObject body = restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, JSONObject.class).getBody();
        Map<Integer,JSONObject > result = JSONObject.parseObject(body.getJSONObject("data").toJSONString(), Map.class);
        return result;
    }


    @Override
    public List<JSONObject> listFileInfoByIdList(List<Integer> fileIdList, DatasetType datasetType, String token) throws ResourceAccessException {
        if (fileIdList.size()==0)
            return new ArrayList<>();

        JSONObject result = null;
        String fileIds = fileIdList.toString().replace("[","").replace("]","").replace(" ","");


        HttpHeaders httpHeaders = new HttpHeaders();
        if (token==null)
            httpHeaders.add("token",request.getHeader("token"));
        else
            httpHeaders.add("token",token);
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("content","false");
        paramMap.put("fileType",datasetType.getValue());
        if (datasetType==DatasetType.TEXT)
            paramMap.put("content","true");

        String requestUrl = (fileInfoByIdListApi.endsWith("/") ? fileInfoByIdListApi + fileIds : fileInfoByIdListApi) + "/" + fileIds ;
        requestUrl = jointGetParam(requestUrl,paramMap);

        try{
            result =  restTemplate.exchange(requestUrl, HttpMethod.GET,httpEntity, JSONObject.class).getBody();
        }catch (ResourceAccessException e){
            postForWarning();
            log.error("访问影像服务接口api：{} 出错", fileInfoApi +fileIds);
            throw new ResourceAccessException("关联服务访问出错");
        }
        return JSONArray.parseArray(result.getJSONArray("data").toJSONString(),JSONObject.class);
    }

    /**
     * 拼接get参数，之所以不用exchange自带拼接，是因为需要在url中填写占位符，参数过多导致url过长
     * @param url
     * @param params
     * @return
     */
    public  String jointGetParam(String url, Map<String, Object> params){
        StringBuilder stringBuilder = new StringBuilder(url+"?");
        Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Object> entry = iterator.next();
            if(entry.getValue()!=null)
                stringBuilder.append(entry).append("&");
        }
        return stringBuilder.substring(0,stringBuilder.length()-1);
    }



    public void postForWarning(){
        //String url = "http://192.168.121.43:15582/rest/biz_alarm/";
        //String url = "http://192.168.139.177:15582/rest/biz_alarm/";
        String url = "http://192.168.154.48:15582/rest/biz_alarm/";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bizCName", "智能处理支撑-智能标注服务");
        jsonObject.put("appName", "ZNBZFW");
        jsonObject.put("serviceName", "ZNBZHD");
        jsonObject.put("segPref", "ZNBZHD");
        jsonObject.put("segCName", "ZNBZHD");
        jsonObject.put("alarmLevel", 3);
        jsonObject.put("host", "192.168.181.65");
        jsonObject.put("alarmTime", DateUtils.nowDate());
        jsonObject.put("alarmEvent", "无法访问数据管理后台服务");
        try {
            JSONObject object = restTemplate.postForEntity(url, jsonObject, JSONObject.class).getBody();
        }
        catch (Exception e) {
            log.error("无法访问告警日志服务");
        }
    }

}
