package cn.iecas.geoai.labelplatform.service.impl;

import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.dto.SampleSetCreationInfo;
import cn.iecas.geoai.labelplatform.service.SampleSetService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class SampleSetServiceImpl implements SampleSetService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest request;


    @Value(value = "${value.api.sampleset-creation}")
    private String sampleSetCreationAPI;



    @Override
    public void createSampleSet(SampleSetCreationInfo sampleSetCreationInfo) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",request.getHeader("token"));
        HttpEntity<SampleSetCreationInfo> httpEntity = new HttpEntity<>(sampleSetCreationInfo,httpHeaders);
        try{
            this.restTemplate.postForEntity(sampleSetCreationAPI,httpEntity, JSONObject.class).getBody();
        }catch (ResourceAccessException e){
            log.error("访问接口:{} 超时",sampleSetCreationAPI);
            throw new ResourceAccessException("访问服务接口api超时");
        }
    }
}
