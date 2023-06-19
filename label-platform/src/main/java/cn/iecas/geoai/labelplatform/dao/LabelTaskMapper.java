package cn.iecas.geoai.labelplatform.dao;

import cn.iecas.geoai.labelplatform.entity.domain.LabelTask;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTaskStatisInfo;
import cn.iecas.geoai.labelplatform.entity.dto.LabelTaskInfo;
import cn.iecas.geoai.labelplatform.entity.dto.LabelTaskSearchRequest;
import cn.iecas.geoai.labelplatform.entity.emun.LabelTaskType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface LabelTaskMapper extends BaseMapper<LabelTask> {
    int getLabelTaskProgress(int labelTaskId);
    int getCheckTaskProgress(int checkTaskId);
    IPage<LabelTaskInfo> listLabelTaskInfos(Page<LabelTaskInfo> page, @Param(value = "labelTaskSearchRequest") LabelTaskSearchRequest labelTaskSearchRequest);
    IPage<LabelTaskStatisInfo> getLabelTaskStatisInfoByUserIds(Page<LabelTaskStatisInfo> page,
                                                               @Param("userIdList") List<Integer> userIdList,
                                                               @Param("userRole") int userRole,
                                                               @Param("projectId") int projectId);
}
