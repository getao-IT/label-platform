package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.common.PageResult;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FavoriteCollectionService {

    void cleanFavoriteCollection(int userId);
    boolean isInFavoriteCollection(int userId, int fileId);
    void addFavoriteCollection(int userId, List<Integer> fileIdList);
    void deleteFromFavoriteCollection(int userId, List<Integer> fileIdList);
    PageResult<JSONObject> getFavoriteCollection(int userId, int pageNo, int pageSize);

}
