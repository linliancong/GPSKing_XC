package com.zxhl.entity;

/**
 * Created by Administrator on 2018/1/18.
 */

public class LockInfo {
    private String time;
    private String info;

    public LockInfo(String time,String info){

        this.time=time;
        this.info=info;
    }



    public void setTime(String time) {
        this.time = time;
    }

    public void setInfo(String info) {
        this.info = info;
    }


    public String getTime() {
        return time;
    }

    public String getInfo() {
        return info;
    }

}
