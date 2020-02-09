package android.dbmeter.net.model;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPrefs {
    private SharedPreferences myPrefs;

    public MyPrefs(Context context) {
        myPrefs = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    public void setIsWarn(boolean value){
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putBoolean("IsWarn",value);
        editor.apply();
    }

    public Boolean getIsWarn(){
        return myPrefs.getBoolean("IsWarn", false);
    }

    public void setWarningValue(int value){
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putInt("WarningValue",value);
        editor.apply();
    }

    public int getWarningValue(){
        return myPrefs.getInt("WarningValue", 80);
    }

    public void setIsVibrate(boolean value){
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putBoolean("IsVibrate",value);
        editor.apply();
    }

    public Boolean getIsVibrate(){
        return myPrefs.getBoolean("IsVibrate", false);
    }

    public void setIsSound(boolean value){
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putBoolean("IsSound",value);
        editor.apply();
    }

    public Boolean getIsSound(){
        return myPrefs.getBoolean("IsSound", false);
    }

    public void setCalibrateValue(int value){
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putInt("CalibrateValue",value);
        editor.apply();
    }

    public int getCalibrateValue(){
        return myPrefs.getInt("CalibrateValue", 0);
    }
}
