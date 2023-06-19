package cn.iecas.geoai.labelplatform.util;

import cn.aircas.utils.file.FileUtils;
import cn.aircas.utils.image.emun.CoordinateSystemType;
import cn.aircas.utils.image.geo.GeoUtils;
import cn.aircas.utils.image.slice.CreateThumbnail;
import cn.iecas.geoai.labelplatform.entity.emun.CoordinateConvertType;
import cn.iecas.geoai.labelplatform.entity.fileFormat.LabelObject;
import cn.iecas.geoai.labelplatform.entity.fileFormat.XMLLabelObjectInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import java.io.File;
import java.util.Map;

@Slf4j
public class LabelPointTypeConvertor {

    static {
        gdal.AllRegister();
        gdal.SetConfigOption("GDAL_PAM_ENABLED","FALSE");
    }

    public static LabelObject convertLabelPointType(String imagePath, LabelObject labelObject, CoordinateConvertType coordinateConvertType){
        String coordindate = labelObject.getCoordinate();
        Map<Integer,double[][]> labelPointMap = labelObject.getPointMap();
        if (!labelPointMap.isEmpty()){
            switch (coordinateConvertType){
                case PIXEL_REVERSION: revisePixel(imagePath,labelPointMap); coordindate = "pixel"; break;
                case LONLAT_TO_PIXEL: lonlatToPixel(imagePath,labelPointMap); coordindate = "pixel"; break;
                case PIXEL_TO_LONLAT: pixelToLONLAT(imagePath,labelPointMap); coordindate = "geodegree"; break;
                case PROJECTION_TO_LONLAT: projectionToLONLAT(imagePath,labelPointMap); coordindate = "geodegree"; break;
                case PIXEL_TO_PROJECTION: pixelToProjection(imagePath,labelPointMap); coordindate = "geodegree"; break;
                case LONLAT_TO_PROJECTION:  lonlatToProjection(imagePath,labelPointMap); coordindate = "geodegree";  break;
                default: NO_ACTION: break;
            }
            labelObject.updatePointList(labelPointMap,coordindate);
        }

        return labelObject;
    }

    /**
     * 将经纬度转换为投影坐标
     * @param imagePath
     * @param labelPointMap
     * @return
     */
    public static Map<Integer,double[][]> projectionToLONLAT(String imagePath, Map<Integer,double[][]> labelPointMap){
        log.info("开始进行影像{} 经纬度---->投影坐标转换",imagePath);
        Dataset dataset = gdal.Open(imagePath);
        for (double[][] pointValue : labelPointMap.values()) {
            for (double[] point : pointValue) {
                double[] coordination = GeoUtils.coordinateConvertor(point[0],point[1],dataset,GeoUtils.COORDINATE_LONLAT);
                point[0] = coordination[0];
                point[1] = coordination[1];
            }
        }
        dataset.delete();
        return labelPointMap;
    }


    /**
     * 经纬度转像素
     * @param imagePath
     * @param labelPointMap
     * @return
     */
    public static Map<Integer,double[][]> lonlatToPixel(String imagePath, Map<Integer,double[][]> labelPointMap){
        log.info("开始进行影像{} 经纬度坐标---->像素转换",imagePath);
        Dataset dataset = gdal.Open(imagePath);
        for (double[][] pointValue : labelPointMap.values()) {
            for (double[] point : pointValue) {
                double[] coordination = new double[]{point[0],point[1]};
                if (point[0] <= 180 && point[0] >=-180 && GeoUtils.isProjection(imagePath)){
                    coordination = GeoUtils.coordinateConvertor(coordination[0],coordination[1],dataset,GeoUtils.COORDINATE_PROJECTION);
                }
                coordination = GeoUtils.convertCoordinateToPixel(coordination[0],coordination[1],dataset,null);
                point[0] = coordination[0];
                point[1] = coordination[1];
            }
        }
        dataset.delete();
        return labelPointMap;
    }

