package com.app.khoaluan.noizy.model;

public class MeasureResult {
    private float MinValue;
    private float AvgValue;
    private float MaxValue;
    private String Date;
    private String Time;
    private String Duration;
    private String Status;

    public MeasureResult(){
        MinValue = 0;
        AvgValue = 0;
        MaxValue = 0;
        Date = "dd-MM-YYYY";
        Time = "00:00:00";
        Duration = "00:00";
        Status = "None";
    }

    public MeasureResult(float minValue, float avgValue, float maxValue, String date, String time, String duration, String status) {
        MinValue = minValue;
        AvgValue = avgValue;
        MaxValue = maxValue;
        Date = date;
        Time = time;
        Duration = duration;
        Status = status;
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

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
