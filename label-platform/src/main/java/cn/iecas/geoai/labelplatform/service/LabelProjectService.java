package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.DeteSampleVo;
import cn.iecas.geoai.labelplatform.entity.domain.Image;
import cn.iecas.geoai.labelplatform.entity.domain.LabelProject;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface LabelProjectService extends IService<LabelProject>{
    List<String> importKeywords(MultipartFile file) throws IOException;
    void createDatasetResult(int projectId) throws IOException;
    void updateLabelProgress(int labelProjectId);
    void deleteLabelProject(List<Integer> projectIdList);
    void createLabelProject(LabelProject labelProject, HttpServletRequest request);
    void updateUniteLabelProjectToHt(LabelProject labelProject, HttpServletRequest request);
    void updateUniteLabelProjectToFzt(LabelProject labelProject, HttpServletRequest request);
    PageResult<LabelProject> getLabelProject(LabelProjectSearchRequest labelProjectSearchRequest);
    void updateLabelProject(LabelProject labelProject);
    Boolean isExistLabelProject(String projectName,int userId);
    void updateLabelProjectUser(LabelProjectUpdateUserInfo labelProjectUpdateUserInfo);
    void exportLabelFileToZip(int projectId, HttpServletResponse response) throws IOException;
    List<Integer> createProjectByFileIsRepeat(int srcProjectId, List<Integer> mergeProjectIds, boolean isMerge,
                                                       String projectName, String projectDescription);
    void createMergeProject(UserInfo userInfo, List<Integer> fileIdList, int srcProjectId,  List<Integer> projectIdList,
                            boolean isMerge, String projectName, String projectDescription);
    List<LabelProject> getProjectByDataSetType(int projectId);
    void updateSampleSetNumById(int projectId, int nums);
    List<LabelProject> getProjectsByRelateDataset(int projectId, String relatedDatasetId);
    List<LabelProjectRelateFileSTCInfo> getProjectSTCByStandard(LabelProjectRelateFileSTCRequest params);
    List<JSONObject> getProjectSTCToHeatmap(LabelProjectRelateFileSTCRequest params);
    JSONObject getChangeDetectionFileById(int id);
    void updateLabelFileById(int id, int relatedFileId);
    List<DeteSampleVo> listDeteSamples(List<JSONObject> sampleVos);
    Map<String, List<Image>> mapDeteSamples(List<JSONObject> sampleVos);
}
