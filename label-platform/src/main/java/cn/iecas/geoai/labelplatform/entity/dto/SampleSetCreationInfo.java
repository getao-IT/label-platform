package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.emun.SampleSetCategory;
import cn.iecas.geoai.labelplatform.entity.emun.SampleSetType;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * @author vanishrain
 */
@Data
public class SampleSetCreationInfo {
    /**
     * 切片大小
     */
    private int sliceSize;


    @Min(value = 1,message = "用户id必须为正整数")
    private int userId;

    /**
     * 数据集id
     */
    @Min(value = 1,message = "数据集id必须为正整数")
    private int datasetId;

    /**
     * 标注项目id
     */
    private int projectId;
    /**
     * 来源
     */
    private String source;


    /**
     * 关键字
     */
    private String keywords;

    /**
     * 是否切片
     */
    private boolean isSlice;

    /**
     * 是否保持完整
     */
    private boolean isCompletion;


    /**
     * 样本集id
     */
    private int sampleSetId;

    /**
     * 宽度
     */
    private int width;

    /**
     * 高度
     */
    private int height;

    /**
     * 步长
     */
    private int step;
    /**
     * 是否公开
     */
    private boolean isPublic;

    /**
     * 描述
     */
    private String description;

    /**
     * manifest路径
     */
    private String manifestPath;

    /**
     * 样本集名称
     */
    private String sampleSetName;

    /**
     * 样本集格式类型
     */
    private SampleSetType sampleSetType;


    private DatasetType sampleSetClassification;

    /**
     * 归一化
     */
    private boolean isNormalization;

    /**
     * 任务生成的数据集标注类型
     */
    private SampleSetCategory sampleSetCategory;

}
