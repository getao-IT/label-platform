package cn.iecas.geoai.labelplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标注任务进度信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabelTaskProgress {
    /**
     * 总数量
     */
    private int totalCount;

    /**
     * 完成数量
     */
    private int finishCount;
}
