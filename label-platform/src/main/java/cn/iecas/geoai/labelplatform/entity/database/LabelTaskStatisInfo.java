package cn.iecas.geoai.labelplatform.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "label_task_statis_info")
public class LabelTaskStatisInfo implements Serializable {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 标注项目id
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int labelProjectId;

    /**
     * 标注人员id
     */
    @Column(nullable = true)
    private int userId;

    /**
     * 标注人员类型（0：标注员 1：审核员）
     */
    @Column(nullable = true)
    private int userRole;

    /**
     * 标注人员姓名
     */
    @Column(name = "user_name", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String userName;

    /**
     * 已申领标注任务总数
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int applyFileCount;

    /**
     * 已提交审核次数，提交一次，累加
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int commitCount;

    /**
     * 提交审核目标数量
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int commitObjectCount;

    /**
     * 已完成标注任务个数，审核通过时，更新其值
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int finishCount;

    /**
     * 完成标注任务耗时，审核通过时，更新其值
     */
    @Column(nullable = true, columnDefinition = "float4 default 0")
    private float timeConsume;

    /**
     * 审核不通过次数，审核拒绝时，累加
     */
    @Column(nullable = true, columnDefinition = "int4 default 0")
    private int refuseCount;

}
