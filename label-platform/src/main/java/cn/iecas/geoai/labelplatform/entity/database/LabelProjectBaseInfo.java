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
@Table(name = "label_project_base_info")
public class LabelProjectBaseInfo implements Serializable {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    @Column(nullable = true)
    private int labelProjectId;

    @Column(name = "base", columnDefinition = "varchar(255) COLLATE \"pg_catalog\".\"default\"")
    private String base;

}
