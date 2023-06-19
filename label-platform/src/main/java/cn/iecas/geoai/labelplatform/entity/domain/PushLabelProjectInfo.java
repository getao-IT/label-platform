package cn.iecas.geoai.labelplatform.entity.domain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class PushLabelProjectInfo implements Serializable {
    private static final long serialVersionUID = 6615363992679658505L;

    /**
     * 用户ID
     */
    private int userId;

    /**
     * 标注项目ID
     */
    private int labelProjectId;

    /**
     * 标注任务ID
     */
    private int labelTaskId;

    /**
     * 标注任务对应文件信息
     */
    private JSONArray imagePathInfo;

    /**
     * 变化检测关联文件信息
     */
    private JSONArray compareImagePathInfo;

    /**
     * 标签
     */
    private String keywords;

    /**
     * 用户token
     */
    private String token;
    /**
     * 专用软件名称
     */
    private String spSoftwareName;
}
