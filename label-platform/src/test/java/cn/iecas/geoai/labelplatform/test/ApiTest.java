package cn.iecas.geoai.labelplatform.test;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.domain.LabelProject;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTask;
import cn.iecas.geoai.labelplatform.entity.emun.LabelProjectStatus;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import cn.iecas.geoai.labelplatform.entity.emun.SampleSetCategory;
import cn.iecas.geoai.labelplatform.service.LabelDatasetFileService;
import cn.iecas.geoai.labelplatform.service.LabelProjectService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class ApiTest {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LabelDatasetFileService labelDatasetFileService;

    class LabelProjectSub {
        int id;
        int datasetId;
        public LabelProjectSub(int id, int datasetId) {
            this.id = id;
            this.datasetId = datasetId;
        }
    }


    @Test
    public void applyDatasetFile() {
        int[] userIds = new int[]{0,1,15};
        List<LabelDatasetFile> labelDatasetFiles = labelDatasetFileService.list(new QueryWrapper<LabelDatasetFile>().eq("dataset_id", 13));
        int len = labelDatasetFiles.size();
        if (labelDatasetFiles.size() > userIds.length) {
            len = userIds.length;
        }
        for (int i = 0; i < len; i++) {
            labelDatasetFiles.get(i).setLabelUserId(userIds[i]);
            labelDatasetFileService.updateById(labelDatasetFiles.get(i));
        }
    }
    @Test
    public void testCacheWork() throws InterruptedException {
        for (int i = 1; i <= 100; i+=50) {
            long t = System.currentTimeMillis();
            this.executeCache(i, 50);
            System.out.println("获取第"+i+"到"+(i+50)+"帧耗时：" + (System.currentTimeMillis() - t));
        }
    }

    private void executeCache(int frameNumber, int returnNumber) {
//        String url = "http://localhost:8085/geoai/V1/label-platform/task/getFrameImgByFrameNbr";
//        String videoPath = "\\录屏\\Wildlife.mp4";
        String url = "http://192.168.3.13:18081/geoai/V1/label-platform/task/getFrameImgByFrameNbr";
        String videoPath = "/file-data/video/1665996629950/Wildlife.mp4";
        String token = "ZDQ3MzVlM2EyNjVlMTZlZWUwM2Y1OTcxOGI5YjVkMDMwMTljMDdkOGI2YzUxZjkwZGEzYTY2NmVlYzEzYWIzNTUwNmJlN2YyYjMyZGM2MjJkY2MwODg5M2Q1ODE0ZjU0NzY0ZDc3MDc2YWZkMGMxOWVmMmRkZmI0OGFlOGI2MDQtMg==";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", token);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        UriComponents build = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("videoPath", videoPath)
                .queryParam("frameNumber", frameNumber)
                .queryParam("returnNumber", returnNumber).build();
        JSONObject result = restTemplate.exchange(build.encode().toUri(), HttpMethod.GET, entity, JSONObject.class).getBody();
        for (int i = 0; i < result.getJSONArray("data").size(); i++) {
            System.out.println(result.getJSONArray("data").getJSONObject(i).get("frameNumber"));
        }
    }

    @Test
    public void testGetDatasetList() {
        String arrStr = "1,2,3";
        ArrayList<Object> objects = new ArrayList<>();
        Collections.addAll(objects, arrStr);
        System.out.println(objects);
        System.out.println(new Date());
        System.out.println(new Timestamp(new Date().getTime()));
        System.out.println(cn.aircas.utils.date.DateUtils.nowDate());
        System.out.println(cn.aircas.utils.date.DateUtils.nowDate("yyyy-MM-dd HH:mm:ss"));
//        String url = "http://localhost:8085/geoai/V1/label-platform/dataset";
        String url = "http://192.168.3.13:18081/geoai/V1/label-platform/dataset";

        int frameNumber = 105;
        String token = "ZDQ3MzVlM2EyNjVlMTZlZWUwM2Y1OTcxOGI5YjVkMDMwMTljMDdkOGI2YzUxZjkwZGEzYTY2NmVlYzEzYWIzNWU4ZTI0MjQ0YmYyOGE3YjA2OTlmZjRkOTc5NzVhMDg0NjE1NzdkZGVkOTA1OWQzOTQ5ZmQ2MzlkOTFjZDc4OTEtMg==";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", token);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        UriComponents build = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("userId", 2)
                .queryParam("datasetName", "集")
                .queryParam("datasetType", "VIDEO").build();
        long t = System.currentTimeMillis();
        JSONObject result = restTemplate.exchange(build.encode().toUri(), HttpMethod.GET, entity, JSONObject.class).getBody();
        System.out.println("耗时：" + (System.currentTimeMillis()-t));
        System.out.println(result.size());
    }

    @Test
    public void testStatus() {
        String url = "http://192.168.3.13:18081/geoai/V1/label-platform/status";
        String token = "ZDQ3MzVlM2EyNjVlMTZlZWUwM2Y1OTcxOGI5YjVkMDMwMTljMDdkOGI2YzUxZjkwZGEzYTY2NmVlYzEzYWIzNWZlODUyYjRlN2E2ODAzZWFiNzY0YmFlNGUyNDU0NGYyYTdhMzEwZGU1MDFmY2QyODc2ZTYyOWU4NTllZDYyOTctMg==";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", token);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        UriComponents build = UriComponentsBuilder.fromHttpUrl(url).build();
        long t = System.currentTimeMillis();
        Integer result = restTemplate.exchange(build.encode().toUri(), HttpMethod.GET, entity, Integer.class).getBody();
        System.out.println("耗时：" + (System.currentTimeMillis()-t));
        System.out.println(result);
    }

    @Test
    public void testSegmentation() throws IOException {
        String filePath = "C:\\Users\\dell\\Desktop\\1815.jpg";
        String outputPath = "C:\\Users\\dell\\Desktop\\imageSegmentation\\1815.jpg";
        File file = new File(filePath);
        String url = "http://192.168.14.11:18900/convert";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("file", new FileSystemResource(file));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);
        String result = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        System.out.println(result);
        StringBuilder binSb = new StringBuilder();
        char[] array = result.toCharArray();
        for (int i = 0; i < array.length; i++) {
            String binaryString = Integer.toBinaryString(array[i]);
            binSb.append(binaryString);
        }
        System.out.println(binSb);
        char[] chars = binSb.toString().toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = Byte.parseByte(String.valueOf(chars[i]));
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outputPath));
        int len=0;
        byte[] buffer = new byte[1024];
        while ((len=inputStream.read(buffer)) != -1) {
            bufferedOutputStream.write(buffer, 0, len);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        inputStream.close();
    }

    @Test
    public void ListFilterTest() {
        LabelProject labelProject = new LabelProject();
        labelProject.setId(100);
        labelProject.setProjectName("test标注项目");
        List<LabelProject> list = new ArrayList<>();
        list.add(labelProject);
        System.out.println("过滤前：" + list);
        List<LabelProjectSub> collect = list.stream().map(result -> new LabelProjectSub(result.getId(), result.getDatasetId())).collect(Collectors.toList());
        System.out.println("过滤后：" + collect);

    }

    @Test
    public void rateTest() {
        RestTemplate restTemplate = new RestTemplate();
        HttpServletRequest request = (HttpServletRequest) RequestContextHolder.getRequestAttributes();
        String token = "NmI4NmIyNzNmZjM0ZmNlMTlkNmI4MDRlZmY1YTNmNTc0N2FkYTRlYWEyMmYxZDQ5YzAxZTUyZGRiNzg3NWI0YmMxMzJjNWIxZmUxN2Q1ZjhkZTZlZGJkMzljYzNmYzg0M2FhNTlmZjJhODEzNzBlZDFjMTViZmExYmI3NTNmMWEtMQ==";
        String url = "http://localhost:8085//geoai/V1/label-platform/task/getLabelTaskStatusRate";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", token);
        HttpEntity<MultiValueMap<String, LabelProject>> stringHttpEntity = new HttpEntity<>(null, httpHeaders);
        String body = restTemplate.exchange(url, HttpMethod.GET, stringHttpEntity, String.class).getBody();
        System.out.println(body);
    }

    @Test
    public void enumTest() {
        int i = 0;
            System.out.println(SampleSetCategory.DETECTION);
    }

    @Test
    public void createLabelTaskStatisTest() {
        RestTemplate restTemplate = new RestTemplate();
        HttpServletRequest request = (HttpServletRequest) RequestContextHolder.getRequestAttributes();
        String token = "NmI4NmIyNzNmZjM0ZmNlMTlkNmI4MDRlZmY1YTNmNTc0N2FkYTRlYWEyMmYxZDQ5YzAxZTUyZGRiNzg3NWI0YjgxZjQxYzdiNGE3OTBlZjc1ZTllMWIxMzczMmYxOWIyMDJkNzQwODdmYWFmYTIwNmU0OTgwZDRlNjQ5MzJiMGUtMQ==";
        String url = "http://localhost:8085//geoai/V1/label-platform/project";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", token);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject jsonPramas = new JSONObject();
        jsonPramas.put("datasetId", 1);
        jsonPramas.put("totalCount", 10);
        jsonPramas.put("isAiLabel", false);
        jsonPramas.put("createTime", new Date());
        jsonPramas.put("finishCount", 0);
        jsonPramas.put("userId", 2);
        jsonPramas.put("category", "1");
        jsonPramas.put("userName", "1");
        jsonPramas.put("keywords", "飞机");
        jsonPramas.put("useLabel", false);
        jsonPramas.put("projectName", "统计流程测试");
        jsonPramas.put("labelUserIds", "1,2,3");
        jsonPramas.put("checkUserIds", "1,2");
        jsonPramas.put("defaultLabelCount", 50);
        jsonPramas.put("defaultCheckCount", 50);
        jsonPramas.put("relatedDatasetId", 1);
        jsonPramas.put("projectDescription", "1");
        jsonPramas.put("status", LabelProjectStatus.LABELING);
        jsonPramas.put("serviceId", "1");
        jsonPramas.put("datasetType", DatasetType.IMAGE);
        HttpEntity<JSONObject> stringHttpEntity = new HttpEntity<>(jsonPramas, httpHeaders);

        UriComponentsBuilder urlParams = UriComponentsBuilder.fromHttpUrl(url).queryParam("request", request);
        ResponseEntity<String> exchange = restTemplate.exchange(urlParams.build().encode().toUri(), HttpMethod.POST, stringHttpEntity, String.class);
        String body = exchange.getBody();
        System.out.println(body);
    }

    @Test
    public void ApplyFileTest() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String token = "NmI4NmIyNzNmZjM0ZmNlMTlkNmI4MDRlZmY1YTNmNTc0N2FkYTRlYWEyMmYxZDQ5YzAxZTUyZGRiNzg3NWI0YjgxZjQxYzdiNGE3OTBlZjc1ZTllMWIxMzczMmYxOWIyMDJkNzQwODdmYWFmYTIwNmU0OTgwZDRlNjQ5MzJiMGUtMQ==";
        String url = "http://localhost:8085/geoai/V1/label-platform/task/apply";
        headers.add("token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Integer> entity = new HttpEntity<>(null, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("taskId", 341);
        String body = restTemplate.exchange(builder.encode().build().toUri(), HttpMethod.GET, entity, String.class).getBody();
        System.out.println(body);

    }

    @Test
    public void commitTest() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8085/geoai/V1/label-platform/task/commit";
        String token = "NmI4NmIyNzNmZjM0ZmNlMTlkNmI4MDRlZmY1YTNmNTc0N2FkYTRlYWEyMmYxZDQ5YzAxZTUyZGRiNzg3NWI0YjM0OGUwNGJkNTBhNmUyOGE5NzcyNTk2YTJjYjNmYWIwY2FlODA2Zjg0MTcwMmI4MmNhZjg3MDE3MjNlODBiNzAtMQ==";
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject params = new JSONObject();
        params.put("labelTaskId", 2);
        params.put("labelFileId", 1);
        params.put("labelInfo", "军舰");
        params.put("feedback", "反馈信息");
        params.put("screenshot", "反馈图像信息");
        params.put("status", LabelStatus.FEEDBACK);
        params.put("labelNumber", 2);
        HttpEntity<JSONObject> entity = new HttpEntity<>(params, headers);
        String body = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        System.out.println(body);
    }

    @Test
    public void funTest() {

        LabelTask task = new LabelTask();
        task.setPublisherId(1);
        task.setKeywords("akfjaklfjakljfklasd");
        LabelTask task1 = new LabelTask();
        task.setPublisherId(2);
        task.setKeywords("jjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
        List<LabelTask> tasksList = new ArrayList<>();
        tasksList.add(task);
        tasksList.add(task1);
        System.out.println(tasksList);
        tasksList.remove(task);
        System.out.println(tasksList);

        List<Integer> list = new ArrayList<Integer>(){
            {add(111);}
            {add(222);}
            {add(333);}
            {add(111);}
            {add(222);}
        };


        Map<Integer, List<Integer>> collect = list.stream().collect(Collectors.groupingBy(Integer::bitCount));
        Map<Integer, Integer> collect1 = list.stream().collect(Collectors.toMap(Integer::bitCount, a -> a, (k1, k2) -> k1));

        String json1 = "[{\"name\":\"sdfsaf\",\"color\":\"rgb(59,146,247)\"}]";
        String json2 = "[{\"name\":\"sdfsaf\",\"color\":\"rgb(59,146,247)\"},{\"name\":\"时间\",\"color\":\"rgb(174,119,187)\"},{\"name\":\"人名\",\"color\":\"rgb(230,219,85)\"}]";
        JSONArray parse = JSONObject.parseArray(json1);
        JSONArray parse1 = JSONObject.parseArray(json2);
        JSONObject result = new JSONObject();

        Set<JSONObject> hashSet = new HashSet<>();
        hashSet.addAll(parse.toJavaList(JSONObject.class));
        hashSet.addAll(parse1.toJavaList(JSONObject.class));
        System.out.println(result);
    }

    @Test
    public void getStatisInfoByUserIds() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        String token = "NmI4NmIyNzNmZjM0ZmNlMTlkNmI4MDRlZmY1YTNmNTc0N2FkYTRlYWEyMmYxZDQ5YzAxZTUyZGRiNzg3NWI0YjgyNzQyYTFiNTZkODQ0OWRmMDJlNmEwZTNjMzZjMGRlMzYyOTcyMzI4MzE1OTNkNTFiMjJmNjk0MDVkOTgxZWMtMQ==";
        httpHeaders.add("token", token);
        List<Integer> userIdList = new ArrayList<>();
        int projectId = 124;
        String url = "http://localhost:8085//geoai/V1/label-platform/task/getLabelTaskStatisInfoByUserIds";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("userIdList", "2,3")
                .queryParam("projectId", projectId);
        HttpEntity entity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, JSONObject.class);
        JSONObject body = exchange.getBody();
        System.out.println(body);
    }

    @Test
    public void getStatisInfoByProjectId() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        String token = "NmI4NmIyNzNmZjM0ZmNlMTlkNmI4MDRlZmY1YTNmNTc0N2FkYTRlYWEyMmYxZDQ5YzAxZTUyZGRiNzg3NWI0YmQ0MmE4YTUxZDQyYjYxNjI3ZmMyNDhlY2VjZmFkODNjYjllYjE5Y2U1NmIyODkxNGMwYmIwYjA5NjJlZjJiZjktMQ==";
        httpHeaders.add("token", token);
        String url = "http://localhost:8085//geoai/V1/label-platform/project/getCommitInfo";
        HttpEntity httpEntity = new HttpEntity(null, httpHeaders);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("projectId", 162).encode();
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, httpEntity, JSONObject.class);
        JSONObject body = exchange.getBody();
        System.out.println(body);
    }
    @Autowired
    private LabelProjectService labelProjectService;

    @Test
    public void testDateConvert() throws ParseException {



        int target = 150;
        int temp = 0;
        List<Integer> intArr = new ArrayList<>();
        for (int i = 0; i < 10000000; i++) {
            if (i < 4000 && i > 100) {
                intArr.add(150);
            } else if (i < 700 && i > 650) {
                intArr.add(200);
            } else {
                intArr.add(i);
            }
        }
        // 遍历
        temp = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            if (intArr.get(i) == 150) {
                temp++;
            }
        }
        System.out.println("遍历得到的结果："+target+"有："+temp+"个，耗时："+(System.currentTimeMillis()-start)+"毫秒");


        // Arrays二分法查找
