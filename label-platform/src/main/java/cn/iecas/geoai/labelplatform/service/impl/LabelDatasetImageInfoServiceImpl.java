//package cn.iecas.geoai.labelplatform.service.impl;
//
//import cn.iecas.geoai.labelplatform.dao.LabelDatasetImageInfoMapper;
//import cn.iecas.geoai.labelplatform.entity.common.PageResult;
//import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
//import cn.iecas.geoai.labelplatform.entity.domain.LabelProjectFileInfo;
//import cn.iecas.geoai.labelplatform.entity.dto.FileSearchParam;
//import cn.iecas.geoai.labelplatform.entity.dto.LabelDatasetFileRequest;
//import cn.iecas.geoai.labelplatform.service.LabelDatasetFileService;
//import cn.iecas.geoai.labelplatform.service.LabelDatasetImageInfoService;
//import cn.iecas.geoai.labelplatform.service.UserInfoService;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.ResourceAccessException;
//import org.springframework.web.client.RestTemplate;
//import redis.clients.jedis.JedisPool;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.*;
//import java.util.stream.Collectors;
//
//
//@Service
//@Slf4j
//public class LabelDatasetImageInfoServiceImpl implements LabelDatasetImageInfoService {
//
//    @Autowired
//    LabelDatasetImageInfoMapper labelDatasetImageInfoMapper;
//
//
//    @Value("${spring.redis.host}")
//    private String ip;
//
//    @Value("${spring.redis.port}")
//    private int port;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Autowired
//    private UserInfoService userInfoService;
//
//    @Autowired
//    private LabelDatasetFileService labelDatasetFileService;
//
//    @Autowired
//    HttpServletRequest request;
//
//    @Autowired
//    private JedisPool jedisPool;
//
////    @Override
////    public PageResult<JSONObject> getFileInfoFromDataset(LabelDatasetFileRequest labelDatasetFileRequest) throws ResourceAccessException {
////        String searchParam = labelDatasetFileRequest.getSearchParam();
////        if (searchParam !=null){
////            List<String> params = Arrays.asList(searchParam.split(" "));
////            labelDatasetFileRequest.setSearchParamList(params);
////        }
////        Page<LabelDatasetImageInfo> page = new Page<>(labelDatasetFileRequest.getPageNo(), labelDatasetFileRequest.getPageSize());
////        IPage<LabelDatasetImageInfo> labelDatasetImageIPage = this.labelDatasetImageInfoMapper.listDatasetImageInfos(page, labelDatasetFileRequest);
////        Jedis jedis = jedisPool.getResource();
////
////        //System.out.println(jedis);
////        String token = request.getHeader("token");
////        HttpHeaders httpHeaders = new HttpHeaders();
////        httpHeaders.add("token",token);
////        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
////        JSONObject allUserInfo = restTemplate.exchange(queryUserNameUrl, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
////        List<Object> userInfoList = allUserInfo.getJSONArray("data");
////        Map<Integer,String> userInfoMap = new HashMap<>();
////       for (Object object : userInfoList){
////            net.sf.json.JSONObject userJson = net.sf.json.JSONObject.fromObject(object);
////            userInfoMap.put(userJson.getInt("id"),userJson.getString("name"));
////        }
////        for (LabelDatasetImageInfo labelDatasetImageInfo : labelDatasetImageIPage.getRecords()){
////            try {
////                if (labelDatasetImageInfo.getLabelUserId() != 0 && !jedis.exists("id:" + String.valueOf(labelDatasetImageInfo.getLabelUserId()))) {
////                    log.info("正在写入redis：{}", labelDatasetImageInfo.getLabelUserId());
////                    labelDatasetImageInfo.setLabelUserName(userInfoMap.get(labelDatasetImageInfo.getLabelUserId()));
////                    jedis.set("id:" + labelDatasetImageInfo.getLabelUserId(), labelDatasetImageInfo.getLabelUserName());
////                } else if (jedis.exists("id:" + String.valueOf(labelDatasetImageInfo.getLabelUserId()))) {
////                    labelDatasetImageInfo.setLabelUserName(jedis.get("id:" + labelDatasetImageInfo.getLabelUserId()));
////                }
////            }catch (Exception e) {
////                labelDatasetImageInfo.setLabelUserName(userInfoMap.get(labelDatasetImageInfo.getLabelUserId()));
////            }
////            try {
////                if (labelDatasetImageInfo.getCheckUserId()!=0 && !jedis.exists("id:"+String.valueOf(labelDatasetImageInfo.getCheckUserId()))){
////                    log.info("正在写入redis：{}",labelDatasetImageInfo.getCheckUserId                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ());
////                    labelDatasetImageInfo.setCheckUserName(userInfoMap.get(labelDatasetImageInfo.getCheckUserId()));
////                    jedis.set("id:" + labelDatasetImageInfo.getCheckUserId() , labelDatasetImageInfo.getCheckUserName());
////                }else if (jedis.exists("id:"+String.valueOf(labelDatasetImageInfo.getCheckUserId()))){
////                    labelDatasetImageInfo.setCheckUserName(jedis.get("id:" + labelDatasetImageInfo.getCheckUserId()));
////                }
////            }catch (Exception e){
////                labelDatasetImageInfo.setCheckUserName(userInfoMap.get(labelDatasetImageInfo.getCheckUserId()));
////            }
////
////        }
////        return new PageResult<>(labelDatasetImageIPage.getCurrent(),labelDatasetImageIPage.getTotal(),labelDatasetImageIPage.getRecords());
////    }
//
//    @Override
//    public PageResult<LabelProjectFileInfo> getFileInfoFromDataset(LabelDatasetFileRequest labelDatasetFileRequest) throws ResourceAccessException {
//        String searchParam = labelDatasetFileRequest.getSearchParam();
//        FileSearchParam fileSearchParam = new FileSearchParam();
//        BeanUtils.copyProperties(labelDatasetFileRequest,fileSearchParam);
//
//        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select("status","file_id","id").eq("status",labelDatasetFileRequest.getStatus());
//        List<LabelDatasetFile> labelDatasetFileList = this.labelDatasetFileService.list(queryWrapper);
//        List<Integer> labelDatasetFileIdList = labelDatasetFileList.stream().map(LabelDatasetFile::getFileId).collect(Collectors.toList());
//        List<LabelProjectFileInfo> labelProjectFileInfoList = new ArrayList<>();
//
//        JSONObject labelDatasetFileInfo = new JSONObject();
//        Set<Integer> labelDatasetFileIdSet = new HashSet<>();
//        Set<Integer> userIdSet = new HashSet<>();
//        JSONArray labelDatasetFileInfoJSONArray = labelDatasetFileInfo.getJSONObject("data").getJSONArray("result");
//        labelDatasetFileInfoJSONArray.stream().forEach(jsonObject->labelDatasetFileIdSet.add(((JSONObject)jsonObject).getInteger("id")));
//        labelDatasetFileList = labelDatasetFileList.stream().filter(labelDatasetFile -> labelDatasetFileIdSet.contains(labelDatasetFile.getFileId())).collect(Collectors.toList());
//        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
//            userIdSet.add(labelDatasetFile.getLabelUserId());
//            userIdSet.add(labelDatasetFile.getCheckUserId());
//        }
//
//        Map<Integer,String> userIdNameMap = userInfoService.getUserInfoById(userIdSet);
//        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
//            LabelProjectFileInfo labelProjectFileInfo = new LabelProjectFileInfo();
//            labelProjectFileInfo.setId(labelDatasetFile.getId());
//            labelProjectFileInfo.
//            labelProjectFileInfo.setStatus(labelDatasetFile.getStatus());
//        }
//
//
//        return new PageResult<>(labelDatasetImageIPage.getCurrent(),labelDatasetImageIPage.getTotal(),labelDatasetImageIPage.getRecords());
//    }
//}
