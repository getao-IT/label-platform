package cn.iecas.geoai.labelplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;



@EnableAsync
@EnableCaching
@SpringBootApplication
@EnableTransactionManagement
@ServletComponentScan
public class LabelPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabelPlatformApplication.class, args);
    }

}
