package com.zxhl.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/11/24.
 */

public class SharedPreferenceUtils {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public SharedPreferenceUtils(Context context,String file){
        sp=context.getSharedPreferences(file,context.MODE_PRIVATE);
        editor=sp.edit();
    }

    /**
    *
    * 1、用户信息
    *
    * */

    //用户ID
    public void setOperatorID(String OperatorID){
        editor.putString("OperatorID",OperatorID);
        editor.commit();
    }

    public String getOperatorID()
    {
        return sp.getString("OperatorID","");
    }

    //用户名称
    public void setOperatorName(String OperatorName){
        editor.putString("OperatorName",OperatorName);
        editor.commit();
    }

    public String getOperatorName()
    {
        return sp.getString("OperatorName","");
    }

    //用户账户
    public void setNickName(String NickName){
        editor.putString("NickName",NickName);
        editor.commit();
    }

    public String getNickName()
    {
        return sp.getString("NickName","");
    }

    //用户密码
    public void setPWD(String pwd){
        editor.putString("PWD",pwd);
        editor.commit();
    }

    public String getPWD()
    {
        return sp.getString("PWD","");
    }

    //用户分组
    public void setVGroupID(String VGroupID) {
        editor.putString("VGroupID", VGroupID);
        editor.commit();
    }

    public String getVGroupID() {
        return sp.getString("VGroupID", "");
    }

    //用户角色
    public void setRoleID(String roleid){
        editor.putString("RoleID",roleid);
        editor.commit();
    }

    public String getRoleID()
    {
        return sp.getString("RoleID","");
    }



    /**
    *
    * 2、登录信息
    *
    * */

    //ip地址
    public void setIP(String IP)
    {
        editor.putString("IP",IP);
        editor.commit();
    }

    public String getIP(){
        return sp.getString("IP",Constants.SERVICE_IP);
    }

    //端口
    public void setPort(int Port){
        editor.putInt("Port",Port);
        editor.commit();
    }

    public int getPort(){
        return sp.getInt("Port",Constants.SERVER_PORT);
    }

    // 是否在后台运行标记
    public void setIsStart(boolean isStart) {
        editor.putBoolean("isStart", isStart);
        editor.commit();
    }

    public boolean getIsStart() {
        return sp.getBoolean("isStart", false);
    }

    // 是否第一次运行本应用
    public void setIsFirst(boolean isFirst) {
        editor.putBoolean("isFirst", isFirst);
        editor.commit();
    }

    public boolean getIsFirst() {
        return sp.getBoolean("isFirst", true);
    }
    // 是否第一次检查更新
    public void setIsFirstUpdate(boolean isFirst) {
        editor.putBoolean("isFirst", isFirst);
        editor.commit();
    }

    public boolean getIsFirstUpdate() {
        return sp.getBoolean("isFirst", true);
    }

    //角色权限
    public void setRolePermission(String RolePermission) {
        editor.putString("RolePermission", RolePermission);
        editor.commit();
    }

    public String getRolePermission() {
        return sp.getString("RolePermission", "");
    }

    //网络是否连接
    public void setIsNetworkConnect(boolean state){
        editor.putBoolean("IsNetworkConnect",state);
        editor.commit();
    }

    public boolean getIsNetworkConnect(){
        return sp.getBoolean("IsNetworkConnect",false);
    }

    //绑定的机号
    public void setVehicleLic(String VehicleLic){
        editor.putString("VehicleLic",VehicleLic);
        editor.commit();
    }

    public String getVehicleLic(){
        return sp.getString("VehicleLic","");
    }

    //显示的天数
    public void setDayCount(String DayCount){
        editor.putString("DayCount",DayCount);
        editor.commit();
    }

    public String getDayCount(){
        return sp.getString("DayCount","7");
    }

}
