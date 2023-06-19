package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.dto.SampleSetCreationInfo;
import org.springframework.http.HttpStatus;

public interface SampleSetService {
    void createSampleSet(SampleSetCreationInfo sampleSetCreationInfo);
}
