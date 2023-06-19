package cn.iecas.geoai.labelplatform.service;


import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletResponse;

public interface UserPermissionService {

    JSONObject getUserInfo();

    void exportUserToCsv(String fileName, HttpServletResponse response);
}
