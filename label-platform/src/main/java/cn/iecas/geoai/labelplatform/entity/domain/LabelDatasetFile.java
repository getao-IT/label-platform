package cn.iecas.geoai.labelplatform.entity.domain;

import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "label_dataset_file")
public class LabelDatasetFile implements Serializable {
    private static final long serialVersionUID = 9002841137241442855L;

    /**
     *
     */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 标注信息
     */
    private String label;

    /**
     * 影像id
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
     * 文件信息
     */
    @TableField(exist = false)
    private JSONObject data;


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
     * 文件提交审核次数
     */
    private int commitCount;

    /**
     * 变化检测关联文件id
     */
    private int relatedFileId;

    /**
     * 预处理结果路径
     */
    private String preprocessPath;
}
