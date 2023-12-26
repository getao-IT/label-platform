package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.dto.FileSearchParam;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FileService {
    JSONObject getFileInfoById(int fileId);
    JSONObject getFileInfoById(int fileId, DatasetType datasetType);
    List<Integer> listFileIdBySearch(LabelDataset labelDataset);
    JSONArray getAllFileIdList(FileSearchParam fileSearchParam);
    List<JSONObject> listFileInfoByIdList(List<Integer> fileIdList, DatasetType datasetType) throws ResourceAccessException;
    PageResult<JSONObject> getFileInfoByPage(FileSearchParam fileSearchParam);
    Map<Integer, JSONObject> getFileByContentId(String fileType, Set<Integer> fileIds);
}