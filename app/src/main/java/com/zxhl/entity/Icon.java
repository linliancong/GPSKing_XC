package com.zxhl.entity;

/**
 * Created by Administrator on 2017/12/13.
 */

public class Icon {
    private int mId;
    private String mName;

    public Icon(){}

    public Icon(int mId,String mName){
        this.mId=mId;
        this.mName=mName;
    }

    public int getmId() {
        return mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }
}
