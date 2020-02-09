package android.dbmeter.net.model;

public class MeasureResult {
    private float CurValue;
    private float MinValue;
    private float AvgValue;
    private float MaxValue;
    private String Date;
    private String Time;
    private String Duration;

    public MeasureResult(){
        CurValue = 0;
        MinValue = 0;
        AvgValue = 0;
        MaxValue = 0;
        Date = "dd-MM-YYYY";
        Time = "00:00:00";
        Duration = "00:00";
    }

    public MeasureResult(float curValue, float minValue, float avgValue, float maxValue, String date, String time, String duration) {
        CurValue = curValue;
        MinValue = minValue;
        AvgValue = avgValue;
        MaxValue = maxValue;
        Date = date;
        Time = time;
        Duration = duration;
    }

    public float getCurValue() {
        return CurValue;
    }

    public void setCurValue(float curValue) {
        CurValue = curValue;
    }

    public float getMinValue() {
        return MinValue;
    }

    public void setMinValue(float minValue) {
        MinValue = minValue;
    }

    public float getAvgValue() {
        return AvgValue;
    }

    public void setAvgValue(float avgValue) {
        AvgValue = avgValue;
    }

    public float getMaxValue() {
        return MaxValue;
    }

    public void setMaxValue(float maxValue) {
        MaxValue = maxValue;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }
}
