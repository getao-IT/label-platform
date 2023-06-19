package cn.iecas.geoai.labelplatform.entity.domain;

import cn.iecas.geoai.labelplatform.entity.emun.CoordinateSystemType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class Image implements Serializable {
    private static final long serialVersionUID = 8551305852347977301L;
    /**
     * id
     */
    private int id;

    /**
     * 波段数
     */
    private int bands;

    /**
     * 影像宽度
     */
    private int width;

    /**
     * 影像位数
     */
    private String bit;

    /**
     * 影像高度
     */
    private int height;

    /**
     * 用户id
     */
    private int userId;

    /**
     * 影像相对路径
     */
    private String path;

    /**
     * 影像缩略图
     */
    private String thumb;

    /**
     * 标注信息
     */
    private String label;


    /**
     * 影像来源
     */
    private String source;

    /**
     * 最小纬度
     */
    private double minLat;

    /**
     * 最小经度
     */
    private double minLon;

    /**
     * 最大纬度
     */
    private double maxLat;

    /**
     * 最大经度
     */
    private double maxLon;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 上传批次
     */
    private int batchNumber;

    /**
     * 影像名称
     */
    private String imageName;

    /**
     * 是否公开
     */
    private boolean isPublic;

    /**
     * 投影信息
     */
    private String projection;

    /**
     * 分辨率
     */
    private double resolution;

    /**
     * 标注状态
     */
    private LabelStatus status;


    /**
     * 最小投影坐标x
     */
    private double minProjectionX;

    /**
     * 最小投影坐标y
     */
    private double minProjectionY;

    /**
     * 最大投影坐标x
     */
    private double maxProjectionX;

    /**
     * 上传影像的用户名
     */
    private String userName;

    /**
     * 最大投影坐标x
     */
    private double maxProjectionY;

    /**
     * 坐标类型
     */
    private CoordinateSystemType coordinateSystemType;

    @Override
    public String toString() {
        String thumb = this.thumb == null ? "" : "base64";
        return "Image{" +
                "id=" + id +
                ", bands=" + bands +
                ", width=" + width +
                ", bit='" + bit + '\'' +
                ", height=" + height +
                ", userId=" + userId +
                ", path='" + path + '\'' +
                ", thumb='" + thumb + '\'' +
                ", label='" + label + '\'' +
                ", source='" + source + '\'' +
                ", minLat=" + minLat +
                ", minLon=" + minLon +
                ", maxLat=" + maxLat +
                ", maxLon=" + maxLon +
                ", createTime=" + createTime +
                ", batchNumber=" + batchNumber +
                ", imageName='" + imageName + '\'' +
                ", isPublic=" + isPublic +
                ", projection='" + projection + '\'' +
                ", resolution='" + resolution + '\'' +
                ", status=" + status +
                ", minProjectionX=" + minProjectionX +
                ", minProjectionY=" + minProjectionY +
                ", maxProjectionX=" + maxProjectionX +
                ", maxProjectionY=" + maxProjectionY +
                '}';
    }
}
