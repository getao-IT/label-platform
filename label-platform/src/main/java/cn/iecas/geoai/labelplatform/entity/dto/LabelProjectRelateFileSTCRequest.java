package cn.iecas.geoai.labelplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelProjectRelateFileSTCRequest implements Serializable {
    private static final long serialVersionUID = -2701669295318838473L;

    private int projectId;

    /**
     * 统计方式
     */
    private String statisWay;
    /**
     * 统计标准
     */
    private String standard;

    /**
     * 筛选条件
     */
    private Date startTime;
    private Date endTime;
    private float maxLon;
    private float minLon;
    private float maxLat;
    private float minLat;
    private float centeredLon;
    private float centeredLat;

    /**
     * 展示数据
     */
    private String tab; // 展示标签
    private int spaceTime; // 展示数据： 时空覆盖性：对应标注项目数据集真实文件个数
}
