package cn.iecas.geoai.labelplatform;

import cn.aircas.utils.image.geo.GeoUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@EnableAsync
@EnableCaching
@SpringBootApplication
@EnableTransactionManagement
@ServletComponentScan
public class LabelPlatformApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(LabelPlatformApplication.class, args);
    }

}
