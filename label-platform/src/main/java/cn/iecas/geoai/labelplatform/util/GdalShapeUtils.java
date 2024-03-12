package cn.iecas.geoai.labelplatform.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.*;
import org.gdal.osr.SpatialReference;

import java.util.HashMap;
import java.util.Map;


/**
 * Gdal工具类
 */
@Slf4j
public class GdalShapeUtils {

    static {
        gdal.AllRegister();
    }


    /**
     * 多边形矢量转换
     *
     * @param inRaster
     * @param outShp
     */
    public static void polygonVectorConvert(String inRaster, String outShp) {
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
        gdal.SetConfigOption("SHAPE_ENCODING", "");
        Dataset dataset = gdal.Open(inRaster, gdalconstConstants.GA_ReadOnly);
        log.info("影响投影信息：{}", dataset.GetProjectionRef());
        Band band = dataset.GetRasterBand(1);
        SpatialReference prj = new SpatialReference();
        prj.SetWellKnownGeogCS("WGS84");
        if (!dataset.GetProjectionRef().isEmpty()) {
            prj.ImportFromWkt(dataset.GetProjectionRef());
        }

        // 创建矢量数据集
        Driver driver = ogr.GetDriverByName("ESRI Shapefile");
        DataSource dataSource = driver.CreateDataSource(outShp);
        if (dataSource == null) {
            log.info("创建矢量数据集 {} 失败", outShp);
        }

        // 创建图层layer  参数说明：新图层名称，坐标系，图层的几何类型，创建选项，与驱动有关
        Layer layer = dataSource.CreateLayer("test_layer", prj);
        FeatureDefn featureDefn = layer.GetLayerDefn();
        int i = featureDefn.GetFieldCount();
        if (layer == null) {
            log.info("图层创建失败");
        }
        log.info("矢量数据集创建成功，矢量图层创建成功");

        // 创建ShapeFile属性
        FieldDefn countryId = new FieldDefn("countryId", ogr.OFTInteger);//创建一个字段用来存储栅格的像素值
        FieldDefn country = new FieldDefn("国家", ogr.OFTString);//创建一个字段用来存储栅格的像素值
        layer.CreateField(countryId);
        layer.CreateField(country);

        Feature feature1 = new Feature(featureDefn);
        Geometry geomTriangle1 = Geometry.CreateFromWkt("POLYGON ((0 0,0 5,20 20,20 20,40 20,10 10,0 0))");
        feature1.SetGeometry(geomTriangle1);
        feature1.SetField(0, "10000");
        feature1.SetField(1, "中国");
        layer.CreateFeature(feature1);
        Feature feature2 = new Feature(featureDefn);
        Geometry geomTriangle2 = Geometry.CreateFromWkt("POLYGON ((50 50,50 60,60 50,50 50))");
        feature2.SetGeometry(geomTriangle2);
        feature2.SetField(0, "100001");
        feature2.SetField(1, "美国");
        layer.CreateFeature(feature2);


        //矢量化
        log.info("{} 矢量转换中...", inRaster);
        //gdal.Polygonize(band, null, layer, 0);
        layer.SyncToDisk();

        // 释放资源
        dataSource.delete();
        dataset.delete();
        log.info("矢量转换成功", inRaster);
    }


