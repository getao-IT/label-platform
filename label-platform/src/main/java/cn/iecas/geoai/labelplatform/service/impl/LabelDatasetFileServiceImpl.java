package cn.iecas.geoai.labelplatform.service.impl;

import cn.aircas.utils.date.DateUtils;
import cn.iecas.geoai.labelplatform.dao.LabelDatasetFileMapper;
import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.domain.LabelProject;
import cn.iecas.geoai.labelplatform.entity.dto.FileSearchParam;
import cn.iecas.geoai.labelplatform.entity.dto.LabelDatasetOrProjectFileInfo;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTask;
import cn.iecas.geoai.labelplatform.entity.dto.LabelDatasetFileRequest;
import cn.iecas.geoai.labelplatform.entity.dto.LabelTaskFileSearchRequest;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import cn.iecas.geoai.labelplatform.service.FileService;
import cn.iecas.geoai.labelplatform.service.LabelDatasetFileService;
import cn.iecas.geoai.labelplatform.service.LabelProjectService;
import cn.iecas.geoai.labelplatform.service.UserInfoService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LabelDatasetFileServiceImpl extends ServiceImpl<LabelDatasetFileMapper, LabelDatasetFile> implements LabelDatasetFileService {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private LabelDatasetFileMapper fileMapper;

    @Autowired
    private LabelProjectService labelProjectService;


    /**
     * 根据标注任务，标注员申请标注数据
     * @param labelTask 标注任务信息
     * @return 申请到的标注影像id列表
     */
    @Override
    public List<Integer> applyFileByTaskRandomly(LabelTask labelTask) {
        int datasetId = labelTask.getLabelDatasetId();
        LabelStatus status = labelTask.getTaskType() == LabelTaskType.LABEL ? LabelStatus.UNAPPLIED : LabelStatus.UNCHECK;
        List<Integer> fileIdList =  this.getBaseMapper().getFileByStatusRandomly(datasetId,status,labelTask.getDefaultApplyCount());
        if (fileIdList.size()==0)
            return fileIdList;
        status = status == LabelStatus.UNAPPLIED ? LabelStatus.LABELING : LabelStatus.CHECKING;

        int userId = labelTask.getUserId();
        LabelTaskType labelTaskType = labelTask.getTaskType();
        UpdateWrapper<LabelDatasetFile> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("file_id",fileIdList).eq("dataset_id",datasetId)
                .set("status",status)
                .set(labelTaskType==LabelTaskType.LABEL,"label_user_id",userId)
                .set(labelTaskType==LabelTaskType.LABEL,"assign_label_time", DateUtils.nowDate())
                .set(labelTaskType==LabelTaskType.CHECK,"check_user_id",userId)
                .set(labelTaskType==LabelTaskType.CHECK,"assign_check_time", DateUtils.nowDate());
        this.update(updateWrapper);

        return fileIdList;
    }

    /**
     * 删除数据集中的所有关联的影像数据
     * @param datasetId 数据集id
     */
    @Override
    public void removeAllByDatasetId(int datasetId) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id",datasetId);
        this.remove(queryWrapper);
    }

    /**
     * 根据数据集id和影像id，获取该影像的标注相关信息
     * @param datasetId 数据集id
     * @param fileId 影像id
     * @return 影像的标注相关信息
     */
    @Override
    public LabelDatasetFile getByDatasetAndFileId(int datasetId, int fileId) {
        LambdaQueryWrapper<LabelDatasetFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LabelDatasetFile::getDatasetId,datasetId)
                .eq(LabelDatasetFile::getFileId, fileId);
        return this.baseMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public List<LabelDatasetFile> getByUserAndDatasetId(int datasetId, int userId, LabelTaskType userRole) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id",datasetId);
        if (userRole == LabelTaskType.LABEL)
            queryWrapper.eq("label_user_id",userId);
        else
            queryWrapper.eq("check_user_id",userId);
        return this.list(queryWrapper);

    }



    @Override
    public List<LabelDatasetFile> searchLabelTaskFile(LabelTaskFileSearchRequest labelTaskFileSearchRequest) {
        List<LabelDatasetFile> labelDatasetFileList = new ArrayList<>();
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id",labelTaskFileSearchRequest.getLabelDatasetId())
                .eq(labelTaskFileSearchRequest.getFileId()>0,"file_id",labelTaskFileSearchRequest.getFileId())
                .eq(labelTaskFileSearchRequest.getStatus()!=null,"status",labelTaskFileSearchRequest.getStatus())
                .eq(labelTaskFileSearchRequest.getLabelTaskType() == LabelTaskType.LABEL && !labelTaskFileSearchRequest.isCooperate(),"label_user_id",labelTaskFileSearchRequest.getUserId())
                .eq(labelTaskFileSearchRequest.getLabelTaskType() == LabelTaskType.CHECK,"check_user_id",labelTaskFileSearchRequest.getUserId())
                .orderByDesc("assign_label_time","id");
        labelDatasetFileList =  this.list(queryWrapper);
        return labelDatasetFileList;
    }

    @Override
    public int getTaskTotalCount(LabelTask labelTask) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id",labelTask.getLabelDatasetId()).eq("label_user_id",labelTask.getUserId())
                .eq("status", LabelStatus.FINISH);
        return this.getBaseMapper().selectCount(queryWrapper);
    }

    @Override
    public int getLabelTaskProgress(int datasetId, int userId) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status",LabelStatus.UNCHECK,LabelStatus.CHECKING,LabelStatus.FINISH)
                .eq("dataset_id",datasetId).eq("label_user_id",userId);
        return this.baseMapper.selectCount(queryWrapper);
    }

    @Override
    public int getCheckTaskProgress(int datasetId, int userId) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status",LabelStatus.FINISH,LabelStatus.FEEDBACK)
                .eq("dataset_id",datasetId).eq("check_user_id",userId);
        return this.baseMapper.selectCount(queryWrapper);
    }

    //todo 优化为空的情况
    @Override
    public PageResult<LabelDatasetOrProjectFileInfo> getFileInfoFromDataset(LabelDatasetFileRequest labelDatasetFileRequest) throws ResourceAccessException {
        FileSearchParam fileSearchParam = new FileSearchParam();
        BeanUtils.copyProperties(labelDatasetFileRequest,fileSearchParam);
        List<Integer> fileIdList = labelDatasetFileRequest.getFileIdList();

        //针对task获取文件信息的情况
        if (fileIdList!=null && fileIdList.isEmpty())
            return new PageResult<>(1,0,new ArrayList<>());

        //查询满足状态条件的数据集文件id
        boolean isTaskGeneration = false;
        if (labelDatasetFileRequest.getCategory() != null
                && labelDatasetFileRequest.getCategory().equals("任务生成")
                && labelDatasetFileRequest.isVisibility()) {
            isTaskGeneration = true;
        }
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status","file_id","id","label_user_id","check_user_id","label","ai_label","feedback","finish_label_time","finish_check_time")
                .eq("dataset_id",labelDatasetFileRequest.getDatasetId())
                .eq(isTaskGeneration,"status",LabelStatus.FINISH)
                .eq(!isTaskGeneration && labelDatasetFileRequest.getStatus()!=null,"status",labelDatasetFileRequest.getStatus())
                .in(fileIdList!=null ,"file_id",fileIdList);
        List<LabelDatasetFile> labelDatasetFileList = this.list(queryWrapper);
        List<Integer> labelDatasetFileIdList = labelDatasetFileList.stream().map(LabelDatasetFile::getFileId).collect(Collectors.toList());
        fileSearchParam.setFileIdList(labelDatasetFileIdList);
        fileSearchParam.setFileType(labelDatasetFileRequest.getDatasetType());
        if (labelDatasetFileRequest.getDatasetType() == DatasetType.TEXT) {
            fileSearchParam.setContent(true);
            fileSearchParam.setFromDataset(true);
        }

        //分页查询满足查询条件的文件信息
        PageResult<JSONObject> fileInfoList = this.fileService.getFileInfoByPage(fileSearchParam);
        if (fileInfoList==null)
            return new PageResult<>(1,0,new ArrayList<>());
        labelDatasetFileIdList = fileInfoList.getResult().stream().map(jsonObject -> jsonObject.getInteger("id")).collect(Collectors.toList());
        List<Integer> finalLabelDatasetFileIdList = labelDatasetFileIdList; // 对应的是文件内容ID
        labelDatasetFileList = labelDatasetFileList.stream()
                .filter(labelDatasetFile -> finalLabelDatasetFileIdList.contains(labelDatasetFile.getFileId()))
                .collect(Collectors.toList());

        //查询文件中所涉及的用户名称，如果是从数据集中查看，则不需要，从项目文件查看则需要
        Set<Integer> userIdSet = new HashSet<>();
        Set<Integer> fileIdSet = new HashSet<>();
        labelDatasetFileList.forEach(labelDatasetFile -> {
            userIdSet.add(labelDatasetFile.getCheckUserId());
            userIdSet.add(labelDatasetFile.getLabelUserId());
            fileIdSet.add(labelDatasetFile.getFileId());});
        Map<Integer,String> userIdNameMap = labelDatasetFileRequest.isFromProject() ?
                this.userInfoService.getUserInfoById(userIdSet) : new HashMap<>();
        Map<Integer, JSONObject> fileMap = new HashMap<>();
        if (labelDatasetFileRequest.getDatasetType() == DatasetType.TEXT) {
            fileMap = this.fileService.getFileByContentId("TEXT", fileIdSet);
        }

        List<LabelDatasetOrProjectFileInfo> labelDatasetOrProjectFileInfoList = new ArrayList<>();
        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            int labelDataFileId = labelDatasetFile.getFileId();
            LabelDatasetOrProjectFileInfo labelDatasetOrProjectFileInfo = new LabelDatasetOrProjectFileInfo();
            BeanUtils.copyProperties(labelDatasetFile, labelDatasetOrProjectFileInfo);
            JSONObject data = fileInfoList.getResult().stream()
                    .filter(jsonObject -> jsonObject.getInteger("id")==labelDataFileId).findFirst().get();
            labelDatasetOrProjectFileInfo.setData(data);
            labelDatasetOrProjectFileInfo.setLabel(labelDatasetFile.getLabel());
            labelDatasetOrProjectFileInfo.setAiLabel(labelDatasetFile.getAiLabel());
            labelDatasetOrProjectFileInfo.setLabelUser(userIdNameMap.get(labelDatasetFile.getLabelUserId()));
            labelDatasetOrProjectFileInfo.setCheckUser(userIdNameMap.get(labelDatasetFile.getCheckUserId()));
            if (labelDatasetFileRequest.getDatasetType() == DatasetType.TEXT && labelDatasetFileRequest.isFromProject()) {
                labelDatasetOrProjectFileInfo.setPublisherName(String.valueOf(fileMap.get(String.valueOf(labelDatasetFile.getFileId())).get("user_name")));
                labelDatasetOrProjectFileInfo.setSource(String.valueOf(fileMap.get(String.valueOf(labelDatasetFile.getFileId())).get("source")));
            }
            if (labelDatasetFileRequest.getDatasetType() != DatasetType.TEXT && labelDatasetFileRequest.isFromProject()) {
                JSONObject fileInfoById = this.fileService.getFileInfoById(labelDatasetFile.getFileId(), labelDatasetFileRequest.getDatasetType(),null);
                labelDatasetOrProjectFileInfo.setPublisherName(String.valueOf(fileInfoById.get("userName")));
                labelDatasetOrProjectFileInfo.setSource(String.valueOf(fileInfoById.get("source")));
            }
            labelDatasetOrProjectFileInfoList.add(labelDatasetOrProjectFileInfo);
        }

        return new PageResult<>(fileInfoList.getPageNo(), fileInfoList.getTotalCount(), labelDatasetOrProjectFileInfoList);
    }

    /**
     * 获取标注任务各种状态以及个数
     * @return
     */
    @Override
    public List<Map<String, Long>> getLabelTaskStatusRate(int projectId) {
        List<Map<String, Long>> result = this.baseMapper.getLabelTaskStatusRate(projectId);
        return result;
    }

    /**
     * 获取标注任务总数
     * @return
     */
    @Override
    public Integer getLabelTaskCount(int projectId) {
        QueryWrapper<LabelProject> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.select("dataset_id").eq("id", projectId);
        LabelProject project = this.labelProjectService.getOne(fileQueryWrapper);
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").eq("dataset_id", project.getDatasetId());
        Integer integer = this.baseMapper.selectCount(queryWrapper);
        return integer;
    }

    /**
     * 根据数据集id获取该数据集文件最开始的申领审核时间
     * @param labelDatasetId
     * @return
     */
    @Override
    public Timestamp getFristAssignCheckTimeByDatasetid(int labelDatasetId, int checkUserId) {
        return this.fileMapper.getFristAssignCheckTimeByDatasetid(labelDatasetId, checkUserId);
    }

    /**
     * 根据数据集id获取该数据集文件最开始的申领标注时间
     * @param labelDatasetId
     * @return
     */
    @Override
    public Timestamp getFristAssignLabelTimeByDatasetid(int labelDatasetId, int labelUserId) {
        return this.fileMapper.getFristAssignLabelTimeByDatasetid(labelDatasetId, labelUserId);
    }

    /**
     * 根据数据集id获取该数据集文件最后的审核完成时间
     * @param labelDatasetId
     * @return
     */
    @Override
    public Timestamp getLastFinishCheckTimeByDatasetid(int labelDatasetId, int checkUserId) {
        return this.fileMapper.getLastFinishCheckTimeByDatasetid(labelDatasetId, checkUserId);
    }

    /**
     * 根据数据集id获取该数据集文件最后的标注完成时间
     * @param labelDatasetId
     * @return
     */
    @Override
    public Timestamp getLastFinishLabelTimeByDatasetid(int labelDatasetId, int labelUserId) {
        return this.fileMapper.getLastFinishLabelTimeByDatasetid(labelDatasetId, labelUserId);
    }

    /**
     * 获取任务下的总数
     * @param datasetId
     * @return
     */
    public int getTaskCount(int datasetId){
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id",datasetId);
        return this.baseMapper.selectCount(queryWrapper);
    }

    /**
     * 获取文件提交审核次数
     * @param datasetId 数据集id
     * @param userId 标注用户id
     * @return
     */
    @Override
    public LabelDatasetFile getFileCommitCount(int datasetId, int userId) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(commit_count) AS commit_count").eq("dataset_id",datasetId).eq("label_user_id", userId);
        return this.baseMapper.selectOne(queryWrapper);
    }

    /**
     * 获取项目文件已完成标注的个数
     * @param datasetId
     * @return
     */
    @Override
    public Integer getTaskFinishCount(int datasetId) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").eq("dataset_id",datasetId).eq("status", LabelStatus.FINISH);
        return this.baseMapper.selectCount(queryWrapper);
    }

    /**
     * 根据数据集id、文件id获取标注文件
     * @param labelDatasetId
     * @param labelFileId
     * @return
     */
    @Override
    public LabelDatasetFile getFileByDatasetAndFileId(int labelDatasetId, int labelFileId) {
        QueryWrapper<LabelDatasetFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").eq("dataset_id",labelDatasetId).eq("file_id", labelFileId);
        return this.baseMapper.selectOne(queryWrapper);
    }

    /**
     * 获取变化检测关联文件信息
     * @param id
     * @return
     */
    @Override
    public LabelDatasetFile getFileByLdfId(int id) {
        QueryWrapper<LabelDatasetFile> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        return this.baseMapper.selectOne(wrapper);
    }

    /**
     * 更新变化检测关联文件信息
     * @param id
     * @return
     */
    @Override
    public void updateLabelFileById(int id, int relatedFileId) {
        LambdaUpdateChainWrapper<LabelDatasetFile> wrapper = new LambdaUpdateChainWrapper<>(this.baseMapper);
        wrapper.set(LabelDatasetFile::getRelatedFileId, relatedFileId).eq(LabelDatasetFile::getId, id).update();
    }
}
