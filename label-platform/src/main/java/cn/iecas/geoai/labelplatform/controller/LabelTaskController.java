package cn.iecas.geoai.labelplatform.controller;

import cn.iecas.geoai.labelplatform.aop.annotation.Log;
import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTaskStatisInfo;
import cn.iecas.geoai.labelplatform.entity.domain.VideoFrame;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.service.LabelTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@Api(tags = "标注任务管理接口")
@RequestMapping(value = "/task")
public class LabelTaskController {
    @Autowired
    LabelTaskService labelTaskService;

    @GetMapping
    @ApiOperation("获取任务列表")
    @Log(value = "获取任务列表")
    public CommonResult<PageResult<LabelTaskInfo>> getTask(LabelTaskSearchRequest labelTaskSearchRequest) {
        PageResult<LabelTaskInfo> labelTaskPageResult = this.labelTaskService.getLabelTasks(labelTaskSearchRequest);
        return new CommonResult<PageResult<LabelTaskInfo>>().success().data(labelTaskPageResult).message("获取标注任务成功");
    }

    @GetMapping("/apply")
    @ApiOperation("申请标注/审核数据")
    @Log(value = "申请标注/审核数据")
    public CommonResult<List<Integer>> applyForTask(@Min(value = 1, message = "任务id必须为正整数") int taskId) {
        List<Integer> imageIdList = this.labelTaskService.applyForData(taskId);
        return new CommonResult<List<Integer>>().success().data(imageIdList).message("申请标注/审核数据成功");
    }
    @GetMapping("/isPreprocess")
    @ApiOperation("判断标注任务是否处于预处理过程中")
    public CommonResult<String> isPreprocess(int taskId) {
        Boolean preprocess = this.labelTaskService.isPreprocess(taskId);
        if (preprocess)
            return new CommonResult<String>().success().message("任务正在进行预处理");
        else
            return new CommonResult<String>().success().message("没有可以继续申领的任务了");
    }

    @ApiOperation("保存/提交标注信息")
    @PostMapping(value = "/commit")
    @Log(value = "保存/提交标注信息")
    public CommonResult<String> commit(@RequestBody LabelCommitInfo labelCommitInfo) {
        this.labelTaskService.commitLabelInfo(labelCommitInfo);
        return new CommonResult<String>().message("保存/提交标注信息成功").success();
    }

    @ApiOperation("删除标注结果")
    @PutMapping(value = "/label/delete")
    @Log(value = "删除标注结果")
    public CommonResult<String> deleteLabelInfo(int labelDataFileId, int labelType) {
        this.labelTaskService.deleteLabelInfo(labelDataFileId, labelType);
        return new CommonResult<String>().message("删除标注结果成功").success();
    }

    @GetMapping(value = "/file")
    @Log(value = "获取标注、审核任务关联文件信息")
    @ApiOperation("获取标注、审核任务关联文件信息")
    public CommonResult<PageResult<LabelTaskFileInfo>> getTaskImageStatusInfo(LabelTaskFileSearchRequest labelTaskFileSearchRequest) throws ResourceAccessException {
        PageResult<LabelTaskFileInfo> pageResult = this.labelTaskService.getTaskFileStatusInfos(labelTaskFileSearchRequest);
        return new CommonResult<PageResult<LabelTaskFileInfo>>().success().data(pageResult).message("获取标注、审核任务关联数据信息成功");
    }

    @GetMapping(value = "/progress")
    @Log(value = "获取标注任务进度信息")
    @ApiOperation("获取标注任务进度信息")
    public CommonResult<LabelTaskProgress> getTaskProgress(@Min(value = 1, message = "任务id必须为正整数") int labelTaskId) {
        LabelTaskProgress labelTaskProgress = this.labelTaskService.getLabelTaskProgress(labelTaskId);
        return new CommonResult<LabelTaskProgress>().success().data(labelTaskProgress).message("获取标注任务进度成功");
    }

    @Log("导入文件")
    @PostMapping(value = "/importLabel")
    @ApiOperation("导入xml标注信息文件")
    public CommonResult<String> importFile(String filePath, LabelPointType labelPointType, MultipartFile file, DatasetType fileType) throws Exception {
        String json = labelTaskService.importLabelFile(filePath, labelPointType, file, fileType);
        return new CommonResult<String>().success().data(json).message("导入文件成功!");
    }

//    @Log("导出文件")
//    @PostMapping(value = "/exportLabel")
//    @ApiOperation("导出xml或vif标注信息文件")
//    public void exportFile(@RequestBody LabelTaskImageStatusInfo labelTaskImageStatusInfo, HttpServletResponse response) throws UnsupportedEncodingException, DocumentException {
//        labelTaskService.exportLabelFile(labelTaskImageStatusInfo,response);
//    }

    @Log("导出文件")
    @PostMapping(value = "/exportLabel")
    @ApiOperation("导出xml或vif标注信息文件")
    public void exportFile(@RequestBody LabelExportParam labelExportParam) throws UnsupportedEncodingException, DocumentException {
        labelTaskService.exportLabelFile(labelExportParam);
    }

    @Log(value = "获取标注任务人员统计信息")
    @GetMapping(value = "/getLabelTaskStatisInfoByUserIds")
    public CommonResult<PageResult<LabelTaskStatisInfo>> getLabelTaskStatisInfoByUserIds(
            @RequestParam(value = "userIdList") List<Integer> userIdList,
            int userRole,
            int projectId,
            LabelTaskSearchRequest labelTaskSearchRequest) {
        PageResult<LabelTaskStatisInfo> result = this.labelTaskService.getLabelTaskStatisInfoByUserIds(
                userIdList, userRole, projectId, labelTaskSearchRequest);
        return new CommonResult<PageResult<LabelTaskStatisInfo>>().success().data(result).message("分页获取标注任务统计信息成功！");
    }

    @Log(value = "获取各标注任务各种状态统计饼形图数据")
    @GetMapping(value = "/getLabelTaskStatusRate")
    public CommonResult<List<HashMap<String, String>>> getLabelTaskStatusRate(int projectId) {
        List<HashMap<String, String>> result = this.labelTaskService.getLabelTaskStatusRate(projectId);
        return new CommonResult<List<HashMap<String, String>>>().success().data(result).message("获取各标注任务各种状态统计数据成功！");
    }

    @Log(value = "获取任意位置后的固定帧视频影像数据")
    @GetMapping(value = "/getFrameImgByFrameNbr")
    public CommonResult<List<VideoFrame>> getFrameImgByFrameNbr(String videoPath, int frameNumber, int returnNumber) {
        List<VideoFrame> result = this.labelTaskService.getFrameImgByFrameNbr(videoPath, frameNumber, returnNumber);
        return new CommonResult<List<VideoFrame>>().message("获取视频帧数据成功").data(result).setCode(0);
    }
}
