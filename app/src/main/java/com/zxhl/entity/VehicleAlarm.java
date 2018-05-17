package com.zxhl.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/2/1.
 */

public class VehicleAlarm implements Serializable {
    public String VehicleID = "";
    public String Mobile = "";
    public String DeviceNum = "";
    public String MModelName = "";
    public String VehicleLic = "";
    public String GPSDateTime = "";
    public String Position = "";
    public String OperatorName = "";
    public String DealType = "";
    public String OwnerName = "";
    public String GroupName = "";
    public String FranchiserID = "";


    public VehicleAlarm()
    {
    }

    public VehicleAlarm(String VehicleID,String Mobile, String DeviceNum, String MModelName,
                        String VehicleLic, String GPSDateTime, String Position,
                        String OperatorName, String DealType, String OwnerName,
                        String GroupName, String FranchiserID)
    {
        this.VehicleID = VehicleID;
        this.Mobile = Mobile;
        this.DeviceNum = DeviceNum;
        this.MModelName = MModelName;
        this.VehicleLic = VehicleLic;
        this.GPSDateTime = GPSDateTime;
        this.Position = Position;
        this.OperatorName = OperatorName;
        this.DealType = DealType;
        this.OwnerName = OwnerName;
        this.GroupName = GroupName;
        this.FranchiserID = FranchiserID;
    }

    public void setVehicleID(String VehicleID) {
        this.VehicleID = VehicleID;
    }
    public void setMobile(String Mobile){
        this.Mobile = Mobile;
    }
    public void setDeviceNum(String DeviceNum){
        this.DeviceNum = DeviceNum;
    }
    public void setMModelName(String MModelName){
        this.MModelName = MModelName;
    }
    public void setVehicleLic(String VehicleLic){
        this.VehicleLic = VehicleLic;
    }
    public void setGPSDateTime(String GPSDateTime){
        this.GPSDateTime = GPSDateTime;
    }
    public void setPosition(String Position){
        this.Position = Position;
    }
    public void setOperatorName(String OperatorName){
        this.OperatorName = OperatorName;
    }
    public void setDealType(String DealType){
        this.DealType = DealType;
    }
    public void setOwnerName(String OwnerName){
        this.OwnerName = OwnerName;
    }
    public void setGroupName(String GroupName){
        this.GroupName = GroupName;
    }
    public void setFranchiserID(String FranchiserID){
        this.FranchiserID = FranchiserID;
    }


    public String getVehicleID() {
        return VehicleID;
    }
    public String getMobile(){
        return Mobile;
    }
    public String getDeviceNum(){
        return DeviceNum;
    }
    public String getMModelName(){
        return MModelName;
    }
    public String getVehicleLic(){
        return VehicleLic;
    }
    public String getGPSDateTime(){
        return GPSDateTime;
    }
    public String getPosition(){
        return Position;
    }
    public String getOperatorName(){
        return OperatorName;
    }
    public String getDealType(){
        return DealType;
    }
    public String getOwnerName(){
        return OwnerName;
    }
    public String getGroupName(){
        return GroupName;
    }
    public String getFranchiserID(){
        return FranchiserID;
    }
}
