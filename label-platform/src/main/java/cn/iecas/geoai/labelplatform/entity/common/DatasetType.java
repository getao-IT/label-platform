package cn.iecas.geoai.labelplatform.entity.common;

import cn.iecas.geoai.labelplatform.service.labelFileService.LabelFileService;
import cn.iecas.geoai.labelplatform.util.SpringContextUtil;

/**
 * @author vanishrain
 * 数据集类型
 */
public enum DatasetType {
    IMAGE("IMAGE"), VIDEO("VIDEO"), TEXT("TEXT"), AUDIO("AUDIO"), ELEC("ELEC");

    private String value;

    DatasetType(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }


    public void setValue(String value){
        this.value = value;
    }

    public LabelFileService getLabelFileService(){
        String serviceName = (this.value + "-LABEL-FILE-SERVICE").toUpperCase();
        return (LabelFileService) SpringContextUtil.getBean(serviceName);
    }


}
