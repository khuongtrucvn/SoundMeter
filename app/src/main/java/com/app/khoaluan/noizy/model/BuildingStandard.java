package com.app.khoaluan.noizy.model;

public class BuildingStandard {
    private int BuildingId;
    private String BuildingName;

    public BuildingStandard(){
        BuildingId = -1;
        BuildingName = "";
    }

    public BuildingStandard(int buildingId, String buildingName){
        BuildingId = buildingId;
        BuildingName = buildingName;
    }

    public int getBuildingId() {
        return BuildingId;
    }

    public void setBuildingId(int buildingId) {
        BuildingId = buildingId;
    }

    public String getBuildingName() {
        return BuildingName;
    }

    public void setBuildingName(String buildingName) {
        BuildingName = buildingName;
    }
}
