package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author vanishrain
 * 标注项目数据信息类
 */
@Data
public class LabelDatasetOrProjectFileInfo {

    /**
     * id
     */
    private int id;

    /**
     * 发布员
     */
    private String publisherName;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 标注信息
     */
    private String label;


    /**
     * ai标注信息
     */
    private String aiLabel;

    /**
     * 审核反馈
     */
    private String feedback;

    /**
     * 标注用户
     */
    private String labelUser;

    /**
     * 审核时间
     */
    private String checkUser;

    /**
     * 标注状态
     */
    private LabelStatus status;

    /**
     * 数据信息
     */
    private JSONObject data;

    /**
     * 审核时间
     */
    private Date assignCheckTime;

    /**
     * 标注时间
     */
    private Date assignLabelTime;

    /**
     * 标注员提交标注的时间
     */
    private Date finishLabelTime;

    /**
     * 审核员审核完成时间
     */
    private Date finishCheckTime;
}
