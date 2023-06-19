package cn.iecas.geoai.labelplatform.service.labelFileService.audio;

import cn.iecas.geoai.labelplatform.service.labelFileService.Manifest;
import cn.iecas.geoai.labelplatform.service.labelFileService.video.VideoManifest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.ArrayList;
import java.util.List;

public class AudioManifest extends Manifest {

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

    @JSONType(orders={"source","results"})
    public static class Data {
        /**
         * source : {"id":"1","filename":"2012_004331.jpg","origin":"数据来源"}
         * objects : {"object":[{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}},{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}}]}
         * segmentation : {"resultfile":["精细化标注_001_Results.jpg","精细化标注_001_Results.jpg"]}
         */

        private Source source;
        private JSONArray results;

        public Data(int fileId, String fileName, String fileSource, JSONObject label) {
            source = new Source();
            source.setId(fileId);
            source.setOrigin(fileSource);
            source.setFilename(fileName);
            results = label.getJSONArray("results");
        }


        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }

        public JSONArray getResults(){
            return this.results;
        }

        public void setResults(String label){
            results = JSONObject.parseObject(label).getJSONArray("results");
        }
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
}
