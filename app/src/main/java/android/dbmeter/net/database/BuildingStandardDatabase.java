package android.dbmeter.net.database;

import android.content.Context;

import android.dbmeter.net.R;
import android.dbmeter.net.model.BuildingStandard;

import java.util.ArrayList;

public class BuildingStandardDatabase {
    private static ArrayList<BuildingStandard> databases = new ArrayList<>();

    private static void init(Context context) {
        databases.add(new BuildingStandard(0, context.getString(R.string.standard_entertainment)));
        databases.add(new BuildingStandard(1, context.getString(R.string.standard_museum)));
        databases.add(new BuildingStandard(2, context.getString(R.string.standard_library)));
        databases.add(new BuildingStandard(3, context.getString(R.string.standard_children_school)));
        databases.add(new BuildingStandard(4, context.getString(R.string.standard_adult_school)));
        databases.add(new BuildingStandard(5, context.getString(R.string.standard_hospital)));
        databases.add(new BuildingStandard(6, context.getString(R.string.standard_nursing_home)));
        databases.add(new BuildingStandard(7, context.getString(R.string.standard_heallth_department)));
        databases.add(new BuildingStandard(8, context.getString(R.string.standard_science_park)));
        databases.add(new BuildingStandard(9, context.getString(R.string.standard_court)));
        databases.add(new BuildingStandard(10, context.getString(R.string.standard_sport)));
        databases.add(new BuildingStandard(10, context.getString(R.string.standard_business)));
        databases.add(new BuildingStandard(12, context.getString(R.string.standard_station)));
    }

    public static ArrayList<BuildingStandard> get(Context context) {
        if (databases.size() == 0)
            init(context);

        return databases;
    }

    public static int searchBuildingId(String buildingName){
        for (BuildingStandard bs: databases) {
            if(bs.getBuildingName().equals(buildingName)){
                return bs.getBuildingId();
            }
        }
        return -1;
    }

    public static BuildingStandard searchBuilding(int buildingId){
        BuildingStandard result = new BuildingStandard();

        for (BuildingStandard bs: databases) {
            if(bs.getBuildingId() == buildingId){
                result = bs;
            }
        }

        return result;
    }
}
