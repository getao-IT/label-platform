package cn.iecas.geoai.labelplatform.controller;

import cn.iecas.geoai.labelplatform.aop.annotation.Log;
import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.service.UserPermissionService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;


@Slf4j
@Validated
@Api(tags = "用户权限管理接口")
@RestController
@RequestMapping("/user")
public class UserPermissionController {

    @Autowired
    private UserPermissionService userPermissionService;

    @Log(value = "导出用户权限挂你用户列表为csv文件")
    @GetMapping("/exportUserToCsv")
    public void exportUserToCsv(HttpServletResponse response) {
        this.userPermissionService.exportUserToCsv("users_info.csv", response);
    }
}
