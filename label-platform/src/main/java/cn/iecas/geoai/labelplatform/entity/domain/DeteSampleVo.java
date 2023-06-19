package cn.iecas.geoai.labelplatform.entity.domain;

import lombok.Data;

import java.util.List;

@Data
public class DeteSampleVo {

    private String groupName;

    private List<Image> samples;
}
