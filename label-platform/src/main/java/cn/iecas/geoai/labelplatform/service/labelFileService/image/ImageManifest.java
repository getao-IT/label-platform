package cn.iecas.geoai.labelplatform.service.labelFileService.image;

import cn.iecas.geoai.labelplatform.entity.domain.Image;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import cn.iecas.geoai.labelplatform.service.labelFileService.Manifest;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@lombok.Data
@JSONType(orders={"datasetinfo","data"})
public class ImageManifest extends Manifest {

    /**
     * datasetinfo : {"id":23,"description":"dataset description","createdTime":"20200401 09:11:52","creator":"user","keywords":"飞机,目标检测","taskid":"标注任务ID","version":"1.0"}
     * data : [{"annotation":{"source":{"id":"1","filename":"2012_004331.jpg","origin":"数据来源"},"objects":{"object":[{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}},{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}}]},"segmentation":{"resultfile":["精细化标注_001_Results.jpg","精细化标注_001_Results.jpg"]}}},{"annotation":{"source":{"id":"1","filename":"2012_004331.jpg","origin":"数据来源"},"objects":{"object":[{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}},{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}}]},"segmentation":{"resultfile":["精细化标注_001_Results.jpg","精细化标注_001_Results.jpg"]}}}]
     */

    private List<Data> data;


    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public void addData(Image image, Image changeImage, String label, int segmentation){
        Data data = new Data(image, changeImage,label,segmentation);
        if(getData()==null)
            setData(new ArrayList<>());
        getData().add(data);
    }



    @JSONType(orders={"source", "relatedFile", "objects","segmentation"})
    public static class Data {
        /**
         * source : {"id":"1","filename":"2012_004331.jpg","origin":"数据来源"}
         * relatedFile : {"filename": "12.tiff","id": 43351,"origin": null,"path": "file-data/1123123/1.tif"}
         * objects : {"object":[{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}},{"coordinate":"pixel/geodegree","type":"rectangle/polygon","description":"描述信息","possibleresult":[{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"},{"name":"目标类别","probability":"0.55"}],"points":{"point":["51.315239, 25.134576","51.315873, 25.134574","51.315872, 25.133982","51.315237, 25.133983","51.315239, 25.134576"]}}]}
         * segmentation : {"resultfile":["精细化标注_001_Results.jpg","精细化标注_001_Results.jpg"]}
         */

        private Source source;
        private RelatedFile relatedFile;
        private JSONObject objects;
        private Segmentation segmentation;

        public Data(Image image, Image changeImage, String label, int segmentationValue){
            source = new Source();
            source.setFilename(image.getImageName());
            source.setId(image.getId());
            source.setOrigin(image.getSource());
            source.setPath(image.getPath());
            relatedFile = new RelatedFile();
            relatedFile.setFilename(changeImage.getImageName());
            relatedFile.setId(changeImage.getId());
            relatedFile.setOrigin(changeImage.getSource());
            relatedFile.setPath(changeImage.getPath());
            objects = JSONObject.parseObject(label);
            this.segmentation = new Segmentation();
            this.segmentation.setSegmentation(segmentationValue);
        }

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

        public void setObjects(String objects) {
            this.objects = JSONObject.parseObject(objects);
        }

        public Segmentation getSegmentation() {
            return segmentation;
        }

        public void setSegmentation(Segmentation segmentation) {
            this.segmentation = segmentation;
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
            private String path;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
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

        private static class RelatedFile {
            /**
             * id : 文件ID
             * filename : 文件名称
             * origin :
             * path : 文件路径
             */
            private int id;

            private String filename;

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
    }
}