Object[] array = intArr.toArray();
        start = System.currentTimeMillis();
        temp = Arrays.binarySearch(array, target);
        System.out.println("遍历得到的结果："+target+"有："+temp+"个，耗时："+(System.currentTimeMillis()-start)+"毫秒");



        System.out.println(cn.aircas.utils.date.DateUtils.nowDate());
        System.out.println(new Date());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lat","39");
        jsonObject.put("lng","39");
        System.out.println(jsonObject);

        ArrayList<Integer> objects = new ArrayList<>();
        objects.add(1);
        objects.add(2);
        objects.add(3);
        objects.add(4);
        List<String> collect = objects.stream().map(String::valueOf).collect(Collectors.toList());
        System.out.println(objects + "-----" + collect);

        LabelProject project = new LabelProject();
        Date parse = new SimpleDateFormat().parse("2022-04-25 12:25:00");
        Date parse1 = new SimpleDateFormat().parse("2022-04-25 09:26:00");
        long consumeMillis = project.getFinishTime().getTime()
                - project.getCreateTime().getTime();
        String consumeTime = cn.iecas.geoai.labelplatform.util.DateUtils.millisToTime(consumeMillis);
        project.setConsumeTime(consumeTime);



        int resultArr[][] = new int[9][9];
        for (int i = 0; i < resultArr.length; i++) {
            for (int j = 0; j < resultArr[i].length; j++) {
                System.out.print("("+i+","+j+")->"+resultArr[i][j]+"  ");
            }
            System.out.println("");
        }

        for (int i = 0; i < 9; i++) {
            System.out.println(i);
            for (int j = 0; j < 9; j++) {
                if (i > 5) {
                    System.out.println("sdjflakfjdklfjasklf");
                    break;
                }
            }
        }

        int m = 1/0;
        int[] a = new int[]{1,2,3};
        System.out.println(a);
        List<int[]> list = new ArrayList<>();
        list.add(a);
        int[][] arr = new int[9][9];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                System.out.print("("+i+"，"+j+")---");
            }
            System.out.println("\n");
        }
        double second = (double) 1001 / 1000;
        BigDecimal bigDecimal = new BigDecimal(second).setScale(0, RoundingMode.DOWN);
        BigDecimal bigDecimal1 = new BigDecimal(second).setScale(0, RoundingMode.HALF_DOWN);
        BigDecimal bigDecimal2 = new BigDecimal(second).setScale(0, RoundingMode.UP);
        BigDecimal bigDecimal3 = new BigDecimal(second).setScale(0, RoundingMode.HALF_UP);
        System.out.println("adfasf");

    }
}
