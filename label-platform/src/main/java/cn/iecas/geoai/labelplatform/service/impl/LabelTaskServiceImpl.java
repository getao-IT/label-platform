package cn.iecas.geoai.labelplatform.service.impl;

import cn.aircas.utils.comporess.CompressUtil;
import cn.aircas.utils.date.DateUtils;
import cn.aircas.utils.file.FileUtils;
import cn.aircas.utils.image.ImageFormat;
import cn.aircas.utils.image.ImageInfo;
import cn.aircas.utils.image.ParseImageInfo;
import cn.aircas.utils.image.emun.CoordinateSystemType;
import cn.aircas.utils.image.geo.GeoUtils;
import cn.iecas.geoai.labelplatform.dao.LabelProjectMapper;
import cn.iecas.geoai.labelplatform.dao.LabelTaskMapper;
import cn.iecas.geoai.labelplatform.dao.LabelTaskStatisInfoMapper;
import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.*;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import cn.iecas.geoai.labelplatform.entity.fileFormat.XMLLabelObjectInfo;
import cn.iecas.geoai.labelplatform.service.*;
import cn.iecas.geoai.labelplatform.service.labelFileService.LabelFileService;
import cn.iecas.geoai.labelplatform.util.FFmpegFrameGrabberUtils;
import cn.iecas.geoai.labelplatform.util.JedisUtils;
import cn.iecas.geoai.labelplatform.util.XMLUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.dom4j.DocumentException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@PropertySource(value = "classpath:application.yml")
public class LabelTaskServiceImpl extends ServiceImpl<LabelTaskMapper, LabelTask> implements LabelTaskService {
    @Autowired
    FileService fileService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @Autowired
    private LabelTaskMapper labelTaskMapper;

    @Autowired
    private LabelProjectService labelProjectService;

    @Autowired
    private LabelDatasetFileService labelDatasetFileService;

    @Autowired
    private LabelTaskStatisInfoMapper taskStatisInfoMapper;

    @Autowired
    private LabelTaskStatisInfoService taskStatisInfoService;

    @Autowired
    private LabelTaskStatisInfoService statisInfoService;

    @Autowired
    private LabelProjectMapper labelProjectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JedisUtils jedisUtils;

    @Autowired
    private FFmpegFrameGrabberUtils fFmpegFrameGrabberUtils;

    @Value("${value.api.query-username-byuserid}")
    private String getUserNameApiUrl;

    @Value( value = "${value.dir.rootDir}")
    private String rootDir;

    /**
     * 删除该图的标注信息
     * @param labelDataFileId
     * @param labelType 0:只删除智能标注结果 1:只删除人工标注结果 2:删除所有标注结果
     */
    @Override
    public void deleteLabelInfo(int labelDataFileId, int labelType) {
        LabelDatasetFile labelDatasetFile = this.labelDatasetFileService.getById(labelDataFileId);
        switch (labelType){
            case 0 : labelDatasetFile.setAiLabel(""); break;
            case 1 : labelDatasetFile.setLabel(""); break;
            case 2 : labelDatasetFile.setLabel(""); labelDatasetFile.setAiLabel(""); break;
            default: break;
        }
        this.labelDatasetFileService.updateById(labelDatasetFile);
    }

    /**
     * 申请标注、审核数据
     * @param labelTaskId 任务id
     * @return 申请到的影像id列表
     */
    @Override
    public List<Integer> applyForData(int labelTaskId) {
        LabelTask labelTask = this.getById(labelTaskId);
        Assert.notNull(labelTask,"标注任务不存在");
        List<Integer> imageIdList = labelDatasetFileService.applyFileByTaskRandomly(labelTask);
        if (imageIdList.size()!=0)
            updateTaskProgress(labelTask,imageIdList);

        // 更新统计信息：申领文件数
        QueryWrapper<LabelTaskStatisInfo> wrapper = new QueryWrapper();
        wrapper.eq("label_project_id", labelTask.getLabelProjectId())
                .eq("user_id", labelTask.getUserId())
                .eq("user_role", labelTask.getTaskType().getValue());
        LabelTaskStatisInfo taskStatisInfo = this.taskStatisInfoMapper.selectOne(wrapper);
        Assert.notNull(taskStatisInfo, "不存在该任务的统计信息");
        taskStatisInfo.setApplyFileCount(taskStatisInfo.getApplyFileCount()+imageIdList.size());
        this.taskStatisInfoMapper.updateById(taskStatisInfo);

        return imageIdList;
    }


    /**
     * 创建标注类型任务
     * @param labelProject 标注项目信息
     */
    @Override
    public void createLabelTasks(LabelProject labelProject) {
        List<String> checkUserIdList = Arrays.asList(labelProject.getLabelUserIds().split(","));
        List<LabelTask> labelTaskList = createProjectTasks(labelProject,checkUserIdList, LabelTaskType.LABEL);
        this.saveBatch(labelTaskList);
        List<LabelTaskStatisInfo> labelTaskStatis = this.createLabelTaskStatis(labelProject, checkUserIdList, LabelTaskType.LABEL);
        this.statisInfoService.saveBatch(labelTaskStatis);
    }

    /**
     * 创建审核任务
     * @param labelProject 标注项目信息
     */
    @Override
    public void createCheckTasks(LabelProject labelProject) {
        List<String> checkUserIdList = Arrays.asList(labelProject.getCheckUserIds().split(","));
        List<LabelTask> labelTaskList = createProjectTasks(labelProject,checkUserIdList, LabelTaskType.CHECK);
        this.saveBatch(labelTaskList);
        List<LabelTaskStatisInfo> labelTaskStatis = this.createLabelTaskStatis(labelProject, checkUserIdList, LabelTaskType.CHECK);
        this.statisInfoService.saveBatch(labelTaskStatis);
    }

    /**
     * 批量删除指定项目的任务
     * @param projectIdList 项目id列表
     */
    @Override
    public void deleteTaskByProjectId(List<Integer> projectIdList) {
        QueryWrapper<LabelTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("label_project_id",projectIdList);
        this.remove(queryWrapper);
    }

    @Override
    public LabelTaskProgress getLabelTaskProgress(int labelTaskId) {
        int finishedCount;
        long l = System.currentTimeMillis();
        QueryWrapper<LabelTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id","task_type","label_dataset_id","total_count").eq("id",labelTaskId);
        LabelTask labelTask = this.baseMapper.selectOne(queryWrapper);
        log.info("查询标注任务end，耗时：{}", (System.currentTimeMillis()-l));
        l = System.currentTimeMillis();
        if (labelTask.getTaskType() == LabelTaskType.LABEL)
            finishedCount = this.labelDatasetFileService.getLabelTaskProgress(labelTask.getLabelDatasetId(),labelTask.getUserId());
        else
            finishedCount = this.labelDatasetFileService.getCheckTaskProgress(labelTask.getLabelDatasetId(),labelTask.getUserId());
        log.info("end，耗时：{}", (System.currentTimeMillis()-l));
        log.info("\n \n \n");
        return new LabelTaskProgress(labelTask.getTotalCount(),finishedCount);
    }


