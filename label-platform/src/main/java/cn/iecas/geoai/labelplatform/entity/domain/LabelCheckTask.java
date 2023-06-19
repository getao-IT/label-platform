package cn.iecas.geoai.labelplatform.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "label_check_task")
public class LabelCheckTask {
    /**
     * imageId
     */
    private int id;

    /**
     * 标注员id
     */
    private int labelUserId;

    /**
     * 总标注数量
     */
    private long totalCount;

    /**
     * 标注完成数量
     */
    private long finishCount;

    /**
     * 发布员id
     */
    private long publisherId;

    /**
     * 任务id
     */
    private int labelProjectId;


}
