package cn.iecas.geoai.labelplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: getao
 * @Date: 2025/10/9 10:57
 * @Description: 样本集发布信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasetPublishInfo {

    private String datasetName;

    private String datasetType;

    private String datasetPath;

    private int imageCount;

    private List<String> coords;

    private String brFrom;

    private Boolean publishStatus;

    private String msg;
}
