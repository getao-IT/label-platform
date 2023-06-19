package cn.iecas.geoai.labelplatform.service.labelFileService.image;

import cn.aircas.utils.comporess.CompressUtil;
import cn.aircas.utils.file.FileUtils;
import cn.aircas.utils.image.geo.GeoUtils;
import cn.iecas.geoai.labelplatform.entity.domain.Image;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.dto.LabelExportParam;
import cn.iecas.geoai.labelplatform.entity.emun.CoordinateConvertType;
import cn.iecas.geoai.labelplatform.entity.emun.CoordinateSystemType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelFileType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelPointType;
import cn.iecas.geoai.labelplatform.entity.fileFormat.LabelObject;
import cn.iecas.geoai.labelplatform.entity.fileFormat.PZLabelObjectInfo;
import cn.iecas.geoai.labelplatform.entity.fileFormat.VifLabelOjectInfo;
import cn.iecas.geoai.labelplatform.entity.fileFormat.XMLLabelObjectInfo;
import cn.iecas.geoai.labelplatform.service.FileService;
import cn.iecas.geoai.labelplatform.service.LabelDatasetFileService;
import cn.iecas.geoai.labelplatform.service.labelFileService.LabelFileService;
import cn.iecas.geoai.labelplatform.util.LabelPointTypeConvertor;
import cn.iecas.geoai.labelplatform.util.XMLUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author vanishrain
 * 影像真值文件处理类
 */
@Slf4j
@Service("IMAGE-LABEL-FILE-SERVICE")
public class ImageLabelFileService implements LabelFileService {
    @Value("${value.dir.rootDir}")
    private String rootDir;

    @Autowired
    private FileService fileService;


    @Override
    public String exportXML(LabelExportParam labelExportParam) {
        LabelObject labelObject;
        String label = labelExportParam.getLabel();
        JSONObject labelJSON = JSONObject.parseObject(label);
        JSONObject fileInfo = labelExportParam.getFileInfo();
        Image imageInfo = JSONObject.parseObject(fileInfo.toJSONString(),Image.class);
        String imageName = imageInfo.getImageName();
        String imageFilePath = FileUtils.getStringPath(this.rootDir,imageInfo.getPath());
        if (labelExportParam.getLabelFileType() == LabelFileType.XML){
            labelObject = JSONObject.toJavaObject(labelJSON,XMLLabelObjectInfo.class);
            if (labelExportParam.isRectangle())
                labelObject.toRectangle();
        }

        else {
            if (labelExportParam.getLabelFileType() == LabelFileType.TJ)
                labelObject = convertLabelToPZLabelObjectInfo(labelJSON,fileInfo);
            else
                labelObject = JSONObject.toJavaObject(labelJSON,VifLabelOjectInfo.class);
        }

        labelObject.addFileName(imageName);

        if (!(labelExportParam.getLabelFileType() == LabelFileType.TJ)){
            CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;
            if (CoordinateSystemType.PIXELCS == imageInfo.getCoordinateSystemType())
                coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;
            if (CoordinateSystemType.PROJCS == imageInfo.getCoordinateSystemType())
                coordinateConvertType = CoordinateConvertType.PROJECTION_TO_LONLAT;

            LabelPointTypeConvertor.convertLabelPointType(imageFilePath,labelObject,coordinateConvertType);
        }

        return XMLUtils.toXMLString(labelObject);
    }


//    @Override
//    public String exportXML(LabelExportParam labelExportParam) {
//        LabelObject labelObject;
//        String label = labelExportParam.getLabel();
//        JSONObject labelJSON = JSONObject.parseObject(label);
//        //JSONObject fileInfo = labelExportParam.getFileInfo();
//        //Image imageInfo = JSONObject.parseObject(fileInfo.toJSONString(),Image.class);
//        //String imageName = imageInfo.getImageName();
//        //String imageFilePath = FileUtils.getStringPath(this.rootDir,imageInfo.getPath());
//        if (labelExportParam.getLabelFileType() == LabelFileType.XML){
//            labelObject = JSONObject.toJavaObject(labelJSON,XMLLabelObjectInfo.class);
//            if (labelExportParam.isRectangle())
//                labelObject.toRectangle();
//        }
//
//        else {
////            if (labelExportParam.getLabelFileType() == LabelFileType.TJ)
////                labelObject = convertLabelToPZLabelObjectInfo(labelJSON,fileInfo);
//            //else
//                labelObject = JSONObject.toJavaObject(labelJSON,VifLabelOjectInfo.class);
//        }
//
////        labelObject.addFileName(imageName);
//
////        if (!(labelExportParam.getLabelFileType() == LabelFileType.TJ)){
////            CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;
////            if (CoordinateSystemType.PIXELCS == imageInfo.getCoordinateSystemType())
////                coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;
////            if (CoordinateSystemType.PROJCS == imageInfo.getCoordinateSystemType())
////                coordinateConvertType = CoordinateConvertType.PROJECTION_TO_LONLAT;
////
////            LabelPointTypeConvertor.convertLabelPointType(imageFilePath,labelObject,coordinateConvertType);
////        }
//
//        return XMLUtils.toXMLString(labelObject);
//    }

