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
@Table(name = "label_plan_selected_record")
public class LabelPlanSelectedRecord implements Serializable {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    @Column(nullable = true)
    private int labelPlanId;

    @Column(nullable = true)
    private int selectBatchNo;

    @Column(name = "country", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String country;

    @Column(name = "satellite", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String satellite;

    @Column(name = "resolution", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String resolution;

    @Column(name = "payload", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String payload;

    @Column(name = "label_object", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String labelObject;

    @Column(nullable = true)
    private int objectCount;

    @Column(nullable = true)
    private int fileCount;

    @Column(name = "file_id_list", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String fileIdList;

    @Column(name = "base", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String base;

    @Column(name = "selected_percent", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String selectedPercent;

}
