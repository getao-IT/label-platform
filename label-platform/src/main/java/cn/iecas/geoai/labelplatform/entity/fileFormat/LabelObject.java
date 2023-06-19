package cn.iecas.geoai.labelplatform.entity.fileFormat;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

/**
 * @author vanishrain
 */
public interface LabelObject {

    /**
     * 是否为空
     * @return
     */
    @JSONField(serialize = false)
    boolean isEmpty();

    /**
     * 转换为正框
     */
    void toRectangle();

    /**
     * 获取标注的坐标系类型
     * @return
     */
    @JSONField(serialize = false)
    String getCoordinate();

    /**
     * 转换成json
     * @return
     */
    JSONObject toJSONObject();

    /**
     * 添加文件名称
     * @param fileName
     */
    void addFileName(String fileName);

    /**
     * 获取坐标点列表
     * @return
     */
    @JSONField(serialize = false)
    Map<Integer,double[][]> getPointMap();


    /**
     * 更新坐标点信息
     * @param labelPointMap
     */
    void updatePointList(Map<Integer,double[][]> labelPointMap, String coordinate);
}
