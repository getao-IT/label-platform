package cn.iecas.geoai.labelplatform.entity.domain;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.*;
import java.io.Serializable;

/**
 * (LabelDatasets)实体类
 *
 * @author vanishrain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "label_dataset")
public class LabelDataset implements Serializable {
    private static final long serialVersionUID = 561575461121364164L;
    /**
    * imageId
    */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 数据集创建用户id
     */
    @Min(value = 1,message = "用户id必须为正整数")
    private int userId;

    /**
     * 结果数据集关联的项目id
     */
    private int projectId;


    /**
     * 数据集关键词
     */
    private String keywords;

    /**
    * 数据集名称
    */
    @NotBlank(message = "数据集名称不能为空")
    private String datasetName;

    /**
    * 数据集目录
    */
    private String datasetPath;

    /**
    * 数据集数量
    */
    private int count;


    /**
    * 创建时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
    * 数据集状态
    */
    private String status;


    /**
     * 数据集类型：用户创建、结果数据集
     */
    private String category;

    /**
     * 是否公开
     */
    private boolean isPublic;

    /**
    * 数据集标注信息路径
    */
    private String manifestPath;

    /**
     * 数据集描述
     */
    private String description;

    /**
     * 是否可见
     */
    private boolean visibility;


    @TableField(exist = false)
    private String searchParam;

    /**
     * 查询结束时间
     */
    @TableField(exist = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String endTime;

    /**
     * 查询开始时间
     */
    @TableField(exist = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String startTime;

    /**
     * 通过审核的文件数量
     */
    private int finishCount;

    @TableField(exist = false)
    private boolean selectAll;

    @TableField(exist = false)
    private String labelPath;

    private String projectCategory;

    private DatasetType datasetType;

    @TableField(exist = false)
    private List<Integer> fileIdList = new ArrayList<>();

    public void setFileIdList(String fileIdListStr){
        List<String> fileList = Arrays.asList(fileIdListStr.split(","));
        fileList.forEach(str->fileIdList.add(Integer.valueOf(str)));
    }








}