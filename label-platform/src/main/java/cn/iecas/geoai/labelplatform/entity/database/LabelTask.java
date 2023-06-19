package cn.iecas.geoai.labelplatform.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Data
@Table(name = "label_task")
public class LabelTask implements Serializable {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 标注员id
     */
    @Column(nullable = true)
    private int userId;

    /**
     * 标注项目id
     */
    @Column(nullable = true)
    private int labelProjectId;

    /**
     * 标注总数
     */
    @Column(nullable = true)
    private int totalCount;

    /**
     * 已完成数量
     */
    @Column(nullable = true)
    private int finishCount;

    /**
     * 发布员id
     */
    @Column(nullable = true)
    private int publisherId;

    /**
     * 任务类型：审核、标注
     */
    @Column(nullable = true)
    private int taskType;

    /**
     * 关联的数据集id
     */
    @Column(nullable = true)
    private int labelDatasetId;

    /**
     * 正在处理的影像idlist表
     */
    @Column(name = "processing_list", nullable = true, columnDefinition = "text COLLATE \"pg_catalog\".\"default\"")
    private int processingList;

    /**
     * 默认申请数量
     */
    @Column(nullable = true)
    private int defaultApplyCount;

    /**
     * 关键字
     */
    @Column(name = "keywords", nullable = true, columnDefinition = "text COLLATE \"pg_catalog\".\"default\"")
    private int keywords;

    /**
     * 样本产出量
     */
    @Column(nullable = true)
    private int makeSampleSetNum;

}
