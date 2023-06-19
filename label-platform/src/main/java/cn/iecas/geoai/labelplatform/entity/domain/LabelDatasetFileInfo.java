package cn.iecas.geoai.labelplatform.entity.domain;

import cn.iecas.geoai.labelplatform.entity.emun.CoordinateSystemType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "label_dataset_file_info")
public class LabelDatasetFileInfo {
    private static final long serialVersionUID = 9002841137241442855L;

    /**
     * imageId
     */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;


    /**
     * 标注信息
     */
    private String label;


    /**
     * 文件id
     */
    private int fileId;

    /**
     * 数据集id
     */
    private int datasetId;

    /**
     * 审核反馈
     */
    private String feedback;


    /**
     * ai标注信息
     */
    private String aiLabel;

    /**
     * 标注员id
     */
    private int labelUserId;

    /**
     * 审核员id
     */
    private int checkUserId;

    /**
     * 反馈图像信息
     */
    private String screenshot;

    /**
     * 该影像的状态：已标注、未标注
     */
    private LabelStatus status;

    /**
     * 分配给标注员的时间
     */
    private Date assignLabelTime;

    /**
     * 分配给审核员的时间
     */
    private Date assignCheckTime;

    /**
     * 标注员提交标注的时间
     */
    private Date finishLabelTime;

    /**
     * 审核员审核完成时间
     */
    private Date finishCheckTime;

    /**
     * 波段
     */
    private int bands;

    /**
     * 影像宽度
     */
    private int width;

    /**
     * 位数
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
     * 保存路径
     */
    private String path;

    /**
     * 最小经度
     */
    private double minLon;

    /**
     * 最小纬度
     */
    private double minLat;

    /**
     * 最大经度
     */
    private double maxLon;

    /**
     * 最大纬度
     */
    private double maxLat;

    /**
     * 影像缩略图
     */
    private String thumb;

    /**
     * 音箱来源
     */
    private String source;

    /**
     * 标签
     */
    private String keywords;


    /**
     * 影像创建时间
     */
    //@JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 影像上传批次
     */
    private int batchNumber;

    /**
     * 分辨率
     */
    private double resolution;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 影像名称
     */
    private String imageName;


    /**
     * 是否公开
     */
    private boolean isPublic;


    /**
     * 影像投影信息
     */
    private String projection;

    /**
     * 影像大小
     */
    private String size;


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
     * 最大投影坐标x
     */
    private double maxProjectionY;

    private CoordinateSystemType coordinateSystemType;


    @TableField(exist = false)
    private String labelUserName;

    @TableField(exist = false)
    private String checkUserName;

}
