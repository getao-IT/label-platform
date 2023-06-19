package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
public class LabelTaskFileSearchRequest {
    private int fileId;
    /**
     * 任务id
     */
    @Min(value = 1,message = "标注任务id必须为正整数")
    private int taskId;

    @Min(value = 1,message = "用户id必须为正整数")
    private int userId;
    private int pageNo = 1;
    private boolean content;
    private int pageSize = 10;
    private int labelDatasetId;
    private String searchParam;
    private LabelStatus status;
    private LabelTaskType labelTaskType;
    private List<String> searchParamList;
    private boolean cooperate;
}
