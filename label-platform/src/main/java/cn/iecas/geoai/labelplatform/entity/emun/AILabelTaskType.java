package cn.iecas.geoai.labelplatform.entity.emun;



public enum AILabelTaskType {

    IMAGE(1, "IMAGE"),

    TEXT(2, "TEXT"),

    ELEC(3, "ELEC"),

    VIDEO(4, "VIDEO"),

    AUDIO(5, "AUDIO");

    int taskCode;

    String taskType;

    AILabelTaskType(int taskCode, String taskType) {
        this.taskCode = taskCode;
        this.taskType = taskType;
    }

    public int getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(int taskCode) {
        this.taskCode = taskCode;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
