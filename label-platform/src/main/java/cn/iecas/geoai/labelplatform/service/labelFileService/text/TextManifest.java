package cn.iecas.geoai.labelplatform.service.labelFileService.text;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.domain.Image;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.service.labelFileService.Manifest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@lombok.Data
@JSONType(orders={"datasetInfo","data"})
public class TextManifest extends Manifest {


    private List<Data> data;


    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public void addData(int fileId, String fileName, String fileSource, JSONObject label){
        Data data = new Data(fileId,fileName,fileSource,label);
        if(getData()==null)
            setData(new ArrayList<>());
        getData().add(data);
    }

    @JSONType(orders={"source","content","entitis"})
    public static class Data {
        /**
         * source : {"id":"1","filename":"2012_004331.jpg","origin":"数据来源"}
         * objects : {"object":[{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}},{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}}]}
         * segmentation : {"resultfile":["精细化标注_001_Results.jpg","精细化标注_001_Results.jpg"]}
         */

        private Source source;
        private String content;
        private List<Entity> entities;

        public Data(int fileId, String fileName, String fileSource, JSONObject label){
            source = new Data.Source();
            source.setId(fileId);
            source.setOrigin(fileSource);
            source.setFilename(fileName);
            content = label.getString("content");
            entities = JSONArray.parseArray(label.getJSONArray("entities").toJSONString(),Entity.class);
        }

        public String getContent(){return content;}
        public void setContent(String content){this.content = content;}

        public Data.Source getSource() {
            return source;
        }

        public void setSource(Data.Source source) {
            this.source = source;
        }

        public List<Entity> getEntities() {
            return entities;
        }

        public void setEntities(String label) {
            this.entities = JSONArray.parseArray(label,Entity.class);
        }



        public static class Source {
            /**
             * id : 1
             * filename : 2012_004331.jpg
             * origin : 数据来源
             */

            private int id;
            private String filename;
            private String origin;

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

        public static class Entity{
            private String type;
            private int endIndex;
            private int startIndex;
            private String mention;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public int getEndIndex() {
                return endIndex;
            }

            public void setEndIndex(int endIndex) {
                this.endIndex = endIndex;
            }

            public int getStartIndex() {
                return startIndex;
            }

            public void setStartIndex(int startIndex) {
                this.startIndex = startIndex;
            }

            public String getMention() {
                return mention;
            }

            public void setMention(String mention) {
                this.mention = mention;
            }
        }
    }

    public static void main(String[] args) {
        LabelDataset labelDataset = new LabelDataset();
        labelDataset.setId(1);
        labelDataset.setDescription("1234");
        labelDataset.setCreateTime(new Date());
        labelDataset.setKeywords("12,132");
        labelDataset.setDatasetType(DatasetType.IMAGE);
        labelDataset.setProjectId(12);
        TextManifest textManifest = new TextManifest();
        textManifest.setDatasetInfo(labelDataset);
        JSONObject label = new JSONObject();
        label.put("content","面对紧张的局势");
        JSONArray jsonArray = new JSONArray();

        JSONObject entity1 = new JSONObject();
        entity1.put("type","国家");
        entity1.put("start_index",8);
        entity1.put("end_index",11);
        entity1.put("mention","乌克兰");

        JSONObject entity2 = new JSONObject();
        entity2.put("type","人物");
        entity2.put("start_index",8);
        entity2.put("end_index",11);
        entity2.put("mention","泽伦斯基");



        jsonArray.add(entity1);
        jsonArray.add(entity2);

        label.put("entities",jsonArray);

        textManifest.addData(1,"1.txt","iecas",label);
        textManifest.addData(2,"2.txt","aircas",label);

        System.out.println("sdf");
    }
}
