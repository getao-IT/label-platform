package cn.iecas.geoai.labelplatform.service.impl;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import cn.iecas.geoai.labelplatform.service.FavoriteCollectionService;
import cn.iecas.geoai.labelplatform.service.FileService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据集收藏夹服务
 */
@Service
public class FavoriteCollectionServiceImpl implements FavoriteCollectionService {

    @Autowired
    private FileService fileService;


    //收藏夹id集合
    private Map<Integer,List<Integer>> favoriteCollection = new HashMap<>();

    /**
     * 获取收藏夹数据
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageResult<JSONObject> getFavoriteCollection(int userId, int pageNo, int pageSize) {
        List<JSONObject> fileInfoList;
        List<Integer> favoriteFileIdList = this.favoriteCollection.get(userId);
        if (favoriteFileIdList == null || favoriteFileIdList.isEmpty())
            return new PageResult<>(1,0,null);
        int size = favoriteFileIdList.size();
        int beginIndex = Math.min((pageNo - 1) * pageSize, size-1);
        int endIndex = Math.min(beginIndex + pageSize,size);
        //endIndex = beginIndex == endIndex ? endIndex+1 : endIndex;
        List<Integer> favoriteIdList = favoriteFileIdList.subList(beginIndex,endIndex);
        fileInfoList = this.fileService.listFileInfoByIdList(favoriteIdList, DatasetType.IMAGE,null);
        return new PageResult<>(pageNo,size,fileInfoList);
    }

    /**
     * 清空收藏夹
     */
    @Override
    public void cleanFavoriteCollection(int userId) {
        if (this.favoriteCollection.containsKey(userId))
            this.favoriteCollection.clear();
    }

    /**
     * 将文件id加入收藏夹
     * @param fileIdList
     */
    @Override
    public void addFavoriteCollection(int userId, List<Integer> fileIdList) {
        List<Integer> favoriteFileIDList = this.favoriteCollection.getOrDefault(userId,new ArrayList<>());
        for (Integer fileId : fileIdList) {
            if (!favoriteFileIDList.contains(fileId))
                favoriteFileIDList.add(fileId);
        }

        this.favoriteCollection.putIfAbsent(userId,favoriteFileIDList);
    }

    /**
     * 判断收藏夹是否包含该文件
     * @param fileId
     * @return
     */
    @Override
    public boolean isInFavoriteCollection(int userId, int fileId) {
        if (this.favoriteCollection.containsKey(userId)){
            return this.favoriteCollection.get(userId).contains(fileId);
        }
        return false;
    }

    /**
     * 将文件从收藏夹中删除
     * @param fileIdList
     */
    @Override
    public void deleteFromFavoriteCollection(int userId, List<Integer> fileIdList) {
        List<Integer> favoriteFileIdList = this.favoriteCollection.get(userId);
        if (favoriteFileIdList!=null){
            for (Integer fileId : fileIdList) {
                int index = favoriteFileIdList.indexOf(fileId);
                if (index >=0)
                    favoriteFileIdList.remove(index);
            }
        }
    }
}
