package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.emun.CoordinateSystemType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelFileType;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Date;

@Data
public class LabelExportParam {
    /**
     * 影像分配id
     */
    private int fileId;

    /**
     * 影像标注
     */
    private String label;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 是否导出正框
     */
    private boolean isRectangle;

    /**
     * 文件信息
     */
    private JSONObject fileInfo;

    /**
     * 文件类型
     */
    private DatasetType fileType;


    /**
     * 导出标注文件类型
     */
    private LabelFileType labelFileType;
}
