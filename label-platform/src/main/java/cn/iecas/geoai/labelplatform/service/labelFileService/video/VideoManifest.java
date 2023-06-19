package cn.iecas.geoai.labelplatform.service.labelFileService.video;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.service.labelFileService.Manifest;
import cn.iecas.geoai.labelplatform.service.labelFileService.text.TextManifest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// 视频标注manifest生成，未测试该类是否可用 TODO getao
@lombok.Data
@JSONType(orders={"datasetInfo","data"})
public class VideoManifest extends Manifest {
    private List<Data> data;


    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public void addData(int fileId, String fileName, String fileSource, String path, JSONObject label){
        Data data = new Data(fileId,fileName,fileSource,path,label);
        if(getData()==null)
            setData(new ArrayList<>());
        getData().add(data);
    }

    @JSONType(orders={"source", "relatedFile", "objects","segmentation"})
    public static class Data {
        /**
         * source : {"id":"1","filename":"2012_004331.jpg","origin":"数据来源"}
         * objects : {"object":[{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}},{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}}]}
         * segmentation : {"resultfile":["精细化标注_001_Results.jpg","精细化标注_001_Results.jpg"]}
         */

        private Source source;
        private RelatedFile relatedFile;
        private JSONObject objects;
        private Segmentation segmentation;

        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }

        public RelatedFile getRelatedFile() {
            return relatedFile;
        }

        public void setRelatedFile(RelatedFile relatedFile) {
            this.relatedFile = relatedFile;
        }

        public JSONObject getObjects() {
            return objects;
        }

        public void setObjects(JSONObject objects) {
            this.objects = objects;
        }

        public Segmentation getSegmentation() {
            return segmentation;
        }

        public void setSegmentation(Segmentation segmentation) {
            this.segmentation = segmentation;
        }

        public Data(int fileId, String fileName, String fileSource, String path, JSONObject label) {
            source = new Source(fileId, fileName, fileSource, path);
            objects = label;
            segmentation = new Segmentation(0, null);
        }

        public static class Source {
            /**
             * id : 1
             * filename : 2012_004331.jpg
             * origin : 数据来源
             * path : 文件路径
             */

            private int id;

            private String filename;

            private String origin;

            private String path;

            public Source(int id, String filename, String origin, String path) {
                this.id = id;
                this.filename = filename;
                this.origin = origin;
                this.path = path;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getFilename() {
                return filename;
            }

            public void setFilename(String filename) {
                this.filename = filename;
            }

            public String getOrigin() {
                return origin;
            }

            public void setOrigin(String origin) {
                this.origin = origin;
            }
        }
    }

    public static class RelatedFile {

        private String filename;

        private int id;

        private String origin;

        private String path;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Segmentation {

        private int segmentation = 0;

        private List<String> resultfile;

        public Segmentation(int segmentation, List<String> resultfile) {
            this.segmentation = segmentation;
            this.resultfile = resultfile;
        }

        public int getSegmentation() {
            return segmentation;
        }

        public void setSegmentation(int segmentation) {
            this.segmentation = segmentation;
        }

        public List<String> getResultfile() {
            return resultfile;
        }

        public void setResultfile(List<String> resultfile) {
            this.resultfile = resultfile;
        }
    }

    public static void main(String[] args) {
        LabelDataset labelDataset = new LabelDataset();
        labelDataset.setId(1);
        labelDataset.setDescription("1234");
        labelDataset.setCreateTime(new Date());
        labelDataset.setKeywords("视频,132");
        labelDataset.setDatasetType(DatasetType.VIDEO);
        labelDataset.setProjectId(12);
        VideoManifest textManifest = new VideoManifest();
        textManifest.setDatasetInfo(labelDataset);

        JSONObject jsonObject = JSONObject.parseObject("{\n" +
                "                \"object\": [\n" +
                "                    {\n" +
                "                        \"id\": 162,\n" +
                "                        \"frameNumber\": 1,\n" +
                "                        \"type\": \"Rectangle\",\n" +
                "                        \"checkStatus\": 0,\n" +
                "                        \"note\": \"\",\n" +
                "                        \"coordinate\": \"pixel\",\n" +
                "                        \"description\": \"像素坐标\",\n" +
                "                        \"entities\":[\n" +
                "                            {\n" +
                "                                \"name\": \"lianheceshi\",\n" +
                "                                \"points\": {\n" +
                "                                    \"point\": [\n" +
                "                                        \"66.62230144443959,170.91167304409737\",\n" +
                "                                        \"66.62230144443959,222.46582685018524\",\n" +
                "                                        \"97.6350447989031,222.46582685018524\",\n" +
                "                                        \"97.6350447989031,170.91167304409737\"\n" +
                "                                    ]\n" +
                "                                }\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"name\": \"lianheceshi\",\n" +
                "                                \"points\": {\n" +
                "                                    \"point\": [\n" +
                "                                        \"66.62230144443959,170.91167304409737\",\n" +
                "                                        \"66.62230144443959,222.46582685018524\",\n" +
                "                                        \"97.6350447989031,222.46582685018524\",\n" +
                "                                        \"97.6350447989031,170.91167304409737\"\n" +
                "                                    ]\n" +
                "                                }\n" +
                "                            }\n" +
                "                        ]\n" +
                "                      \n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": 162,\n" +
                "                        \"frameNumber\": 2,\n" +
                "                        \"type\": \"Rectangle\",\n" +
                "                        \"checkStatus\": 0,\n" +
                "                        \"note\": \"\",\n" +
                "                        \"coordinate\": \"pixel\",\n" +
                "                        \"description\": \"像素坐标\",\n" +
                "                        \"entities\":[\n" +
                "                            {\n" +
                "                                \"name\": \"lianheceshi\",\n" +
                "                                \"points\": {\n" +
                "                                    \"point\": [\n" +
                "                                        \"66.62230144443959,170.91167304409737\",\n" +
                "                                        \"66.62230144443959,222.46582685018524\",\n" +
                "                                        \"97.6350447989031,222.46582685018524\",\n" +
                "                                        \"97.6350447989031,170.91167304409737\"\n" +
                "                                    ]\n" +
                "                                }\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"name\": \"lianheceshi\",\n" +
                "                                \"points\": {\n" +
                "                                    \"point\": [\n" +
                "                                        \"66.62230144443959,170.91167304409737\",\n" +
                "                                        \"66.62230144443959,222.46582685018524\",\n" +
                "                                        \"97.6350447989031,222.46582685018524\",\n" +
                "                                        \"97.6350447989031,170.91167304409737\"\n" +
                "                                    ]\n" +
                "                                }\n" +
                "                            }\n" +
                "                        ]\n" +
                "                      \n" +
                "                    }\n" +
                "                ]\n" +
                "            }");


        textManifest.addData(1,"1.txt","iecas", "/home/data/1.txt", jsonObject);
        textManifest.addData(2,"2.txt","aircas", "/home/data/.txt",jsonObject);

        System.out.println("sdf");
    }
}
