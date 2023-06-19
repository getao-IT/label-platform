package cn.iecas.geoai.labelplatform.dao;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DatabaseMapper {
    List<String>  selectTableNames();
    List<String> selectViewNames();
    void DatasetImageInfo();

}
