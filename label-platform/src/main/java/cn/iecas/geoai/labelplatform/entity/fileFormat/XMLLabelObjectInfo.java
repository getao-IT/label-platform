package cn.iecas.geoai.labelplatform.entity.fileFormat;

import cn.aircas.utils.image.geo.GeoUtils;
import cn.iecas.geoai.labelplatform.entity.domain.Image;
import cn.iecas.geoai.labelplatform.entity.emun.CoordinateSystemType;
import cn.iecas.geoai.labelplatform.util.XMLUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;

@Data
@Slf4j
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "annotation")
public class XMLLabelObjectInfo implements LabelObject {

    @JSONField(serialize = false)
    private Source source = new Source();

    @JSONField(name = "object")
    @XmlElement(name = "object")
    @XmlElementWrapper(name = "objects")
    private List<XMLLabelObject> XMLLabelObjectList;

    @Data
    public static class Source {
        private int id;
        private String fileName;
        private String original;
        private String path;
    }

    @Data
    @XmlType(name = "object",propOrder = {"id","type","coordinate","description","possibleResultList","points"})
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XMLLabelObject {
        @JSONField(ordinal = 0)
        private int id;
        @JSONField(ordinal = 1)
        private String type;
        @JSONField(ordinal = 5)
        private Points points;
        @JSONField(ordinal = 2)
        private String coordinate;
        @JSONField(ordinal = 3)
        private String description;

        @XmlElement(name = "possibleresult")
        @JSONField(name = "possibleresult",ordinal = 4)
        private List<PossibleResult> possibleResultList;


        @Data
        public static class PossibleResult {
            private String name;
            private String probability;
        }

        @Data
        public static class Points{
            private List<String> point;
        }
    }


    /**
     * 标注信息是否为空
     * @return
     */
    @Override
    public boolean isEmpty() {
        return XMLLabelObjectList.isEmpty();
    }

    @Override
    public void toRectangle() {
        for (XMLLabelObject xmlLabelObject : this.XMLLabelObjectList) {
            List<String> pointList = new ArrayList<>();
            double maxX = 0;
            double maxY = 0;
            double minX = -1;
            double minY = -1;
            for (int index = 0; index < xmlLabelObject.getPoints().getPoint().size(); index++) {
                String points = xmlLabelObject.getPoints().point.get(index);
                String[] strPoint = points.split(",");
                double[] point = new double[]{Double.parseDouble(strPoint[0].trim()),Double.parseDouble(strPoint[1].trim())};
                if (point[0]>maxX)
                    maxX = point[0];
                if (point[1]>maxY)
                    maxY = point[1];

                if (minX == -1 || point[0] < minX)
                    minX = point[0];

                if (minY == -1 || point[1] < minY)
                    minY = point[1];
            }
            pointList.add(minX+","+minY);
            pointList.add(maxX+","+minY);
            pointList.add(maxX+","+maxY);
            pointList.add(minX+","+maxY);
            xmlLabelObject.getPoints().setPoint(pointList);
        }

    }

    /**
     * 获取坐标系类型
     * @return
     */
    @Override
    public String getCoordinate() {
        if (XMLLabelObjectList.isEmpty())
            return null;
        return XMLLabelObjectList.get(0).getCoordinate();
    }

    @Override
    public JSONObject toJSONObject() {
        return JSONObject.parseObject(JSONObject.toJSONString(this));
    }

    @Override
    public void addFileName(String fileName) {
        this.source.setFileName(fileName);
    }

    /**
     * 获取坐标点列表
     * @return
     */
    @Override
    public Map<Integer,double[][]> getPointMap() {
        Map<Integer,double[][]> pointMap = new HashMap<>();
        for (int objectIndex = 0; objectIndex < this.XMLLabelObjectList.size(); objectIndex++) {
            XMLLabelObject xmlLabelObject = this.XMLLabelObjectList.get(objectIndex);
            XMLLabelObject.Points objectPoints = xmlLabelObject.getPoints();
            if (objectPoints == null || objectPoints.getPoint()==null)
                continue;
            List<String> objectPointList = objectPoints.getPoint();
            double[][] objectPointArray = new double[objectPointList.size()][2];

            for (int index = 0; index < objectPointList.size(); index++) {
                String[] points = objectPointList.get(index).split(",");
                double xCoordinate = parseDouble(points[0]);
                double yCoordinate = parseDouble(points[1]);
                objectPointArray[index] = new double[]{xCoordinate,yCoordinate};
            }
            pointMap.put(objectIndex,objectPointArray);
        }
        return pointMap;
    }

    /**
     * 更新坐标点列表
     * @param labelPointMap
     */
    @Override
    public void updatePointList(Map<Integer, double[][]> labelPointMap, String coordinate) {
        for (Integer objectIndex : labelPointMap.keySet()) {
            XMLLabelObject xmlLabelObject = this.XMLLabelObjectList.get(objectIndex);
            xmlLabelObject.setCoordinate(coordinate.toLowerCase());
            List<String> pointList = xmlLabelObject.getPoints().getPoint();
            pointList.clear();
            for (int i = 0; i < labelPointMap.get(objectIndex).length; i++) {
                double[] point = labelPointMap.get(objectIndex)[i];
                pointList.add(point[0]+","+point[1]);
            }
        }
    }
}
