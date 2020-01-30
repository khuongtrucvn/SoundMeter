package com.app.khoaluan.noizy.model;

public class HearingTestData {
    private int Side; //#0:Left - 1:Right
    private int Frequency;
    private int SoundFile;
    private int SoundLevel;
    private int VolumeLevel;
    private int ResultSoundLevel;

    public HearingTestData(int side, int frequency, int soundFile, int soundLevel, int volumeLevel, int resultSoundLevel){
        Side = side;
        Frequency = frequency;
        SoundFile = soundFile;
        SoundLevel = soundLevel;
        VolumeLevel = volumeLevel;
        ResultSoundLevel = resultSoundLevel;
    }

    public int getSide() {
        return Side;
    }

    public void setSide(int side) {
        Side = side;
    }

    public int getFrequency() {
        return Frequency;
    }

    public void setFrequency(int frequency) {
        Frequency = frequency;
    }

    public int getSoundFile() {
        return SoundFile;
    }

    public void setSoundFile(int soundFile) {
        SoundFile = soundFile;
    }

    public int getSoundLevel() {
        return SoundLevel;
    }

    public void setSoundLevel(int soundLevel) {
        SoundLevel = soundLevel;
    }

    public int getVolumeLevel() {
        return VolumeLevel;
    }

    public void setVolumeLevel(int volumeLevel) {
        VolumeLevel = volumeLevel;
    }

    public int getResultSoundLevel() {
        return ResultSoundLevel;
    }

    public void setResultSoundLevel(int resultSoundLevel) {
        ResultSoundLevel = resultSoundLevel;
    }
}
