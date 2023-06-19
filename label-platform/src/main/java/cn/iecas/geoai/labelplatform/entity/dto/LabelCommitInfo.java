package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class LabelCommitInfo {
    /**
     * 任务id
     */
    @Min(value = 1,message = "任务id必须为正整数")
        private int labelTaskId;

    /**
     * 影像id
     */
    @Min(value = 1,message = "文件id必须为正整数")
    private int labelFileId;

    /**
     * 标注信息
     */
    private String labelInfo;

    /**
     * 反馈信息
     */
    private String feedback;

    /**
     * 反馈图像信息
     */
    private String screenshot;

    /**
     * 标注状态
     */
    private LabelStatus status;

    /**
     * 智能标注信息
     */
    private String aiLabelInfo;

    /**
     * 变化检测关联的文件ID
     */
    private int labelCompareFileId = -1;

    /**
     * 标注数量
     */
    private int labelNumber;

    /**
     * 审核目标任务ID
     */
    private int checkTargetTaskId;

    /**
     * 帧位，视频标注用
     */
    private int frameNumber;

    /**
     * 用户token-专用软件用
     */
    private String token;
}
