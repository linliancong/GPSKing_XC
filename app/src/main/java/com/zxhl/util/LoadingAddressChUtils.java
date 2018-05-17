package com.zxhl.util;

import android.content.Context;

import com.zxhl.entity.AddressCh;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/28.
 */

public class LoadingAddressChUtils {


    /**
     * @return
     * @param context
     * @param path
     * @throws Exception
     *  打开地址的json文件
    * */
    public static InputStream getAddressFile(Context context,String path)throws Exception{
        InputStream in=context.getResources().getAssets().open(path);
        return in;
    }

    /**
     * 读取json文件并且返回Address
     * @param context
     * @return AddressCh
    * */
    public static AddressCh loading(Context context){
        StringBuffer sb=new StringBuffer();
        try{
            InputStream in=getAddressFile(context,"city_china.json");
            InputStreamReader reader=new InputStreamReader(in,"UTF-8");
            BufferedReader br=new BufferedReader(reader);

            String buff=null;
            while ((buff=br.readLine())!=null)
            {
                sb.append(buff);
            }
            in.close();
            reader.close();
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return parseAddress(sb.toString());
    }

    /**
     *解析 json
     * @param json
     * @return AddressCh
    * */
    public static AddressCh parseAddress(String json)
    {
        AddressCh addressCh=new AddressCh();
        //省份数据列表
        List<AddressCh> mCity=new ArrayList<>();
        //省份名称列表
        ArrayList<String> province=new ArrayList<>();
        //<省份<城市>>名称列表
        ArrayList<ArrayList<String>> city=new ArrayList<>();
        //<省份<城市<区县>>>名称列表
        ArrayList<ArrayList<ArrayList<String>>> county=new ArrayList<>();
        try{
            JSONArray jsonArray=new JSONArray(json);
            for(int i=0;i<jsonArray.length();i++){
                //省份数据
                JSONObject provinceJson=jsonArray.getJSONObject(i);
                //省份
                AddressCh childrendProvince=new AddressCh();

                //城市名称列表
                ArrayList<String> arrCity=new ArrayList<>();
                //<城市<区县>>名称列表
                ArrayList<ArrayList<String>> arrCity1=new ArrayList<>();
                //城市数据列表
                List<AddressCh> listCity=new ArrayList<>();

                JSONArray jsonArray1=provinceJson.getJSONArray("cityList");
                for (int j=0;j<jsonArray1.length();j++){
                    //城市数据
                    JSONObject cityJson=jsonArray1.getJSONObject(j);
                    //城市
                    AddressCh childrendCity=new AddressCh();

                    //<区/县>名称列表
                    ArrayList<String> arrCounty=new ArrayList<>();
                    //区县数据列表
                    List<AddressCh> listCounty=new ArrayList<>();

                    JSONArray jsonArray2=cityJson.getJSONArray("cityList");
                    for (int k=0;k<jsonArray2.length();k++){
                        //区县数据
                        JSONObject countyJson=jsonArray2.getJSONObject(k);
                        //县、区
                        AddressCh childrenCounty=new AddressCh();
                        childrenCounty.setGisBd09Lat(countyJson.optString("gisBd09Lat"));
                        childrenCounty.setGisBd09Lng(countyJson.optString("gisBd09Lng"));
                        childrenCounty.setGisGcj02Lat(countyJson.optString("gisGcj02Lat"));
                        childrenCounty.setGisGcj02Lng(countyJson.optString("gisGcj02Lng"));
                        childrenCounty.setId(countyJson.optString("id"));
                        childrenCounty.setName(countyJson.optString("name"));
                        childrenCounty.setPinyin(countyJson.optString("pinyin"));
                        listCounty.add(childrenCounty);
                        arrCounty.add(countyJson.optString("name"));
                    }
                    childrendCity.setGisBd09Lat(cityJson.optString("gisBd09Lat"));
                    childrendCity.setGisBd09Lng(cityJson.optString("gisBd09Lng"));
                    childrendCity.setGisGcj02Lat(cityJson.optString("gisGcj02Lat"));
                    childrendCity.setGisGcj02Lng(cityJson.optString("gisGcj02Lng"));
                    childrendCity.setId(cityJson.optString("id"));
                    childrendCity.setName(cityJson.optString("name"));
                    childrendCity.setPinyin(cityJson.optString("pinyin"));
                    childrendCity.setmCity(listCounty);
                    listCity.add(childrendCity);
                    arrCity.add(cityJson.optString("name"));
                    arrCity1.add(arrCounty);
                }
                childrendProvince.setGisBd09Lat(provinceJson.optString("gisBd09Lat"));
                childrendProvince.setGisBd09Lng(provinceJson.optString("gisBd09Lng"));
                childrendProvince.setGisGcj02Lat(provinceJson.optString("gisGcj02Lat"));
                childrendProvince.setGisGcj02Lng(provinceJson.optString("gisGcj02Lng"));
                childrendProvince.setId(provinceJson.optString("id"));
                childrendProvince.setName(provinceJson.optString("name"));
                childrendProvince.setPinyin(provinceJson.optString("pinyin"));
                childrendProvince.setmCity(listCity);
                mCity.add(childrendProvince);
                province.add(provinceJson.optString("name"));
                city.add(arrCity);
                county.add(arrCity1);
            }
            addressCh.setmCity(mCity);
            addressCh.setProvince(province);
            addressCh.setCity(city);
            addressCh.setCounty(county);
        }catch (Exception e){
            e.printStackTrace();
        }
        return addressCh;
    }
}
