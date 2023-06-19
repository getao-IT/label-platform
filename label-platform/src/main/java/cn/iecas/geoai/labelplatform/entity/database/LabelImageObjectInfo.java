package cn.iecas.geoai.labelplatform.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;


@Entity
@Data
@Table(name = "label_image_object_info")
public class LabelImageObjectInfo {
    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    private String country;

    private String sensor;

    private String satellite;

    private String resolution;

    private String label1;

    private String label2;

    private String label3;

    private String label4;

    private String label5;

    private String label6;

    @Column(nullable = true)
    private int imageId;

    private String base;

    @Column(nullable = true)
    private int planId;

    @Column(name = "selected", columnDefinition = "bool default false")
    private boolean selected;
}
