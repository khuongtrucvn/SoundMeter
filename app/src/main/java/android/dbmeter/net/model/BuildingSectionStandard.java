package android.dbmeter.net.model;

public class BuildingSectionStandard {
    private int BuildingId;
    private int SectionId;
    private String SectionName;
    private int SectionNoiseLevel;

    public BuildingSectionStandard(){
        BuildingId = -1;
        SectionId = -1;
        SectionName = "";
        SectionNoiseLevel = 0;
    }

    public BuildingSectionStandard(int buildingId, int sectionId, String sectionName, int sectionNoiseLevel){
        BuildingId = buildingId;
        SectionId = sectionId;
        SectionName = sectionName;
        SectionNoiseLevel = sectionNoiseLevel;
    }

    public int getBuildingId() {
        return BuildingId;
    }

    public void setBuildingId(int buildingId) {
        BuildingId = buildingId;
    }

    public int getSectionId() {
        return SectionId;
    }

    public void setSectionId(int sectionId) {
        SectionId = sectionId;
    }

    public String getSectionName() {
        return SectionName;
    }

    public void setSectionName(String sectionName) {
        SectionName = sectionName;
    }

    public int getSectionNoiseLevel() {
        return SectionNoiseLevel;
    }

    public void setSectionNoiseLevel(int sectionNoiseLevel) {
        SectionNoiseLevel = sectionNoiseLevel;
    }
}
