package cn.iecas.geoai.labelplatform.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.Entity;

/**
 * @author vanishrain
 */
@Data
public class LabelCategory {
    private String id;
    private String categoryName;
    private String serviceId;
    private int versionId;
    private String versionInfo;
}
