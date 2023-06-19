package cn.iecas.geoai.labelplatform.controller;

import cn.iecas.geoai.labelplatform.aop.annotation.Log;
import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.dto.*;
import cn.iecas.geoai.labelplatform.service.LabelDatasetService;
import cn.iecas.geoai.labelplatform.util.CollectionsUtils;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Validated
@Lazy
@RestController
@Api(tags = "数据集管理接口")
@RequestMapping(value = "/dataset")
public class DatasetController {
    @Autowired
    LabelDatasetService labelDatasetService;

    /**
     * 根据条件分页查询数据集信息
     * @param labelDatasetsSearchRequest 数据集信息查询参数
     * @return 数据集信息内容
     */
    @GetMapping
    @ApiOperation("获取数据集信息")
    @Log(value = "获取数据集信息")
    public CommonResult<PageResult<LabelDataset>> getLabelDatasets(@Validated LabelDatasetsSearchRequest labelDatasetsSearchRequest) {
        PageResult<LabelDataset> pageResult = this.labelDatasetService.getLabelDatasetInfo(labelDatasetsSearchRequest);
        return new CommonResult<PageResult<LabelDataset>>().data(pageResult).success().message("查询数据集信息成功");
    }

    /*@ApiOperation("获取用户所有数据集的名称")
    @Log(value = "获取用户所有数据集的名称")
    @GetMapping(value = "/name")
    public CommonResult<List<Map.Entry<Integer,String>>> getLabelDatasetNames(*//*@Min(value = 0,message = "userId必须为正整数") *//*int userId, DatasetType datasetType){
        Map<Integer,String> datasetNameList = this.labelDatasetService.getDatasetNameList(userId,datasetType);
        datasetNameList = CollectionsUtils.sortMapByNumKey(datasetNameList, CollectionsUtils.SORT_DESC);
        List<Map.Entry<Integer, String>> result = CollectionsUtils.parseMapToList(datasetNameList);
        return new CommonResult<List<Map.Entry<Integer,String>>>().data(result).success().message("查询数据集名称成功");
    }*/

    @ApiOperation("获取用户所有数据集的名称")
    @Log(value = "获取用户所有数据集的名称")
    @GetMapping(value = "/name")
    public CommonResult<Map<Integer,String>> getLabelDatasetNames(/*@Min(value = 0,message = "userId必须为正整数") */int userId, DatasetType datasetType){
        Map<Integer,String> datasetNameList = this.labelDatasetService.getDatasetNameList(userId,datasetType);
        return new CommonResult<Map<Integer,String>>().data(datasetNameList).success().message("查询数据集名称成功");
    }

    @Log(value = "创建数据集")
    @PostMapping
    @ApiOperation("创建数据集")
    public CommonResult<String> createLabelDataset(@RequestBody LabelDataset labelDataset) throws IOException {
        labelDatasetService.createLabelDatasetInfo(labelDataset);
        return new CommonResult<String>().success().message("创建数据集成功");
    }

    @Log(value = "验证要创建项目的数据集名称是否存在")
    @GetMapping("/isExist")
    @ApiOperation("验证要创建项目的项目名称是否存在")
    public CommonResult<Boolean> isExistDataset(String datasetName,/*@Min(value = 0,message = "userId必须为正整数")*/ int userId){
        Boolean exist = labelDatasetService.isExistDataset(datasetName,userId);
        return new CommonResult<Boolean>().data(exist).success().message("查看数据集名称是否存在成功");
    }

    @ApiOperation("删除数据集")
    @Log(value = "删除数据集")
    @DeleteMapping(value = "/{datasetIdList}")
    public CommonResult<String> deleteLabelDataset(@PathVariable("datasetIdList") List<Integer> datasetIdList){
        labelDatasetService.deleteLabelDataset(datasetIdList);
        return new CommonResult<String>().success().message("删除数据集成功");
    }

    @ApiOperation("分页获取数据集所关联的文件数据")
    @Log(value = "分页获取数据集所关联的文件数据")
    @GetMapping(value = "/file")
    public CommonResult<PageResult<LabelDatasetOrProjectFileInfo>> getImageFromDataset(LabelDatasetFileRequest labelDatasetFileRequest) throws ResourceAccessException, IOException {
        PageResult<LabelDatasetOrProjectFileInfo> imagePageResult = this.labelDatasetService.getFileInfoFromDataset(labelDatasetFileRequest);
        return new CommonResult<PageResult<LabelDatasetOrProjectFileInfo>>().success().message("获取数据集影像成功").data(imagePageResult);
    }

    @ApiOperation("获取数据集的manifest文件")
    @GetMapping(value = "/manifest")
    @Log(value = "获取数据集的manifest文件")
    public void getManifest(@Min(value = 1,message = "数据集id必须为正整数") int datasetId, HttpServletResponse httpServletResponse) throws IOException {
        this.labelDatasetService.getManifest(datasetId,httpServletResponse);
    }

    @ApiOperation("生成数据集的样本集合")
    @Log(value = "生成数据集的样本集合")
    @PostMapping(value = "/sampleset")
    public CommonResult<String> createSampleSet(@RequestBody SampleSetCreationInfo sampleSetCreationInfo) throws IOException {
        this.labelDatasetService.createSampleSet(sampleSetCreationInfo);
        return new CommonResult<String>().success().message("样本集生成成功");
    }

    @Log(value = "创建数据集并创建标注项目")
    @PostMapping(value = "/createDatasetAndProject")
    public CommonResult<String> createDatasetAndProject(@RequestBody LabelDataset labelDataset) throws IOException {
        this.labelDatasetService.createLabelDatasetInfo(labelDataset);
        int datasetId = labelDataset.getId();
        this.labelDatasetService.copyLabelInfo(datasetId,labelDataset.getUserId(),labelDataset.getLabelPath());
        return new CommonResult<String>().success().message("创建数据集并创建标注项目");
    }

    @Log(value = "分页获取所有文件信息，用来创建数据集")
    @GetMapping(value = "/fileList")
    public CommonResult<PageResult<JSONObject>> getFileInfoList(FileSearchParam fileSearchParam) {
        PageResult<JSONObject> result = this.labelDatasetService.getFileInfoList(fileSearchParam);
        return new CommonResult<PageResult<JSONObject>>().success().data(result).message("分页获取所有文件信息，用来创建数据集");
    }

    @Log(value = "获取所有文件ID信息，用来创建数据集")
    @GetMapping(value = "/fileIdList")
    public CommonResult<List<Integer>> getAllFileIdList(FileSearchParam fileSearchParam) {
        List<Integer> result = this.labelDatasetService.getAllFileIdList(fileSearchParam);
        return new CommonResult<List<Integer>>().success().data(result).message("获取所有文件ID信息，用来创建数据集");
    }

}
