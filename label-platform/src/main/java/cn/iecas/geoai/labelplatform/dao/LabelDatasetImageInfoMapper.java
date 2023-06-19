package cn.iecas.geoai.labelplatform.dao;

import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFileInfo;
import cn.iecas.geoai.labelplatform.entity.dto.LabelDatasetFileRequest;
import cn.iecas.geoai.labelplatform.entity.dto.LabelTaskFileSearchRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LabelDatasetImageInfoMapper extends BaseMapper<LabelDatasetFileInfo> {
    IPage<LabelDatasetFileInfo> listDatasetImageInfos(Page<LabelDatasetFileInfo> pageInfo, @Param(value = "labelDatasetImageRequest") LabelDatasetFileRequest labelDatasetFileRequest);
    IPage<LabelDatasetFileInfo> listTaskImageInfos(Page<LabelDatasetFileInfo> pageInfo, @Param(value = "labelTaskImageSearchRequest") LabelTaskFileSearchRequest labelTaskFileSearchRequest);


}