    /**
     * 提交标注、审核信息
     * @param labelCommitInfo 影像标注、审核信息
     */
    @Override
    public synchronized void commitLabelInfo(LabelCommitInfo labelCommitInfo) {
        LabelStatus status = labelCommitInfo.getStatus();
        int labelFileId = labelCommitInfo.getLabelFileId();
        LabelTask labelTask = this.getById(labelCommitInfo.getLabelTaskId());
        int labelDatasetId = labelTask.getLabelDatasetId();
        LabelDatasetFile labelDatasetFile = this.labelDatasetFileService.getByDatasetAndFileId(labelDatasetId,labelFileId) ;
        Assert.notNull(labelDatasetFile,"数据集中的影像数据不存在，或者被删除");
        BeanUtils.copyProperties(labelCommitInfo, labelDatasetFile);

        //如果提交为待审核状态，但是审核用户id不为0，则说明该影像已经被审核员申领，则将状态修改为审核中，而不是待审核。
        if (labelCommitInfo.getStatus() == LabelStatus.UNCHECK
                && labelDatasetFile.getCheckUserId() !=0) {
            labelDatasetFile.setStatus(LabelStatus.CHECKING);
            // 审核员任务状态为审核中，则完成数减1
            QueryWrapper<LabelTask> wrapper = new QueryWrapper<>();
            wrapper.eq("task_type", 1)
                    .eq("label_project_id", labelTask.getLabelProjectId())
                    .eq("user_id", labelDatasetFile.getCheckUserId());
            List<LabelTask> checkTasks = this.labelTaskMapper.selectList(wrapper);
            this.updateFinshCount(checkTasks.get(0), -1);
        }
        // 获取标注任务统计信息
        LabelTaskStatisInfo checkTargetTaskInfo = new LabelTaskStatisInfo();
        QueryWrapper<LabelTaskStatisInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("label_project_id", labelTask.getLabelProjectId())
                .eq("user_id", labelTask.getUserId())
                .eq("user_role", labelTask.getTaskType().getValue());
        LabelTaskStatisInfo taskStatisInfo = this.taskStatisInfoService.getOne(wrapper);
        if (labelTask.getTaskType() == LabelTaskType.CHECK) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("label_project_id", labelTask.getLabelProjectId())
                    .eq("user_id", labelDatasetFile.getLabelUserId())
                    .eq("user_role", LabelTaskType.LABEL);
            checkTargetTaskInfo = this.taskStatisInfoService.getOne(wrapper);
        }

        LambdaUpdateChainWrapper<LabelTaskStatisInfo> updateWrapper = null;
        //提交标注任务，更新提交标注时间，更新标注提交次数
        if (labelCommitInfo.getStatus() == LabelStatus.UNCHECK){
            labelDatasetFile.setFinishLabelTime(DateUtils.nowDate());
            // 更新统计信息：提交审核次数，标注耗时
            SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss").withLocale(Locale.CHINA);
            Duration interval;
            long intervalMin;
            LocalDateTime startTime = LocalDateTime.parse(
                    dateFormat.format(this.labelDatasetFileService.getFristAssignLabelTimeByDatasetid(labelDatasetId, labelDatasetFile.getLabelUserId())), formatter);
            LocalDateTime endTime = LocalDateTime.parse(dateFormat.format(labelDatasetFile.getFinishLabelTime()), formatter);
            interval = Duration.between(startTime, endTime);
            intervalMin = interval.toMinutes();
            if (intervalMin <= 1) {
                intervalMin = 1;
            }
            // 更新统计表提交信息
            updateWrapper = new LambdaUpdateChainWrapper(this.taskStatisInfoMapper);
            updateWrapper.set(LabelTaskStatisInfo::getCommitCount, taskStatisInfo.getCommitCount()+1)
                    .set(LabelTaskStatisInfo::getTimeConsume, intervalMin)
                    .eq(LabelTaskStatisInfo::getId, taskStatisInfo.getId()).update();
            // 更新标注项目提交次数
            LambdaUpdateChainWrapper<LabelProject> pupdateWrapper = new LambdaUpdateChainWrapper(this.labelProjectMapper);
            LabelProject project = this.labelProjectService.getById(labelTask.getLabelProjectId());
            BigDecimal rate = new BigDecimal((double)(project.getRefuseCount())*100 / (project.getCommitCount()+1)).setScale(2, RoundingMode.HALF_UP);
            project.setRefuseRate(100-rate.floatValue());
            pupdateWrapper.set(LabelProject::getCommitCount, project.getCommitCount()+1)
                    .set(LabelProject::getRefuseRate, project.getRefuseRate())
                    .eq(LabelProject::getId, project.getId()).update();

            this.updateFinshCount(labelTask, 1);

            // 更新标注文件提交次数，这里可能有问题，后期完善时，在进行该地方处理 TODO 注释回来就能使用
            /*LabelDatasetFile datasetFile = this.labelDatasetFileService.getFileByDatasetAndFileId(labelDatasetId,labelCommitInfo.getLabelFileId());
            UpdateWrapper<LabelDatasetFile> update = new UpdateWrapper<>();
            update.eq("id", datasetFile.getId()).set("commit_count", datasetFile.getCommitCount()+1);
            this.labelDatasetFileService.update(update);*/
        }

        //LabelDatasetFile targetFile = this.labelDatasetFileService.getByDatasetAndFileId(labelDatasetId, labelFileId);
        if (labelCommitInfo.getStatus() == LabelStatus.FINISH || labelCommitInfo.getStatus() == LabelStatus.FEEDBACK){
            // 更新统计信息：计算审核任务耗时
            labelDatasetFile.setFinishCheckTime(DateUtils.nowDate());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss").withLocale(Locale.CHINA);
            LocalDateTime startTime = LocalDateTime.parse(
                    dateFormat.format(this.labelDatasetFileService.getFristAssignCheckTimeByDatasetid(labelDatasetId, labelDatasetFile.getCheckUserId())), formatter);
            LocalDateTime endTime = LocalDateTime.parse(dateFormat.format(labelDatasetFile.getFinishCheckTime()), formatter);
            Duration interval;
            long intervalMin;
            interval = Duration.between(startTime, endTime);
            intervalMin = interval.toMinutes();
            if (intervalMin <= 1) {
                intervalMin = 1;
            }
            updateWrapper = new LambdaUpdateChainWrapper<>(this.taskStatisInfoMapper);
            updateWrapper.set(LabelTaskStatisInfo::getTimeConsume, intervalMin)
                    .set(LabelTaskStatisInfo::getCommitCount, taskStatisInfo.getCommitCount()+1)
                    .eq(LabelTaskStatisInfo::getId, taskStatisInfo.getId()).update();
        }
        labelDatasetFile.setLabel(labelCommitInfo.getLabelInfo());
        this.labelDatasetFileService.updateById(labelDatasetFile);

