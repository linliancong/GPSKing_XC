package com.zxhl.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/28.
 */

public class AddressCh {
    //百度地图坐标
    private String gisBd09Lat;
    private String gisBd09Lng;
    //高德地图坐标
    private String gisGcj02Lat;
    private String gisGcj02Lng;
    //城市的行政代码、名字、拼音
    private String id;
    private String name;
    private String pinyin;
    //获取省、市、县/区
    private List<AddressCh> mCity;

    private ArrayList<String> province;
    private ArrayList<ArrayList<String>> city;
    private ArrayList<ArrayList<ArrayList<String>>> county;


    public String getGisBd09Lat() {
        return gisBd09Lat;
    }

    public String getGisBd09Lng() {
        return gisBd09Lng;
    }

    public String getGisGcj02Lat() {
        return gisGcj02Lat;
    }

    public String getGisGcj02Lng() {
        return gisGcj02Lng;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public List<AddressCh> getmCity() {
        return mCity;
    }

    public ArrayList<String> getProvince() {
        return province;
    }

    public ArrayList<ArrayList<String>> getCity() {
        return city;
    }

    public ArrayList<ArrayList<ArrayList<String>>> getCounty() {
        return county;
    }

    public void setGisBd09Lat(String gisBd09Lat) {
        this.gisBd09Lat = gisBd09Lat;
    }

    public void setGisBd09Lng(String gisBd09Lng) {
        this.gisBd09Lng = gisBd09Lng;
    }

    public void setGisGcj02Lat(String gisGcj02Lat) {
        this.gisGcj02Lat = gisGcj02Lat;
    }

    public void setGisGcj02Lng(String gisGcj02Lng) {
        this.gisGcj02Lng = gisGcj02Lng;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public void setmCity(List<AddressCh> mCity) {
        this.mCity = mCity;
    }

    public void setProvince(ArrayList<String> province) {
        this.province = province;
    }

    public void setCity(ArrayList<ArrayList<String>> city) {
        this.city = city;
    }

    public void setCounty(ArrayList<ArrayList<ArrayList<String>>> county) {
        this.county = county;
    }

}
