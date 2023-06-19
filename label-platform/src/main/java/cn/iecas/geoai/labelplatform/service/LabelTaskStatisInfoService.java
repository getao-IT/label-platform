package cn.iecas.geoai.labelplatform.service;

import cn.iecas.geoai.labelplatform.entity.domain.LabelTaskStatisInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

public interface LabelTaskStatisInfoService extends IService<LabelTaskStatisInfo> {
    void deleteTaskStatisByProjectId(List<Integer> projectIdList);
    void deleteByProjectIdAndUser(int projectId, int userId, int userRole);
}