        if (status == LabelStatus.FINISH){
            this.labelProjectService.updateLabelProgress(labelTask.getLabelProjectId());
            LabelProject labelProject = this.labelProjectService.getById(labelTask.getLabelProjectId());
            // 更新统计信息：审核通过的目标总数，已完成任务书更新+1，审核通过的标注个数
            JSONObject objectJson = null;
            JSONArray objectArr = null;
            int objectSize = 0;
            if (labelProject.getCategory().contains("entity-label")) {
                objectJson = JSONObject.parseObject(JSONObject.toJSONString(JSONObject.parseObject(labelCommitInfo.getLabelInfo()).get("object")));
                objectSize = objectJson.size();
            } else if (labelProject.getCategory().contains("elec-")) {
                objectArr = JSONObject.parseObject(labelCommitInfo.getLabelInfo()).getJSONObject("object").getJSONArray("entities");
                objectSize = objectArr.size();
            } else {
                objectArr = JSONObject.parseObject(labelCommitInfo.getLabelInfo()).getJSONArray("object");
                objectSize = objectArr.size();
            }
            updateWrapper = new LambdaUpdateChainWrapper<>(this.taskStatisInfoMapper);
            updateWrapper.set(LabelTaskStatisInfo::getCommitObjectCount,
                    taskStatisInfo.getCommitObjectCount()+objectSize)
                    .set(LabelTaskStatisInfo::getFinishCount, taskStatisInfo.getFinishCount()+1)
                    .eq(LabelTaskStatisInfo::getId, taskStatisInfo.getId()).update();
            if (labelTask.getTaskType() == LabelTaskType.CHECK) {
                updateWrapper = new LambdaUpdateChainWrapper<>(this.taskStatisInfoMapper);
                updateWrapper.set(LabelTaskStatisInfo::getCommitObjectCount,
                        checkTargetTaskInfo.getCommitObjectCount()+objectSize)
                        .set(LabelTaskStatisInfo::getFinishCount, checkTargetTaskInfo.getFinishCount()+1)
                        .eq(LabelTaskStatisInfo::getId, checkTargetTaskInfo.getId()).update();
            }
            // 若标注项目完成，更新完成时间
            LabelProject project = this.labelProjectService.getById(labelTask.getLabelProjectId());
            if (project.getTotalCount() == project.getFinishCount()) {
                project.setFinishTime(DateUtils.nowDate());
                long consumeMillis = project.getFinishTime().getTime() - project.getCreateTime().getTime();
                String consumeTime = cn.iecas.geoai.labelplatform.util.DateUtils.millisToTime(consumeMillis);
                project.setConsumeTime(consumeTime);
                LambdaUpdateChainWrapper<LabelProject> pupdateWrapper = new LambdaUpdateChainWrapper(this.labelProjectMapper);
                pupdateWrapper.set(LabelProject::getFinishTime, project.getFinishTime())
                        .set(LabelProject::getConsumeTime, project.getConsumeTime())
                        .eq(LabelProject::getId, project.getId()).update();
            }

            // 标注通过，审核任务完成数加1
            this.updateFinshCount(labelTask, 1);

            // ht-503-专用软件标注变化检测关联文件更新
            if (labelCommitInfo.getLabelCompareFileId() != -1) {
                this.labelDatasetFileService.updateLabelFileById(labelDatasetFile.getId(), labelCommitInfo.getLabelCompareFileId());
            }
        }

