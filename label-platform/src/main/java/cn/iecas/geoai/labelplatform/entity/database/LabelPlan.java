package cn.iecas.geoai.labelplatform.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Data
@Table(name = "label_plan")
public class LabelPlan implements Serializable {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 数据集id
     */
    @Column(nullable = true)
    private int datasetId;

    /**
     * 计划名称
     */
    @Column(name = "plan_name", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String planName;

    /**
     * 第一阶段标注项目分配员
     */
    @Column(nullable = true)
    private int firstProjectUserId;

    /**
     * 第二阶段标注项目分配员
     */
    @Column(nullable = true)
    private int secondProjectUserId;

    /**
     * 第三阶段标注项目分配员
     */
    @Column(nullable = true)
    private int thirdProjectUserId;

    /**
     * 数据集总数
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int totalCount;

    /**
     * 数据类型
     */
    @Column(name = "dataset_type", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String datasetType;

    /**
     * 计划发布员id
     */
    @Column(nullable = true)
    private int planPublisherId;

    /**
     * 标注类别
     */
    @Column(name = "category", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String category;

    /**
     * 创建时间
     */
    @Column(name = "create_time", columnDefinition = "timestamptz(0) DEFAULT NULL::timestamp with time zone")
    private Date createTime;

    /**
     * 完成数
     */
    @Column(nullable = true)
    private int finishCount;

    /**
     * 标注员id
     */
    @Column(nullable = true)
    private int labelUserId;

    /**
     * 抽取数量
     */
    @Column(nullable = true)
    private int selectedCount;

    /**
     * 审核不通过数量
     */
    @Column(nullable = true)
    private int rejectCount;

    /**
     * 是否抽中
     */
    @Column(nullable = true)
    private boolean selected;

    /**
     * 上传批次
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int batchNo;

    /**
     * 抽中批次
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int selectedBatchNo;
}
