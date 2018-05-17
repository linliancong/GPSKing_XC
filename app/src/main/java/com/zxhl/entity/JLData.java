package com.zxhl.entity;

/**
 * Created by Administrator on 2017/12/21.
 */

public class JLData {
    private String time;
    private String text;

    public JLData(String time,String text){
        this.time=time;
        this.text=text;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }
}
