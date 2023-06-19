package cn.iecas.geoai.labelplatform.service.labelFileService;

import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFileInfo;
import cn.iecas.geoai.labelplatform.entity.dto.LabelExportParam;
import cn.iecas.geoai.labelplatform.entity.dto.LabelTaskFileInfo;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface LabelFileService {
    String exportXML(LabelExportParam labelExportParam);
    String importLabelXML(String filePath, LabelPointType labelPointType, MultipartFile file);
    String importLabelFromXzFile(String filePath, LabelPointType labelPointType, File file);
    String createManifest(LabelDataset labelDataset, List<LabelDatasetFile> labelDatasetFileList);

}
