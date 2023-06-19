package cn.iecas.geoai.labelplatform.runner;


import cn.iecas.geoai.labelplatform.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@ConditionalOnProperty(value = "value.database.enable", havingValue = "true")
public class DatabaseInitRunner implements ApplicationRunner {

    @Autowired
    DatabaseService databaseService;

    @Override
    public void run(ApplicationArguments arguments){
        databaseService.initDatabase();
    }



}