        // 更新统计信息：拒绝审核时，更新拒绝次数\更新refuse_rate
        if (status == LabelStatus.FEEDBACK) {
            updateWrapper = new LambdaUpdateChainWrapper<>(this.taskStatisInfoMapper);
            updateWrapper.set(LabelTaskStatisInfo::getRefuseCount, taskStatisInfo.getRefuseCount()+1)
            .eq(LabelTaskStatisInfo::getId, taskStatisInfo.getId()).update();
            updateWrapper = new LambdaUpdateChainWrapper<>(this.taskStatisInfoMapper);
            updateWrapper.set(LabelTaskStatisInfo::getRefuseCount, checkTargetTaskInfo.getRefuseCount()+1)
                    .eq(LabelTaskStatisInfo::getId, checkTargetTaskInfo.getId()).update();
            // 更新对应标注任务拒绝审核次数、refuse_rate
            LambdaUpdateChainWrapper<LabelProject> pupdateWrapper = new LambdaUpdateChainWrapper(this.labelProjectMapper);
            LabelProject project = this.labelProjectService.getById(labelTask.getLabelProjectId());
            BigDecimal rate = new BigDecimal((double)(project.getRefuseCount()+1)*100 / project.getCommitCount()).setScale(2, RoundingMode.HALF_UP);
            project.setRefuseRate(100-rate.floatValue() < 0 ? 0 : 100-rate.floatValue());
            pupdateWrapper.set(LabelProject::getRefuseCount, project.getRefuseCount()+1)
                    .set(LabelProject::getRefuseRate, project.getRefuseRate())
                    .eq(LabelProject::getId, project.getId()).update();

            // 标注拒绝，审核任务完成数加1，审核的目标任务完成数减1
            this.updateFinshCount(labelTask, 1);
            QueryWrapper<LabelTask> taskQueryWrapper = new QueryWrapper<>();
            taskQueryWrapper.eq("task_type", 0)
                    .eq("label_project_id", labelTask.getLabelProjectId())
                    .eq("user_id", labelDatasetFile.getLabelUserId());
            List<LabelTask> labelTasks = this.labelTaskMapper.selectList(taskQueryWrapper);
            labelTasks.stream().map(task -> {
                List<String> processingFiles = Arrays.asList(task.getProcessingList().replace(" ", "").split(","));
                return processingFiles.contains(labelFileId);
            });
            this.updateFinshCount(labelTasks.get(0), -1);
        }
    }

    /**
     * 提交标注或审核，更新对应任务完成数
     * @param labelTask
     * @param variation
     */
    private void updateFinshCount(LabelTask labelTask, int variation) {
        Lock lock = new ReentrantLock();
        try {
            lock.lock();
            if (labelTask.getFinishCount() + variation > labelTask.getTotalCount()
                    || labelTask.getFinishCount() + variation < 0)
                return;
            LambdaUpdateChainWrapper<LabelTask> updateWrapper = new LambdaUpdateChainWrapper<>(this.labelTaskMapper);
            updateWrapper.set(LabelTask::getFinishCount, labelTask.getFinishCount() + variation)
                    .eq(LabelTask::getId, labelTask.getId()).update();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取标注、审核任务关联影像信息
     * @param labelTaskFileSearchRequest 查询条件
     * @return 任务包含影像的的相关信息
     */
    @Override
    public PageResult<LabelTaskFileInfo> getTaskFileStatusInfos(LabelTaskFileSearchRequest labelTaskFileSearchRequest) throws ResourceAccessException {
        int labelTaskId = labelTaskFileSearchRequest.getTaskId();
        LabelTask labelTask = this.labelTaskMapper.selectById(labelTaskId);
        Assert.notNull(labelTask,"标注任务不存在");

        //查询标注任务所包含的所有数据集文件id
        LabelTaskType labelTaskType = labelTask.getTaskType();
        labelTaskFileSearchRequest.setLabelTaskType(labelTaskType);
        labelTaskFileSearchRequest.setUserId(labelTask.getUserId());
        labelTaskFileSearchRequest.setLabelDatasetId(labelTask.getLabelDatasetId());
        LabelProject labelProject = this.labelProjectService.getById(labelTask.getLabelProjectId());
        labelTaskFileSearchRequest.setCooperate(labelProject.isCooperate());
        List<LabelDatasetFile> labelDatasetFileList = this.labelDatasetFileService.searchLabelTaskFile(labelTaskFileSearchRequest);
        List<Integer> fileIdList = labelDatasetFileList.stream().map(LabelDatasetFile::getFileId).collect(Collectors.toList());
//
//        //设置标注任务包含的数据的查询信息
//        LabelDatasetFileRequest labelDatasetFileRequest = new LabelDatasetFileRequest();
//        labelDatasetFileRequest.setFileIdList(fileIdList);
//        labelDatasetFileRequest.setDatasetId(labelTask.getLabelDatasetId());
//        labelDatasetFileRequest.setDatasetType(labelProject.getDatasetType());
//        labelDatasetFileRequest.setStatus(labelTaskFileSearchRequest.getStatus());
//        labelDatasetFileRequest.setPageNo(labelTaskFileSearchRequest.getPageNo());
//        labelDatasetFileRequest.setPageSize(labelTaskFileSearchRequest.getPageSize());
//        labelDatasetFileRequest.setSearchParam(labelTaskFileSearchRequest.getSearchParam());
//        if(labelProject.getDatasetType() == DatasetType.TEXT)
//            labelDatasetFileRequest.setContent(true);


        //设置标注任务包含的数据的查询信息
        FileSearchParam fileSearchParam = new FileSearchParam();
        fileSearchParam.setFileIdList(fileIdList);
        fileSearchParam.setFileType(labelProject.getDatasetType());
        fileSearchParam.setPageNo(labelTaskFileSearchRequest.getPageNo());
        fileSearchParam.setPageSize(labelTaskFileSearchRequest.getPageSize());
        fileSearchParam.setSearchParam(labelTaskFileSearchRequest.getSearchParam());
        if(labelProject.getDatasetType() == DatasetType.TEXT) {
            fileSearchParam.setContent(true);
            fileSearchParam.setFromDataset(true);
        }

        //根据条件查找标注任务包含文件的信息
        List<LabelTaskFileInfo> labelTaskFileInfoList = new ArrayList<>();
        //分页查询满足查询条件的文件信息
        PageResult<JSONObject> filePageInfo = this.fileService.getFileInfoByPage(fileSearchParam);
        if (filePageInfo==null)
            return new PageResult<>(1,0,new ArrayList<>());
        List<Integer> labelDatasetFileIdList = filePageInfo.getResult().stream().map(jsonObject -> jsonObject.getInteger("id")).collect(Collectors.toList());
        fileIdList.retainAll(labelDatasetFileIdList);
        labelDatasetFileList = labelDatasetFileList.stream().filter(labelDatasetFile -> fileIdList.contains(labelDatasetFile.getFileId())).collect(Collectors.toList());
        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            LabelTaskFileInfo labelTaskFileInfo = new LabelTaskFileInfo();
            int fileId = labelDatasetFile.getFileId();
            JSONObject data = filePageInfo.getResult().stream().filter(jsonObject -> jsonObject.getInteger("id")==fileId).findFirst().get();
            BeanUtils.copyProperties(labelDatasetFile,labelTaskFileInfo);

            labelTaskFileInfo.setData(data);
            labelTaskFileInfo.setKeywords(labelProject.getKeywords());
            labelTaskFileInfo.setScreenshot(labelDatasetFile.getScreenshot());
            labelTaskFileInfoList.add(labelTaskFileInfo);
        }
//        for (JSONObject fileInfo : fileInfoList.getResult()) {
//            fileInfo.
//        }
//        labelDatasetFileIdList = fileInfoList.getResult().stream().map(jsonObject -> jsonObject.getInteger("id")).collect(Collectors.toList());
//        List<Integer> finalLabelDatasetFileIdList = labelDatasetFileIdList;
//        labelDatasetFileList = labelDatasetFileList.stream()
//                .filter(labelDatasetFile -> finalLabelDatasetFileIdList.contains(labelDatasetFile.getFileId()))
//                .collect(Collectors.toList());
//        PageResult<LabelDatasetOrProjectFileInfo> pageResult = this.labelDatasetFileService.getFileInfoFromDataset(labelDatasetFileRequest);
//        List<LabelDatasetOrProjectFileInfo> labelDatasetOrProjectFileInfoList = pageResult.getResult();
//        for (LabelDatasetOrProjectFileInfo labelDatasetOrProjectFileInfo : labelDatasetOrProjectFileInfoList) {
//            LabelTaskFileInfo labelTaskFileInfo = new LabelTaskFileInfo();
//            int labelDatasetFileId = labelDatasetOrProjectFileInfo.getId();
//            LabelDatasetFile labelDatasetFile = labelDatasetFileList.stream()
//                    .filter(labelDatasetFileInner -> labelDatasetFileInner.getId() == labelDatasetFileId).findFirst().get();
//
//            BeanUtils.copyProperties(labelDatasetFile,labelTaskFileInfo);
//            labelTaskFileInfo.setKeywords(labelProject.getKeywords());
//            labelTaskFileInfo.setData(labelDatasetOrProjectFileInfo.getData());
//            labelTaskFileInfoList.add(labelTaskFileInfo);
//        }

        return new PageResult<>(filePageInfo.getPageNo(), filePageInfo.getTotalCount(), labelTaskFileInfoList);

    }

//    /**
//     * 获取标注、审核任务关联影像信息
//     * @param labelTaskFileSearchRequest 查询条件
//     * @return 任务包含影像的的相关信息
//     */
//    @Override
//    public PageResult<LabelDatasetFileInfo> getTaskFileStatusInfos(LabelTaskFileSearchRequest labelTaskFileSearchRequest) throws ResourceAccessException {
//        List<LabelTaskImageStatusInfo> labelTaskImageStatusInfos = new ArrayList<>();
//        int labelTaskId = labelTaskFileSearchRequest.getTaskId();
//        Page<LabelDatasetFileInfo> page = new Page<>(labelTaskFileSearchRequest.getPageNo(), labelTaskFileSearchRequest.getPageSize());
//        LabelTask labelTask = this.labelTaskMapper.selectById(labelTaskId);
//        Assert.notNull(labelTask,"标注任务不存在");
//        LabelTaskType labelTaskType = labelTask.getTaskType();
//        labelTaskFileSearchRequest.setLabelTaskType(labelTaskType);
//        labelTaskFileSearchRequest.setUserId(labelTask.getUserId());
//        labelTaskFileSearchRequest.setLabelDatasetId(labelTask.getLabelDatasetId());
//
//        if(labelTaskFileSearchRequest.getSearchParam()!=null){
//            String[] params = labelTaskFileSearchRequest.getSearchParam().split(" ");
//            if (params.length>0)
//                labelTaskFileSearchRequest.setSearchParamList(Arrays.asList(params));
//        }
//
//        IPage<LabelDatasetFileInfo> imageIPage = this.labelDatasetImageInfoMapper.listTaskImageInfos(page, labelTaskFileSearchRequest);
//        List<LabelDatasetFileInfo> labelDatasetFileInfoList = imageIPage.getRecords();
//        labelDatasetFileInfoList.forEach(labelDatasetImageInfo -> labelDatasetImageInfo.setKeywords(labelTask.getKeywords()));
//        return new PageResult<>(imageIPage.getCurrent(), imageIPage.getTotal(), labelDatasetFileInfoList);
//
//    }

    /**
     * 分页查询某一标注、审核任务
     * @param labelTaskSearchRequest  标注任务查询条件、
     * @return 标注任务列表
     */
    @Override
    public PageResult<LabelTaskInfo> getLabelTasks(LabelTaskSearchRequest labelTaskSearchRequest) {
        Page<LabelTaskInfo> labelTaskPage = new Page<>(labelTaskSearchRequest.getPageNo(),labelTaskSearchRequest.getPageSize());
        IPage<LabelTaskInfo> labelTaskInfoIPage = this.labelTaskMapper.listLabelTaskInfos(labelTaskPage,labelTaskSearchRequest);
        List<LabelTaskInfo> labelTaskInfoList = labelTaskInfoIPage.getRecords();

        for (LabelTaskInfo labelTaskInfo : labelTaskInfoList) {
            int labelTaskId = labelTaskInfo.getId();
            // LabelTaskProgress labelTaskProgress = this.getLabelTaskProgress(labelTaskId);
            // labelTaskInfo.setFinishCount(labelTaskProgress.getFinishCount());
            LabelTask task = this.labelTaskMapper.selectById(labelTaskId);
            labelTaskInfo.setFinishCount(task.getFinishCount());
        }
        return new PageResult<>(labelTaskInfoIPage.getCurrent(), labelTaskPage.getTotal(), labelTaskInfoList);
    }

    /**
     * 创建标注任务
     * @param labelProject 标注项目信息
     * @param userIdList 标注员、审核员id 列表
     * @param taskType 任务类型：标注任务、审核任务
     * @return 标注任务信息
     */
    private List<LabelTask> createProjectTasks(LabelProject labelProject, List<String> userIdList, LabelTaskType taskType){
        List<LabelTask> labelTaskList = new ArrayList<>();

        for (String userId : userIdList) {
            int defaultApplyCount = taskType == LabelTaskType.LABEL ? labelProject.getDefaultLabelCount() : labelProject.getDefaultCheckCount();
            LabelTask labelTask = LabelTask.builder()
                    .totalCount(0).finishCount(0)
                    .labelProjectId(labelProject.getId()).publisherId(labelProject.getUserId())
                    .taskType(taskType).userId(Integer.parseInt(userId)).keywords(labelProject.getKeywords())
                    .labelDatasetId(labelProject.getDatasetId()).defaultApplyCount(defaultApplyCount).build();
            if (labelProject.isCooperate() && taskType == LabelTaskType.LABEL && labelProject.getImageIds() != null && labelProject.getImageIds().size() != 0) {
                labelTask.setProcessingList(labelProject.getImageIds().toString().replace("[","").replace("]",""));
                labelTask.setTotalCount(labelProject.getImageIds().size());
            }
            labelTaskList.add(labelTask);
        }
        return labelTaskList;
    }

    /**
     * 创建标注任务统计信息
     * @param labelProject
     * @param userIdList
     * @return
     */
    @Override
    public List<LabelTaskStatisInfo> createLabelTaskStatis(LabelProject labelProject, List<String> userIdList, LabelTaskType taskType) {
        ArrayList<LabelTaskStatisInfo> taskStatisList = new ArrayList<>();
        String token = request.getHeader("token");
        String userName = "";
        int userRole;
        for (String userId : userIdList) {
            userRole = taskType == LabelTaskType.LABEL ? 0 : 1;
            List<String> list = new ArrayList<String>(){{add(userId);}};
            JSONObject userNameByUserId = getUserNameByUserId(token, list);
            List<Map<String, Object>> data = (List<Map<String, Object>>) userNameByUserId.get("data");
            LabelTaskStatisInfo taskStatisInfo = LabelTaskStatisInfo.builder().labelProjectId(labelProject.getId()).userId(Integer.parseInt(userId))
                    .userRole(userRole).userName(String.valueOf(data.get(0).get("name"))).build();
            if (labelProject.isCooperate() && taskType == LabelTaskType.LABEL)
                taskStatisInfo.setApplyFileCount(taskStatisInfo.getApplyFileCount()+labelProject.getImageIds().size());
            taskStatisList.add(taskStatisInfo);
        }
        return taskStatisList;
    }

    /**
     * 根据用户ID获取用户名
     * @param userIdList
     * @return
     */
    private JSONObject getUserNameByUserId(String token, List<String> userIdList) {
        JSONObject result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", token);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(this.getUserNameApiUrl)
                .queryParam("user_ids", userIdList);
        result = restTemplate.exchange(urlBuilder.build().encode().toUri(), HttpMethod.GET, httpEntity, JSONObject.class).getBody();
        return result;
    }

    /**
     * 更新任务进度
     * @param labelTask 标注任务信息
     * @param imageIdList 影像id列表
     */
    private void updateTaskProgress(LabelTask labelTask, List<Integer> imageIdList ){
        int totalCount = labelTask.getTotalCount() + imageIdList.size();
        labelTask.setTotalCount(totalCount);
        labelTask.setProcessingList(imageIdList.toString());
        LambdaUpdateChainWrapper<LabelTask> labelTaskLambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(this.labelTaskMapper);
        labelTaskLambdaUpdateChainWrapper.eq(LabelTask::getId,labelTask.getId())
                .set(LabelTask::getTotalCount,totalCount)
                .set(LabelTask::getProcessingList,imageIdList.toString())
                .update();
    }

    /**
     * 更新项目和任务的进度
     * @param labelTask 标注任务信息
     */
    private synchronized void updateProgress(LabelTask labelTask, LabelStatus status){
        if (status == LabelStatus.CHECKING || status == LabelStatus.FINISH){
            labelTask.setFinishCount(labelTask.getFinishCount()+1);
            this.updateById(labelTask);
        }
        if (status == LabelStatus.FINISH){
            this.labelProjectService.updateLabelProgress(labelTask.getLabelProjectId());
        }

        if (status == LabelStatus.FEEDBACK){
            QueryWrapper<LabelTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("label_project_id",labelTask.getLabelProjectId())
                    .eq("task_type",LabelTaskType.LABEL);
            LabelTask associationLabelTask = this.getOne(queryWrapper);
            associationLabelTask.setFinishCount(associationLabelTask.getFinishCount()-1);
            this.updateById(associationLabelTask);
        }
    }

//    /**
//     * 导入xml文件
//     */
//
//    @Override
//    public String importLabelFile(String imagePath , LabelPointType labelPointType , MultipartFile file) throws Exception {
//        XMLSerializer xmlSerializer = new XMLSerializer();
//
//        if (file.getOriginalFilename().endsWith("xml")){
//            JSONObject jsonLabel = (JSONObject) xmlSerializer.readFromStream(file.getInputStream());
//            return processXml(imagePath,labelPointType,jsonLabel);
//        }
//        else {
//            String result = new String(file.getBytes());
//            return processVif(imagePath , labelPointType , result.toString());
//        }
//    }

//    /**
//     * 导入xml文件
//     */
//
//    @Override
//    public String importLabelFile(String imagePath , LabelPointType labelPointType , MultipartFile file) throws Exception {
//        if (file.isEmpty()){
//            log.error("上传文件为空");
//            return null;
//        }
//
//        imagePath = FileUtils.getStringPath(this.rootDir,imagePath);
//        LabelObject labelObject = null;
//        if (file.getOriginalFilename().endsWith("xml"))
//            labelObject = XMLUtils.parseXMLFromStream(file.getInputStream(), XMLLabelObjectInfo.class);
//        else
//            labelObject = XMLUtils.parseXMLFromStream(file.getInputStream(), VifLabelOjectInfo.class);
//
//        String originalCoordinateType = labelObject.getCoordinate();
//        CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;
//
//        if(labelPointType.name().equalsIgnoreCase(originalCoordinateType)){
//            if (LabelPointType.GEODEGREE ==labelPointType && GeoUtils.isProjection(imagePath))
//                coordinateConvertType = CoordinateConvertType.LONLAT_TO_PROJECTION;
//            if (LabelPointType.PIXEL == labelPointType)
//                coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;
//        }else {
//            if (LabelPointType.GEODEGREE == labelPointType){
//                if (GeoUtils.isProjection(imagePath))
//                    coordinateConvertType = CoordinateConvertType.PIXEL_TO_PROJECTION;
//                else
//                    coordinateConvertType = CoordinateConvertType.PIXEL_TO_LONLAT;
//            }else
//                coordinateConvertType = CoordinateConvertType.LONLAT_TO_PIXEL;
//        }
//
//
//        LabelPointTypeConvertor.convertLabelPointType(imagePath,labelObject,coordinateConvertType);
//        return labelObject.toJSONObject().toString();
//    }

    /**
     * 导入xml文件
     */

    @Override
    public String importLabelFile(String filePath , LabelPointType labelPointType , MultipartFile file, DatasetType datasetType) throws Exception {
        if (file.isEmpty()){
            log.error("上传文件为空");
            return null;
        }
        LabelFileService labelFileService = datasetType.getLabelFileService();
        String labelObject = null;

        if (file.getOriginalFilename().endsWith("xz")) {
            ImageInfo imageInfo = ParseImageInfo.parseInfo(FileUtils.getStringPath(this.rootDir, filePath));
            if (imageInfo.getCoordinateSystemType() == CoordinateSystemType.PIXELCS) {
                log.error("不支持对pix坐标影像进行导入 {}", filePath);
                return null;
            }

            String tempFilePath = FileUtils.getStringPath(this.rootDir, filePath) + System.currentTimeMillis() + ".xz";
            String destPath = FileUtils.getStringPath(this.rootDir, filePath) + ".geojson";
            File tempFile = new File(tempFilePath);
            OutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.write(file.getBytes());
            if (outputStream != null) {
                outputStream.close();
            }
            JSONObject labelInfo = this.parseLabelFromXzFile(destPath, tempFile, imageInfo);
            String tempXmlPath = FileUtils.getStringPath(this.rootDir, filePath) + ".xml";
            XMLUtils.toXMLFile(tempXmlPath, JSONObject.toJavaObject(labelInfo, XMLLabelObjectInfo.class));
            labelObject = labelFileService.importLabelFromXzFile(filePath, labelPointType, new File(tempXmlPath));
        } else {
            labelObject = labelFileService.importLabelXML(filePath,labelPointType,file);
        }

        return labelObject;
    }

    /**
     * 解析*.XZ文件为样本库可以识别的JSON格式
     * @param destPath 解析.xz文件生成的.geojson文件路径
     * @param file .xz源文件
     * @param imageInfo 标注的影像信息
     * @return
     * @throws IOException
     */
    private JSONObject parseLabelFromXzFile(String destPath, File file, ImageInfo imageInfo) throws IOException {
        JSONObject labelInfo = new JSONObject();
        JSONArray labels = new JSONArray();
        File destFile = new File(destPath);
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        CompressUtil.deCompressXZFile(file,destFile, false);
        StringBuilder stringBuilder = new StringBuilder();
        FileReader fileReader = new FileReader(destFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        JSONObject fileContent = JSONObject.parseObject(stringBuilder.toString());
        JSONArray features = fileContent.getJSONArray("features");
        for (Object feature : features) {
            JSONObject label = new JSONObject();
            JSONObject featureJson = JSONObject.parseObject(feature.toString());
            JSONObject geometry = featureJson.getJSONObject("geometry");

            if (!geometry.getString("type").equalsIgnoreCase("MultiPolygon"))
                continue;

            List<List> coordinates = geometry.getJSONArray("coordinates").toJavaList(List.class);
            JSONObject point = new JSONObject();
            JSONArray pt = new JSONArray();
            for (List coordinate : coordinates) {
                for (Object o : coordinate) {
                    for (List p : (List<List>) o) {
                        if (Double.parseDouble(String.valueOf(p.get(0))) > imageInfo.getMaxLon()
                                || Double.parseDouble(String.valueOf(p.get(1))) > imageInfo.getMaxLat()
                                || Double.parseDouble(String.valueOf(p.get(0))) < imageInfo.getMinLon()
                                || Double.parseDouble(String.valueOf(p.get(1))) < imageInfo.getMinLat())
                            continue;
                        pt.add(p.toString().replace("[","").replace("]","").replace(" ", ""));
                    }
                    point.put("point", pt);
                    label.put("points", point);
                    label.put("id", null);
                    label.put("type", "Polygon");
                    label.put("checkStatus", null);
                    label.put("note", null);
                    label.put("coordinate", "geodegree");
                    label.put("description", "经纬度坐标系");
                    JSONArray possibleresult = new JSONArray();
                    JSONObject psiblt = new JSONObject();
                    psiblt.put("probability", 1);
                    psiblt.put("name", "未知");
                    Map<String, Object> properties = featureJson.getJSONObject("properties").getInnerMap();
                    List<String> names = Arrays.stream(new String[]{"landuse","building","highway","amenity","barrier"}).collect(Collectors.toList());
                    List<String> keyList = properties.keySet().stream().filter(k -> names.contains(k)).collect(Collectors.toList());
                    if (keyList.size() == 1)
                        psiblt.put("name", keyList.get(0));
                    possibleresult.add(psiblt);
                    label.put("possibleresult", possibleresult);
                    labels.add(label);
                }
            }
        }
        file.delete();
        labelInfo.put("object", labels);
        return labelInfo;
    }
    public static void main(String[] args) throws Exception {
        /*String srcDir = "C:\\Users\\dell\\Desktop\\getao\\work_file\\cjl\\20230427\\planet_112.578_37.677_1cfc348b.osm.geojson.xz";
        String destDir = "C:\\Users\\dell\\Desktop\\getao\\work_file\\cjl\\20230427\\1.geojson";
        CompressUtil.deCompressXZFile(new File(srcDir), new File(destDir), false);*/

        //new LabelTaskServiceImpl().importLabelFile(null,null,null,null);

        List<List> list = new ArrayList<>();
        List<List> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        list2.add("aaa");
        list2.add("bbb");
        list2.add("ccc");
        list2.add("ddd");
        list1.add(list2);
        list3.addAll(list2);
        list1.add(list3);
        list.add(list1);
        System.out.println(list);
        System.out.println(list.toString());
        System.out.println(list.toString().replace("[[", "--"));
        System.out.println(Arrays.asList(list.toString().replace("[[", "").replace("]]", "")).get(0));
        System.out.println(Arrays.asList(list.toString().replace("[[", "").replace("]]", "")).get(1));
    }


    /**
     * 导出xml文件
     */
    @Override
    public void exportLabelFile(LabelExportParam labelExportParam) throws UnsupportedEncodingException, DocumentException {
        labelExportParam.setFileInfo(null);

        JSONObject fileInfo = this.fileService.getFileInfoById(labelExportParam.getFileId(),null);
        labelExportParam.setFileInfo(fileInfo);

        DatasetType fileType = labelExportParam.getFileType();
        String xml = fileType.getLabelFileService().exportXML(labelExportParam);

        OutputStream os = null;
        try {

            response.setCharacterEncoding("UTF-8");
            //response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("content-type","application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((labelExportParam.getFileName() + "." + labelExportParam.getLabelFileType().toString().toLowerCase()).getBytes(),"UTF-8"));
            byte[] bytes = xml.getBytes("UTF-8");
            os = response.getOutputStream();

            os.write(bytes);
            os.close();
        } catch (Exception ex) {
            log.error("导出失败:", ex);
            throw new RuntimeException("导出失败");
        }finally {
            try {
                if (null != os) {
                    os.close();
                }
            } catch (IOException ioEx) {
                log.error("导出失败:", ioEx);
            }
        }

    }

    /**
     * 根据项目ID获取对应的标注任务
     * @param projectIdList 标注项目id集合
     * @return
     */
    @Override
    public List<LabelTask> getLabelTaskByProjectId(List<Integer> projectIdList) {
        List<LabelTask> labelTaskList = this.labelTaskMapper.selectList(
                new QueryWrapper<LabelTask>().in("label_project_id", projectIdList));
        return labelTaskList;
    }

    /**
     * 合并标注项目：保留原始任务数据 TODO 待完善
     * @param labelProject 已合并合并的标注项目
     * @param userInfo 操作用户信息
     * @return
     */
    @Override
    public List<LabelTask> mergeTaskByRepeatFileToNotNull(List<LabelTask> labelTaskList, LabelProject labelProject, int srcProjectId, List<Integer> projectIdList,  boolean isMerge, List<Integer> fileIdList, UserInfo userInfo) {
        Map<String, LabelTask> taskMap = new HashMap<>();
        labelTaskList.forEach(labelTask -> {
            LabelTask task = null;
            if (labelTask.getTaskType().getValue() == 0) {
                task = taskMap.get(labelTask.getUserId()+"L");
            } else {
                task = taskMap.get(labelTask.getUserId()+"C");
            }
            if (task == null) {
                labelTask.setLabelDatasetId(labelProject.getDatasetId());
                labelTask.setLabelProjectId(labelProject.getId());
                this.putTaskToMap(taskMap, labelTask);
            } else {
                if (labelTask.getTaskType().equals(task.getTaskType())) {
                    labelTask.setPublisherId(userInfo.getId());
                    labelTask.setKeywords(getKeywordFromJson(task.getKeywords(), labelTask.getKeywords()));
                    // 根据合并或者覆盖，处理processingList
                    String taskProcessingList = task.getProcessingList();
                    String labelTaskProcessingList = labelTask.getProcessingList();
                    if (isMerge) {
                        if (fileIdList != null && fileIdList.size() != 0) { // 这里是重复文件情况，如果有，都设为空，否则合并任务信息
                            labelTask.setTotalCount(0);
                            labelTask.setFinishCount(0);
                            labelTask.setProcessingList(null);
                        } else {
                            Set<Object> processingSet = processingSet = new HashSet<>();
                            if (taskProcessingList != null) {
                                String[] processingArr = taskProcessingList.replace("[", "").replace("]", "").replace(" ", "")
                                        .split(",");
                                Collections.addAll(processingSet, processingArr);
                            }
                            if (labelTaskProcessingList != null) {
                                String[] processingArr = processingArr = labelTaskProcessingList.replace("[", "").replace("]", "").replace(" ", "")
                                        .split(",");
                                Collections.addAll(processingSet, processingArr);
                            }
                            if (processingSet.size() != 0) {
                                labelTask.setProcessingList(processingSet.toString().replace(" ", ""));
                                labelTask.setTotalCount(processingSet.size());
                            } else {
                                labelTask.setTotalCount(0);
                            }
                            LabelTask task1 = LabelTask.builder().labelDatasetId(labelProject.getDatasetId()).userId(task.getUserId()).build();
                            labelTask.setFinishCount(this.labelDatasetFileService.getTaskTotalCount(task1));
                        }
                        labelTask.setLabelDatasetId(labelProject.getDatasetId());
                        labelTask.setLabelProjectId(labelProject.getId());
                        this.putTaskToMap(taskMap, labelTask);
                    } else {
                        if (labelTask.getLabelProjectId() != srcProjectId) {
                            labelTask.setLabelDatasetId(labelProject.getDatasetId());
                            labelTask.setLabelProjectId(labelProject.getId());
                            this.putTaskToMap(taskMap, labelTask);
                        }
                    }
                } else {
                    labelTask.setLabelDatasetId(labelProject.getDatasetId());
                    labelTask.setLabelProjectId(labelProject.getId());
                    this.putTaskToMap(taskMap, labelTask);
                }
            }
        });
        return new ArrayList<>(taskMap.values());
    }


    /**
     * 合并标注项目：不保留原始数据
     * @param labelProject 已合并合并的标注项目
     * @param userInfo 操作用户信息
     * @return
     */
    @Override
    public List<LabelTask> mergeTaskToNull(List<LabelTask> labelTaskList, LabelProject labelProject, int srcProjectId, List<Integer> projectIdList, boolean isMerge, List<Integer> fileIdList, UserInfo userInfo) {
        Map<String, LabelTask> taskMap = new HashMap<>();
        labelTaskList.forEach(labelTask -> {
            // 初始化任务
            labelTask.setTotalCount(0);
            labelTask.setFinishCount(0);
            labelTask.setProcessingList(null);
            labelTask.setLabelDatasetId(labelProject.getDatasetId());
            labelTask.setLabelProjectId(labelProject.getId());
            labelTask.setPublisherId(userInfo.getId());
            LabelTask task = null;
            if (labelTask.getTaskType().getValue() == 0) {
                task = taskMap.get(labelTask.getUserId()+"L");
            } else {
                task = taskMap.get(labelTask.getUserId()+"C");
            }
            if (task != null && labelTask.getTaskType().equals(task.getTaskType())) {
                labelTask.setKeywords(getKeywordFromJson(task.getKeywords(), labelTask.getKeywords()));
            }
            this.putTaskToMap(taskMap, labelTask);
        });
        return new ArrayList<>(taskMap.values());
    }

    /**
     * 创建合并任务
     * @return
     */
    private void putTaskToMap(Map<String, LabelTask> taskMap, LabelTask labelTask) {
        if (labelTask.getTaskType().getValue() == 0) {
            taskMap.put(labelTask.getUserId()+"L", labelTask);
        } else {
            taskMap.put(labelTask.getUserId()+"C", labelTask);
        }
    }
    /**
     * 合并标注关键字
     * @param keywords
     * @param keywords1
     * @return
     */
    private String getKeywordFromJson(String keywords, String keywords1) {
        HashSet<JSONObject> set = new HashSet<>();
        JSONArray array = JSONObject.parseArray(keywords);
        JSONArray array1 = JSONObject.parseArray(keywords1);
        set.addAll(array.toJavaList(JSONObject.class));
        set.addAll(array1.toJavaList(JSONObject.class));
        return set.toString();
    }

    /**
     * 获取标注任务人员统计信息
     * @param userIdList
     * @return
     */
    @Override
    public PageResult<LabelTaskStatisInfo> getLabelTaskStatisInfoByUserIds(
            List<Integer> userIdList, int userRole, int projectId, LabelTaskSearchRequest labelTaskSearchRequest) {
        Page<LabelTaskStatisInfo> page = new Page<>(labelTaskSearchRequest.getPageNo(),labelTaskSearchRequest.getPageSize());
        IPage<LabelTaskStatisInfo> labelTaskStatisInfoByTaskTYpe = this.labelTaskMapper.getLabelTaskStatisInfoByUserIds(page, userIdList, userRole, projectId);

        List<LabelTaskStatisInfo> labelTaskStatisInfoList = labelTaskStatisInfoByTaskTYpe.getRecords();
        DecimalFormat format = new DecimalFormat("#.##");
        for (LabelTaskStatisInfo labelTaskStatisInfo : labelTaskStatisInfoList) {
            if (labelTaskStatisInfo.getUserRole() == 1) {
                if (labelTaskStatisInfo.getFinishCount() == 0) {
                    labelTaskStatisInfo.setAveLabelTime("0");
                } else {
                    labelTaskStatisInfo.setAveLabelTime(format.format(
                            (double) labelTaskStatisInfo.getTimeConsume()/labelTaskStatisInfo.getFinishCount()));
                }
            }
            if (labelTaskStatisInfo.getUserRole() == 0) {
                if (labelTaskStatisInfo.getCommitCount() == 0 || labelTaskStatisInfo.getFinishCount() == 0) {
                    labelTaskStatisInfo.setAveLabelTime("0");
                    labelTaskStatisInfo.setCheckPassRate("0");
                } else {
                    labelTaskStatisInfo.setAveLabelTime(format.format(
                            (double) labelTaskStatisInfo.getTimeConsume()/labelTaskStatisInfo.getCommitObjectCount()));
                    labelTaskStatisInfo.setCheckPassRate(
                            format.format((double)(labelTaskStatisInfo.getFinishCount())
                                    *100/labelTaskStatisInfo.getCommitCount()));
                }
            }
        }

        return new PageResult<>(page.getCurrent(), labelTaskStatisInfoByTaskTYpe.getTotal(), labelTaskStatisInfoList);
    }

    /**
     * 获取标注任务各类型所占比率
     * @return
     */
    @Override
    public List<HashMap<String, String>> getLabelTaskStatusRate(int projectId) {
        List<HashMap<String, String>> resultList = new ArrayList<>();
        List<Map<String, Long>> labelTaskStatusRate = this.labelDatasetFileService.getLabelTaskStatusRate(projectId);
        Integer taskCount = this.labelDatasetFileService.getLabelTaskCount(projectId);
        DecimalFormat format = new DecimalFormat("#.##");
        labelTaskStatusRate.forEach(rate->{
            BigDecimal count = new BigDecimal((double) (rate.get("count") * 100) / taskCount).setScale(2, RoundingMode.HALF_UP);
            HashMap<String, String> map = new HashMap<>();
            String status = String.valueOf(rate.get("status"));
            switch (status) {
                case "0":
                    map.put("labelStatus", "智能标注中");
                    map.put("rate", format.format(count));
                    break;
                case "1":
                    map.put("labelStatus", "待标注");
                    map.put("rate", format.format(count));
                    break;
                case "2":
                    map.put("labelStatus", "标注中");
                    map.put("rate", format.format(count));
                    break;
                case "3":
                    map.put("labelStatus", "待审核");
                    map.put("rate", format.format(count));
                    break;
                case "4":
                    map.put("labelStatus", "审核中");
                    map.put("rate", format.format(count));
                    break;
                case "5":
                    map.put("labelStatus", "审核通过");
                    map.put("rate", format.format(count));
                    break;
                case "6":
                    map.put("labelStatus", "审核被拒");
                    map.put("rate", format.format(count));
                    break;
                default:
                    map.put("labelStatus", "其他");
                    map.put("rate", format.format(count));
            }
            resultList.add(map);
        });
        return resultList;
    }

    /**
     * 获取任意位置后的固定帧视频影像数据
     * @param videoPath
     * @param frameNumber
     * @return
     */
    @Override
    public List<VideoFrame> getFrameImgByFrameNbr(String videoPath, int frameNumber, int returnNumber) {
        List<VideoFrame> result = new ArrayList<>();
        videoPath = FileUtils.getStringPath(rootDir, videoPath);
        try {
            FFmpegFrameGrabber fFmpegFrameGrabber = fFmpegFrameGrabberUtils.getFfmpegFrameGrabberByPath(videoPath);
            long begin = System.currentTimeMillis();
            try {
                fFmpegFrameGrabber.start();
            } catch (Exception e) {
                log.info("Start() has already been called: fFmpegFrameGrabber 实例已经存在，无需再次启动");
            }
            int lengthInVideoFrames = fFmpegFrameGrabber.getLengthInVideoFrames();
            double fps = Double.parseDouble(String.format("%.2f", fFmpegFrameGrabber.getVideoFrameRate()));
            if (lengthInVideoFrames <= 10 || frameNumber < 1)
                frameNumber = 1;
            else if (frameNumber == lengthInVideoFrames)
                frameNumber = frameNumber - 1;
            else if (frameNumber > lengthInVideoFrames)
                frameNumber = frameNumber - 10;
            fFmpegFrameGrabber.setVideoFrameNumber(frameNumber);
            Frame frame = null;
            Java2DFrameConverter converter = new Java2DFrameConverter();
            log.info("预处理耗时：{}", System.currentTimeMillis()-begin);

            begin = System.currentTimeMillis();
            Jedis jedis = jedisUtils.getInstance();
            for (int i = frameNumber; i <= lengthInVideoFrames; i++) {
                if (result.size() >= returnNumber)
                    break;
                if (jedisUtils.getJavaObject(videoPath+i, VideoFrame.class) != null) {
                    log.info("命中缓存");
                    result.add((VideoFrame) jedisUtils.getJavaObject(videoPath+i, VideoFrame.class));
                    continue;
                }
                try {
                    frame = fFmpegFrameGrabber.grabImage();
                    BufferedImage bufferedImage = converter.getBufferedImage(frame);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    Base64Encoder encoder = new Base64Encoder();
                    VideoFrame videoFrame = VideoFrame.builder().videoPath(videoPath).lengthInFrames(lengthInVideoFrames)
                            .frameNumber(i).frameBase64Str("data:image/jpeg;base64,"+encoder.encode(bytes)).fps(fps).build();
                    result.add(videoFrame);
                    jedisUtils.setJavaObject(videoPath+i, videoFrame);
                } catch (IllegalArgumentException e) {
                    result.add(VideoFrame.builder().videoPath(videoPath).frameNumber(i).fps(fps).status(1).build());
                }
            }
            jedisUtils.takebackJedis(jedis);
            fFmpegFrameGrabber.close();
            log.info("获取帧数据耗时：{}", System.currentTimeMillis()-begin);
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException("FFmpegFrameGrabber处理异常");
        } catch (IOException e) {
            throw new RuntimeException("IO异常");
        }
        return result;
    }

    /**
     * 判断某标注任务是否处于预处理过程中
     * @param taskId
     * @return
     */
    @Override
    public Boolean isPreprocess(int taskId) {
        LabelTask task = this.labelTaskMapper.selectById(taskId);
        QueryWrapper<LabelDatasetFile> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_id", task.getLabelDatasetId()).isNull("preprocess_path");
        return labelDatasetFileService.list(wrapper).size() != 0;
    }
}
