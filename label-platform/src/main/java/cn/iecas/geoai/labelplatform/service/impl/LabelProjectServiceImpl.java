package cn.iecas.geoai.labelplatform.service.impl;

import cn.aircas.utils.comporess.CompressUtil;
import cn.aircas.utils.date.DateUtils;
import cn.aircas.utils.file.FileUtils;
import cn.aircas.utils.image.geo.GeoUtils;
import cn.iecas.geoai.labelplatform.dao.LabelDatasetFileMapper;
import cn.iecas.geoai.labelplatform.dao.LabelDatasetImageInfoMapper;
import cn.iecas.geoai.labelplatform.dao.LabelProjectMapper;
import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.*;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import cn.iecas.geoai.labelplatform.entity.emun.*;
import cn.iecas.geoai.labelplatform.entity.fileFormat.LabelObject;
import cn.iecas.geoai.labelplatform.entity.fileFormat.XMLLabelObjectInfo;
import cn.iecas.geoai.labelplatform.service.*;
import cn.iecas.geoai.labelplatform.util.LabelPointTypeConvertor;
import cn.iecas.geoai.labelplatform.util.XMLUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author vanishrain
 */
@Slf4j
@Service
//@Transactional
@EnableAsync
public class LabelProjectServiceImpl extends ServiceImpl<LabelProjectMapper,LabelProject> implements LabelProjectService {

    private final static String DEFAULT_GROUP_NAME = "随机分配";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LabelTaskService labelTaskService;

    @Autowired
    private LabelProjectMapper labelProjectMapper;

    @Autowired
    private LabelDatasetService labelDatasetService;

    @Lazy
    @Autowired
    private  AILabelService aiLabelService;

    @Autowired
    FileService imageService;

    @Autowired
    LabelDatasetFileService labelDatasetFileService;

    @Autowired
    LabelDatasetImageInfoMapper labelDatasetImageInfoMapper;

    @Autowired
    private LabelDatasetFileMapper labelDatasetFileMapper;

    @Autowired
    private LabelTaskStatisInfoService statisInfoService;

    @Autowired
    private LabelProjectService labelProjectService;

    @Autowired
    private FileService fileService;

    @Value( value = "${value.dir.rootDir}")
    private String rootDir;

    @Value(value = "${value.api.push-label-project}")
    private String pushLabelProjectUrl;

    /**
     * 根据条件分页查询标注project信息
     * @param labelProjectSearchRequest 标注project查询条件信息
     * @return 满足条件的标注project信息
     */
    @Override
    public PageResult<LabelProject> getLabelProject(LabelProjectSearchRequest labelProjectSearchRequest){
        Page<LabelProject> page = new Page<>();
        page.setSize(labelProjectSearchRequest.getPageSize());
        page.setCurrent(labelProjectSearchRequest.getPageNo());
        IPage<LabelProject> labelProjectIPage = labelProjectMapper.getLabelProject(page,labelProjectSearchRequest);
        List<LabelProject> list = labelProjectIPage.getRecords();
        long consumeMillis = 0;
        String consumeTime = "";
        for (LabelProject project : list) {
            if (project.getFinishTime() != null) {
                consumeMillis = project.getFinishTime().getTime() - project.getCreateTime().getTime();
                consumeTime = cn.iecas.geoai.labelplatform.util.DateUtils.millisToTime(consumeMillis);
                project.setConsumeTime(consumeTime);
            } else {
                consumeMillis = new Date().getTime() - project.getCreateTime().getTime();
            }
            consumeTime = cn.iecas.geoai.labelplatform.util.DateUtils.millisToTime(consumeMillis);
            project.setConsumeTime(consumeTime);
        }
        return new PageResult<>(labelProjectSearchRequest.getPageNo(),labelProjectIPage.getTotal(),list);
    }

    /**
     * 创建标注项目
     * @param labelProject 标注项目信息
     */
    @Override
    public void createLabelProject(LabelProject labelProject, HttpServletRequest request) {
        labelProject.setCreateTime(DateUtils.nowDate());
        labelProject.setStatus(labelProject.getIsAiLabel()?LabelProjectStatus.AILABELING:LabelProjectStatus.LABELING);
        this.labelProjectMapper.insert(labelProject);
        int labelDatasetId = this.labelDatasetService.createDatasetFromProject(labelProject,labelProject.isUseLabel());

        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id", labelDatasetId);
        labelProject.setDatasetId(labelDatasetId);
        this.updateById(labelProject);
        labelTaskService.createLabelTasks(labelProject);
        labelTaskService.createCheckTasks(labelProject);
        if (labelProject.getIsAiLabel()){
            String token = request.getHeader("token");
            aiLabelService.callAIService(labelProject,token);
        }
        if (labelProject.isPreprocessing()) {
            aiLabelService.setImagePretreatPath(labelProject, request.getHeader("token"));
        }
        if (labelProject.isUnite()) {
            if (labelProject.getCategory().contains("503-"))
                this.labelProjectService.updateUniteLabelProjectToFzt(labelProject, request);
            if (labelProject.getCategory().contains("ht-"))
                this.labelProjectService.updateUniteLabelProjectToHt(labelProject, request);
            if (labelProject.getCategory().contains("YJ-"))
                this.labelProjectService.updateUniteLabelProjectToHt(labelProject, request);
        }

    }

    /**
     * 创建联合标注项目
     * @param labelProject
     *
     */
    @Override
    public void updateUniteLabelProjectToHt(LabelProject labelProject, HttpServletRequest request) {
        String token = request.getHeader("token");
        List<String> labelUserIds = new ArrayList<>();
        Collections.addAll(labelUserIds, labelProject.getLabelUserIds().split(","));
        Assert.isTrue(labelUserIds.size()==1, "标注员分配错误，默认应为当前用户！");
        List<String> checkUserIds = new ArrayList<>();
        Collections.addAll(checkUserIds, labelProject.getLabelUserIds().split(","));
        Assert.isTrue(checkUserIds.size()==1, "审核员分配错误，默认应为当前用户！");
        JSONArray fileJsonArray = new JSONArray();
        List<LabelDatasetFile> labelDatasetFileList = labelDatasetFileService.list(
                new QueryWrapper<LabelDatasetFile>().eq("dataset_id", labelProject.getDatasetId())); // 根据数据集id更新标注员
        for (int i = 0; i < labelDatasetFileList.size(); i++) {
            labelDatasetFileList.get(i).setLabelUserId(Integer.parseInt(labelUserIds.get(0)));
            labelDatasetFileList.get(i).setCheckUserId(Integer.parseInt(checkUserIds.get(0)));
            labelDatasetFileList.get(i).setStatus(LabelStatus.LABELING);
            labelDatasetFileList.get(i).setAssignLabelTime(new Timestamp(new Date().getTime()));
            labelDatasetFileList.get(i).setAssignCheckTime(new Timestamp(new Date().getTime()));
            JSONObject fileInfoById = fileService.getFileInfoById(labelDatasetFileList.get(i).getFileId(),null);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileId", fileInfoById.get("id"));
            jsonObject.put("filePath", fileInfoById.get("path"));
            fileJsonArray.add(jsonObject);
        }
        this.labelDatasetFileService.updateBatchById(labelDatasetFileList);
        int assignNums = labelDatasetFileList.size();
        updateLabelTaskInfo(labelProject, Integer.parseInt(labelUserIds.get(0)), assignNums, fileJsonArray, null, token);
    }

