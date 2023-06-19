package cn.iecas.geoai.labelplatform.entity.emun;


public enum SampleSetCategory {

    DETECTION("DETECTION"),
    SEGMENTATION("SEGMENTATION"),
    CHANGE_DETECTION("CHANGE_DETECTION"),
    CLASSIFICATION("CLASSIFICATION"),
    OTHER("OTHER"),
    VIDEO_LABEL("VIDEO_LABEL"),
    ELEC_LABEL("ELEC_LABEL");


    private String value;

    SampleSetCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
