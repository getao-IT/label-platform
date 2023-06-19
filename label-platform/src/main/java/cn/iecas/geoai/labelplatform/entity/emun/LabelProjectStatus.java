package cn.iecas.geoai.labelplatform.entity.emun;

public enum LabelProjectStatus {
    AILABELING(0),LABELING(1),FINISH(2);

    private int value;
    LabelProjectStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    public void setValue(int value){
        this.value = value;
    }

}
