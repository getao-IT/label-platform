package cn.iecas.geoai.labelplatform.entity.domain;

import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "label_task")
public class LabelTask {
    /**
     * imageId
     */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 标注员id
     */
    private int userId;

    /**
     * 所属标注项目的关键字
     */
    private String keywords;

    /**
     * 总标注数量
     */
    private int totalCount;

    /**
     * 标注完成数量
     */
    private int finishCount;

    /**
     * 发布员id
     */
    private long publisherId;

    /**
     * 任务id
     */
    private int labelProjectId;

    /**
     * 关联数据集id
     */
    private int labelDatasetId;

    /**
     * 默认申请数量
     */
    private int defaultApplyCount;

    /**
     * 正在处理的影像id列表
     */
    private String processingList;

    /**
     * 任务类型
     */
    private LabelTaskType taskType;
}
