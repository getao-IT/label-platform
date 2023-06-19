package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class LabelTaskSearchRequest {
    /**
     * 任务所属用户id
     */
    @Min(value = 1,message = "任务id必须为正整数")
    private int userId;

    /**
     * 任务id
     */
    private int taskId;

    /**
     * 获取页码
     */
    private int pageNo = 1;

    /**
     * 每页大小
     */
    private int pageSize = 10;

    /**
     * 任务名称
     */
    private String projectName;

    /**
     * 任务类型
     */
    @NotNull(message = "必须填写任务类型")
    private LabelTaskType taskType;
}
