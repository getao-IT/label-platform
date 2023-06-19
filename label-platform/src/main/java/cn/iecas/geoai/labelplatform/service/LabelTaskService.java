package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelProject;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTask;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTaskStatisInfo;
import cn.iecas.geoai.labelplatform.entity.domain.VideoFrame;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.dom4j.DocumentException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LabelTaskService extends IService<LabelTask> {
    List<Integer> applyForData(int labelTaskId);
    void createLabelTasks(LabelProject labelProject);
    void createCheckTasks(LabelProject labelProject);
    void deleteTaskByProjectId(List<Integer> projectIdList);
    LabelTaskProgress getLabelTaskProgress(int labelTaskId);
    void commitLabelInfo(LabelCommitInfo labelCommitInfo);
    void deleteLabelInfo(int labelDataFileId, int labelType);
    PageResult<LabelTaskInfo> getLabelTasks(LabelTaskSearchRequest labelTaskSearchRequest);
    PageResult<LabelTaskFileInfo> getTaskFileStatusInfos(LabelTaskFileSearchRequest labelTaskFileSearchRequest) throws ResourceAccessException;
    String importLabelFile(String imagePath, LabelPointType labelPointType, MultipartFile file, DatasetType datasetType) throws Exception;
    void exportLabelFile(LabelExportParam labelExportParam) throws UnsupportedEncodingException, DocumentException;
    List<LabelTask> getLabelTaskByProjectId(List<Integer> projectIds);
    List<LabelTask> mergeTaskByRepeatFileToNotNull(
            List<LabelTask> labelTaskList, LabelProject labelProject, int srcProjectId,
            List<Integer> projectIdList, boolean isMerge, List<Integer> fileIdList, UserInfo userInfo);
    List<LabelTask> mergeTaskToNull(
            List<LabelTask> labelTaskList, LabelProject labelProject, int srcProjectId,
            List<Integer> projectIdList, boolean isMerge, List<Integer> fileIdList, UserInfo userInfo);
    PageResult<LabelTaskStatisInfo> getLabelTaskStatisInfoByUserIds(List<Integer> userIdList, int userRole, int projectId, LabelTaskSearchRequest labelTaskSearchRequest);
    List<HashMap<String, String>> getLabelTaskStatusRate(int projectId);
    List<LabelTaskStatisInfo> createLabelTaskStatis(LabelProject labelProject, List<String> userIdList, LabelTaskType taskType);
    List<VideoFrame> getFrameImgByFrameNbr(String videoPath, int frameNumber, int returnNumber);
    Boolean isPreprocess(int taskId);
}