    /**
     * 像素转经纬度
     * @param imagePath
     * @param labelPointMap
     * @return
     */
    public static Map<Integer,double[][]> pixelToLONLAT(String imagePath, Map<Integer,double[][]> labelPointMap){
        log.info("开始进行影像{} 像素---->经纬度坐标转换",imagePath);
        Dataset dataset = gdal.Open(imagePath);
        for (double[][] pointValue : labelPointMap.values()) {
            for (double[] point : pointValue) {
                double[] coordination = GeoUtils.pixel2Coordinate(point[0],point[1],dataset,GeoUtils.COORDINATE_LONLAT);
                point[0] = coordination[0];
                point[1] = coordination[1];
            }
        }
        dataset.delete();
        return labelPointMap;
    }

    /**
     * 像素转投影坐标
     * @param imagePath
     * @param labelPointMap
     * @return
     */
    public static Map<Integer,double[][]> pixelToProjection(String imagePath, Map<Integer,double[][]> labelPointMap){
        log.info("开始进行影像{} 像素---->投影坐标转换",imagePath);
        Dataset dataset = gdal.Open(imagePath);
        for (double[][] pointValue : labelPointMap.values()) {
            for (double[] point : pointValue) {
                double[] coordination = GeoUtils.pixel2Coordinate(point[0],point[1],dataset,GeoUtils.COORDINATE_PROJECTION);
                point[0] = coordination[0];
                point[1] = coordination[1];
            }
        }
        dataset.delete();
        return labelPointMap;
    }

    /**
     * 将经纬度转换为投影坐标
     * @param imagePath
     * @param labelPointMap
     * @return
     */
    public static Map<Integer,double[][]> lonlatToProjection(String imagePath, Map<Integer,double[][]> labelPointMap){
        log.info("开始进行影像{} 经纬度---->投影坐标转换",imagePath);
        Dataset dataset = gdal.Open(imagePath);
        for (double[][] pointValue : labelPointMap.values()) {
            for (double[] point : pointValue) {
                double[] coordination = GeoUtils.coordinateConvertor(point[0],point[1],dataset,GeoUtils.COORDINATE_PROJECTION);
                point[0] = coordination[0];
                point[1] = coordination[1];
            }
        }
        dataset.delete();
        return labelPointMap;
    }

    /**
     * 将像素坐标原点进行反转
     * @param imagePath
     * @param labelPointMap
     * @return
     */
    public static Map<Integer,double[][]> revisePixel(String imagePath, Map<Integer,double[][]> labelPointMap){
        log.info("开始进行影像{} 像素坐标原点转换",imagePath);
        Dataset dataset = gdal.Open(imagePath);
        int ySize = dataset.getRasterYSize();
        for (double[][] pointValue : labelPointMap.values()) {
            for (double[] point : pointValue) {
                point[1] = ySize - point[1];
            }
        }
        dataset.delete();
        return labelPointMap;
    }

    public static void main(String[] args) {
        File file = new File("Z:\\测试样本库考题数据\\301-01可见光舰船检测识别\\1m\\1\\1\\imgs");
        File xmlFiles = FileUtils.getFile("Z:\\测试样本库考题数据\\301-01可见光舰船检测识别数据集\\1m\\1\\1\\xmls");
        for (File imgFile : file.listFiles()) {
            String baseName = FilenameUtils.getBaseName(imgFile.getName());
            String imageFile = imgFile.getAbsolutePath();
            String xmlFile = FileUtils.getStringPath(xmlFiles.getAbsolutePath(),baseName+".xml");
            LabelObject labelObject = XMLUtils.parseXMLFromFile(XMLLabelObjectInfo.class,xmlFile);
            CoordinateConvertType convertLabelPointType = CoordinateConvertType.LONLAT_TO_PIXEL;
            try{
                if (labelObject == null || imgFile.getName().endsWith("xml"))
                    continue;
                String coordinate = labelObject.getCoordinate();
                if ("pixel".equalsIgnoreCase(coordinate))
                    convertLabelPointType = CoordinateConvertType.PIXEL_REVERSION;
            }catch (Exception e){
                System.out.println("sdf");
            }

            LabelPointTypeConvertor.convertLabelPointType(imgFile.getAbsolutePath(),labelObject,convertLabelPointType);
            File saveFile = FileUtils.getFile(imgFile.getParentFile().getParentFile().getAbsolutePath(),"newXML",baseName+".xml");
            if (!saveFile.getParentFile().exists())
                saveFile.getParentFile().mkdirs();
            XMLUtils.toXMLFile(saveFile.getAbsolutePath(),labelObject);
        }

        System.out.println("sdf");

    }

}
