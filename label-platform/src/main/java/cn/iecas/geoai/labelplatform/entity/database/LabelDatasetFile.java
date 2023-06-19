package cn.iecas.geoai.labelplatform.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Data
@Table(name = "label_dataset_file")
public class LabelDatasetFile implements Serializable {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 数据集id
     */
    @Column(nullable = true)
    private int datasetId;

    /**
     * 影像id
     */
    @Column(nullable = true)
    private int fileId;

    /**
     * 标注状态
     */
    @Column(nullable = true)
    private int status;

    /**
     * 标注信息
     */
    @Column(name = "label", columnDefinition = "text")
    private String label;

    /**
     * ai标注信息
     */
    @Column(name = "ai_label", columnDefinition = "text")
    private String aiLabel;

    /**
     * 审核反馈信息
     */
    private String feedback;

    /**
     * 标注员id
     */
    @Column(nullable = true)
    private int labelUserId;

    /**
     * 审核员id
     */
    @Column(nullable = true)
    private int checkUserId;

    /**
     * 反馈截图信息
     */
    @Column(name = "screenshot", columnDefinition = "text")
    private String screenshot;

    /**
     * 申领标注任务时间
     */
    @Column(name = "assign_label_time", columnDefinition = "timestamp", length = 6)
    private Date assignLabelTime;

    /**
     * 申领审核任务时间
     */
    @Column(name = "assign_check_time", columnDefinition = "timestamp", length = 6)
    private Date assignCheckTime;

    /**
     * 完成标注时间
     */
    @Column(name = "finish_label_time", columnDefinition = "timestamp", length = 6)
    private Date finishLabelTime;

    /**
     * 完成校验时间
     */
    @Column(name = "finish_check_time", columnDefinition = "timestamp", length = 6)
    private Date finishCheckTime;

    /**
     * 提交次数
     */
    @Column(nullable = true)
    private int commitCount;

    /**
     * 变化检测对比文件id
     */
    @Column(nullable = true)
    private int relatedFileId;

    /**
     * 预处理结果路径
     */
    @Column(name = "preprocess_path")
    private String preprocessPath;
}