    /**
     * 创建联合标注项目-503
     * @param labelProject
     *
     */
    @Override
    public void updateUniteLabelProjectToFzt(LabelProject labelProject, HttpServletRequest request) {
        String token = request.getHeader("token");
        List<String> labelUserIds = new ArrayList<>();
        Collections.addAll(labelUserIds, labelProject.getLabelUserIds().split(","));
        List<String> checkUserIds = new ArrayList<>();
        Collections.addAll(checkUserIds, labelProject.getCheckUserIds().split(","));
        JSONArray fileJsonArray = new JSONArray();
        JSONArray compareFileJsonArray = new JSONArray();
        List<LabelDatasetFile> labelDatasetFileList = labelDatasetFileService.list(
                new QueryWrapper<LabelDatasetFile>().eq("dataset_id", labelProject.getDatasetId())); // 根据数据集id更新标注员

        int fileCount = labelDatasetFileList.size();
        int labelUserCount = labelUserIds.size();
        int labelAssignNums = fileCount < labelUserCount ? 1 :
                (fileCount % labelUserCount) != 0 ? (fileCount / labelUserCount) + 1 : fileCount / labelUserCount;
        int labelCurrentAssignNums = 0;
        int labelCurrentApplyUser = 0;
        int checkUserCount = checkUserIds.size();
        int checkAssignNums = fileCount < checkUserCount ? 1 :
                (fileCount % checkUserCount) != 0 ? (fileCount / checkUserCount) + 1 : fileCount / checkUserCount;
        int checkCurrentAssignNums = 0;
        int checkCurrentApplyUser = 0;

        Map<String, List<Image>> deteSampleVos = null;
        List<JSONObject> fileInfoList = null;
        if (labelProject.getCategory().contains("change")) {
            List<Integer> fileIdList = labelDatasetFileList.stream().map(LabelDatasetFile::getFileId).collect(Collectors.toList());
            fileInfoList = this.fileService.listFileInfoByIdList(fileIdList, DatasetType.IMAGE,null);
            this.coordinateConvertor(fileInfoList);
            deteSampleVos = this.mapDeteSamples(fileInfoList);
            labelAssignNums = labelAssignNums / 2;
            checkAssignNums = checkAssignNums / 2;
        }

        for (int i = 0; i < labelDatasetFileList.size(); i++) {
            if (labelCurrentApplyUser > (labelUserIds.size()-1) || checkCurrentApplyUser > (checkUserIds.size()-1)) {
                log.info("*******************标注任务分发已完毕******************");
                break;
            }
            labelDatasetFileList.get(i).setLabelUserId(Integer.parseInt(labelUserIds.get(labelCurrentApplyUser)));
            labelDatasetFileList.get(i).setCheckUserId(Integer.parseInt(checkUserIds.get(checkCurrentApplyUser)));
            labelDatasetFileList.get(i).setStatus(LabelStatus.LABELING);
            labelDatasetFileList.get(i).setAssignLabelTime(new Timestamp(new Date().getTime()));
            labelDatasetFileList.get(i).setAssignCheckTime(new Timestamp(new Date().getTime()));
            JSONObject fileInfoById = fileService.getFileInfoById(labelDatasetFileList.get(i).getFileId(),null);
            JSONObject filejsonObject = new JSONObject();
            JSONObject compareFileJsonObject = new JSONObject();

            String groupName = "";
            if (labelProject.getCategory().contains("change")) {
                groupName = this.getGroupName(
                        ((BigDecimal)fileInfoById.get("minLat")).doubleValue(), ((BigDecimal)fileInfoById.get("minLon")).doubleValue(),
                        ((BigDecimal)fileInfoById.get("maxLat")).doubleValue(), ((BigDecimal)fileInfoById.get("maxLon")).doubleValue());
                List<Image> fileGroup = deteSampleVos.get(groupName);
                if (fileGroup != null) {
                    filejsonObject.put("fileId", fileInfoById.get("id"));
                    filejsonObject.put("filePath", fileInfoById.get("path"));
                    compareFileJsonObject.put("fileId", fileGroup.get(1).getId() == Integer.parseInt(String.valueOf(fileInfoById.get("id"))) ? fileGroup.get(0).getId() : fileGroup.get(1).getId());
                    compareFileJsonObject.put("filePath", String.valueOf(fileGroup.get(1).getPath()).equals(fileInfoById.get("path")) ? fileGroup.get(0).getPath() : fileGroup.get(1).getPath());
                    fileJsonArray.add(filejsonObject);
                    compareFileJsonArray.add(compareFileJsonObject);
                    labelDatasetFileList.get(i).setRelatedFileId(Integer.parseInt(String.valueOf(compareFileJsonObject.get("fileId"))));
                } else {
                    continue;
                }
                deteSampleVos.remove(groupName);
            }

            labelDatasetFileService.updateById(labelDatasetFileList.get(i));

            if (labelProject.getCategory().contains("detection-square")) {
                filejsonObject.put("fileId", fileInfoById.get("id"));
                filejsonObject.put("filePath", fileInfoById.get("path"));
                fileJsonArray.add(filejsonObject);
            }

            labelCurrentAssignNums++;
            if (labelAssignNums == labelCurrentAssignNums || (i == labelDatasetFileList.size()-1)) {
                updateLabelTaskInfo(labelProject, Integer.parseInt(labelUserIds.get(labelCurrentApplyUser)), labelCurrentAssignNums, fileJsonArray, compareFileJsonArray, token);
                labelCurrentAssignNums = 0;
                labelCurrentApplyUser++;
            }

            checkCurrentAssignNums++;
            if (checkAssignNums == checkCurrentAssignNums || (i == labelDatasetFileList.size()-1)) {
                checkCurrentAssignNums = 0;
                checkCurrentApplyUser++;
            }
        }
    }

    /**
     * 转换坐标类型
     * @param fileInfoList
     */
    private void coordinateConvertor(List<JSONObject> fileInfoList) {
        fileInfoList.stream().forEach(f -> {
            String imagePath = FileUtils.getStringPath("/home/data/", f.get("path"));
            if (!GeoUtils.hasGeoInfo(imagePath)) {
                throw new RuntimeException("不规范的卫星影像，该影像不包含地理信息："+imagePath);
            }
            /*String coordSystemType = f.get("coordinateSystemType").equals("PIXELCS") ? String.valueOf(CoordinateConvertType.PIXEL_TO_LONLAT)
                    : (f.get("coordinateSystemType").equals("PROJCS") ? String.valueOf(CoordinateConvertType.PROJECTION_TO_LONLAT) : null);
            if (coordSystemType != null) {
                double width = Double.parseDouble(String.valueOf(f.get("width")));
                double height = Double.parseDouble(String.valueOf(f.get("height")));
                double[] leftCoord = GeoUtils.pixel2Coordinate(0, height, imagePath, coordSystemType);
                double[] rightCoord = GeoUtils.pixel2Coordinate(width, 0, imagePath, coordSystemType);
                f.put("minLat", rightCoord[1]);
                f.put("minLon", leftCoord[0]);
                f.put("maxLat", leftCoord[1]);
                f.put("maxLon", rightCoord[0]);
            }*/
        });
    }

    /**
     * 更新标注任务信息
     */
    public void updateLabelTaskInfo(LabelProject labelProject, int userId, int currentAssignNums, JSONArray fileJsonArray, JSONArray compareFileJsonArray, String token) {
        LabelTask labelTask = labelTaskService.getOne(new QueryWrapper<LabelTask>().
                eq("label_project_id", labelProject.getId()).
                eq("task_type", 0).eq("user_id", userId));
        labelTask.setTotalCount(currentAssignNums);
        labelTaskService.updateById(labelTask);
        this.statisInfoService.lambdaUpdate().set(LabelTaskStatisInfo::getApplyFileCount, currentAssignNums)
                .eq(LabelTaskStatisInfo::getLabelProjectId, labelProject.getId()).eq(LabelTaskStatisInfo::getUserId, userId)
                .eq(LabelTaskStatisInfo::getUserRole, 0).update();
        PushLabelProjectInfo pushLabelProjectInfo = PushLabelProjectInfo.builder().userId(userId)
                .labelProjectId(labelProject.getId()).labelTaskId(labelTask.getId()).imagePathInfo(fileJsonArray)
                .compareImagePathInfo(compareFileJsonArray).keywords(labelProject.getKeywords()).token(token).build();
        pushLabelProjectInfo.setSpSoftwareName(labelProject.getCategory());
        pushLabelProject(pushLabelProjectInfo);
        if (fileJsonArray != null) {
            fileJsonArray.clear();
        }
        if (compareFileJsonArray != null) {
            compareFileJsonArray.clear();
        }
    }

