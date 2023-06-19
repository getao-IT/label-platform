package cn.iecas.geoai.labelplatform.service.labelFileService;

import cn.iecas.geoai.labelplatform.entity.common.DatasetType;
import cn.iecas.geoai.labelplatform.entity.domain.LabelDataset;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.Date;

public class Manifest {

    protected DatasetInfo datasetInfo;


    public DatasetInfo getDatasetInfo() {
        return datasetInfo;
    }

    public void setDatasetInfo(LabelDataset labelDataset) {
        datasetInfo = new DatasetInfo();
        datasetInfo.setId(labelDataset.getId());
        datasetInfo.setKeywords(labelDataset.getKeywords());
        datasetInfo.setCreatedTime(labelDataset.getCreateTime());
        datasetInfo.setDatasetType(labelDataset.getDatasetType());
        datasetInfo.setDescription(labelDataset.getDescription());
        datasetInfo.setCategory(labelDataset.getProjectCategory());
    }


    @JSONType(orders={"id","description","createdTime","creator","keywords","datasetType","taskid","version", "category"})
    public static class DatasetInfo {
        /**
         * id : 23
         * description : dataset description
         * createdTime : 20200401 09:11:52
         * creator : user
         * keywords : 飞机,目标检测
         * taskid : 标注任务ID
         * version : 1.0
         * category :
         */

        private int id;
        private int projectId;
        private String creator;
        private String keywords;
        private Date createdTime;
        private String description;
        private String version = "1.0";
        private DatasetType datasetType;
        private String category;




        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public int getProjectId() {
            return projectId;
        }

        public void setProjectId(int projectId) {
            this.projectId = projectId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public DatasetType getDatasetType(){return datasetType;}

        public void setDatasetType(DatasetType datasetType){this.datasetType = datasetType;}

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}
