package cn.iecas.geoai.labelplatform.controller;


import cn.aircas.utils.date.DateUtils;
import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "服务管理接口")
@RestController
@RequestMapping("/status")
public class StatusController {

    @Autowired(required = false)
    BuildProperties buildProperties;

    @GetMapping("/version")
    @ApiOperation("获取服务版本号")
    public CommonResult<JSONObject> version(){
        JSONObject result = new JSONObject();
        String version = this.buildProperties == null ? "latest" : this.buildProperties.getVersion();
        String time = this.buildProperties == null ? DateUtils.nowDate().toString() : this.buildProperties.getTime().toString();
        result.put("version",version);
        result.put("buildTime",time);
        return new CommonResult<JSONObject>().data(result).success().message("服务运行正常");
    }

    @GetMapping
    @ApiOperation("返回状态标识")
    public int getVersion() {
        return 1;
    }
}
