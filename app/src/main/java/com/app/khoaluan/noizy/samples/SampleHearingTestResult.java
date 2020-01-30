package com.app.khoaluan.noizy.samples;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.model.HearingTestData;

import java.util.ArrayList;

public class SampleHearingTestResult {
    private static ArrayList<HearingTestData> results = new ArrayList<>();

    private static void init() {
        results.add(new HearingTestData(0, 125,R.raw.pure_tone_125hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(0, 250,R.raw.pure_tone_250hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(0, 500,R.raw.pure_tone_500hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(0, 1000,R.raw.pure_tone_1000hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(0, 2000,R.raw.pure_tone_2000hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(0, 4000,R.raw.pure_tone_4000hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(0, 8000,R.raw.pure_tone_8000hz_80dbhl, 20,2, 20));

        results.add(new HearingTestData(1, 125,R.raw.pure_tone_125hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(1, 250,R.raw.pure_tone_250hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(1, 500,R.raw.pure_tone_500hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(1, 1000,R.raw.pure_tone_1000hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(1, 2000,R.raw.pure_tone_2000hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(1, 4000,R.raw.pure_tone_4000hz_80dbhl, 20,2, 20));
        results.add(new HearingTestData(1, 8000,R.raw.pure_tone_8000hz_80dbhl, 20,2, 20));

    }

    public static ArrayList<HearingTestData> get() {
        if (results.size() == 0)
            init();

        return results;
    }

    public static ArrayList<HearingTestData> updateSoundLevel(int position, int soundLevel, int volumeLevel, int resultSoundLevel){
        HearingTestData result = results.get(position);

        result.setSoundLevel(soundLevel);
        result.setVolumeLevel(volumeLevel);
        result.setResultSoundLevel(resultSoundLevel);

        results.set(position,result);

        return results;
    }

    public static void delete(){
        results.clear();
    }
}
