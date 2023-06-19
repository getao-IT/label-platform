package cn.iecas.geoai.labelplatform.entity.database;

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

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Entity
@Data
@Table(name = "label_dataset")
public class LabelDataset implements Serializable {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 数据集创建用户id
     */
    @Column(nullable = true)
    private int userId;

    /**
     * 结果数据集关联的项目id
     */
    @Column(nullable = true)
    private int projectId;

    /**
     * 数据集关键词
     */
    private String keywords;

    /**
     * 数据集名称
     */
    private String datasetName;

    /**
     * 数据集标注信息路径
     */
    private String datasetPath;

    /**
     * 数据集数量
     */
    @Column(nullable = true)
    private int count;

    /**
     * 创建时间
     */
    @Column(columnDefinition = "timestamp")
    private Date createTime;

    /**
     * 数据集状态
     */
    private String status;

    /**
     * 数据集目录
     */
    private String manifestPath;

    /**
     * 数据集描述
     */
    private String description;

    /**
     * 数据集类型
     */
    private String category;

    /**
     * 是否可见
     */
    @Column(nullable = true)
    private boolean visibility;

    /**
     * 是否公开
     */
    @Column(name = "is_public", nullable = true)
    private boolean ispublic;

    /**
     * 数据集类型
     */
    private String datasetType;

    /**
     * 任务生成的数据集标注类型
     */
    private String projectCategory;

    /**
     * 任务生成数据集已完成标注数量
     */
    @Column(nullable = true)
    private int finishCount;

}