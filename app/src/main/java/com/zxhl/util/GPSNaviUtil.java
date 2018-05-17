package com.zxhl.util;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.zxhl.gpsking.R;

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/1/8.
 */

public class GPSNaviUtil extends AppCompatActivity implements AMapNaviListener,AMapNaviViewListener{

    private ImgTxtLayout back;


    //以下定义导航相关
    //声明导航视图
    private AMapNaviView aMapNaviView;
    //声明导航控制器
    private AMapNavi aMapNavi;
    //算路终点坐标
    private NaviLatLng endLatlng;
    //算路起点坐标
    private NaviLatLng startLatlng;
    //算路终点坐标集合
    private List<NaviLatLng> endList=new ArrayList<>();
    //算路起点坐标集合
    private List<NaviLatLng> startList=new ArrayList<>();
    private double slat=0;
    private double elng=0;
    //语音引擎
    private TTSController mTTSManage;

    private List<NaviLatLng> mWayPointList=new ArrayList<>();

    private long mTime=0;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_gpsnavi);

        back=(ImgTxtLayout)findViewById(R.id.query_imgtxt_title1);
        //实例化语音引擎
        mTTSManage=TTSController.getInstance(GPSNaviUtil.this);
        mTTSManage.init();

        //开始获取开始和结束的点坐标
        Intent intent=getIntent();
        Bundle bd=intent.getExtras();
        startLatlng=new NaviLatLng(bd.getDouble("sLat",0),bd.getDouble("sLng",0));
        endLatlng=new NaviLatLng(bd.getDouble("eLat",0),bd.getDouble("eLng",0));


        //获取导航地图控件
        aMapNaviView=findViewById(R.id.query_mao_navi);
        //获取AmapNavi实例
        aMapNavi = AMapNavi.getInstance(GPSNaviUtil.this);
        //设置导航监听
        aMapNavi.addAMapNaviListener(this);
        aMapNaviView.setAMapNaviViewListener(this);
        aMapNavi.addAMapNaviListener(mTTSManage);
        //设置模拟导航的行车速度
        //aMapNavi.setEmulatorNaviSpeed(75);

        //设置终点和起点
        startList.add(startLatlng);
        endList.add(endLatlng);

        aMapNaviView.onCreate(savedInstanceState);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(GPSNaviUtil.this)
                        .setTitle("提示")
                        .setMessage("确定退出导航?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .show();
                //finish();
            }
        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        aMapNaviView.onDestroy();
        mTTSManage.destroy();
        aMapNavi.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        aMapNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        aMapNaviView.onPause();
        //仅仅是停止当前再说的话，到新路口还是会说
        mTTSManage.stopSpeaking();
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()-mTime>2000)
        {
            Toast.makeText(getApplicationContext(),"再按一次退出导航",Toast.LENGTH_SHORT).show();
            mTime=System.currentTimeMillis();
        }
        else {
            super.onBackPressed();
        }
    }

    /**
    *以下均为导航监听事件
    * */
    @Override
    public void onInitNaviFailure() {
        Log.i("MapNavi:","初始化失败");

    }

    //导航初始化成功，进行路径规划
    @Override
    public void onInitNaviSuccess() {
        /**
         * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
         *
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        //重置起点、终点
        /*startList.clear();
        startLatlng=new NaviLatLng();
        startList.add(startLatlng);
        endList.clear();
        endLatlng=new NaviLatLng();
        endList.add(endLatlng);*/
        int strategy=0;
        try{
            //最后一个参数为true时代表多路径，否者为单路径
            strategy=aMapNavi.strategyConvert(true,false,false,false,false);
        }catch (Exception e){
            e.printStackTrace();
        }
        aMapNavi.calculateDriveRoute(startList,endList,mWayPointList,strategy);

    }

    //开始导航回调
    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    //当前位置回调
    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    //播报类型和播报文字回调
    @Override
    public void onGetNavigationText(int i, String s) {

    }

    //播报文字回调
    @Override
    public void onGetNavigationText(String s) {

    }

    //结束模拟导航
    @Override
    public void onEndEmulatorNavi() {

    }

    //到达目的地
    @Override
    public void onArriveDestination() {

    }

    //路线计算失败
    @Override
    public void onCalculateRouteFailure(int i) {
        Log.i("MapNavi:","路线规划失败");
    }

    //偏航后重新计算路线回调
    @Override
    public void onReCalculateRouteForYaw() {

    }

    //拥堵后重新计算路线回调
    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    //到达途径点
    @Override
    public void onArrivedWayPoint(int i) {

    }

    //GPS开关状态回调
    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    //显示转弯回调
    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    //隐藏转弯回调
    @Override
    public void hideCross() {

    }

    //显示转弯回调
    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    //隐藏转弯回调
    @Override
    public void hideModeCross() {

    }

    //显示车道信息
    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    //隐藏车道信息
    @Override
    public void hideLaneInfo() {

    }

    //算路成功，开始导航
    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        //设置模拟导航
        //aMapNavi.startNavi(NaviType.EMULATOR);
        aMapNavi.startNavi(NaviType.GPS);
    }

    @Override
    public void notifyParallelRoad(int i) {
        if(i==0){
            //当前在主路过渡
        }
        if(i==1){
            //当前在主路
        }
        if(i==2){
            //当前在辅路
        }

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    /**
     *
     *以下均为导航视图监听
     *
    **/

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {
        //导航加载成功
        //请不要使用AMapNaviView.getMap().setOnMapLoadedListener();
        // 会overwrite导航SDK内部画线逻辑

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定退出导航?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();


        return super.onKeyDown(keyCode, event);
    }

}
