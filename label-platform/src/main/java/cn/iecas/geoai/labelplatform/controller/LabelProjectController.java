package cn.iecas.geoai.labelplatform.controller;

import cn.iecas.geoai.labelplatform.aop.annotation.Log;
import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.*;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import cn.iecas.geoai.labelplatform.service.LabelDatasetService;
import cn.iecas.geoai.labelplatform.service.LabelProjectService;
import cn.iecas.geoai.labelplatform.service.UserInfoService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.DocumentException;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;


@Slf4j
@Validated
@Api(tags = "标注项目管理接口")
@RestController
@RequestMapping("/project")
public class LabelProjectController {

    @Autowired
    private LabelProjectService labelProjectService;

    @Autowired
    LabelDatasetService labelDatasetService;


    @GetMapping
    @Log(value = "获取标注项目列表")
    @ApiOperation("获取标注项目列表")
    public CommonResult<PageResult<LabelProject>> getProjects(LabelProjectSearchRequest labelProjectSearchRequest){
        PageResult<LabelProject> labelProjectList = labelProjectService.getLabelProject(labelProjectSearchRequest);
        return new CommonResult<PageResult<LabelProject>>().success().message("获取标注项目信息成功").data(labelProjectList);
    }

    @PostMapping("/updateLabelInfo")
    @Log(value = "更新标注项目信息")
    @ApiOperation("更新标注项目信息")
    public CommonResult<String> updateLabelProject(@RequestBody LabelProject labelProject){
        this.labelProjectService.updateLabelProject(labelProject);
        return new CommonResult<String>().success().message("更新标注项目信息成功！");
    }


    @PostMapping("/updateProjectUser")
    @Log(value = "更新标注项目用户")
    @ApiOperation("更新标注项目用户")
    public CommonResult<String> updateLabelProjectUser(@RequestBody LabelProjectUpdateUserInfo labelProjectUpdateUserInfo){
        this.labelProjectService.updateLabelProjectUser(labelProjectUpdateUserInfo);
        return new CommonResult<String>().success().message("更新标注项目用户成功！");
    }


    @Log(value = "删除标注项目")
    @ApiOperation("删除标注项目")
    @DeleteMapping(value = "/{projectIdList}")
    public CommonResult<String> deleteProjects(@PathVariable(value = "projectIdList") @NotEmpty(message = "项目id列表不能为空") List<Integer> projectIdList){
        labelProjectService.deleteLabelProject(projectIdList);
        return new CommonResult<String>().success().message("删除标注项目成功");
    }


    @PostMapping
    @ApiOperation("创建新的标注项目")
    @Log(value = "创建新的标注项目")
    public CommonResult<String> createLabelProject(@RequestBody LabelProject labelProject, HttpServletRequest request){
        //labelProject.setCooperate(true);
        this.labelProjectService.createLabelProject(labelProject,request);
        return new CommonResult<String>().success().message("创建新的标注项目成功");
    }


    @GetMapping("/isExist")
    @ApiOperation("验证要创建项目的项目名称是否存在")
    @Log("验证要创建项目的项目名称是否存在")
    public CommonResult<Boolean> isExistLabelProject(String projectName,/*@Min(value = 0,message = "userId必须为正整数")*/ int userId){
        Boolean exist = labelProjectService.isExistLabelProject(projectName,userId);
        return new CommonResult<Boolean>().data(exist).success().message("查看项目名称是否存在成功");
    }


    //生成样本集
    @PostMapping("/sampleset")
    @Log(value = "生成样本集")
    @ApiOperation("生成样本集")
    public CommonResult<String> createSamplesetResult(@RequestBody SampleSetCreationInfo sampleSetCreationInfo) throws IOException {
        sampleSetCreationInfo.setDatasetId(labelProjectService.getById(sampleSetCreationInfo.getProjectId()).getDatasetId());
        this.labelProjectService.createDatasetResult(sampleSetCreationInfo.getProjectId());
        this.labelDatasetService.createSampleSet(sampleSetCreationInfo);
        return new CommonResult<String>().success().message("生成结果数据集成功");
    }


    @GetMapping("/manifest")
    @Log(value = "生成结果数据集")
    @ApiOperation("生成结果数据集")
    public CommonResult<String> createDatasetResult(@Min(value = 1,message = "项目id必须为正整数")int labelProjectId) throws IOException {
        this.labelProjectService.createDatasetResult(labelProjectId);
        return new CommonResult<String>().success().message("生成结果数据集成功");
    }


    @Log("从我标注的任务导出标注文件")
    @GetMapping(value = "/exportLabelFileToZip")
    @ApiOperation("从我标注的任务导出标注文件")
    public void exportLabelFileToZip(int projectId, HttpServletResponse response) throws IOException {
        labelProjectService.exportLabelFileToZip(projectId,response);
    }


    @PostMapping("/importKeyword")
    @Log(value = "解析txt文件中的keyword")
    @ApiOperation("解析txt文件中的keyword")
    public CommonResult<List<String>> importKeywords(MultipartFile file) throws IOException, JDOMException, DocumentException {
        List<String> tags =  labelProjectService.importKeywords(file);
        return new CommonResult<List<String>>().success().data(tags).message("解析txt文件中的keyword成功");
    }


    /**
     * 根据传入项目的数据集ID，获取同类型所有项目
     * @param projectId
     * @return
     */
    @GetMapping("/getProjectByDataSetType")
    @Log("根据传入项目的数据集ID，获取同类型所有项目")
    public CommonResult<List<LabelProject>> getProjectByDataSetType(int projectId) {
        List<LabelProject> result = this.labelProjectService.getProjectByDataSetType(projectId);
        return new CommonResult<List<LabelProject>>().data(result).success().message("获取同类型标注项目成功");
    }


