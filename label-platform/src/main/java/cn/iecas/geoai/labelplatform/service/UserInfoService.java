package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.dto.UserInfo;
import com.alibaba.fastjson.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;
import java.util.Set;

public interface UserInfoService {
    Map<Integer,String> getAllUserInfo();
    Map<Integer,String> getUserInfoById(Set<Integer> userIdSet);
    CommonResult<UserInfo> getUserInfoByToken(String token) throws ResourceAccessException;
}
