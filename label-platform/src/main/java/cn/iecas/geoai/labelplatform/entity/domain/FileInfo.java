package cn.iecas.geoai.labelplatform.entity.domain;

import lombok.Data;



@Data
public class FileInfo {
    private String source;
    private String keywords;
    private String description;
    private Boolean isPublic;
}
