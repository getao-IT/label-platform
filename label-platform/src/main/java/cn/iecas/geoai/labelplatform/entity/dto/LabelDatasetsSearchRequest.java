package cn.iecas.geoai.labelplatform.entity.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 数据集查询请求参数
 * @author vanishrain
 */
@Data
public class LabelDatasetsSearchRequest implements Serializable {
    private static final long serialVersionUID = 1853919413576275923L;

    /**
     * 发布员id
     */
    //@Min(value = 1, message = "userId必须为正整数")
    private int userId;
    /**
     * 数据集名称
     */
    private String datasetName;

    /**
     * 页数
     */
    private int pageNo = 1;

    /**
     * 每页数量
     */
    private int pageSize = 10;

    /**
     * 数据集类型
     */
    private String datasetType;

    /**
     * 数据集种类
     */
    private String category;
}