    /**
     * 将label json格式转化为tj格式
     * @param label
     * @param fileInfo
     * @return
     */
    public PZLabelObjectInfo convertLabelToPZLabelObjectInfo(JSONObject label, JSONObject fileInfo){
        String imagePath = FileUtils.getStringPath(this.rootDir, fileInfo.getString("path"));
        CoordinateSystemType coordinateSystemType = CoordinateSystemType.valueOf(fileInfo.getString("coordinateSystemType"));

        PZLabelObjectInfo newPZLabelObjectInfo = new PZLabelObjectInfo();

        List<PZLabelObjectInfo.Item> itemList = new ArrayList<>();
        JSONArray jsonArray = label.getJSONArray("object");
        int size = jsonArray.size();
        for (int index = 0; index < size; index++) {
            PZLabelObjectInfo.Item item = new PZLabelObjectInfo.Item();
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            PZLabelObjectInfo.Item.Objects objects = new PZLabelObjectInfo.Item.Objects();
            objects.setShape("4");
            objects.setLabel1(jsonObject.getJSONArray("possibleresult").getJSONObject(0).getString("name"));
//            objects.setLabel2(jsonObject.getString("label2"));
//            objects.setLabel3(jsonObject.getString("label3"));
//            objects.setLabel4(jsonObject.getString("label4"));
//            objects.setLabel5(jsonObject.getString("label5"));
//            objects.setLabel6(jsonObject.getString("label6"));
            objects.setCamouflage(jsonObject.getString("camouflage"));
            objects.setTruncated(jsonObject.getString("truncated"));
            objects.setDirection(jsonObject.getString("direction"));
            objects.setBackground(jsonObject.getString("background"));
            objects.setContent(jsonObject.getString("content"));
            objects.setGraphicId(jsonObject.getString("graphicId"));
            objects.setChange(jsonObject.getString("change"));


            List<PZLabelObjectInfo.Item.Objects.Points.Coordinate> pzCoordinateList = new ArrayList<>();
            JSONArray points = label.getJSONArray("object").getJSONObject(index).getJSONObject("points").getJSONArray("point");
            int pointSize = points.size();

            Set<Double> xSet = new HashSet<>();
            Set<Double> ySet = new HashSet<>();
            for (int pointIndex = 0; pointIndex < pointSize; pointIndex++) {
                List<Double> pointItem = new ArrayList<>();
                String[] point = points.getString(pointIndex).split(",");
                double[] coordinate = new double[]{Double.parseDouble(point[0]),Double.parseDouble(point[1])};
                if (coordinateSystemType == CoordinateSystemType.PROJCS || coordinateSystemType == CoordinateSystemType.GEOGCS){
                    coordinate = GeoUtils.convertCoordinateToPixel(coordinate[0], coordinate[1], imagePath,null);
                }else {
                    coordinate[1] = fileInfo.getDouble("height") - coordinate[1];
                }
                PZLabelObjectInfo.Item.Objects.Points.Coordinate pzCoordinate = new PZLabelObjectInfo.Item.Objects.Points.Coordinate();
                pointItem.add(coordinate[0]);
                pointItem.add(coordinate[1]);
                pzCoordinate.setItems(pointItem);
                pzCoordinateList.add(pzCoordinate);
                xSet.add(coordinate[0]);
                ySet.add(coordinate[1]);
            }

            if (xSet.size() == 2 && ySet.size() == 2){
                pzCoordinateList.clear();
                Double[] xArray = xSet.toArray(new Double[2]);
                Double[] yArray = ySet.toArray(new Double[2]);
                double minX = Math.min(xArray[0],xArray[1]);
                double maxX = Math.max(xArray[0],xArray[1]);
                double minY = Math.min(yArray[0],yArray[1]);
                double maxY = Math.max(yArray[0],yArray[1]);
                PZLabelObjectInfo.Item.Objects.Points.Coordinate pzCoordinate = new PZLabelObjectInfo.Item.Objects.Points.Coordinate();
                List<Double> pointItem = new ArrayList<>();
                pointItem.add(minX);
                pointItem.add(minY);
                pzCoordinate.setItems(pointItem);
                pzCoordinateList.add(pzCoordinate);

                pzCoordinate = new PZLabelObjectInfo.Item.Objects.Points.Coordinate();
                pointItem = new ArrayList<>();
                pointItem.add(maxX);
                pointItem.add(maxY);
                pzCoordinate.setItems(pointItem);
                pzCoordinateList.add(pzCoordinate);
                objects.setShape("0");
            }

            PZLabelObjectInfo.Item.Objects.Points pzPoints = new PZLabelObjectInfo.Item.Objects.Points();
            pzPoints.setCoordinateList(pzCoordinateList);
            objects.setPoints(pzPoints);
            item.setObjects(objects);

            itemList.add(item);
        }

        newPZLabelObjectInfo.setItemList(itemList);
        return newPZLabelObjectInfo;
    }

