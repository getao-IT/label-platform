package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelCategory;
import cn.iecas.geoai.labelplatform.entity.domain.LabelProject;
import cn.iecas.geoai.labelplatform.entity.dto.AIRequestParam;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AILabelService {
    void callAIService(LabelProject labelProject, String token);
    CommonResult<JSONObject> checkBackState(String tokenId);
    List<LabelCategory> listAIAlgorithms(String serviceName);
    String getTokenId(AIRequestParam params);
    JSONObject getLabelMessage(int imageId, int taskId, String tokenId, LabelPointType labelPointType, HttpServletRequest request , int taskType);
    void updateLabelInfo(int imageId, int taskId);
    List<LabelCategory> getServoceVersionById(int serviceId);
    void setImagePretreatPath(LabelProject labelProject, String token);
}
