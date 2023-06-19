package cn.iecas.geoai.labelplatform.service.impl;

import cn.iecas.geoai.labelplatform.dao.DatabaseMapper;
import cn.iecas.geoai.labelplatform.entity.domain.DatabaseConfig;
import cn.iecas.geoai.labelplatform.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@EnableConfigurationProperties
public class DatabaseServiceImpl implements DatabaseService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private DatabaseMapper databaseMapper;


    @Override
    public void initDatabase(){
        List<String> tables = databaseConfig.getTables();
        List<String> views = databaseConfig.getViews();
        List<String> tableNameList = databaseMapper.selectTableNames();
        List<String> viewNameList = databaseMapper.selectViewNames();
        InputStreamReader inputStreamReader;
        InputStream inputStream;
        System.out.println("已查到所有数据库名:"+tableNameList);
        try {
            ScriptRunner runner=new ScriptRunner(dataSource.getConnection());
            runner.setAutoCommit(false);
            runner.setStopOnError(true);
            runner.setSendFullScript(false);
            runner.setLogWriter(null);
            runner.setFullLineDelimiter(false);
            runner.setDelimiter(";");
            for(String table:tables){
                if(tableNameList.indexOf(table)>-1){
                    log.info("表：{} 已经存在",table);
                }
                else {
                    inputStream = this.getClass().getClassLoader().getResourceAsStream("sql/"+table+".sql");
                    inputStreamReader=new InputStreamReader(inputStream,"utf-8");
                    runner.runScript(inputStreamReader);
                    log.info("创建表:{}",table);
                }
            }

//            for(String view : views){
//                if (viewNameList.indexOf(view)>-1)
//                    log.info("视图：{} 已经存在",view);
//                else {
//                    switch(view){
//                        case "label_dataset_image_info" : databaseMapper.createDatasetImageInfo(); log.info("创建视图:{}",view); break;
//                        default: break;
//                    }
//                }
//
//            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
