package cn.iecas.geoai.labelplatform.entity.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabelProjectRelateFileSTCInfo implements Serializable {
    private static final long serialVersionUID = 33051051218703398L;

    /**
     * 展示数据
     */
    private String tab; // 展示标签
    private int spaceTime; // 展示数据： 时空覆盖性：对应标注项目数据集真实文件个数
}
