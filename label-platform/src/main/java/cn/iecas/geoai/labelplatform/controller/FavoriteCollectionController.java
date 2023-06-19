package cn.iecas.geoai.labelplatform.controller;


import cn.iecas.geoai.labelplatform.aop.annotation.Log;
import cn.iecas.geoai.labelplatform.entity.common.CommonResult;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.service.FavoriteCollectionService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "收藏夹管理接口")
@RequestMapping(value = "/favorite")
public class FavoriteCollectionController {

    @Autowired
    private FavoriteCollectionService favoriteCollectionService;

    @Log(value = "获取创建数据集的收藏夹列表")
    @GetMapping
    public CommonResult<PageResult<JSONObject>> getFavoriteCollection(int userId, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "10")int pageSize) {
        PageResult<JSONObject> pageResult = this.favoriteCollectionService.getFavoriteCollection(userId,pageNo,pageSize);
        return new CommonResult<PageResult<JSONObject>>().data(pageResult).success().message("获取用户数据集收藏夹成功");
    }

    @Log(value = "用户将文件添加到收藏夹")
    @PostMapping
    public CommonResult<String> addFavoriteFile(int userId, @RequestParam("fileIdList") List<Integer> fileIdList){
        this.favoriteCollectionService.addFavoriteCollection(userId,fileIdList);
        return new CommonResult<String>().success().message("用户数据集添加收藏夹成功");
    }

    @Log(value = "用户将文件从收藏夹移除")
    @DeleteMapping
    public CommonResult<String> deleteFavoriteFile(int userId, @RequestParam("fileIdList") List<Integer> fileIdList){
        this.favoriteCollectionService.deleteFromFavoriteCollection(userId,fileIdList);
        return new CommonResult<String>().success().message("从用户数据集收藏夹删除成功");
    }

    @Log(value = "清空用户数据集收藏夹")
    @DeleteMapping("/clear")
    public CommonResult<String> clearFavoriteCollection(int userId){
        this.favoriteCollectionService.cleanFavoriteCollection(userId);
        return new CommonResult<String>().success().message("清空用户数据集收藏夹成功");
    }

//    public CommonResult<>


}
