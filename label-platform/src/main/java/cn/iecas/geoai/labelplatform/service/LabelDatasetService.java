package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.domain.LabelProject;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.client.ResourceAccessException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vanishrain
 */
public interface LabelDatasetService extends IService<LabelDataset> {
    List<Integer> listImageIdByDatasetId(int datasetId);
    void deleteLabelDataset(List<Integer> datasetIdList);
    void setDatasetVisible(int datasetId, boolean visible);
    void copyLabelInfo(int datasetId, int userId, String labelPath);
    PageResult<JSONObject> getFileInfoList(FileSearchParam fileSearchParam);
    List<Integer> getAllFileIdList(FileSearchParam fileSearchParam);
    int createDatasetFromProject(LabelProject labelProject, boolean useLabel);
    void createLabelDatasetInfo(LabelDataset labelDataset) throws IOException;
    Map<Integer,String> getDatasetNameList(int userId, DatasetType datasetType);
    void createSampleSet(SampleSetCreationInfo sampleSetCreationInfo) throws IOException;
    PageResult<LabelDatasetOrProjectFileInfo> getFileInfoFromDataset(LabelDatasetFileRequest labelDatasetFileRequest) throws ResourceAccessException;
    void getManifest(int datasetId, HttpServletResponse httpServletResponse) throws IOException;
    PageResult<LabelDataset> getLabelDatasetInfo(LabelDatasetsSearchRequest labelDatasetsSearchRequest);
    String createManifest(LabelDataset labelDataset) throws IOException;
    Boolean isExistDataset(String datasetName,int userId);
    void createMergeProjectDataset(LabelDataset labelDataset, List<LabelDatasetFile> datasetFileIdList, List<Integer> projectIds);
    LabelDataset getMergeDataSet(UserInfo userInfo, List<Integer> datasetIdList, List<LabelDatasetFile> labelDatasetFileList);
    int getDatasetFileFinishCount(LabelDataset labelDataset);

    List<String> getLabelDataPath(List<Integer> id);
}