package cn.iecas.geoai.labelplatform.dao;

import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
public interface LabelDatasetFileMapper extends BaseMapper<LabelDatasetFile> {
    void setLabelUserId(int labelUserId, int datasetId, List<Integer> fileIdList);
    void setCheckUserId(int checkUserId, int datasetId, List<Integer> fileIdList);
    List<Integer> getFileByStatusRandomly(int datasetId, LabelStatus status, int count);
    void removeAllByDatasetIds(@Param(value = "datasetIdList") List<String> datasetIdList);
    List<Integer> getRepeatDatasetFile(@Param(value = "datasetIdList") List<Integer> datasetIdList);
    List<Map<String, Long>> getLabelTaskStatusRate(int projectId);
    Timestamp getFristAssignCheckTimeByDatasetid(@Param("labelDataSetId") int labelDataSetId,
                                                 @Param("checkUserId") int checkUserId);
    Timestamp getFristAssignLabelTimeByDatasetid(@Param("labelDataSetId") int labelDataSetId,
                                                 @Param("labelUserId") int labelUserId);

    Timestamp getLastFinishCheckTimeByDatasetid(@Param("labelDataSetId") int labelDataSetId,
                                                @Param("checkUserId") int checkUserId);
    Timestamp getLastFinishLabelTimeByDatasetid(@Param("labelDataSetId") int labelDataSetId,
                                                @Param("labelUserId") int labelUserId);
}
