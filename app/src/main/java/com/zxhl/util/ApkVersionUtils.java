package com.zxhl.util;

import android.content.Context;

/**
 * Created by Administrator on 2017/12/15.
 */

public class ApkVersionUtils {

    //获取版本号
    public static int getVerCode(Context context){
        int verCode=-1;
        try {
            verCode=context.getPackageManager().getPackageInfo("com.zxhl.gpsking",0).versionCode;
        }catch (Exception e){
            e.printStackTrace();
        }
        return verCode;
    }

    //获取版本名称
    public static String getVerName(Context context){
        String verName="";
        try {
            verName=context.getPackageManager().getPackageInfo("com.zxhl.gpsking",0).versionName;
        }catch (Exception e){
            e.printStackTrace();
        }
        return verName;
    }
}
