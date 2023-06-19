package cn.iecas.geoai.labelplatform.entity.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class MergeLabelProject  implements Serializable {
    private static final long serialVersionUID = -270443392962750154L;

    private LabelDataset labelDataset;

    private LabelProject labelProject;
}
