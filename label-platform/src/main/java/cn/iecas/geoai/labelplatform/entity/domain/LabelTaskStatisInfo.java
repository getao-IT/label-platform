package cn.iecas.geoai.labelplatform.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelTaskStatisInfo implements Serializable {
    private static final long serialVersionUID = -2043402613104698000L;

    /**
     * 任务统计信息id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 标注项目id
     */
    private int labelProjectId;

    /**
     * 发布人id
     */
    private int userId;

    /**
     * 发布人角色
     */
    private int userRole;

    /**
     * 发布人姓名
     */
    private String userName;

    /**
     * 已申领任务个数
     */
    private int applyFileCount = 0;

    /**
     * 提交审核次数
     */
    private int commitCount = 0;

    /**
     * 提交审核目标数量
     */
    private int commitObjectCount = 0;

    /**
     * 已完成的标注文件个数
     */
    private int finishCount = 0;

    /**
     * 完成标注任务消耗的时间
     */
    private float timeConsume = 0;

    /**
     * 审核不同通过次数
     */
    private int refuseCount = 0;

    /**
     * 平均标注时间
     */
    @TableField(exist = false)
    private String aveLabelTime = "";

    /**
     * 审核通过率
     */
    @TableField(exist = false)
    private String checkPassRate = "";

    /**
     * 标注任务状态
     */
    @TableField(exist = false)
    private Integer labelStatus;

    /**
     * 该状态标注任务所占比率
     */
    @TableField(exist = false)
    private String statusRate = "0";

}
