package com.app.khoaluan.noizy.database;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.model.HearingTestData;

import java.util.ArrayList;

public class HearingTestResultDatabase {
    private static ArrayList<HearingTestData> databases = new ArrayList<>();

    private static void init() {
        databases.add(new HearingTestData(0, 125,R.raw.pure_tone_125hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(0, 250,R.raw.pure_tone_250hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(0, 500,R.raw.pure_tone_500hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(0, 1000,R.raw.pure_tone_1000hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(0, 2000,R.raw.pure_tone_2000hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(0, 4000,R.raw.pure_tone_4000hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(0, 8000,R.raw.pure_tone_8000hz_80dbhl, 20,2, 20));

        databases.add(new HearingTestData(1, 125,R.raw.pure_tone_125hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(1, 250,R.raw.pure_tone_250hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(1, 500,R.raw.pure_tone_500hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(1, 1000,R.raw.pure_tone_1000hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(1, 2000,R.raw.pure_tone_2000hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(1, 4000,R.raw.pure_tone_4000hz_80dbhl, 20,2, 20));
        databases.add(new HearingTestData(1, 8000,R.raw.pure_tone_8000hz_80dbhl, 20,2, 20));

    }

    public static ArrayList<HearingTestData> get() {
        if (databases.size() == 0)
            init();

        return databases;
    }

    public static ArrayList<HearingTestData> updateSoundLevel(int position, int soundLevel, int volumeLevel, int resultSoundLevel){
        HearingTestData result = databases.get(position);

        result.setSoundLevel(soundLevel);
        result.setVolumeLevel(volumeLevel);
        result.setResultSoundLevel(resultSoundLevel);

        databases.set(position,result);

        return databases;
    }

    public static void delete(){
        databases.clear();
    }
}
