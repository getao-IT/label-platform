package cn.iecas.geoai.labelplatform.service.labelFileService.video;

import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.dto.LabelExportParam;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.service.FileService;
import cn.iecas.geoai.labelplatform.service.labelFileService.LabelFileService;
import cn.iecas.geoai.labelplatform.service.labelFileService.audio.AudioManifest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service("VIDEO-LABEL-FILE-SERVICE")
public class VideoLabelFileService implements LabelFileService {
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

    @Autowired
    private FileService fileService;

    @Override
    public String createManifest(LabelDataset labelDataset, List<LabelDatasetFile> labelDatasetFileList) {
        VideoManifest videoManifest = new VideoManifest();
        videoManifest.setDatasetInfo(labelDataset);

        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            JSONObject videoInfo = labelDatasetFile.getData();
            int fileId = videoInfo.getInteger("id");
            String source = videoInfo.getString("source");
            String videoName = videoInfo.getString("videoName");
            String path = videoInfo.getString("path");
            JSONObject labelJSON = JSONObject.parseObject(labelDatasetFile.getLabel());
            videoManifest.addData(fileId,videoName,source,path,labelJSON);
        }
        return JSON.toJSONString(videoManifest, SerializerFeature.WriteMapNullValue, SerializerFeature.PrettyFormat);
    }
}
