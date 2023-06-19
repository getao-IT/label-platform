package cn.iecas.geoai.labelplatform.entity.domain;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelProjectStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.lang.annotation.Target;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@TableName(value = "label_project")
public class LabelProject implements Serializable {
    private static final long serialVersionUID = -7037658665290800397L;
    /**
     * 标注项目id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 标注项目关联数据集
     */
    private int datasetId;

    private int totalCount;

    /**
     * 是否需要AI标注
     */
    @TableField
    private Boolean isAiLabel = false;

    @TableField(exist = false)
    private boolean preprocessing;

    /**
     * 标注项目创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 标注项目完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime;

    /**
     * 完成标注数量
     */
    private int finishCount;

    /**
     * 发布员id
     */
    @Min(value = 1,message = "发布员id不能为空")
    private int userId;

    /**
     * 类别
     */
    private String category;


    /**
     * 发布者姓名
     */
    @TableField("publisher_name")
    @NotBlank(message = "发布员姓名不能为空")
    private String userName;

    /**
     * 关键字
     */
    private String keywords;

    /**
     * 是否使用数据集已有的标注信息
     */
    @TableField
    private boolean useLabel;

    /**
     * 标注项目名称
     */
    @NotBlank(message = "标注项目名称不能为空")
    private String projectName;

    /**
     * 标注员id列表
     */
    @NotBlank(message = "标注员不能为空")
    private String labelUserIds;

    /**
     * 审核员列表
     */
    @NotBlank(message = "审核员不能为空")
    private String checkUserIds;

    /**
     * 默认标注分配最大数量
     */
    @Min(value = 1,message = "默认标注分配数量必须为正整数")
    private int defaultLabelCount;

    /**
     * 默认审核分配最大数量
     */
    @Min(value = 1,message = "默认审核分配数量必须为正整数")
    private int defaultCheckCount;

    /**
     * 标注项目使用数据集
     */
    @NotBlank(message = "项目使用数据集不能为空")
    private String relatedDatasetId;

    /**
     * 标注项目描述
     */
    private String projectDescription;

    /**
     * 标注项目状态
     */
    private LabelProjectStatus status;


    /**
    * 算法id
    */
    private String serviceId;

    /**
     * 数据集类型
     */
    private DatasetType datasetType;

    /**
     * 样本产出量
     */
    private int makeSampleSetNum;

    /**
     * 任务提交次数
     */
    private int commitCount;

    /**
     * 任务审核驳回次数
     */
    private int refuseCount;

    /**
     * 任务总耗时
     */
    private String consumeTime;

    /**
     * 任务被驳回率
     */
    private float refuseRate;

    /**
     * 数据集名称
     */
    @TableField(exist = false)
    private String datasetName;

    /**
     * 是否联合标注
     */
    @TableField(exist = false)
    private boolean unite;

    /**
     * 是否创建协同标注项目
     */
    private boolean cooperate;

    /**
     * 该标注项目影像ID
     */
    @TableField(exist = false)
    private List<Integer> imageIds;
}

