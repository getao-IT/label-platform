package cn.iecas.geoai.labelplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ResourceInfo {
    int  cpu_count;
    long mem_size;
    Map<String, Integer> gpu_dict;
    long shm_size;
}
