package android.dbmeter.net.model;

import android.dbmeter.net.R;

public class NoiseLevel {
    private int Level;
    private int Description;

    public NoiseLevel(){
        Level = 0;
        Description = 0;
    }

    public NoiseLevel(int level, int description){
        Level=level;
        Description = description;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    public int getDescription() {
        return Description;
    }

    public void setDescription(int description) {
        Description = description;
    }

    public void setSoundLevel(int level, int description){
        Level = level;
        Description = description;
    }

    public int getNoiseLevel(float value) {
        if (value < 30)
            return 20;
        else if (value < 40)
            return 30;
        else if (value < 50)
            return 40;
        else if (value < 60)
            return 50;
        else if (value < 70)
            return 60;
        else if (value < 80)
            return 70;
        else if (value < 90)
            return 80;
        else if (value < 100)
            return 90;
        else if (value < 110)
            return 100;
        else if (value < 120)
            return 110;
        else if (value < 130)
            return 120;
        else if (value < 140)
            return 130;
        else
            return 140;
    }

    public int getNoiseLevelDescription(int level) {
        int description = 0;
        switch (level) {
            case 20: {
                description = R.string.noise_20_des;
                break;
            }
            case 30: {
                description = R.string.noise_30_des;
                break;
            }
            case 40: {
                description = R.string.noise_40_des;
                break;
            }
            case 50: {
                description = R.string.noise_50_des;
                break;
            }
            case 60: {
                description = R.string.noise_60_des;
                break;
            }
            case 70: {
                description = R.string.noise_70_des;
                break;
            }
            case 80: {
                description = R.string.noise_80_des;
                break;
            }
            case 90: {
                description = R.string.noise_90_des;
                break;
            }
            case 100: {
                description = R.string.noise_100_des;
                break;
            }
            case 110: {
                description = R.string.noise_110_des;
                break;
            }
            case 120: {
                description = R.string.noise_120_des;
                break;
            }
            case 130: {
                description = R.string.noise_130_des;
                break;
            }
            case 140: {
                description = R.string.noise_140_des;
                break;
            }
        }
        return description;
    }

    public int getWarningColor(float value) {
        if(value > 120)
            return R.color.extreme_danger;
        else if(value > 90)
            return R.color.high_danger;
        else if(value > 80)
            return R.color.considerable_danger;
        else if(value > 70)
            return R.color.moderate_danger;
        else
            return R.drawable.color_background;
    }

    public int getInfluenceLevel(float value){
        if(value > 120)
            return R.string.il_extreme_danger;
        else if(value > 90)
            return R.string.il_high_danger;
        else if(value > 80)
            return R.string.il_considerable_danger;
        else if(value > 70)
            return R.string.il_moderate_danger;
        else
            return R.string.il_normal;
    }
}