    /**
     * 推送联合标注任务
     * @param pushLabelProjectInfo
     */
    public void pushLabelProject(PushLabelProjectInfo pushLabelProjectInfo) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", pushLabelProjectInfo.getToken());
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(pushLabelProjectInfo));
        HttpEntity<JSONObject> entity = new HttpEntity<JSONObject>(jsonObject, httpHeaders);
        JSONObject body = restTemplate.exchange(pushLabelProjectUrl, HttpMethod.POST, entity, JSONObject.class).getBody();
        log.info("完成项目推送：推送项目ID {} , 本次推送任务ID {} , 包含文件信息 {}, 包含关联文件信息 {}", pushLabelProjectInfo.getLabelProjectId(), pushLabelProjectInfo.getLabelTaskId(), pushLabelProjectInfo.getImagePathInfo(), pushLabelProjectInfo.getCompareImagePathInfo());
    }

    @Override
    public List<String> importKeywords(MultipartFile file) throws IOException {
        String keyword = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] tagArrays = keyword.split("\n");
        return Arrays.stream(tagArrays).map(String::trim).collect(Collectors.toList());
    }

    /**
     * 根据项目生成结果数据集信息
     * @param labelProjectId 项目id
     */
    @Override
    public void createDatasetResult(int labelProjectId) throws IOException {
        LabelDataset labelDataset = labelDatasetService.getOne(new QueryWrapper<LabelDataset>().eq("project_id",labelProjectId));
        Assert.notNull(labelDataset,"查找标注项目关联的数据集失败");
        this.labelDatasetService.createManifest(labelDataset);
        LabelProject labelProject = this.getById(labelProjectId);
        labelDataset.setCount(labelProject.getFinishCount());
        labelDataset.setProjectCategory(labelProject.getCategory());
        int finishCount = this.labelDatasetService.getDatasetFileFinishCount(labelDataset);
        labelDataset.setFinishCount(finishCount);
        this.labelDatasetService.updateById(labelDataset);
        this.labelDatasetService.setDatasetVisible(labelDataset.getId(),true);
    }

    /**
     * 更新标注项目进度
     * @param labelProjectId 标注项目id
     */
    @Override
    public void updateLabelProgress(int labelProjectId) {
        LabelProject labelProject = this.getById(labelProjectId);
        if (labelProject.getStatus() == LabelProjectStatus.FINISH)
            return;
        int totalCount = labelProject.getTotalCount();
        int finishCount = labelProject.getFinishCount() + 1;
        UpdateWrapper<LabelProject> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("finish_count",finishCount).eq("id",labelProjectId)
                .set(totalCount==finishCount,"status",LabelProjectStatus.FINISH);
        this.update(updateWrapper);

    }

    /**
     * 批量删除标注project信息
     * @param projectIdList 标注project imageId 列表
     */
    @Override
    public void deleteLabelProject(List<Integer> projectIdList){
        labelTaskService.deleteTaskByProjectId(projectIdList);
        statisInfoService.deleteTaskStatisByProjectId(projectIdList);
        labelProjectMapper.deleteBatchIds(projectIdList);
    }


    @Override
    public void updateLabelProjectUser(LabelProjectUpdateUserInfo labelProjectUpdateUserInfo){
        int labelProjectId = labelProjectUpdateUserInfo.getLabelProjectId();
        String updateUserIdStr = labelProjectUpdateUserInfo.getUpdateUserIdStr();
        LabelTaskType labelUserType = labelProjectUpdateUserInfo.getLabelUserType();
        LabelProject labelProject = this.getById(labelProjectId);
        Assert.notNull(labelProject,String.format("id为:%d 的标注项目不存在",labelProjectId));
        String userIdStr = labelUserType == LabelTaskType.LABEL ? labelProject.getLabelUserIds() : labelProject.getCheckUserIds();
        List<String> userIdList = Arrays.asList(userIdStr.split(","));
        List<String> updateUserIdList = Arrays.asList(updateUserIdStr.split(","));
        List<Integer> deleteUserIdList = userIdList.stream().filter(userId -> !updateUserIdList.contains(userId)).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> addUserIdList = updateUserIdList.stream().filter(userId -> !userIdList.contains(userId)).map(Integer::parseInt).collect(Collectors.toList());

        if (labelUserType == LabelTaskType.LABEL){
            this.updateProjectLabelUser(labelProject,deleteUserIdList,addUserIdList);
            labelProject.setLabelUserIds(updateUserIdStr);
        }
        else {
            this.updateProjectCheckUser(labelProject,deleteUserIdList,addUserIdList);
            labelProject.setCheckUserIds(updateUserIdStr);
        }

        labelProjectMapper.updateById(labelProject);

    }

    /**
     * 更新项目中的审核员
     * @param labelProject
     * @param deleteUserIdList
     * @param addUserIdList
     * 对于删除审核员，如果其分配的任务已经完成，则不再处理，
     * 如果状态为审核中，审核员字段标为0，标注状态为UNCHECKED，如果为审核未通过，则审核员字段标为0
     * 对于新增审核员，则为其分配标注任务
     */
    private void updateProjectCheckUser(LabelProject labelProject, List<Integer> deleteUserIdList, List<Integer> addUserIdList){
        int dataSetId = labelProject.getDatasetId();

        //处理删除的审核员
        for (Integer deleteUserId : deleteUserIdList) {
            List<LabelDatasetFile> labelDatasetFileList = this.labelDatasetFileService.getByUserAndDatasetId(dataSetId,deleteUserId,LabelTaskType.CHECK);
            for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
                if (labelDatasetFile.getStatus() == LabelStatus.FINISH)
                    continue;
                if (labelDatasetFile.getStatus() == LabelStatus.CHECKING)
                    labelDatasetFile.setStatus(LabelStatus.UNCHECK);

                labelDatasetFile.setCheckUserId(0);
            }
            if (!labelDatasetFileList.isEmpty())
                this.labelDatasetFileService.updateBatchById(labelDatasetFileList);

            // 删除对应统计数据
            this.statisInfoService.deleteByProjectIdAndUser(labelProject.getId(), deleteUserId, LabelTaskType.CHECK.getValue());
        }

        if (!deleteUserIdList.isEmpty()){
            QueryWrapper<LabelTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("user_id",deleteUserIdList).eq("label_project_id",labelProject.getId())
                    .eq("label_dataset_id",labelProject.getDatasetId()).eq("task_type", LabelTaskType.CHECK.getValue());
            labelTaskService.remove(queryWrapper);
        }

        //处理添加审核员
        List<LabelTask> labelTaskList = new ArrayList<>();
        for (Integer addUserId : addUserIdList) {
            LabelTask labelTask = LabelTask.builder().userId(addUserId).taskType(LabelTaskType.CHECK)
                    .keywords(labelProject.getKeywords()).labelProjectId(labelProject.getId()).labelDatasetId(dataSetId)
                    .defaultApplyCount(labelProject.getDefaultCheckCount()).build();

            labelTask.setTotalCount(this.labelDatasetFileService.getTaskTotalCount(labelTask));

            labelTaskList.add(labelTask);
        }
        if (!labelTaskList.isEmpty())
            this.labelTaskService.saveBatch(labelTaskList);

        // 添加对应统计数据
        List<LabelTaskStatisInfo> labelTaskStatis = this.labelTaskService.createLabelTaskStatis(labelProject,
                addUserIdList.stream().map(String::valueOf).collect(Collectors.toList()), LabelTaskType.CHECK);
        this.statisInfoService.saveBatch(labelTaskStatis);
    }

    /**
     * 更新项目中的标注员
     * @param labelProject
     * @param deleteUserIdList
     * @param addUserIdList
     * 对于删除标注员，如果其分配的任务已经完成，则不再处理，
     * 如果分配的任务没有完成，则将其标注信息置空，标注员、审核员字段标为0，标注状态为UNAPPLIED
     * 对于新增标注员，则为其分配标注任务
     */
    private void updateProjectLabelUser(LabelProject labelProject, List<Integer> deleteUserIdList, List<Integer> addUserIdList){
        int dataSetId = labelProject.getDatasetId();

        if (!deleteUserIdList.isEmpty()){
            QueryWrapper<LabelTask> queryWrapperLabel = new QueryWrapper<>();
            queryWrapperLabel.in("user_id",deleteUserIdList).eq("label_project_id",labelProject.getId())
                    .eq("label_dataset_id",labelProject.getDatasetId()).eq("task_type", LabelTaskType.LABEL.getValue());
            labelTaskService.remove(queryWrapperLabel);
        }

        QueryWrapper<LabelTask> queryWrapperCheck = new QueryWrapper<>();

        //处理删除的标注员
        for (Integer deleteUserId : deleteUserIdList) {
            List<LabelDatasetFile> labelDatasetFileList = this.labelDatasetFileService.getByUserAndDatasetId(dataSetId,deleteUserId,LabelTaskType.LABEL);

            List<Integer> checkIdList = labelDatasetFileList.stream().map(LabelDatasetFile::getCheckUserId).collect(Collectors.toList());
            if (checkIdList.size()!=0){
                queryWrapperCheck.in("user_id",checkIdList).eq("label_project_id",labelProject.getId())
                        .eq("label_dataset_id",labelProject.getDatasetId()).eq("task_type", LabelTaskType.CHECK.getValue());
                List<LabelTask> checkTaskList = labelTaskService.list(queryWrapperCheck);
                for (LabelTask checkTask: checkTaskList) {
                    checkTask.setTotalCount(checkTask.getFinishCount());
                    labelTaskService.updateById(checkTask);
                }
            }

            List<Integer> labelDatasetImageIdList = labelDatasetFileList.stream().filter(labelDatasetImage->labelDatasetImage.getStatus()!=LabelStatus.FINISH)
                    .map(LabelDatasetFile::getId).collect(Collectors.toList());
            if (!labelDatasetImageIdList.isEmpty()){
                UpdateWrapper<LabelDatasetFile> updateWrapper = new UpdateWrapper<>();
                updateWrapper.in("id",labelDatasetImageIdList).set("label_user_id",0).set("check_user_id",0)
                        .set("status",LabelStatus.UNAPPLIED).set("label",null).set("ai_label",null);
                this.labelDatasetFileService.update(updateWrapper);
            }

            // 删除对应统计数据
            this.statisInfoService.deleteByProjectIdAndUser(labelProject.getId(), deleteUserId, LabelTaskType.LABEL.getValue());
//            for (LabelDatasetImage labelDatasetImage : labelDatasetImageList) {
//                if (labelDatasetImage.getStatus() == LabelStatus.FINISH)
//                    continue;
//                //labelDatasetImage.setLabel(null);
//                updateWrapper.set(LabelDatasetImage::getLabel,null);
//                labelDatasetImage.setLabelUserId(0);
//                labelDatasetImage.setCheckUserId(0);
//                labelDatasetImage.setStatus(LabelStatus.UNAPPLIED);
//            }
//
//            if(!labelDatasetImageList.isEmpty())
//                this.labelDatasetImageService.updateBatchById(labelDatasetImageList);
        }

        //处理添加标注员
        List<LabelTask> labelTaskList = new ArrayList<>();
        for (Integer addUserId : addUserIdList) {
            LabelTask labelTask = LabelTask.builder().userId(addUserId).taskType(LabelTaskType.LABEL)
                    .keywords(labelProject.getKeywords()).labelProjectId(labelProject.getId()).labelDatasetId(dataSetId)
                    .defaultApplyCount(labelProject.getDefaultLabelCount()).build();

            labelTask.setTotalCount(this.labelDatasetFileService.getTaskTotalCount(labelTask));
            labelTaskList.add(labelTask);
        }
        if (!labelTaskList.isEmpty())
            this.labelTaskService.saveBatch(labelTaskList);

        // 添加对应统计数据
        List<LabelTaskStatisInfo> labelTaskStatis = this.labelTaskService.createLabelTaskStatis(labelProject,
                addUserIdList.stream().map(String::valueOf).collect(Collectors.toList()), LabelTaskType.LABEL);
        this.statisInfoService.saveBatch(labelTaskStatis);
    }

    /**
     * 更新标注项目和标注任务中的描述和关键字字段
     * @param labelProject
     */
    @Override
    public void updateLabelProject(LabelProject labelProject) {
        LabelProject dbLabelProject = labelProjectMapper.selectById(labelProject.getId());
        dbLabelProject.setKeywords(labelProject.getKeywords());
        dbLabelProject.setProjectDescription(labelProject.getProjectDescription());
        labelProjectMapper.updateById(dbLabelProject);

        UpdateWrapper<LabelTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("keywords",labelProject.getKeywords()).eq("label_project_id",labelProject.getId())
                .eq("label_dataset_id",dbLabelProject.getDatasetId());
        labelTaskService.update(updateWrapper);
    }

    @Override
    public Boolean isExistLabelProject(String projectName,int userId) {
        QueryWrapper<LabelProject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_name",projectName).eq("user_id",userId);
        if (labelProjectMapper.selectCount(queryWrapper)!=0){
            return false;
        }
        return true;
    }

    /**
     * 导出标注项目的标注信息并生成压缩包下载
     * @param projectId
     * @param response
     * @throws IOException
     */
    @Override
    public void exportLabelFileToZip(int projectId, HttpServletResponse response) throws IOException {
        log.info("开始处理项目：{} 的真值导出",projectId);
        LabelProject labelProject = labelProjectMapper.selectById(projectId);
        int datasetId = labelProject.getDatasetId();
        List<LabelDatasetFileInfo> labelDatasetFileInfos = this.labelDatasetImageInfoMapper
                .selectList(new QueryWrapper<LabelDatasetFileInfo>().eq("dataset_id",datasetId));

        log.info("开始处理真值");
        for (LabelDatasetFileInfo labelDatasetFileInfo : labelDatasetFileInfos) {
            String imagePath = FileUtils.getStringPath(this.rootDir, labelDatasetFileInfo.getPath());
            JSONObject labelJSON = JSONObject.parseObject(labelDatasetFileInfo.getLabel());
            LabelObject labelObject = JSONObject.toJavaObject(labelJSON, XMLLabelObjectInfo.class);
            if (labelObject == null || labelObject.isEmpty())
                continue;

            String coordinate = labelObject.getCoordinate();
            CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;

            if ("pixel".equalsIgnoreCase(coordinate))
                coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;
            if ("geodegree".equalsIgnoreCase(coordinate) &&
                    labelDatasetFileInfo.getCoordinateSystemType() == CoordinateSystemType.PROJCS)
                coordinateConvertType = CoordinateConvertType.PROJECTION_TO_LONLAT;
            LabelPointTypeConvertor.convertLabelPointType(imagePath,labelObject,coordinateConvertType);

            String imageName = labelDatasetFileInfo.getImageName();
            File labelXMLFile = FileUtils.getFile(rootDir, labelProject.getProjectName(), imageName.substring(0, imageName.lastIndexOf(".")) + ".xml");
            if (!labelXMLFile.getParentFile().exists())
                labelXMLFile.getParentFile().mkdirs();
            XMLUtils.toXMLFile(labelXMLFile.getAbsolutePath(),labelObject);
        }
        log.info("真值处理结束");

        log.info(String.valueOf(System.currentTimeMillis()));

        String srcPath = FileUtils.getStringPath(rootDir,labelProject.getProjectName());
        File srcFile = new File(srcPath);
        File zipFile = Paths.get(srcFile.getParent(),srcFile.getName()+".zip").toFile();
        OutputStream outputStream = new FileOutputStream(zipFile);
        CompressUtil.toZip(srcPath,outputStream);

        log.info(String.valueOf(System.currentTimeMillis()));

        try {
            response.setBufferSize(402800);
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(zipFile.getName(), "UTF-8"));
            response.setHeader("Content-Length", String.valueOf(FileUtils.getFile(zipFile.getAbsolutePath()).length()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("文件名转换失败：【{}】", e);
        }
        byte[] buff = new byte[(int) FileUtils.getFile(zipFile.getAbsolutePath()).length()];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {

            os = response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(zipFile.getAbsolutePath()));
            int i = bis.read(buff);
            os.write(buff, 0, i);
            os.flush();
            i = bis.read(buff);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("文件流读取异常：【{}】",e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("文件流关闭异常：【{}】",e);
                }
            }
        }
        log.info("文件：{} 下载成功",zipFile.getAbsolutePath());
        zipFile.delete();
    }

    /**
     * 获取合并任务的文件
     * @param
     * @return
     */
    @Override
    public List<Integer> createProjectByFileIsRepeat(int srcProjectId, List<Integer> mergeProjectIds, boolean isMerge,
                                                              String projectName, String projectDescription) {
        mergeProjectIds.add(srcProjectId); // 该参数只是选择的项目，后续操作需要合并包括原项目的所有标注项目，所以需要添加到这个参数中

        // 获取合并发布任务数据集
        List<LabelProject> projectList = this.labelProjectMapper.selectList(
                new QueryWrapper<LabelProject>().in("id", mergeProjectIds));
        // 通过对应数据集id获取数据集对应文件id
        List<Integer> datasetIdList = new ArrayList<>();
        for (LabelProject labelProject : projectList) {
            datasetIdList.add(labelProject.getDatasetId());
        }
        List<Integer> repeatDatasetFileIdList = this.labelDatasetFileMapper.getRepeatDatasetFile(datasetIdList);

        // 创建新标注项目
        UserInfo userInfo = new UserInfo();
        userInfo.setId(projectList.get(0).getUserId());
        userInfo.setName(projectList.get(0).getUserName());
        // 数据集文件不重复，可以直接合并为标注任务数据集
        if (repeatDatasetFileIdList.size() == 0) {
            this.createMergeProject(userInfo, null, srcProjectId, mergeProjectIds, true, projectName, projectDescription);
            return new ArrayList<>();
        }
        // 数据集文件重复，获取重复文件，根据合并或覆盖，调用合并标注的接口创建新标注
        this.createMergeProject(userInfo, repeatDatasetFileIdList, srcProjectId, mergeProjectIds, isMerge, projectName, projectDescription);

        return repeatDatasetFileIdList;
    }

    /**
     * 创建合并标注项目
     * @param userInfo 操作用户信息
     * @param fileIdList 如果有重复文件，传入选择的文件id集合
     * @param projectIdList 合并项目id集合
     * @param isMerge 若有冲突文件，是否合并
     * @param projectName 新建项目名称
     * @param projectDescription 项目描述
     */
    @Override
    public void createMergeProject(UserInfo userInfo, List<Integer> fileIdList, int srcProjectId,  List<Integer> projectIdList,
                                   boolean isMerge, String projectName, String projectDescription) {
        LabelProject labelProject = new LabelProject();
        // 获取合并发布任务数据集
        List<LabelProject> projectList = this.labelProjectMapper.selectList(
                new QueryWrapper<LabelProject>().select("dataset_id", "keywords", "category", "default_label_count", "default_check_count").in("id", projectIdList));
        // 通过对应数据集id获取数据集对应文件id
        List<LabelDatasetFile> labelDatasetFileList = new ArrayList<LabelDatasetFile>();
        List<Integer> datasetIdList = new ArrayList<>();
        for (LabelProject project : projectList) {
            datasetIdList.add(project.getDatasetId());
            labelProject.setKeywords(getKeywordFromJson(labelProject.getKeywords(), project.getKeywords()));
            labelProject.setCategory(project.getCategory());
            labelProject.setDefaultCheckCount(project.getDefaultCheckCount());
            labelProject.setDefaultLabelCount(project.getDefaultLabelCount());
        }

        if (fileIdList != null) { // 有重复，根据是否合并，进行创建项目
            if (isMerge) { // 合并文件，现在是按照合并之后相当于一个新创建的项目，后期要根据合并的项目原有信息，进行生成一个已有标注并合理分配给人员的项目
                // 设为空
                labelDatasetFileList = this.getDataSetFileByMergeToNull(labelDatasetFileList, datasetIdList);
                // 保留原有数据
                // labelDatasetFileList = this.getDataSetFileByMergeToNull(labelDatasetFileList, datasetIdList);
            } else { // 覆盖原标注项目文件
                // 设为空
                labelDatasetFileList = this.getDataSetFileToNull(labelDatasetFileList, datasetIdList, srcProjectId, fileIdList);
                // 保留原有数据
                // labelDatasetFileList = this.getDataSetFileToNotNull(labelDatasetFileList, datasetIdList, srcProjectId, fileIdList);
            }
        } else { // 没有重复文件，直接创建项目
            // 获取不重复文件所有信息，使得生成的文件信息，是有数据的，这里可能有问题，后期完善时，在进行该地方处理 TODO 注释回来就能使用
            //labelDatasetFileList = this.getDataSetFileByUnrepeatToNotNull(labelDatasetFileList, datasetIdList);
            // 生成空的文件
            labelDatasetFileList = this.getDataSetFileByUnrepeatToNull(labelDatasetFileList, datasetIdList);
        }
        LabelDataset labelDataset = this.labelDatasetService.getMergeDataSet(userInfo, datasetIdList, labelDatasetFileList);
        this.labelDatasetService.createMergeProjectDataset(labelDataset, labelDatasetFileList, projectIdList);

        // 创建合并标注项目
        labelProject.setDatasetId(labelDataset.getId());
        labelProject.setProjectName(projectName);
        labelProject.setProjectDescription(projectDescription);
        labelProject.setCreateTime(DateUtils.nowDate());
        labelProject.setTotalCount(labelDataset.getCount());
        labelProject.setUseLabel(true);
        labelProject.setDatasetType(labelDataset.getDatasetType());
        labelProject.setUserId(userInfo.getId());
        labelProject.setUserName(userInfo.getName());

        // 合并真实数据集ID
        List<String> projectRelateDatasetId = this.labelProjectMapper.getMergeProjectRelateDatasetId(projectIdList);
        labelProject.setRelatedDatasetId(this.getStrFromList(projectRelateDatasetId));

        // 是否使用已有的标注
        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            if (labelDatasetFile.getLabel() != null && !labelDatasetFile.getLabel().equals("")) {
                labelProject.setUseLabel(true);
                break;
            }
            labelProject.setUseLabel(false);
        }

        // 合并标注员
        List<LabelProject> labelProjectList = this.labelProjectMapper.selectBatchIds(projectIdList);
        Set<String> tempSet = new HashSet<>();
        List<String> tempList = new ArrayList<>();
        for (LabelProject project : labelProjectList) {
            Collections.addAll(tempList, project.getLabelUserIds().replace(" ", "").split(","));
            for (String s : tempList) {
                tempSet.add(s);
            }
        }
        tempList.clear();
        CollectionUtils.addAll(tempList, tempSet.iterator());
        labelProject.setLabelUserIds(this.getStrFromList(tempList));

        // 合并审核员
        tempSet.clear();
        tempList.clear();
        for (LabelProject project : labelProjectList) {
            Collections.addAll(tempList, project.getCheckUserIds().replace(" ", "").split(","));
            for (String s : tempList) {
                tempSet.add(s);
            }
        }
        tempList.clear();
        CollectionUtils.addAll(tempList, tempSet.iterator());
        labelProject.setCheckUserIds(this.getStrFromList(tempList));
        labelProject.setFinishCount(this.labelDatasetFileService.getTaskFinishCount(labelProject.getDatasetId()));
        labelProject.setCreateTime(DateUtils.nowDate());
        labelProject.setStatus(labelProject.getIsAiLabel()?LabelProjectStatus.AILABELING:LabelProjectStatus.LABELING);
        this.labelProjectMapper.insert(labelProject); // 创建标注项目
        //this.labelProjectService.createLabelProject(labelProject, request); // 创建标注项目

        // 更新数据集以及数据集文件
        labelDataset.setProjectId(labelProject.getId());
        this.labelDatasetService.updateById(labelDataset);

        // 合并标注任务
        List<LabelTask> labelTaskList = labelTaskService.getLabelTaskByProjectId(projectIdList);
        List<LabelTask> labelTasks = new ArrayList<>();
        if (fileIdList != null && fileIdList.size() != 0) {
            // 重复文件不保留原始数据
             labelTasks = labelTaskService.mergeTaskToNull(labelTaskList, labelProject, srcProjectId, projectIdList, isMerge, fileIdList, userInfo);

            // 重复文件保留原始数据
            // labelTasks = labelTaskService.mergeTaskByRepeatFileToNotNull(labelTaskList, labelProject, srcProjectId, projectIdList, isMerge, fileIdList, userInfo);
        } else {
            labelTasks = labelTaskService.mergeTaskToNull(labelTaskList, labelProject, srcProjectId, projectIdList, isMerge, fileIdList, userInfo);
        }
        this.labelTaskService.saveBatch(labelTasks);

        // 生成统计信息
        List<LabelDatasetFile> list = new ArrayList<>();
        QueryWrapper<LabelDatasetFile> wrapper = null;
        List<String> labelUserIdList = Arrays.asList(labelProject.getLabelUserIds().split(","));
        List<String> checkUserIdList = Arrays.asList(labelProject.getCheckUserIds().split(","));
        // 更新统计数据
        List<LabelTaskStatisInfo> labelTaskStatis = this.labelTaskService.createLabelTaskStatis(labelProject, labelUserIdList, LabelTaskType.LABEL);
        for (LabelTaskStatisInfo labelStatis : labelTaskStatis) {
            LabelDatasetFile fileCommitCount = this.labelDatasetFileService.getFileCommitCount(labelProject.getDatasetId(), labelStatis.getUserId());
            wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_id", labelDataset.getId()).eq("label_user_id", labelStatis.getUserId());
            list = this.labelDatasetFileService.list(wrapper);
            if (list != null && list.size() != 0) {
                labelStatis.setApplyFileCount(list.size());
                labelStatis.setCommitCount(fileCommitCount.getCommitCount());
               this.updateTaskStatisByFileList(list, labelStatis);
            }
        }
        this.statisInfoService.saveBatch(labelTaskStatis);
        List<LabelTaskStatisInfo> checkTaskStatis = this.labelTaskService.createLabelTaskStatis(labelProject, checkUserIdList, LabelTaskType.CHECK);
        for (LabelTaskStatisInfo labelStatis : checkTaskStatis) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_id", labelDataset.getId()).eq("check_user_id", labelStatis.getUserId());
            list = this.labelDatasetFileService.list(wrapper);
            if (list != null && list.size() != 0) {
                labelStatis.setApplyFileCount(list.size());
                this.updateTaskStatisByFileList(list, labelStatis);
            }
        }
        this.statisInfoService.saveBatch(checkTaskStatis);
    }

    /**
     * 根据目标项目类型，获取同类型标注项目
     * @param projectId
     * @return
     */
    @Override
    public List<LabelProject> getProjectByDataSetType(int projectId) {
        LabelProject labelProject = this.labelProjectMapper.selectById(projectId);
        QueryWrapper<LabelProject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_type", labelProject.getDatasetType())
                .eq("user_id", labelProject.getUserId())
                .ne("id", projectId);
        List<LabelProject> labelProjectList = this.labelProjectMapper.selectList(queryWrapper);
        return labelProjectList;
    }

    /**
     * 将集合中的元素拼接为逗号分隔的字符串
     * @param list
     * @return
     */
    private String getStrFromList(List<String> list) {
        StringBuffer buffer = new StringBuffer();
        String[] split = null;
        for (int i = 0; i < list.size(); i++) {
            split = list.get(i).split(",");
            if (split.length > 1) { // 说明该标注项目有多个真实数据集
                buffer.append(getStrFromList(Arrays.asList(split.clone())));
                buffer.append(",");
                continue;
            }
            buffer.append(list.get(i));
            if (i < list.size() - 1) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

    /**
     * 合并标注关键字
     * @param keywords
     * @param keywords1
     * @return
     */
    public String getKeywordFromJson(String keywords, String keywords1) {
        HashSet<JSONObject> set = new HashSet<>();
        JSONArray array = JSONObject.parseArray(keywords);
        JSONArray array1 = JSONObject.parseArray(keywords1);
        if (array != null) {
            set.addAll(array.toJavaList(JSONObject.class));
        }
        if (array1 != null) {
            set.addAll(array1.toJavaList(JSONObject.class));
        }
        return set.toString().replace(" ", "");
    }

    /**
     * 更新合并项目统计数据
     * @param list
     * @return
     */
    public void updateTaskStatisByFileList(List<LabelDatasetFile> list, LabelTaskStatisInfo statisInfo) {
        JSONArray object = null;
        boolean finishFlag = false;
        for (LabelDatasetFile labelDatasetFile : list) {
            if (labelDatasetFile.getLabel() != null && labelDatasetFile.getStatus() == LabelStatus.FINISH) {
                object = (JSONArray) JSONObject.parseObject(labelDatasetFile.getLabel()).get("object");
                statisInfo.setCommitObjectCount(statisInfo.getCommitObjectCount()+object.size());
            }
            if (labelDatasetFile.getStatus() == LabelStatus.FINISH) {
                statisInfo.setFinishCount(statisInfo.getFinishCount()+1);
            }
            if (labelDatasetFile.getFinishLabelTime() != null) {
                finishFlag = true;
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss").withLocale(Locale.CHINA);
        if (statisInfo.getUserRole() == 0 && finishFlag) {
                LocalDateTime startTime = LocalDateTime.parse(
                        dateFormat.format(this.labelDatasetFileService.getFristAssignLabelTimeByDatasetid(list.get(0).getDatasetId(), list.get(0).getLabelUserId())), formatter);
                LocalDateTime endTime = LocalDateTime.parse(
                        dateFormat.format(this.labelDatasetFileService.getLastFinishLabelTimeByDatasetid(list.get(0).getDatasetId(), list.get(0).getLabelUserId())), formatter);
                Duration interval;
                long intervalMin;
                interval = Duration.between(startTime, endTime);
                intervalMin = interval.toMinutes();
                if (intervalMin <= 1) {
                    intervalMin = 1;
                }
                statisInfo.setTimeConsume(intervalMin);
        }
        if (statisInfo.getUserRole() == 1 && statisInfo.getFinishCount() != 0) {
            LocalDateTime startTime = LocalDateTime.parse(
                    dateFormat.format(this.labelDatasetFileService.getFristAssignCheckTimeByDatasetid(list.get(0).getDatasetId(), list.get(0).getCheckUserId())), formatter);
            LocalDateTime endTime = LocalDateTime.parse(
                    dateFormat.format(this.labelDatasetFileService.getLastFinishCheckTimeByDatasetid(list.get(0).getDatasetId(), list.get(0).getCheckUserId())), formatter);
            Duration interval;
            long intervalMin;
            interval = Duration.between(startTime, endTime);
            intervalMin = interval.toMinutes();
            if (intervalMin <= 1) {
                intervalMin = 1;
            }
            statisInfo.setTimeConsume(intervalMin);
        }
    }

    /**
     * 没有重复文件：不保留原始数据
     * @param labelDatasetFileList
     * @param datasetIdList
     * @return
     */
    private List<LabelDatasetFile> getDataSetFileByUnrepeatToNull(List<LabelDatasetFile> labelDatasetFileList, List<Integer> datasetIdList) {
        labelDatasetFileList = this.labelDatasetFileMapper.selectList(new QueryWrapper<LabelDatasetFile>()
                .select("id", "dataset_id", "file_id", "1 AS status").in("dataset_id", datasetIdList));
        return labelDatasetFileList;
    }

    /**
     * 没有重复文件：保留原始数据
     * @param labelDatasetFileList
     * @param datasetIdList
     * @return
     */
    private List<LabelDatasetFile> getDataSetFileByUnrepeatToNotNull(List<LabelDatasetFile> labelDatasetFileList, List<Integer> datasetIdList) {
        labelDatasetFileList = this.labelDatasetFileMapper.selectList(new QueryWrapper<LabelDatasetFile>()
                .in("dataset_id", datasetIdList));
        return labelDatasetFileList;
    }

    /**
     * 重复文件-合并情况：不保留原始数据
     * @param labelDatasetFileList
     * @param datasetIdList
     * @return
     */
    private List<LabelDatasetFile> getDataSetFileByMergeToNull(List<LabelDatasetFile> labelDatasetFileList, List<Integer> datasetIdList) {
        Map<Integer, LabelDatasetFile> fileMap = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        labelDatasetFileList = this.labelDatasetFileMapper.selectList(new QueryWrapper<LabelDatasetFile>()
                .select("id", "dataset_id", "file_id", "1 AS status").in("dataset_id", datasetIdList));
        labelDatasetFileList.forEach(file->{
            LabelDatasetFile tempFile = fileMap.get(file.getFileId());
            if (tempFile == null) {
                fileMap.put(file.getFileId(), file);
            } else {
                //tempFile.setDatasetId();
                if (file.getLabel() != null) {
                    JSONArray jsonArr = (JSONArray) JSONObject.parseObject(file.getLabel()).get("object");
                    jsonArray.addAll(jsonArr);
                }
                if (tempFile.getLabel() != null) {
                    JSONArray jsonArr1 = (JSONArray) JSONObject.parseObject(tempFile.getLabel()).get("object");
                    jsonArray.addAll(jsonArr1);
                }
                if (jsonArray.size() != 0) {
                    jsonObject.put("object", jsonArray);
                    String labelValue = JSON.toJSONString(jsonObject);
                    tempFile.setLabel(labelValue);
                    jsonArray.clear();
                }
                tempFile.setStatus(LabelStatus.UNAPPLIED);
                fileMap.put(file.getFileId(), tempFile);
            }
        });
        return labelDatasetFileList = new ArrayList<>(fileMap.values());
    }

    /**
     *  重复文件-覆盖情况：不保留原有值
     * @param labelDatasetFileList
     * @param datasetIdList
     * @param srcProjectId
     * @param fileIdList
     */
    private List<LabelDatasetFile> getDataSetFileToNull(List<LabelDatasetFile> labelDatasetFileList, List<Integer> datasetIdList,
                                      int srcProjectId, List<Integer> fileIdList) {
        labelDatasetFileList = this.labelDatasetFileMapper.selectList(new QueryWrapper<LabelDatasetFile>()
                .select("id", "dataset_id", "file_id", "1 AS status").in("dataset_id", datasetIdList));
        LabelProject srcProject = this.labelProjectMapper.selectById(srcProjectId);
        List<LabelDatasetFile> srcProjectFileList = this.labelDatasetFileMapper.selectList(new QueryWrapper<LabelDatasetFile>()
                .select("id", "dataset_id", "file_id", "1 AS status").in("dataset_id", datasetIdList)
                .and(qw -> {
                    return qw.eq("dataset_id", srcProject.getDatasetId());
                }));
        Map<Integer, LabelDatasetFile> srcProjectFileMap = srcProjectFileList
                .stream().collect(Collectors.toMap(LabelDatasetFile::getFileId, a -> a, (k1, k2) -> k1));
        for (Integer fileId : fileIdList) {
            LabelDatasetFile labelDatasetFile = srcProjectFileMap.get(fileId);
            labelDatasetFileList.remove(labelDatasetFile);
        }
        return labelDatasetFileList;
    }

    /**
     *  重复文件-覆盖情况：保留原有值
     * @param labelDatasetFileList
     * @param datasetIdList
     * @param srcProjectId
     * @param fileIdList
     */
    private List<LabelDatasetFile> getDataSetFileToNotNull(List<LabelDatasetFile> labelDatasetFileList, List<Integer> datasetIdList,
                                      int srcProjectId, List<Integer> fileIdList) {
        labelDatasetFileList = this.labelDatasetFileMapper.selectList(new QueryWrapper<LabelDatasetFile>()
                .in("dataset_id", datasetIdList));
        LabelProject srcProject = this.labelProjectMapper.selectById(srcProjectId);
        List<LabelDatasetFile> srcProjectFileList = this.labelDatasetFileMapper.selectList(new QueryWrapper<LabelDatasetFile>()
                .in("dataset_id", datasetIdList)
                .and(qw -> {
                    return qw.eq("dataset_id", srcProject.getDatasetId());
                }));
        Map<Integer, LabelDatasetFile> srcProjectFileMap = srcProjectFileList
                .stream().collect(Collectors.toMap(LabelDatasetFile::getFileId, a -> a, (k1, k2) -> k1));
        for (Integer fileId : fileIdList) {
            LabelDatasetFile labelDatasetFile = srcProjectFileMap.get(fileId);
            labelDatasetFileList.remove(labelDatasetFile);
        }
        return labelDatasetFileList;
    }

    /**
     * 获取同目标标注任务使用同一真实数据集的标注项目
     * @param projectId
     * @param relatedDatasetId
     * @return
     */
    @Override
    public List<LabelProject> getProjectsByRelateDataset(int projectId, String relatedDatasetId) {
        QueryWrapper<LabelProject> queryWrapper = new QueryWrapper<LabelProject>().ne("id", projectId)
                .eq("related_dataset_id", relatedDatasetId);
        List<LabelProject> labelProjects = this.labelProjectMapper.selectList(queryWrapper);
        return labelProjects;
    }

    /**
     * 更新样本产出量
     * @param projectId
     * @param nums
     */
    @Override
    public void updateSampleSetNumById(int projectId, int nums) {
        LabelProject project = this.labelProjectMapper.selectById(projectId);
        LambdaUpdateChainWrapper<LabelProject> updateWrapper = new LambdaUpdateChainWrapper<>(this.labelProjectMapper);
        updateWrapper.set(LabelProject::getMakeSampleSetNum, project.getMakeSampleSetNum()+nums).eq(LabelProject::getId, projectId).update();
    }

    /**
     * 时空覆盖率：以不同统计标准返回对应标准影像个数
     * @return
     */
    @Override
    public List<LabelProjectRelateFileSTCInfo> getProjectSTCByStandard(LabelProjectRelateFileSTCRequest params) {
        LabelProject labelProject = this.getById(params.getProjectId());
        if (labelProject.getDatasetType() != DatasetType.IMAGE) {
            return null;
        }
        List<LabelProjectRelateFileSTCInfo> relateFileList = new ArrayList<>();

        try {
            if (params.getStatisWay().equals("date")) {
                // 按时间统计：统计维度处理
                relateFileList = this.getRelateFileByStandard(params);
            } else if (params.getStatisWay().equals("lon")) {
                // 按经度统计
                relateFileList = this.getRelateFileByLon(params);
            } else if (params.getStatisWay().equals("map")) {
                // STC空间覆盖热力图
                int[][] relateFileToMap = this.getRelateFileToMap(params);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relateFileList;
    }


    /**
     * 时空覆盖率：热力图数据返回为List列表
     * @return
     */
    @Override
    public List<JSONObject> getProjectSTCToHeatmap(LabelProjectRelateFileSTCRequest params) {
        List<JSONObject> result = new ArrayList<>();
        Map<String, Integer> maps = new HashMap<>();
        String mapKey = "";
        JSONObject element = new JSONObject();

        List<LabelProjectRelateFileSTCRequest> files = this.labelProjectMapper.getProjectFilesByPid(params.getProjectId());
        float centeredLon = 0;
        float centeredLat = 0;
        for (LabelProjectRelateFileSTCRequest file : files) {
            centeredLon = file.getMinLon()+((file.getMaxLon()-file.getMinLon())/2);
            centeredLat = file.getMinLat()+((file.getMaxLat()-file.getMinLat())/2);
            mapKey = centeredLon+":"+centeredLat;
            if (maps.get(mapKey) == null ) {
                maps.put(mapKey, 1);
            } else {
                maps.put(mapKey, maps.get(mapKey)+1);
            }
        }

        // 设置返回数据
        Set<String> keySet = maps.keySet();
        String[] lonAndLat = null;
        for (String key : keySet) {
            lonAndLat = key.split(":");
            element = new JSONObject();
            element.put("lng", lonAndLat[0]);
            element.put("lat", lonAndLat[1]);
            element.put("deaths", maps.get(key).toString());
            result.add(element);
        }
        return result;
    }

    /**
     * 以经纬度统计标注项目文件热力图
     * @param params
     * @return
     */
    private int[][] getRelateFileToMap(LabelProjectRelateFileSTCRequest params) {
        List<int[]> result = new Vector<>();
        List<LabelProjectRelateFileSTCRequest> files = this.labelProjectMapper.getProjectFilesByPid(params.getProjectId());
        float centeredLon = 0;
        float centeredLat = 0;
//        int[][] resultArr = new int[9][9];
        int[][] resultArr = new int[13][7];
        for (LabelProjectRelateFileSTCRequest file : files) {
            centeredLon = file.getMinLon()+((file.getMaxLon()-file.getMinLon())/2);
            centeredLat = file.getMinLat()+((file.getMaxLat()-file.getMinLat())/2);
            this.setSTCMapValue(resultArr, centeredLon, centeredLat);
        }
        for (int i = 0; i < resultArr.length; i++) {
            for (int j = 0; j < resultArr[i].length; j++) {
                System.out.print("("+i+","+j+")->"+resultArr[i][j]+"  ");
            }
            System.out.println("");
        }

        return resultArr;
    }

    /**
     * 设置热力分布图值
     * @param resultArr
     * @param centeredLon
     * @param centeredLat
     */
    private void setSTCMapValue(int[][] resultArr, float centeredLon, float centeredLat) {
        float preX = -180;
        float preY = -90;
        float indX = -150;
        float indY = -60;
        /*float preX = 0;
        float preY = 0;
        float indX = 20;
        float indY = 20;*/
        for (int i = 0; i < resultArr.length-1; i++) {
            if (preX <= centeredLon && centeredLon <= indX) {
                for (int j = 0; j < resultArr[i].length-1; j++) {
                    if (preY <= centeredLat && centeredLat <= indY) {
                        resultArr[i][j] = resultArr[i][j] + 1;
                        return; // 找到属于某一热力区域，执行结束
                    } else {
                        preY = indY;
                        indY += 30;
                    }
                }
            } else {
                preX = indX;
                indX += 30;
            }
        }

    }/**/

    /**
     * 以时间基准维度统计标注项目文件数
     * @param params
     * @return
     * @throws ParseException
     */
    private List<LabelProjectRelateFileSTCInfo> getRelateFileByStandard(LabelProjectRelateFileSTCRequest params) throws ParseException {
        boolean flag = true;
        Date lastTime = null;
        List<LabelProjectRelateFileSTCInfo> result = new Vector<>();
        // 时间处理
        LabelProjectRelateFileSTCRequest projectFile = this.labelProjectMapper.getProjectFileSelectConditon(params);
        LabelProjectRelateFileSTCInfo stcInfo = new LabelProjectRelateFileSTCInfo();
        Date startTime = projectFile.getStartTime();
        Date endTime = projectFile.getEndTime();
        // 开始处理
        if (params.getStandard() != null) {
            String standard = params.getStandard().toUpperCase();
            while (flag) {
                params.setStartTime(startTime);
                if (standard.equals("DAY")) { // 筛选维度
                    lastTime = cn.iecas.geoai.labelplatform.util.DateUtils.getDayLastTime(startTime);
                } else if (standard.equals("WEEK")) {
                    lastTime = cn.iecas.geoai.labelplatform.util.DateUtils.getWeekLastTime(startTime);
                } else if (standard.equals("MONTH")) {
                    lastTime = cn.iecas.geoai.labelplatform.util.DateUtils.getMonthLastTime(startTime);
                } else if (standard.equals("YEAR")) {
                    lastTime = cn.iecas.geoai.labelplatform.util.DateUtils.getYearLastTime(startTime);
                }

                if (lastTime.compareTo(endTime) >= 0) {
                    flag = false;
                }
                // 返回List类型统计数据
                params.setEndTime(lastTime);
                projectFile = this.labelProjectMapper.getProjectRelateFileByPid(params);
                String validDate = cn.iecas.geoai.labelplatform.util.DateUtils.getValidDate(params.getStartTime(), params.getStandard());
                stcInfo = LabelProjectRelateFileSTCInfo.builder().tab(validDate).spaceTime(projectFile.getSpaceTime()).build();
                result.add(stcInfo);

                // 通用筛选维度：下一天、周、月开始时间
                startTime = cn.iecas.geoai.labelplatform.util.DateUtils.getTomorrowStartTime(lastTime);
            }
        } else {
            while (flag) {
                params.setStartTime(startTime);
                // 筛选维度 默认按照年统计
                lastTime = cn.iecas.geoai.labelplatform.util.DateUtils.getYearLastTime(startTime);

                if (lastTime.compareTo(endTime) >= 0) {
                    flag = false;
                }
                // 返回List类型统计数据
                params.setEndTime(lastTime);
                projectFile = this.labelProjectMapper.getProjectRelateFileByPid(params);
                String validDate = cn.iecas.geoai.labelplatform.util.DateUtils.getValidDate(params.getStartTime(), "YEAR");
                stcInfo = LabelProjectRelateFileSTCInfo.builder().tab(validDate).spaceTime(projectFile.getSpaceTime()).build();
                result.add(stcInfo);

                // 通用筛选维度：下一天、周、月开始时间
                startTime = cn.iecas.geoai.labelplatform.util.DateUtils.getTomorrowStartTime(lastTime);
            }
        }

        return result;
    }

    /**
     * 以经度基准维度统计标注项目文件数
     * @param params
     * @return
     */
    private List<LabelProjectRelateFileSTCInfo> getRelateFileByLon(LabelProjectRelateFileSTCRequest params) {
        List<LabelProjectRelateFileSTCInfo> result = new Vector<>();
        LabelProjectRelateFileSTCRequest projectFile = null;
        LabelProjectRelateFileSTCInfo stcInfo = new LabelProjectRelateFileSTCInfo();

        for (int i = 0; i <= 120; i+=60) {
            params.setMinLon(i);
            params.setMaxLon(i+60);
            projectFile = this.labelProjectMapper.getProjectRelateFileByLon(params);
            stcInfo = LabelProjectRelateFileSTCInfo.builder().tab(i+"~"+(i+60)).spaceTime(projectFile.getSpaceTime()).build();
            result.add(stcInfo);
        }

        for (int i = -180; i <= -60; i+=60) {
            params.setMinLon(i);
            params.setMaxLon(i+60);
            projectFile = this.labelProjectMapper.getProjectRelateFileByLon(params);
            stcInfo = LabelProjectRelateFileSTCInfo.builder().tab(i+"~"+(i+60)).spaceTime(projectFile.getSpaceTime()).build();
            result.add(stcInfo);
        }
        return result;
    }

    public Map<String, Integer> initMap() {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                map.put(i+"-"+j, 0);
            }
        }
        return map;
    }

    /**
     * 获取变化检测关联文件信息
     * @param id
     * @return
     */
    @Override
    public JSONObject getChangeDetectionFileById(int id) {
        LabelDatasetFile labelDatasetFile = this.labelDatasetFileService.getFileByLdfId(id);
        if (labelDatasetFile.getRelatedFileId() == 0) {
            return null;
        }
        JSONObject imageInfo = this.fileService.getFileInfoById(labelDatasetFile.getRelatedFileId(),null);
        return imageInfo;
    }

    /**
     * 更新变化检测关联文件信息
     * @param id
     * @return
     */
    @Override
    public void updateLabelFileById(int id, int relatedFileId) {
        this.labelDatasetFileService.updateLabelFileById(id, relatedFileId);
    }

    /**
     * 变化检测时对样本集合进行简单分组
     * @param sampleVos
     * @return List
     */
    @Override
    public List<DeteSampleVo> listDeteSamples(List<JSONObject> sampleVos) {
        //中心点经纬度
        Map<String, List<Image>> lngMap = new HashMap<>(sampleVos.size());
        List<Image> value;
        String key;
        for (JSONObject sample : sampleVos) {
            Image image = JSONObject.parseObject(sample.toJSONString(), Image.class);
            key = getGroupName(image.getMinLat(), image.getMinLon(), image.getMaxLat(), image.getMaxLon());
            value = lngMap.get(key);
            if (CollectionUtils.isEmpty(value)) {
                value = new ArrayList<>(1);
                value.add(image);
                lngMap.put(key, value);
                continue;
            }
            value.add(image);
            lngMap.put(key, value);
        }
        if (MapUtils.isNotEmpty(lngMap)) {
            List<DeteSampleVo> deteSampleVoList = new ArrayList<>();
            DeteSampleVo deteSampleVo;
            int size;
            for (Map.Entry<String, List<Image>> entry : lngMap.entrySet()) {
                size = entry.getValue().size();
                if (size <= 1) {
                    continue;
                }
                deteSampleVo = new DeteSampleVo();
                deteSampleVo.setGroupName(entry.getKey());
                deteSampleVo.setSamples(entry.getValue());
                deteSampleVoList.add(deteSampleVo);
            }
            return deteSampleVoList;
        }
        return new ArrayList<>(1);
    }

    /**
     * 变化检测时对样本集合进行简单分组
     * @param sampleVos
     * @return Map
     */
    @Override
    public Map<String, List<Image>> mapDeteSamples(List<JSONObject> sampleVos) {
        //中心点经纬度
        Map<String, List<Image>> lngMap = new HashMap<>(sampleVos.size());
        List<Image> value;
        String key;
        for (JSONObject sample : sampleVos) {
            Image image = JSONObject.parseObject(sample.toJSONString(), Image.class);
            key = getGroupName(image.getMinLat(), image.getMinLon(), image.getMaxLat(), image.getMaxLon());
            value = lngMap.get(key);
            if (CollectionUtils.isEmpty(value)) {
                value = new ArrayList<>(1);
                value.add(image);
                lngMap.put(key, value);
                continue;
            }
            if (value.size() > 2) {
                throw new RuntimeException("关联文件过多，每组关联文件个数应为2");
            }
            value.add(image);
            lngMap.put(key, value);
        }
        return lngMap;
    }

    /**
     * 按边界坐标经纬度小数点后2位进行分组
     * 根据边界坐标经纬度拼接成分组名称
     *
     * @param minLat
     * @param minLon
     * @param maxLat
     * @param maxLon
     * @return
     */
    private String getGroupName(Double minLat, Double minLon, Double maxLat, Double maxLon) {
        if (minLat.isNaN() || minLon.isNaN() || maxLat.isNaN() || maxLon.isNaN()) {
            return DEFAULT_GROUP_NAME;
        }
        String minLatStr = String.format("%.2f", minLat);
        String minLonStr = String.format("%.2f", minLon);
        String maxLatStr = String.format("%.2f", maxLat);
        String maxLonStr = String.format("%.2f", maxLon);
        StringBuilder key = new StringBuilder(minLatStr).append(",").append(minLonStr)
                .append("~").append(maxLatStr).append(",").append(maxLonStr);
        return key.toString();
    }


    //    @Override
//    public void exportLabelFileToZip(int projectId, HttpServletResponse response) throws IOException {
//        LabelProject labelProject = labelProjectMapper.selectById(projectId);
//        int datasetId = labelProject.getDatasetId();
//        List<LabelDatasetImage> labelDatasetImageList = labelDatasetImageService.list(new QueryWrapper<LabelDatasetImage>().eq("dataset_id",datasetId));
//        for (LabelDatasetImage labelDatasetImage : labelDatasetImageList){
//            //if (labelDatasetImage.getStatus() ==LabelStatus.FINISH){
//                List<Integer> imageIdList = new ArrayList<>();
//                imageIdList.add(labelDatasetImage.getImageId());
//                String imageName = imageService.listImageInfoByIdList(imageIdList).get(0).getImageName();
//                if (labelDatasetImage.getLabel()!=null && !labelDatasetImage.getLabel().equals("{\"object\":[]}")) {
//                    String xml = this.labelJsonToXml(labelDatasetImage.getLabel(), labelDatasetImage.getImageId(), FileType.XML);
//                    File imageLabelFile = FileUtils.getFile(rootDir, labelProject.getProjectName(), imageName.substring(0, imageName.lastIndexOf(".")) + ".xml");
//                    if (!imageLabelFile.getParentFile().exists()) {
//                        imageLabelFile.getParentFile().mkdirs();
//                    }
//                    try {
//                        FileWriter writer = new FileWriter(imageLabelFile);
//                        writer.write(xml);
//                        writer.flush();
//                        writer.close();
//                    } catch (IOException ioException) {
//                        ioException.printStackTrace();
//                    }
//                }
//           // }
//        }
//
//        log.info(String.valueOf(System.currentTimeMillis()));
//        String zipPath = ZipUtils.zipDirectory(FileUtils.getStringPath(rootDir,labelProject.getProjectName()));
//        File zipFile = new File(zipPath);
//        log.info(String.valueOf(System.currentTimeMillis()));
//
//        try {
//            response.setBufferSize(402800);
//            response.setHeader("content-type", "application/octet-stream");
//            response.setContentType("application/octet-stream");
//            response.setCharacterEncoding("UTF-8");
//            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(zipFile.getName(), "UTF-8"));
//            response.setHeader("Content-Length", String.valueOf(FileUtils.getFile(zipPath).length()));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            log.error("文件名转换失败：【{}】", e);
//        }
//        byte[] buff = new byte[(int) FileUtils.getFile(zipPath).length()];
//        BufferedInputStream bis = null;
//        OutputStream os = null;
//        try {
//
//            os = response.getOutputStream();
//            bis = new BufferedInputStream(new FileInputStream(zipPath));
//            int i = bis.read(buff);
//            os.write(buff, 0, i);
//            os.flush();
//            i = bis.read(buff);
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.error("文件流读取异常：【{}】",e);
//        } finally {
//            if (bis != null) {
//                try {
//                    bis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    log.error("文件流关闭异常：【{}】",e);
//                }
//            }
//        }
//        log.info("文件：{} 下载成功",zipPath);
//        zipFile.delete();
//    }

//
//    public String labelJsonToXml(String label,int imageId,FileType fileType){
//        String xml;
//        JSONObject jsonObject = new JSONObject();
//        List<Integer> imageIdList = new ArrayList<>();
//        imageIdList.add(imageId);
//        Image imageInfo = imageService.listImageInfoByIdList(imageIdList).get(0);
//        String imagePath = FileUtils.getStringPath(rootDir,imageInfo.getPath());
//
//        if (fileType == FileType.XML){
//            JSONObject labelInfo = JSONObject.fromObject(label);
//            JSONArray jsonArray = labelInfo.getJSONArray("object");
//
//            for (int index = 0; index < jsonArray.size(); index++) {
//                JSONObject object = jsonArray.getJSONObject(index);
//                String coordinate = object.getString("coordinate");
//                JSONArray pointObjects = object.getJSONObject("points").getJSONArray("point");
//                List<String> points = new ArrayList<>();
//
//                for(Object point : pointObjects){
//                    double lon = Double.parseDouble(point.toString().split(",")[0]);
//                    double lat = Double.parseDouble(point.toString().split(",")[1]);
//                    if ("pixel".equalsIgnoreCase(coordinate) ||  CoordinateSystemType.PIXELCS.equals(imageInfo.getCoordinateSystemType())){
//                        lat = imageInfo.getHeight() - lat;
//                    }
//                    if ("geodegree".equalsIgnoreCase(coordinate) && CoordinateSystemType.PROJCS.equals(imageInfo.getCoordinateSystemType())){
//                        double[] coordinates = GeoUtils.coordinateConvertor(lon,lat,imagePath, GeoUtils.COORDINATE_LONLAT);
//                        lon = coordinates[0];
//                        lat = coordinates[1];
//
//                        //应对一些object为像素坐标，但是coordinate为geodegree的错误情况
//                        if (lon > 180 || lon < -180){
//                            object.put("coordinate","pixel");
//                            lat = imageInfo.getHeight() - lat;
//                        }
//                    }
//
//                    point = lon + "," + lat;
//                    points.add((String) point);
//                }
//
//                //应对一些object为像素坐标，但是coordinate为geodegree的错误情况
//                double firstLon = Double.parseDouble(points.get(0).split(",")[0]);
//                if (firstLon > 180 || firstLon < -180){
//                    object.put("coordinate","pixel");
//                    object.put("description","像素坐标");
//                }
//                object.getJSONObject("points").put("point",points);
//
////                if ("pixel".equalsIgnoreCase(coordinate))
////                    break;
////                double firstLon = Double.parseDouble(pointObjects.get(0).toString().split(",")[0]);
////                if (firstLon > 180 || firstLon < -180){
////                    List<String> points = new ArrayList<>();
////                    for (Object point : pointObjects){
////                        double lon = Double.parseDouble(point.toString().split(",")[0]);
////                        double lat = Double.parseDouble(point.toString().split(",")[1]);
////                        double[] coordinates = GeoUtils.coordinateConvertor(lon,lat,imagePath, GeoUtils.COORDINATE_LONLAT);
////                        point = coordinates[0] + "," + coordinates[1];
////                        points.add((String) point);
////                    }
////
////                    //应对一些object为像素坐标，但是coordinate为geodegree的错误情况
////                    firstLon = Double.parseDouble(points.get(0).split(",")[0]);
////                    if (firstLon > 180 || firstLon < -180){
////                        object.put("coordinate","pixel");
////                        object.put("description","像素坐标");
////                    }
////                    object.getJSONObject("points").put("point",points);
////                }
//            }
//
//            JSONObject jsonObjectObj = new JSONObject();
//            jsonObjectObj.put("object",jsonArray);
//            JSONObject imageSource = new JSONObject();
//            imageSource.put("filename",imagePath);
//
//            Annotation annotation = new Annotation();
//            //annotation.builder().objects(jsonArray).source(imageInfo).build();
//            annotation.setObjects(jsonObjectObj);
//            annotation.setSource(imageSource);
//
//            jsonObject.put("annotation",annotation);
//        }else{
//            jsonObject = JSONObject.fromObject(FormatJson.jsonToVif(label).toString());
//        }
//
//
//
//        StringReader input = new StringReader(jsonObject.toString());
//        StringWriter output = new StringWriter();
//        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).repairingNamespaces(false).build();
//        try {
//            XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
//            XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);
//            writer = new PrettyXMLEventWriter(writer);
//            writer.add(reader);
//            reader.close();
//            writer.close();
//        } catch( Exception e){
//            e.printStackTrace();
//        } finally {
//            try {
//                output.close();
//                input.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if(output.toString().length()>=38){//remove <?xml version="1.0" encoding="UTF-8"?>
//            xml = output.toString().substring(39);
//        }
//        else {
//            xml = output.toString();
//        }
//        return xml;
//    }


}