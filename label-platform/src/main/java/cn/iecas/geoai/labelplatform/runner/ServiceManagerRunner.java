package cn.iecas.geoai.labelplatform.runner;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;


@Component
@Order(value = 2)
@Slf4j
@ConditionalOnProperty(value = "value.service.enable", havingValue = "true")
public class ServiceManagerRunner<T> implements ApplicationRunner {
    JSONObject jsonObject=new JSONObject();
    //获取配置文件值
    @Value("${value.service.port}")
    private String servicePort;
    @Value("${value.service.name}")
    private String serviceName;
    @Value("${value.service.url}")
    private String url;
    @Value("${value.service.status}")
    private String serviceStatusUrl;
    @Value("${value.service.prefix}")
    private String prefix;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 获取本机IP
        InetAddress address= InetAddress.getLocalHost();
        String hostAddress=address.getHostAddress();

        jsonObject.put("serviceName",serviceName) ;
        jsonObject.put("serviceIp",hostAddress);
        jsonObject.put("servicePort",servicePort);
        jsonObject.put("serviceStatusUrl",serviceStatusUrl);
        jsonObject.put("prefix",prefix);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.POST;
        MediaType type = MediaType.parseMediaType("application/json;charset=utf-8");
        headers.setContentType(type);
        headers.add("Accept",MediaType.APPLICATION_JSON.toString());
        HttpEntity<JSONObject> entity= new HttpEntity<JSONObject>(jsonObject,headers);
        try {
            String s = restTemplate.postForEntity(url,entity,String.class).getBody();
            JSONObject object = JSONObject.parseObject(s);
            if (object.get("code").equals("Ok")) {
                log.info("服务注册成功：IP - {}，ServiceName - {}", hostAddress, serviceName);
            } else {
                log.error("服务注册失败：IP - {}，ServiceName - {}", hostAddress, serviceName);
            }
        } catch (Exception e) {
            log.error("错误：注册服务接口调用失败！");
        }
   }
}
