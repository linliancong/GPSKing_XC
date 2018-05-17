package com.zxhl.entity;

/**
 * Created by Administrator on 2018/1/18.
 */

public class BytxInfo {
    private String time;
    private String configHour;
    private String factHour;
    private String type;

    public BytxInfo(String time, String configHour, String factHour,String type){

        this.time=time;
        this.configHour=configHour;
        this.factHour=factHour;
        this.type=type;
    }



    public void setTime(String time) {
        this.time = time;
    }

    public void setConfigHour(String configHour) {
        this.configHour = configHour;
    }

    public void setFactHour(String factHour) {
        this.factHour = factHour;
    }

    public void setType(String info) {
        this.type = info;
    }


    public String getTime() {
        return time;
    }

    public String getConfigHour() {
        return configHour;
    }

    public String getFactHour() {
        return factHour;
    }

    public String getType() {
        return type;
    }

}
