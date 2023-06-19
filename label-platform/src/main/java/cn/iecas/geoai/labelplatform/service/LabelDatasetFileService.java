package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.dto.LabelDatasetOrProjectFileInfo;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTask;
import cn.iecas.geoai.labelplatform.entity.dto.LabelDatasetFileRequest;
import cn.iecas.geoai.labelplatform.entity.dto.LabelTaskFileSearchRequest;
import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface LabelDatasetFileService extends IService<LabelDatasetFile>{
    void removeAllByDatasetId(int datasetId);
    int getTaskTotalCount(LabelTask labelTask);
    int getLabelTaskProgress(int dataSetId, int userId);
    int getCheckTaskProgress(int dataSetId, int userId);
    List<Integer> applyFileByTaskRandomly(LabelTask labelTask);
    LabelDatasetFile getByDatasetAndFileId(int datasetId, int fileId);
    List<LabelDatasetFile> getByUserAndDatasetId(int dataSetId, int userId, LabelTaskType userRole);
    List<LabelDatasetFile> searchLabelTaskFile(LabelTaskFileSearchRequest labelTaskFileSearchRequest);
    PageResult<LabelDatasetOrProjectFileInfo> getFileInfoFromDataset(LabelDatasetFileRequest labelDatasetFileRequest);
    List<Map<String, Long>> getLabelTaskStatusRate(int projectId);
    Integer getLabelTaskCount(int projectId);
    Timestamp getFristAssignCheckTimeByDatasetid(int labelDatasetId, int checkUserId);
    Timestamp getFristAssignLabelTimeByDatasetid(int labelDatasetId, int labelUserId);
    Timestamp getLastFinishCheckTimeByDatasetid(int labelDatasetId, int checkUserId);
    Timestamp getLastFinishLabelTimeByDatasetid(int labelDatasetId, int labelUserId);
    LabelDatasetFile getFileCommitCount(int datasetId, int userId);
    Integer getTaskFinishCount(int datasetId);
    LabelDatasetFile getFileByDatasetAndFileId(int labelDatasetId, int labelFileId);
    LabelDatasetFile getFileByLdfId(int id);
    void updateLabelFileById(int id, int relatedFileId);
}
