package com.zxhl.entity;

/**
 * Created by Administrator on 2018/6/21.
 */

public class WorkTimeInfo {

    private String BeginTime;
    private String EndTime;
    private String WorkTime;

    public WorkTimeInfo(){}

    public String getBeginTime() {
        return BeginTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public String getWorkTime() {
        return WorkTime;
    }

    public void setBeginTime(String beginTime) {
        BeginTime = beginTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public void setWorkTime(String workTime) {
        WorkTime = workTime;
    }
}
