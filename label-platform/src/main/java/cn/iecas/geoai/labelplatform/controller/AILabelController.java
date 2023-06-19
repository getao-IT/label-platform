package cn.iecas.geoai.labelplatform.controller;

import cn.iecas.geoai.labelplatform.aop.annotation.Log;
import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelCategory;
import cn.iecas.geoai.labelplatform.entity.dto.AIRequestParam;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.service.AILabelService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * todo
 * 新建AILabelService服务，调用服务市场的接口
 */
@RestController
@Api(tags = "智能标注接口")
@RequestMapping(value = "/ai")
public class AILabelController {

    @Autowired
    AILabelService aiLabelService;

    /*todo
    1、新建algorithm实体类，只包含algorithm的name的algorithm的id
    2、调用服务市场获取服务列表
     */
    @Log("获取服务市场算法信息类")
    @ApiOperation("获取服务市场算法信息类")
    @GetMapping(value = "/algorithm")
    public CommonResult<List<LabelCategory>> getAlgorithms(String serviceName){
        List<LabelCategory> labelCategoryList = this.aiLabelService.listAIAlgorithms(serviceName);
        return new CommonResult<List<LabelCategory>>().success().data(labelCategoryList).message("获取服务市场算法信息类成功");
    }

    @Log("获取服务详情信息")
    @ApiOperation("获取服务详情信息")
    @GetMapping(value = "/serviceInfo")
    public CommonResult<List<LabelCategory>> getServoceVersionById(int serviceId){
        List<LabelCategory> result = this.aiLabelService.getServoceVersionById(serviceId);
        return new CommonResult<List<LabelCategory>>().success().data(result).message("获取服务详情信息成功");
    }

    @Log("获取服务市场Token_ID")
    @ApiOperation("获取服务市场Token_ID")
    @GetMapping(value = "/tokenId")
    public  CommonResult<String> getTokenId(int serviceId, int versionId, @RequestParam(required = false) List<String> imagePathList , Integer taskType , String content){
        AIRequestParam param = new AIRequestParam();
        param.setServiceId(serviceId);
        param.setVersionId(versionId);
        param.setImagePathList(imagePathList);
        param.setTaskType(taskType);
        param.setContent(content);
        String tokenId = this.aiLabelService.getTokenId(param);
        return new CommonResult<String>().success().data(tokenId).message("获取服务市场Token_ID成功");
    }

    @Log("获取服务市场算法标注状态")
    @ApiOperation("获取服务市场算法标注状态")
    @GetMapping(value = "/status")
    public CommonResult<JSONObject> backState(String taskId){
        CommonResult<JSONObject> result = this.aiLabelService.checkBackState(taskId);
        return result;
    }

    @Log("获取服务市场算法标注信息")
    @ApiOperation("获取服务市场算法标注信息")
    @GetMapping(value = "/label")
    public CommonResult<JSONObject> getLabelMessage(Integer taskType, @RequestParam(required = false) int imageId, int taskId, String tokenId, @RequestParam(required = false) LabelPointType labelPointType, HttpServletRequest request){
        JSONObject result = this.aiLabelService.getLabelMessage(imageId, taskId, tokenId, labelPointType, request , taskType);
        return new CommonResult<JSONObject>().success().data(result)
                .message("取服务市场算法标注信息");
    }

    @Log("应用智能标注结果")
    @ApiOperation("应用智能标注结果")
    @PostMapping(value = "/applyAilabel")
    public CommonResult<String> updateLabelInfo(int imageId, int taskId){
        this.aiLabelService.updateLabelInfo(imageId,taskId);
        return new CommonResult<String>().success().data(null).message("应用智能标注结果成功");
    }
}
