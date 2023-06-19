package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
public class LabelDatasetFileRequest {
    @Min(value = 1,message = "数据集id必须为正整数")
    private int datasetId;
    private int pageNo = 1;
    private boolean content;
    private int pageSize = 10;
    private String searchParam;
    private String category;
    private LabelStatus status;
    private boolean fromProject;
    private DatasetType datasetType;
    private List<Integer> fileIdList;
    private boolean visibility;
}