    @Override
    public String importLabelFromXzFile(String filePath, LabelPointType labelPointType, File file) {
        filePath = FileUtils.getStringPath(this.rootDir,filePath);
        LabelObject labelObject = null;

        try{
            labelObject = XMLUtils.parseXMLFromStream(new FileInputStream(file), XMLLabelObjectInfo.class);
            file.delete();
        }catch (IOException e){
            log.error("解析真值文件:{} 出错",file.getAbsolutePath());
            return null;
        }


        String originalCoordinateType = labelObject.getCoordinate();
        CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;

        if(labelPointType.name().equalsIgnoreCase(originalCoordinateType)){
            if (LabelPointType.GEODEGREE ==labelPointType && GeoUtils.isProjection(filePath))
                coordinateConvertType = CoordinateConvertType.LONLAT_TO_PROJECTION;
            if (LabelPointType.PIXEL == labelPointType)
                coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;
        }else {
            if (LabelPointType.GEODEGREE == labelPointType){
                if (GeoUtils.isProjection(filePath))
                    coordinateConvertType = CoordinateConvertType.PIXEL_TO_PROJECTION;
                else
                    coordinateConvertType = CoordinateConvertType.PIXEL_TO_LONLAT;
            }else
                coordinateConvertType = CoordinateConvertType.LONLAT_TO_PIXEL;
        }


        LabelPointTypeConvertor.convertLabelPointType(filePath,labelObject,coordinateConvertType);
        return labelObject.toJSONObject().toString();
    }

