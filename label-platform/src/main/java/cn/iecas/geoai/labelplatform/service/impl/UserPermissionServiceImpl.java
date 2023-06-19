package cn.iecas.geoai.labelplatform.service.impl;

import cn.aircas.utils.file.FileUtils;
import cn.iecas.geoai.labelplatform.service.UserPermissionService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;


@Slf4j
@Service
@PropertySource(value = "classpath:application.yml")
public class UserPermissionServiceImpl implements UserPermissionService {

    @Value("${value.api.user-info-export}")
    private String userInfoApi;

    @Value("${value.dir.rootDir}")
    private String rootDir;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest request;


    /**
     * 获取用户权限管理用户列表
     * @return
     */
    @Override
    public JSONObject getUserInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", request.getHeader("token"));
        HttpEntity<JSONObject> entity = new HttpEntity<>(null, headers);
        JSONObject body = restTemplate.exchange(userInfoApi, HttpMethod.GET, entity, JSONObject.class).getBody();
        return body;
    }

    /**
     * 导出用户权限管理用户数据为CSV文件
     */
   @Override
   public void exportUserToCsv(String fileName, HttpServletResponse response) {
       JSONObject userInfo = getUserInfo();
       String[] data = userInfo.get("data").toString().split("\n");
       String fileSavePath = FileUtils.getStringPath(this.rootDir, "userTemp", fileName);
       File file = new File(fileSavePath);
       BufferedWriter bufferedWriter = null;
       try {
           if (!file.getParentFile().exists()) {
               Files.createDirectory(Paths.get(file.getParent()));
           }
           // 服务器本地临时文件，下载过后需要删除
           bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), 1024);
           String[] lineDatas;
           StringJoiner line = null;
           for (int i = 0; i < data.length; i++) {
               line = new StringJoiner(",");
               lineDatas = data[i].split(",");
               for (int i1 = 0; i1 < lineDatas.length; i1++) {
                   if (i1 == 0 || i1 == 2 || i1 == 3 || i1 == 4 || i1 == 5 || i1 == 7
                           || i1 == 8 || i1 == 9 || i1 == 10) {
                        //System.out.println("i="+i+"---i1="+i1);
                       if (i1 == 7 || i1 == 10) {
                           lineDatas[i1] = lineDatas[i1]+"\t";
                       }
                        line.add(lineDatas[i1]);
                   }
               }
               bufferedWriter.write(line.toString());
               bufferedWriter.newLine();
           }
           bufferedWriter.close();
           // 下载服务器文件
           response.setContentType("application/octet-stream");
           response.setHeader("content-type","application/octet-stream");
           response.setHeader("Content-Disposition","attachment;filename=users_info.csv");
           response.setCharacterEncoding("UTF-8");
           BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
           ServletOutputStream outputStream = response.getOutputStream();
           outputStream.write(new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF});
           int len = 0;
           byte[] buffer = new byte[1024];
           while ((len=inputStream.read(buffer))!= -1) {
               outputStream.write(buffer, 0, len);
           }
           if (outputStream != null) {
               outputStream.close();
           }
           if (inputStream != null) {
               inputStream.close();
           }
           // 删除临时文件
           Files.delete(Paths.get(fileSavePath));
       } catch (FileNotFoundException e) {
           log.error("FileNotFoundException 文件不存在异常：{} ", e.getMessage());
       } catch (IOException e) {
           log.error("IOException IO异常：{} ", e.getMessage());
       }

    }
}
