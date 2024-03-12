package cn.iecas.geoai.labelplatform.util;

import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.dto.UserInfo;
import cn.iecas.geoai.labelplatform.service.UserInfoService;
import cn.iecas.geoai.labelplatform.service.impl.UserInfoServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 用户工具类
 */
@Component
public class UserUtils {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 判断用户是否为管理员
     * @param token
     * @return
     */
    public boolean isAdmin(String token) {
        System.out.println("token:"+token);
        CommonResult<UserInfo> userInfo = userInfoService.getUserInfoByToken(token);
        UserInfo data = JSONObject.toJavaObject(JSONObject.parseObject(String.valueOf(userInfo.getData())), UserInfo.class);
        if (data.getId() == 0) {
            return true;
        }
        return false;
    }
}
