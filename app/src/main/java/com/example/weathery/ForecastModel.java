package com.example.weathery;

public class ForecastModel {

    private String temparatureDay;
    private String iconDay;
    private String timeDay;

    public ForecastModel (String temparatureDay,String iconDay,String timeDay){

        this.temparatureDay = temparatureDay;
        this.iconDay = iconDay;
        this.timeDay = timeDay;

    }

    public String getTemparatureDay() {
        return temparatureDay;
    }

    public void setTemparatureDay(String temparatureDay) {
        this.temparatureDay = temparatureDay;
    }

    public String getIconDay() {
        return iconDay;
    }

    public void setIconDay(String iconDay) {
        this.iconDay = iconDay;
    }

    public String getTimeDay(){
        return timeDay;
    }

    public void setTimeDay(String timeDay){
        this.timeDay = timeDay;
    }
}
