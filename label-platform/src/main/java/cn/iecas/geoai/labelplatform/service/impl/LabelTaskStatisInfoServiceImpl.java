package cn.iecas.geoai.labelplatform.service.impl;

import cn.iecas.geoai.labelplatform.dao.LabelTaskStatisInfoMapper;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTaskStatisInfo;
import cn.iecas.geoai.labelplatform.service.LabelTaskStatisInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelTaskStatisInfoServiceImpl extends ServiceImpl<LabelTaskStatisInfoMapper, LabelTaskStatisInfo> implements LabelTaskStatisInfoService {
    @Override
    public void deleteTaskStatisByProjectId(List<Integer> projectIdList) {
        QueryWrapper<LabelTaskStatisInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("label_project_id", projectIdList);
        this.remove(queryWrapper);
    }

    @Override
    public void deleteByProjectIdAndUser(int projectId, int userId, int userRole) {
        QueryWrapper<LabelTaskStatisInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("label_project_id", projectId).eq("user_id", userId)
        .eq("user_role", userRole);
        this.remove(queryWrapper);
    }
}
