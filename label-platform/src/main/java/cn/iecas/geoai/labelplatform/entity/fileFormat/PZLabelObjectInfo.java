package cn.iecas.geoai.labelplatform.entity.fileFormat;

import cn.iecas.geoai.labelplatform.util.XMLUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "annotation")
public class PZLabelObjectInfo implements LabelObject{

    @XmlTransient
    private String coordinate;

    @JSONField(name = "object")
    @XmlElement(name = "item")
    private List<Item> itemList = new ArrayList<>();


    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item{
        private int id;

        @XmlElement(name = "objects")
        private Objects objects;

        @XmlElement(name = "imageInfo")
        private ImageInfo imageInfo;

        @XmlElement(name = "checkInfo")
        private CheckInfo checkInfo;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Objects{
            private String shape;
            private Points points;
            private String label1;
            private String label2;
            private String label3;
            private String label4;
            private String label5;
            private String label6;
            private String camouflage;
            private String truncated;
            private String direction;
            private String background;
            private String content;
            private String graphicId;
            private String change;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Points{
                @XmlElement(name = "item")
                private List<Coordinate> coordinateList;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Coordinate{
                    @XmlElement(name = "item",required = true)
                    private List<Double> items;
                }
            }
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ImageInfo{
            private String satellite;
            private String payload;

            @XmlElement(name = "side_sway")
            private String sideSway;

            @XmlElement(name = "angle")
            private String angle;
            private String resolution;

            @XmlElement(name = "image_name")
            private String imageName;

            @XmlElement(name = "image_width")
            private String imageWidth;

            @XmlElement(name = "image_height")
            private String imageHeight;

            @XmlElement(name = "image_depth")
            private String imageDepth;
            private String location;
            private String imageTime;
            private String pitchAngle;
            private String polarization;
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class CheckInfo{
            private String annotationUser;
            private String reviewUser;
            private String annotationTime;
            private String reviewTime;
            private String status;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void toRectangle() {

    }

    @Override
    public String getCoordinate() {
        return this.coordinate;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject result = new JSONObject();
        JSONArray objectList = new JSONArray();
        for (int index = 0; index < this.itemList.size(); index++) {
            Item item = this.itemList.get(index);
            item.setId(index);
            JSONObject object = new JSONObject();
            Item.Objects objects = item.getObjects();
            if (objects.getLabel1()!=null && objects.getLabel1().equals("缩略图"))
                continue;
            List<String> point = new ArrayList<>();
            object.put("id",index);
            object.put("selectId",index);
            object.put("coordinate",this.getCoordinate());
            object.put("type","Rectangle");
            object.put("description",this.getCoordinate());
            object.put("label1",objects.getLabel1());
            object.put("label2",objects.getLabel2());
            object.put("label3",objects.getLabel3());
            object.put("label4",objects.getLabel4());
            object.put("label5",objects.getLabel5());
            object.put("label6",objects.getLabel6());
            object.put("camouflage",objects.getCamouflage());
            object.put("truncated",objects.getTruncated());
            object.put("direction",objects.getDirection());
            object.put("background",objects.getBackground());
            object.put("content",objects.getContent());
            object.put("graphicId",objects.getGraphicId());
            object.put("change",objects.getChange());

            String possibleName = objects.getLabel6();
            if (StringUtils.isNoneBlank(objects.getLabel5())){
                possibleName = objects.getLabel5();
            }else if (StringUtils.isNoneBlank(objects.getLabel4())){
                possibleName = objects.getLabel4();
            }else if (StringUtils.isNoneBlank(objects.getLabel3())){
                possibleName = objects.getLabel3();
            }else if (StringUtils.isNoneBlank(objects.getLabel2())){
                possibleName = objects.getLabel2();
            }else if (StringUtils.isNoneBlank(objects.getLabel1())){
                possibleName = objects.getLabel1();
            }
            JSONArray possibleResults = new JSONArray();
            JSONObject possibleResult = new JSONObject();
            possibleResult.put("name",possibleName);
            possibleResult.put("probability","1");
            possibleResults.add(possibleResult);

            for (Item.Objects.Points.Coordinate coordinate : objects.getPoints().coordinateList) {
                point.add(coordinate.getItems().get(0)+","+coordinate.getItems().get(1));
            }
            JSONObject points = new JSONObject();
            points.put("point",point);
            object.put("points",points);
            object.put("possibleresult",possibleResults);

            objectList.add(object);
        }
        result.put("object",objectList);
        return result;
    }

    @Override
    public void addFileName(String fileName) {

    }

    @Override
    public Map<Integer, double[][]> getPointMap() {
        Map<Integer,double[][]> pointMap = new HashMap<>();
        for (int objectIndex = 0; objectIndex < this.itemList.size(); objectIndex++) {
            Item item = this.itemList.get(objectIndex);
            List<Item.Objects.Points.Coordinate> coordinateList = item.getObjects().getPoints().getCoordinateList();
            if (coordinateList == null || coordinateList.size() == 0)
                continue;
            double[][] objectPointArray = new double[coordinateList.size()][2];

            for (int index = 0; index < coordinateList.size(); index++) {
                double xCoordinate = coordinateList.get(index).getItems().get(0);
                double yCoordinate = coordinateList.get(index).getItems().get(1);
                objectPointArray[index] = new double[]{xCoordinate,yCoordinate};
            }
            pointMap.put(objectIndex,objectPointArray);
        }
        return pointMap;
    }

    @Override
    public void updatePointList(Map<Integer, double[][]> labelPointMap, String coordinate) {
        for (Integer objectIndex : labelPointMap.keySet()) {
            Item item = this.itemList.get(objectIndex);
            List<Item.Objects.Points.Coordinate> coordinateList  = item.getObjects().getPoints().getCoordinateList();
            for (int i = 0; i < labelPointMap.get(objectIndex).length; i++) {
                double[] point = labelPointMap.get(objectIndex)[i];
                coordinateList.get(i).getItems().set(0,point[0]);
                coordinateList.get(i).getItems().set(1,point[1]);
//                List<Double> items = coordinateList.get(i).getItems();
//                List<Double> doubleItems = new ArrayList<>(items.size());
//                doubleItems.add(Double.valueOf(point[0]));
//                doubleItems.add(Double.valueOf(point[1]));
//                coordinateList.get(i).setDoubleItems(doubleItems);
            }
        }
        setCoordinate(coordinate);
    }

    public static void main(String[] args) {
        PZLabelObjectInfo pzLabelObjectInfo = XMLUtils.parseXMLFromFile(PZLabelObjectInfo.class,"C:\\Users\\dell\\Desktop\\SAR\\USA-Base\\0.xml");
        System.out.println("sdf");
    }
}
