package cn.iecas.geoai.labelplatform.service.labelFileService.text;


import cn.aircas.utils.file.FileUtils;
import cn.iecas.geoai.labelplatform.entity.domain.Image;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.dto.LabelExportParam;
import cn.iecas.geoai.labelplatform.entity.emun.CoordinateConvertType;
import cn.iecas.geoai.labelplatform.entity.emun.CoordinateSystemType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.entity.fileFormat.LabelObject;
import cn.iecas.geoai.labelplatform.entity.fileFormat.XMLLabelObjectInfo;
import cn.iecas.geoai.labelplatform.service.labelFileService.LabelFileService;
import cn.iecas.geoai.labelplatform.service.labelFileService.image.ImageManifest;
import cn.iecas.geoai.labelplatform.util.LabelPointTypeConvertor;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service("TEXT-LABEL-FILE-SERVICE")
public class TextLabelFileService implements LabelFileService {
    @Override
    public String exportXML(LabelExportParam labelExportParam) {
        return null;
    }

    @Override
    public String importLabelXML(String filePath, LabelPointType labelPointType, MultipartFile file) {
        return null;
    }

    @Override
    public String importLabelFromXzFile(String filePath, LabelPointType labelPointType, File file) {
        return null;
    }

    @Override
    public String createManifest(LabelDataset labelDataset, List<LabelDatasetFile> labelDatasetFileList) {
        TextManifest textManifest = new TextManifest();
        textManifest.setDatasetInfo(labelDataset);

        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            JSONObject textInfo = labelDatasetFile.getData();
            int fileId = textInfo.getInteger("id");
            String textName = textInfo.getString("textName");
            String source = textInfo.getString("textSource");
            JSONObject labelJSON = JSONObject.parseObject(labelDatasetFile.getLabel()).getJSONObject("object");
            textManifest.addData(fileId,textName,source,labelJSON);
        }
        return JSON.toJSONString(textManifest, SerializerFeature.WriteMapNullValue, SerializerFeature.PrettyFormat);
    }
}