    /**
     * 点矢量转换
     *
     * @param inRaster
     * @param outShp
     */
    public static void pointVectorConvert(String inRaster, String outShp) {
        gdal.SetConfigOption("SHAPE_ENCODING", "");
        Dataset dataset = gdal.Open(inRaster);

        // 创建矢量数据集
        Driver driver = ogr.GetDriverByName("ESRI Shapefile");
        DataSource dataSource = driver.CreateDataSource(outShp);

        // 创建矢量图层
        String layerName = "points";
        Layer layer = dataSource.CreateLayer(layerName, null, ogr.wkbPoint);
        log.info("矢量数据集创建成功，矢量图层创建成功");

        // 创建字段
        FieldDefn fieldDefn = new FieldDefn("pointField", ogr.OFTInteger);
        layer.CreateField(fieldDefn);

        // 读取栅格数据并转换为点矢量数据
        log.info("{} 矢量转换中...", inRaster);
        int width = dataset.GetRasterXSize();
        int height = dataset.GetRasterYSize();
        double[] geoTransform = dataset.GetGeoTransform();
        double[] pixelValue = new double[1];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dataset.GetRasterBand(1).ReadRaster(x, y, 1, 1, pixelValue);
                double[] pixelCoordinates = new double[2];
                pixelCoordinates[0] = geoTransform[0] + x * geoTransform[1] + y * geoTransform[2];
                pixelCoordinates[1] = geoTransform[3] + x * geoTransform[4] + y * geoTransform[5];
                Geometry point = new Geometry(ogr.wkbPoint);
                point.AddPoint(pixelCoordinates[0], pixelCoordinates[1]);
                Feature feature = new Feature(layer.GetLayerDefn());
                feature.SetGeometry(point);
                feature.SetField("pointField", (int) pixelValue[0]);
                layer.CreateFeature(feature);
                feature.delete();
            }
        }
        // 释放资源
        dataSource.delete();
        dataset.delete();
        log.info("矢量转换成功", inRaster);
    }


    /**
     * 解析获取Shape File信息
     *
     * @param shpPath
     */
    public static void getShapeFileInfo(String shpPath) {
        // 配置GDAL_DATA路径
        //gdal.SetConfigOption("GDAL_DATA", "D:\\GDAL\\GDAL\\release-1911-x64-gdal-2-4-0-mapserver-7-2-1\\bin\\gdal-data");
        // 支持中文路径
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
        // 属性表字段支持中文
        gdal.SetConfigOption("SHAPE_ENCODING", "");

        // 读取数据
        String strDriverName = "ESRI Shapefile";
        Driver oDriver = ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            log.info("{} 驱动不可用！", strDriverName);
            return;
        }
        DataSource dataSource = oDriver.Open(shpPath);
        Layer layer = dataSource.GetLayer(0);
        SpatialReference spatialReference = layer.GetSpatialRef();
        if (spatialReference != null) {
            log.info("空间参考坐标系：{} {}", spatialReference.GetAttrValue("AUTHORITY", 0), spatialReference.GetAttrValue("AUTHORITY", 1));
        }

        double[] layerExtent = layer.GetExtent();
        log.info("图层范围：minx:" + layerExtent[0] + ", maxx:" + layerExtent[1] + ", miny:" + layerExtent[2] + ", maxy:" + layerExtent[3]);

        log.info("=====================字段属性信息=======================");
        FeatureDefn featureDefn = layer.GetLayerDefn();
        int filedCount = featureDefn.GetFieldCount();
        log.info("图层属性个数：{}", filedCount);
        Map<String, Object> fieldMap = new HashMap<>();
        for (int i = 0; i < filedCount; i++) {
            FieldDefn fieldDefn = featureDefn.GetFieldDefn(i);
            // 得到属性字段类型
            int fieldType = fieldDefn.GetFieldType();
            String fieldTypeName = fieldDefn.GetFieldTypeName(fieldType);
            // 的到属性字段名称
            String fieldName = fieldDefn.GetName();
            fieldMap.put("filed" + i, fieldName);
            log.info("字段类型：{}， 属性类型：{}， 字段名称：{} ", fieldType, fieldTypeName, fieldName);
        }

        log.info("=====================要素信息=======================");
        JSONObject objectInfo = new JSONObject();
        JSONArray object = new JSONArray();
        long featureCount = layer.GetFeatureCount();
        log.info("图层要素个数：{}", featureCount);
        for (int i = 0; i < featureCount; i++) {
            /*if (i > 200)
                break;*/
            Feature feature = layer.GetFeature(i);
            Geometry geometry = feature.GetGeometryRef();
            // 转为json
            String geometryJson = geometry.ExportToJson();
            // 获取边界坐标
            double[][] boundaryPoints = geometry.GetBoundary().GetPoints();
            Object[] arr = fieldMap.values().toArray();

            // 生成标注信息
            JSONObject element = new JSONObject();
            // 获取属性值
            for (int k = 0; k < arr.length; k++) {
                String fvalue = feature.GetFieldAsString(arr[k].toString());
                element.put(arr[k].toString(), fvalue);
                //log.info("元素类型：{}, 属性名称：{}, 属性值：{}", geometry.GetGeometryName(), arr[k].toString(), fvalue);
            }
            //System.out.println("要素类型：" + geometry.GetGeometryName() + " 边界坐标：" + geometryJson.substring(geometryJson.lastIndexOf(":") + 1, geometryJson.length()));
            JSONObject point = new JSONObject();
            JSONArray ps = new JSONArray();
            double[][] doubles = geometry.GetBoundary().GetPoints();
            try {
                for (double[] aDouble : doubles) {
                    ps.add(aDouble[0] + "," + aDouble[1]);
                }
            } catch (Exception e) {
                log.error("出错：{}", i);
            }

            JSONArray possibleresult = new JSONArray();
            JSONObject pJson = new JSONObject();
            pJson.put("name", "标签test");
            pJson.put("probability", "1");
            possibleresult.add(pJson);

            element.put("type", "Rectangle");
            element.put("checkStatus", 0);
            element.put("note", "");
            element.put("rotated", false);
            element.put("coordinate", "geodegree");
            element.put("description", "经纬度坐标");
            element.put("possibleresult", possibleresult);
            point.put("point", ps);
            element.put("points", point);
            object.add(element);
        }
        objectInfo.put("object", object);
        System.out.println(objectInfo);
    }
}
