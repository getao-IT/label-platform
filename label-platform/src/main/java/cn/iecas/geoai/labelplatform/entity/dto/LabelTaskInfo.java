package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class LabelTaskInfo {

    /**
     * 任务id
     */
    private int id;

    /**
     * 标注项目ID
     */
    private int labelProjectId;


    /**
     * 完成总数
     */
    private int finishCount;

    /**
     * 标注总数
     */
    private int totalCount;

    /**
     * 发布日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date publishDate;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 发布员
     */
    private String publisherName;

    /**
     * 项目类别
     */
    private String projectCategory;

    /**
     * 项目描述
     */
    private String projectDescription;

    /**
     * 表述项目数据总数
     */
    private int projectTotalCount;

    /**
     * 标注项目完成总数
     */
    private int projectFinishCount;

    /**
     * 任务数据类型
     */
    private DatasetType datasetType;

}
