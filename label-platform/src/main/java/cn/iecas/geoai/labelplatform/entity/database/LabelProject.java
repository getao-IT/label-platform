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
@Table(name = "label_project")
public class LabelProject implements Serializable {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 项目名称
     */
    @Column(name = "project_name", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String projectName;

    /**
     * 项目描述
     */
    @Column(name = "project_description", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String projectDescription;

    /**
     * 关联数据集id
     */
    @Column(nullable = true)
    private int datasetId;

    /**
     * 状态
     */
    @Column(nullable = true)
    private int status;

    /**
     * 分类
     */
    @Column(name = "category", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String category;

    /**
     * 标签
     */
    @Column(name = "label", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\" DEFAULT 0")
    private String label;

    /**
     * 发布人
     */
    @Column(nullable = true)
    private int userId;

    /**
     * 标注员列表
     */
    @Column(name = "label_user_ids", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String labelUserIds;

    /**
     * 审核员列表
     */
    @Column(name = "check_user_ids", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String checkUserIds;

    /**
     * 每次最多标注申请数量
     */
    @Column(name = "default_label_count", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String defaultLabelCount;

    /**
     * 每次最多审核申请数量
     */
    @Column(name = "default_check_count", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String defaultCheckCount;

    /**
     * 关联基础数据集
     */
    @Column(name = "related_dataset_id", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String relatedDatasetId;

    /**
     * 发布者姓名
     */
    @Column(name = "publisher_name", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String publisherName;

    /**
     * 关联影像总数
     */
    @Column(nullable = true)
    private int totalCount;

    /**
     * 已完成标注数量
     */
    @Column(nullable = true)
    private int finishCount;

    /**
     * 关键字
     */
    @Column(name = "keywords", columnDefinition = "text COLLATE \"pg_catalog\".\"default\"")
    private String keywords;

    /**
     * 创建时间
     */
    @Column(name = "create_time", columnDefinition = "timestamp", length = 6)
    private Date createTime;

    /**
     * 算法服务id
     */
    @Column(name = "service_id", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String serviceId;

    /**
     * 是否需要AI标注
     */
    @Column(name = "is_ai_label", nullable = true)
    private boolean isAiLabel;

    /**
     * 是否使用数据集已有的标注信息
     */
    @Column(name =  "use_label", nullable = true)
    private boolean useLabel;

    /**
     * 标注项目类型
     */
    @Column(name = "dataset_type", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String datasetType;

    /**
     * 样本产出量
     */
    private String makeSampleSetNum;

    /**
     * 审核驳回次数
     */
    @Column(nullable = true)
    private int refuseCount;

    /**
     * 提交审核次数
     */
    @Column(nullable = true)
    private int commitCount;

    /**
     * 标注任务完成时间
     */
    @Column(name = "finish_time", columnDefinition = "timestamp", length = 6)
    private Date finishTime;

    /**
     * 标注任务完成耗时
     */
    @Column(name = "consume_time", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String consumeTime;

    /**
     * 审核驳回率
     */
    @Column(name = "refuse_rate", columnDefinition = "numeric(32,2)")
    private Long refuseRate;

}
