package android.dbmeter.net.model;

public class Global {
    public static float dbCount = 40;   //Độ ồn không bao gồm sai số
    public static float avgDb = 0;
    public static float minDb = 140;
    public static float maxDb = 0;
    public static float lastDbCount = dbCount;
    private static float min = 0.5f;  //Set the minimum sound change
    private static float value = 0;   // Sound decibel value
    public static  int calibrateValue = 0;  //Sai số
    public static float lastDb = dbCount + calibrateValue; //Độ ồn bao gồm sai số

    public static void setDbCount(float dbValue) {
        /*if (dbValue > lastDbCount) {
            value = dbValue - lastDbCount > min ? dbValue - lastDbCount : min;
        }
        else{
            value = dbValue - lastDbCount < -min ? dbValue - lastDbCount : -min;
        }
        dbCount = lastDbCount + value * 0.2f ; //To prevent the sound from changing too fast
        lastDbCount = dbCount;
        if(dbCount<minDb) minDb=dbCount;
        if(dbCount>maxDb) maxDb=dbCount;*/

        dbCount = dbValue;
        lastDb = dbCount + calibrateValue;
        if(lastDb<minDb) minDb=lastDb;
        if(lastDb>maxDb) maxDb=lastDb;
    }
}
