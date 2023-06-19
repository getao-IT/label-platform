package cn.iecas.geoai.labelplatform.dao;

import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.dto.LabelDatasetsSearchRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * (LabelDatasets)表数据库访问层
 *
 * @author vanishrain
 * @since 2020-05-10 10:28:20
 */
@Repository
public interface LabelDatasetMapper extends BaseMapper<LabelDataset> {
}