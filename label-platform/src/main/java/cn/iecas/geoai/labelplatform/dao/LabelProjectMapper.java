package cn.iecas.geoai.labelplatform.dao;

import cn.iecas.geoai.labelplatform.entity.domain.LabelProject;
import cn.iecas.geoai.labelplatform.entity.dto.LabelProjectRelateFileSTCInfo;
import cn.iecas.geoai.labelplatform.entity.dto.LabelProjectRelateFileSTCRequest;
import cn.iecas.geoai.labelplatform.entity.dto.LabelProjectSearchRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LabelProjectMapper extends BaseMapper<LabelProject> {
    void updateProjectProgress(int projectId);
    IPage<LabelProject> getLabelProject(Page<LabelProject> iPage, @Param(value = "labelProjectSearchRequest") LabelProjectSearchRequest labelProjectSearchRequest);
    List<String> getMergeProjectRelateDatasetId(@Param(value = "labelProjectIds") List<Integer> labelProjectIds);
    LabelProjectRelateFileSTCRequest getProjectRelateFileByPid(@Param(value = "params") LabelProjectRelateFileSTCRequest params);
    LabelProjectRelateFileSTCRequest getProjectFileSelectConditon(@Param(value = "params") LabelProjectRelateFileSTCRequest params);
    LabelProjectRelateFileSTCRequest getProjectRelateFileByLon(@Param(value = "params") LabelProjectRelateFileSTCRequest params);
    List<LabelProjectRelateFileSTCRequest> getProjectFilesByPid(int projectId);

    void updateKeyWords(int labelProjectId, String keywords);
}
