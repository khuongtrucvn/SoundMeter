package android.dbmeter.net.database;

import android.content.Context;

import android.dbmeter.net.R;
import android.dbmeter.net.model.BuildingSectionStandard;

import java.util.ArrayList;

public class BuildingSectionStandardDatabase {
    private static ArrayList<BuildingSectionStandard> databases = new ArrayList<>();

    private static void init(Context context) {
        databases.add(new BuildingSectionStandard(0, 0,context.getString(R.string.section_concert),50));
        databases.add(new BuildingSectionStandard(0, 1,context.getString(R.string.section_auditorium),50));
        databases.add(new BuildingSectionStandard(0, 2,context.getString(R.string.section_cinema),50));
        databases.add(new BuildingSectionStandard(0, 3,context.getString(R.string.section_outdoor_theater),50));

        databases.add(new BuildingSectionStandard(1, 0,context.getString(R.string.section_showroom),50));
        databases.add(new BuildingSectionStandard(1, 1,context.getString(R.string.section_office),50));

        databases.add(new BuildingSectionStandard(2, 0,context.getString(R.string.section_reading_area),50));
        databases.add(new BuildingSectionStandard(2, 1,context.getString(R.string.section_office),50));

        databases.add(new BuildingSectionStandard(3, 0,context.getString(R.string.section_bedroom),35));
        databases.add(new BuildingSectionStandard(3, 1,context.getString(R.string.section_classroom),50));
        databases.add(new BuildingSectionStandard(3, 2,context.getString(R.string.section_playground),55));
        databases.add(new BuildingSectionStandard(3, 3,context.getString(R.string.section_outdoor_area),60));

        databases.add(new BuildingSectionStandard(4, 0,context.getString(R.string.section_conference_room),45));
        databases.add(new BuildingSectionStandard(4, 1,context.getString(R.string.section_classroom),50));
        databases.add(new BuildingSectionStandard(4, 2,context.getString(R.string.section_hall),50));
        databases.add(new BuildingSectionStandard(4, 3,context.getString(R.string.section_laboratory),50));
        databases.add(new BuildingSectionStandard(4, 4,context.getString(R.string.section_office),50));
        databases.add(new BuildingSectionStandard(4, 5,context.getString(R.string.section_lounge),50));

        databases.add(new BuildingSectionStandard(5, 0,context.getString(R.string.section_patient_room),35));
        databases.add(new BuildingSectionStandard(5, 1,context.getString(R.string.section_doctor_room),45));
        databases.add(new BuildingSectionStandard(5, 2,context.getString(R.string.section_operating_room),40));
        databases.add(new BuildingSectionStandard(5, 3,context.getString(R.string.section_outdoor_area),50));

        databases.add(new BuildingSectionStandard(6, 0,context.getString(R.string.section_bedroom),40));
        databases.add(new BuildingSectionStandard(6, 1,context.getString(R.string.section_office),50));

        databases.add(new BuildingSectionStandard(7, 0,context.getString(R.string.section_office),50));
        databases.add(new BuildingSectionStandard(7, 1,context.getString(R.string.section_reception_room),50));

        databases.add(new BuildingSectionStandard(8, 0,context.getString(R.string.section_office),50));
        databases.add(new BuildingSectionStandard(8, 1,context.getString(R.string.section_reception_room),50));

        databases.add(new BuildingSectionStandard(9, 0,context.getString(R.string.section_court_room),45));
        databases.add(new BuildingSectionStandard(9, 1,context.getString(R.string.section_office),50));

        databases.add(new BuildingSectionStandard(10, 0,context.getString(R.string.section_office),50));
        databases.add(new BuildingSectionStandard(10, 1,context.getString(R.string.section_training_room),55));
        databases.add(new BuildingSectionStandard(10, 2,context.getString(R.string.section_stadium_cover),60));
        databases.add(new BuildingSectionStandard(10, 3,context.getString(R.string.section_stadium),60));

        databases.add(new BuildingSectionStandard(11, 0,context.getString(R.string.section_mart),60));
        databases.add(new BuildingSectionStandard(11, 1,context.getString(R.string.section_restaurant),55));
        databases.add(new BuildingSectionStandard(11, 2,context.getString(R.string.section_public_shop),60));
        databases.add(new BuildingSectionStandard(11, 3,context.getString(R.string.section_central_market),60));

        databases.add(new BuildingSectionStandard(12, 0,context.getString(R.string.section_guest_room),50));
        databases.add(new BuildingSectionStandard(12, 1,context.getString(R.string.section_office),50));
    }

    public static ArrayList<BuildingSectionStandard> get(Context context) {
        if (databases.size() == 0)
            init(context);

        return databases;
    }

    public static int searchSectionIdOfBuilding(int buildingId, String sectionName){
        for (BuildingSectionStandard bs: databases) {
            if(bs.getBuildingId() == buildingId && bs.getSectionName().equals(sectionName)){
                return bs.getSectionId();
            }
        }
        return -1;
    }

    public static ArrayList<BuildingSectionStandard> searchSectionFromBuilding(int buildingId){
        ArrayList<BuildingSectionStandard> result = new ArrayList<>();

        for (BuildingSectionStandard bss : databases) {
            if(bss.getBuildingId() == buildingId){
                result.add(bss);
            }
        }

        return result;
    }

    public static BuildingSectionStandard searchSection(int buildingId, int sectionId){
        BuildingSectionStandard result = new BuildingSectionStandard();

        for (BuildingSectionStandard bss : databases) {
            if(bss.getBuildingId() == buildingId && bss.getSectionId() == sectionId){
                result = bss;
            }
        }

        return result;
    }
}
