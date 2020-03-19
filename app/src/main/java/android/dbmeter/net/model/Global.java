package android.dbmeter.net.model;

public class Global {
    public static float dbCount = 40;   //Độ ồn không bao gồm sai số
    public static float avgDb = 0;
    public static float minDb = 140;
    public static float maxDb = 0;
    public static  int calibrateValue = 0;  //Sai số
    public static float lastDb = dbCount + calibrateValue; //Độ ồn bao gồm sai số

    public static void setDbCount(float dbValue) {
        dbCount = dbValue;
        lastDb = dbCount + calibrateValue;
        if(lastDb<minDb) {
            minDb=lastDb;
        }
        if(lastDb>maxDb){
            maxDb=lastDb;
        }
    }
}
