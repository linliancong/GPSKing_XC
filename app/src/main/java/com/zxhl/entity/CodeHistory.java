package com.zxhl.entity;

/**
 * Created by Administrator on 2018/2/1.
 */

public class CodeHistory {

    public String VehicleLic = "";
    public String OperatorTime = "";
    public String OperatorName = "";
    public String OperaProject = "";
    public String OperaResult = "";

    public CodeHistory()
    {
    }

    public CodeHistory(String VehicleLic, String OperatorTime, String OperatorName, String OperaProject, String OperaResult)
    {
        this.VehicleLic = VehicleLic;
        this.OperatorTime = OperatorTime;
        this.OperatorName = OperatorName;
        this.OperaProject = OperaProject;
        this.OperaResult = OperaResult;
    }

    public void setVehicleLic(String VehicleLic){
        this.VehicleLic = VehicleLic;
    }
    public void setOperatorTime(String OperatorTime){
        this.OperatorTime = OperatorTime;

    }
    public void setOperatorName(String OperatorName){
        this.OperatorName = OperatorName;

    }
    public void setOperaProject(String OperaProject){
        this.OperaProject = OperaProject;

    }
    public void setOperaResult(String OperaResult){
        this.OperaResult = OperaResult;
    }


    public String getVehicleLic(){
        return VehicleLic;
    }
    public String getOperatorTime(){
        return OperatorTime;
    }
    public String getOperatorName(){
        return OperatorName;
    }
    public String getOperaProject(){
        return OperaProject;
    }
    public String getOperaResult(){
        return OperaResult;
    }
}
