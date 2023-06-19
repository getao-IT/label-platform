package cn.iecas.geoai.labelplatform.service.impl;

import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.dto.UserInfo;
import cn.iecas.geoai.labelplatform.service.UserInfoService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Value("${value.api.user-info}")
    private String userInfoApi;


    @Value(value = "${value.api.user-query-name}")
    private String queryUserNameUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest httpRequest;

    public Map<Integer,String> getAllUserInfo(){
        String token = httpRequest.getHeader("token");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        Map<Integer,String> userIdNameMap = new HashMap<>();
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        JSONObject allUserInfo = restTemplate.exchange(queryUserNameUrl, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
        JSONArray userInfoJSONArray = allUserInfo.getJSONArray("data");
        for (int index = 0; index < userInfoJSONArray.size(); index++) {
            JSONObject userInfo = userInfoJSONArray.getJSONObject(index);
            userIdNameMap.put(userInfo.getInteger("id"),userInfo.getString("name"));
        }
        return userIdNameMap;
    }



    @Override
    public Map<Integer,String> getUserInfoById(Set<Integer> userIdSet) {
        String token = httpRequest.getHeader("token");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("user_ids",userIdSet.toString());
        Map<Integer,String> userIdNameMap = new HashMap<>();
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        JSONObject allUserInfo = restTemplate.exchange(queryUserNameUrl, HttpMethod.GET,httpEntity,JSONObject.class,paramMap).getBody();
        JSONArray userInfoJSONArray = allUserInfo.getJSONArray("data");
        for (int index = 0; index < userInfoJSONArray.size(); index++) {
            JSONObject userInfo = userInfoJSONArray.getJSONObject(index);
            userIdNameMap.put(userInfo.getInteger("id"),userInfo.getString("name"));
        }
        return userIdNameMap;
    }

    /**
     * 通过token获取用户信息
     * @param token
     * @return
     */
    @Override
    public CommonResult<UserInfo> getUserInfoByToken(String token) throws ResourceAccessException {
        log.info("开始验证token");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);

        CommonResult result = new CommonResult();

        try {
            JSONObject jsonResult = restTemplate.exchange(userInfoApi, HttpMethod.GET,httpEntity, JSONObject.class).getBody();
            result.setCode(jsonResult.getString("code"));
            result.setData(jsonResult.getJSONObject("data"));
            result.setMessage(jsonResult.getString("msg"));
        }catch (ResourceAccessException e){
            log.error("访问用户信息接口：{} 超时",userInfoApi);
            throw new ResourceAccessException("关联服务访问出错");
        }
        return result;
    }


}
