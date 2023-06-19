package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Date;

@Data
public class LabelTaskFileInfo {

    /**
     * datasetFileId
     */
    private int id;


    /**
     * 任务名称
     */
    private int taskId;

    /**
     * 标注信息
     */
    private String label;

    /**
     * 数据集id
     */
    private int datasetId;

    /**
     * 反馈
     */
    private String feedback;

    /**
     * 反馈图像
     */
    private String screenshot;

    /**
     * 智能标注结果
     */
    private String aiLabel;

    /**
     * 审核员id
     */
    private int checkUserId;

    /**
     * 标注员id
     */
    private int labelUserId;

    /**
     * 标注文件信息
     */
    private JSONObject data;

    /**
     * 标注项目关键字
     */
    private String keywords;

    /**
     * 申请审核时间
     */
    private Date assignCheckTime;

    /**
     * 申请标注时间
     */
    private Date assignLabelTime;

    /**
     * 完成审核时间
     */
    private Date finishCheckTime;

    /**
     * 完成标注时间
     */
    private Date finishLabelTime;

    /**
     * 任务类型
     */
    private String projectCategory;

    /**
     * 标注状态
     */
    private LabelStatus status;

    private String preprocessPath;






}
