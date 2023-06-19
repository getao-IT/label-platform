package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import lombok.Data;

@Data
public class LabelProjectUpdateUserInfo {
    int labelProjectId;
    String updateUserIdStr;
    LabelTaskType labelUserType;
}
