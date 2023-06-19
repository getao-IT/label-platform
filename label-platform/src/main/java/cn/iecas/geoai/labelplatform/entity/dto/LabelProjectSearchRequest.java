package cn.iecas.geoai.labelplatform.entity.dto;

import cn.iecas.geoai.labelplatform.entity.domain.LabelProject;
import cn.iecas.geoai.labelplatform.entity.emun.LabelProjectStatus;
import lombok.Data;

import javax.validation.constraints.Min;
import java.sql.Timestamp;
import java.util.Calendar;

@Data
public class LabelProjectSearchRequest {

    /**
     * 页码
     */
    private int pageNo = 1;

    /**
     * 发布员id
     */
    @Min(value = 1,message = "发布员id必须为正整数")
    private int userId;

    /**
     * 页面数量
     */
    private int pageSize = 10;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目状态
     */
    private LabelProjectStatus labelProjectStatus;

    /**
     * 项目类型
     */
    private String category;

    /**
     * 关键字
     */
    private String keywords;

    /**
     * 内容
     */
    private String projectDescription;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * 开始时间
     */
    private Timestamp endTime;


    /**
     * 根据字段排序
     */
    private String orderByCol;

    private String orderByWay;

    public void setStartTime(Timestamp startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.HOUR, -8);
        this.startTime = new Timestamp(calendar.getTimeInMillis());
    }

    public void setEndTime(Timestamp endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.add(Calendar.HOUR, -8);
        this.endTime = new Timestamp(calendar.getTimeInMillis());
    }
}
