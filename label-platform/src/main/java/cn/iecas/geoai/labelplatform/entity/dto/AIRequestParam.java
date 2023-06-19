package cn.iecas.geoai.labelplatform.entity.dto;

import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class AIRequestParam {
    /**
     * 1 影像
     * 2 文本
     * 3 电子
     * 4 ..
     */
    Integer taskType;

    String content;
    Integer serviceId;
    Integer versionId;
    String input_file;
    String output_file;
    int source_service_id;
    int working_type;
    int running_type;
    List<String> imagePathList;
    ResourceInfo resource_info;

    public AIRequestParam() {
        this.serviceId = 1;
        this.versionId = 1;
        this.source_service_id = 0;
        this.working_type = 0;
        this.running_type = 0;
        this.resource_info = new ResourceInfo(1, 1024 * 1024 * 1024 * 8l, new HashMap<String, Integer>(){{
            put("GeForce RTX 2080 Ti", 1);
        }}, 1024 * 1024 * 1024 * 4l);
    }

}
