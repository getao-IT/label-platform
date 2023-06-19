package cn.iecas.geoai.labelplatform.service.labelFileService.audio;

import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.dto.LabelExportParam;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.service.labelFileService.LabelFileService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service("AUDIO-LABEL-FILE-SERVICE")
public class AudioLabelFileService implements LabelFileService {
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
        AudioManifest audioManifest = new AudioManifest();
        audioManifest.setDatasetInfo(labelDataset);

        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            JSONObject audioInfo = labelDatasetFile.getData();
            int fileId = audioInfo.getInteger("id");
            String source = audioInfo.getString("source");
            String audioName = audioInfo.getString("audioName");
            JSONObject labelJSON = JSONObject.parseObject(labelDatasetFile.getLabel()).getJSONObject("object");
            audioManifest.addData(fileId,audioName,source,labelJSON);
        }
        return JSON.toJSONString(audioManifest, SerializerFeature.WriteMapNullValue, SerializerFeature.PrettyFormat);
    }
}