    @ApiOperation("创建合并的标注任务")
    @Log("获取合并任务的文件")
    @PostMapping("/createProjectByFileIsRepeat")
    public CommonResult<List<Integer>> createMergeProjectProject(int srcProjectId,
                                                                 @RequestParam List<Integer> mergeProjectIds,
                                                                  boolean isMerge,
                                                                  String projectName,
                                                                  String projectDescription) {
        List<Integer> result = labelProjectService.createProjectByFileIsRepeat(srcProjectId, mergeProjectIds, isMerge, projectName, projectDescription);
        return new CommonResult<List<Integer>>().success().data(result)
                .message(result.size() == 0 ? "合并标注项目成功！" : "检测到重复文件并合并成功！");
    }


    @ApiOperation("创建合并的标注任务")
    @Log("有重复文件情况下，创建合并的标注任务")
    @PostMapping("/createMergeProject")
    public CommonResult<List<LabelDatasetFile>> createMergeProjectProject(String projectName, @RequestParam List<Integer> fileIdList,
                                                                          @RequestParam List<Integer> projectIdList) {
        List<LabelProject> projectList = this.labelProjectService.list(new QueryWrapper<LabelProject>()
                .in("id", projectIdList));
        UserInfo userInfo = new UserInfo();
        userInfo.setId(projectList.get(0).getUserId());
        userInfo.setName(projectList.get(0).getUserName());
        //this.labelProjectService.createMergeProject(projectName, fileIdList, projectIdList, userInfo);
        return new CommonResult<List<LabelDatasetFile>>().success().data(null).message("合并标注项目成功！");
    }


    @Log(value = "更新标注任务样本产出量")
    @PutMapping(value = "/updateMakeSampleNum")
    public CommonResult<Object> updateMakeSampleNum(int projectId, int nums) {
        this.labelProjectService.updateSampleSetNumById(projectId, nums);
        return new CommonResult<Object>().success().data(null).message("更新标注任务样本产出量成功");
    }


    @Log(value = "获取同目标标注任务使用同一真实数据集的标注项目")
    @GetMapping(value = "/getProjectsByRelateDataset")
    public CommonResult<List<LabelProject>> getProjectsByRelateDataset(int projectId, String relatedDatasetId) {
        List<LabelProject> result = this.labelProjectService.getProjectsByRelateDataset(projectId, relatedDatasetId);
        return new CommonResult<List<LabelProject>>().success().data(result).message("获取重叠数据集项目成功");
    }

    /**
     * 时空覆盖率：以不同统计标准返回对应标准影像个数
     * 统计标准：时间，经度
     * @param params
     * @return
     */
    @Log(value = "时空覆盖率：以不同统计标准返回对应标准影像个数")
    @GetMapping("getProjectSTCByStandard")
    public CommonResult<List<LabelProjectRelateFileSTCInfo>> getProjectSTCByStandard(LabelProjectRelateFileSTCRequest params) {
        List<LabelProjectRelateFileSTCInfo> result = this.labelProjectService.getProjectSTCByStandard(params);
        return new CommonResult<List<LabelProjectRelateFileSTCInfo>>().data(result)
                .message(result==null || result.size()==0?"标注任务没有数据文件" : "获取时空覆盖性统计数据成功");
    }


    /**
     * 时空覆盖率：热力图
     * 统计标准：经度，纬度
     * @param params
     * @return
     */
    @Log(value = "时空覆盖率：热力图")
    @GetMapping("getProjectSTCToHeatmap")
    public CommonResult<List<JSONObject>> getProjectSTCToHeatmap(LabelProjectRelateFileSTCRequest params) {
        List<JSONObject> result = this.labelProjectService.getProjectSTCToHeatmap(params);
        return new CommonResult<List<JSONObject>>().data(result)
                .message(result.size()==0?"标注任务没有数据文件" : "获取影像分布数据成功");
    }


    /**
     * 根据标注文件ID，获取变化检测文件信息
     * @return
     */
    @Log(value = "根据标注文件ID，获取变化检测文件信息")
    @GetMapping("/getChangeDetectionByFileId")
    public CommonResult<JSONObject> getChangeDetectionById(int id) {
        JSONObject image = this.labelProjectService.getChangeDetectionFileById(id);
        return new CommonResult<JSONObject>().data(image).success().message("获取变化检测关联文件成功");
    }


    /**
     * 根据标注文件ID，更新变化检测文件信息
     * @return
     */
    @Log(value = "根据标注文件ID，更新变化检测文件信息")
    @PutMapping("/updateLabelFileById")
    public CommonResult<LabelDatasetFile> updateLabelFileById(int id, int relatedFileId) {
        this.labelProjectService.updateLabelFileById(id, relatedFileId);
        return new CommonResult<LabelDatasetFile>().success().message("更新变化检测关联文件信息成功");
    }

    @Log(value = "变化检测时对样本集合进行简单分组")
    @PostMapping("/group")
    public CommonResult<List<DeteSampleVo>> group(@RequestBody List<JSONObject> sampleVos) {
            Assert.isTrue(CollectionUtils.isEmpty(sampleVos), "样本信息不能为空!");
        Assert.isTrue(sampleVos.size() % 2 != 0, "样本数必须为偶数集合!");
        return new CommonResult<List<DeteSampleVo>>().success().data(this.labelProjectService.listDeteSamples(sampleVos));
    }
}