    @Override
    public String importLabelXML(String filePath, LabelPointType labelPointType, MultipartFile file) {

        filePath = FileUtils.getStringPath(this.rootDir,filePath);
        LabelObject labelObject = null;

        try{
            if (file.getOriginalFilename().endsWith("xml"))
                labelObject = XMLUtils.parseXMLFromStream(file.getInputStream(), XMLLabelObjectInfo.class);
            else
                labelObject = XMLUtils.parseXMLFromStream(file.getInputStream(), VifLabelOjectInfo.class);
        }catch (IOException e){
            log.error("解析真值文件:{} 出错",file.getOriginalFilename());
            return null;
        }


        String originalCoordinateType = labelObject.getCoordinate();
        CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;

        if(labelPointType.name().equalsIgnoreCase(originalCoordinateType)){
            if (LabelPointType.GEODEGREE ==labelPointType && GeoUtils.isProjection(filePath))
                coordinateConvertType = CoordinateConvertType.LONLAT_TO_PROJECTION;
            if (LabelPointType.PIXEL == labelPointType)
                coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;
        }else {
            if (LabelPointType.GEODEGREE == labelPointType){
                if (GeoUtils.isProjection(filePath))
                    coordinateConvertType = CoordinateConvertType.PIXEL_TO_PROJECTION;
                else
                    coordinateConvertType = CoordinateConvertType.PIXEL_TO_LONLAT;
            }else
                coordinateConvertType = CoordinateConvertType.LONLAT_TO_PIXEL;
        }


        LabelPointTypeConvertor.convertLabelPointType(filePath,labelObject,coordinateConvertType);
        return labelObject.toJSONObject().toString();
    }

    @Override
    public String createManifest(LabelDataset labelDataset, List<LabelDatasetFile> labelDatasetFileList) {
        int segmentation = 0;
        ImageManifest imageManifest = new ImageManifest();
        imageManifest.setDatasetInfo(labelDataset);
        if (labelDataset.getProjectCategory().equalsIgnoreCase("segmentation"))
            segmentation = 1;

        for (LabelDatasetFile labelDatasetFile : labelDatasetFileList) {
            JSONObject imageInfo = labelDatasetFile.getData();
            JSONObject labelJSON = JSONObject.parseObject(labelDatasetFile.getLabel());
            LabelObject labelObject = JSONObject.toJavaObject(labelJSON, XMLLabelObjectInfo.class);
            CoordinateSystemType coordinateSystemType = CoordinateSystemType.valueOf(imageInfo.getString("coordinateSystemType"));
            CoordinateConvertType coordinateConvertType = CoordinateConvertType.NO_ACTION;
            if (coordinateSystemType == CoordinateSystemType.PROJCS)
                coordinateConvertType = CoordinateConvertType.PROJECTION_TO_LONLAT;
            if (coordinateSystemType == CoordinateSystemType.PIXELCS)
                coordinateConvertType = CoordinateConvertType.PIXEL_REVERSION;

            String imagePath = FileUtils.getStringPath(this.rootDir, imageInfo.getString("path"));
            LabelPointTypeConvertor.convertLabelPointType(imagePath,labelObject,coordinateConvertType);

            // 获取标注真实文件信息
            JSONObject labelRelatedFile = fileService.getFileInfoById(labelDatasetFile.getFileId(),null);
            Image image = new Image();
            image.setId(imageInfo.getInteger("id"));
            image.setImageName(imageInfo.getString("imageName"));
            image.setSource(String.valueOf(labelRelatedFile.get("source")));
            image.setPath(String.valueOf(labelRelatedFile.get("path")));
            BeanUtils.copyProperties(labelDatasetFile,image,"id");
            // 获取对比文件信息
            Image changeImage = new Image();
            if (labelDatasetFile.getRelatedFileId() != 0) {
                JSONObject changeFile = fileService.getFileInfoById(labelDatasetFile.getRelatedFileId(),null);
                changeImage.setId(Integer.parseInt(String.valueOf(changeFile.get("id"))));
                changeImage.setImageName(String.valueOf(changeFile.get("imageName")));
                changeImage.setSource(String.valueOf(changeFile.get("source")));
                changeImage.setPath(String.valueOf(changeFile.get("path")));
                //BeanUtils.copyProperties(changeFile, changeImage);
            }
            imageManifest.addData(image, changeImage, JSONObject.toJSONString(labelObject),segmentation);
        }
        imageManifest.getDatasetInfo().setProjectId(labelDataset.getProjectId());

        return JSON.toJSONString(imageManifest, SerializerFeature.WriteMapNullValue, SerializerFeature.PrettyFormat);
    }
}
