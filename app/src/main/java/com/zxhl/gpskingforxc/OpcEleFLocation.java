package com.zxhl.gpskingforxc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.zxhl.entity.LatLngInfo;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/1/8.
 */

public class OpcEleFLocation extends StatusBarUtil implements AMapLocationListener,LocationSource,View.OnClickListener,TextWatcher,RadioGroup.OnCheckedChangeListener{

    private MapView map;
    private ImgTxtLayout back;
    private EditText img1;
    private ImageView img2;
    private TextView title;

    private AutoCompleteTextView vehicle;
    private ImageView search;
    private Button getVeh;

    private SharedPreferenceUtils sp;
    private List<String> locat=new ArrayList<>();
    private List<String> autoVehLic=new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private double longitude =0;
    private double latitude =0;


    //声明AMapLocationClient对象
    private AMapLocationClient mapClient;
    //声明AMapLocationClientOption对象
    private AMapLocationClientOption mapOption=null;
    //声明地图控制器
    private AMap aMap;
    //声明定位蓝点
    private MyLocationStyle style;
    //监听对象
    private OnLocationChangedListener mListener;
    //是第一次运行标志
    private boolean isFirst=true;
    //是否切换视角
    private boolean isChange=false;
    //定义地图图层
    private MarkerOptions markerOptions;
    private Marker marker;

    private RelativeLayout opc_ly_sche;
    private ImageView opc_img_sche;
    private AnimationDrawable anima;

    private int pression=0;

    //Google地图相关
    //类型查询
    private RadioGroup rg;
    private RadioButton gaodemap;
    private RadioButton googlemap;

    private WebView google_map;
    //是否有数据
    private boolean isPoi=false;
    //该地图是否显示过
    private boolean isShowMapTag=false;
    private boolean isShowMapTag2=false;
    //现在显示的是什么地图1、高德2、谷歌
    private int mapType=1;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x403:
                    opc_ly_sche.setVisibility(View.GONE);
                    if(mapType==1) {
                        map.setVisibility(View.VISIBLE);
                    }else {
                        google_map.setVisibility(View.VISIBLE);
                    }
                    anima.stop();
                    Toast.makeText(OpcEleFLocation.this,"没有查询到位置信息",Toast.LENGTH_SHORT).show();
                    break;
                case 0x404:
                    opc_ly_sche.setVisibility(View.GONE);
                    if(mapType==1) {
                        map.setVisibility(View.VISIBLE);
                    }else {
                        google_map.setVisibility(View.VISIBLE);
                    }
                    anima.stop();
                    Toast.makeText(OpcEleFLocation.this,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x001:
                    adapter=new ArrayAdapter<String>(OpcEleFLocation.this,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
                case 0x002:
                    opc_ly_sche.setVisibility(View.GONE);
                    map.setVisibility(View.VISIBLE);
                    anima.stop();
                    isShowMapTag=false;
                    isShowMapTag2=false;
                    isPoi=true;
                    if(mapType==1){
                        map.setVisibility(View.VISIBLE);
                        showGaodeMap();
                        isShowMapTag=true;
                    }
                    else {
                        //showGoogleMap();
                        google_map.setVisibility(View.VISIBLE);
                        google_map.addJavascriptInterface(new SharpJS(),"sharp");
                        google_map.loadUrl("file:///android_asset/google_mapTag.html");
                        isShowMapTag2=true;
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.opc_location);

        sp=new SharedPreferenceUtils(this, Constants.SAVE_USER);
        back=(ImgTxtLayout)findViewById(R.id.opc_imgtxt_title);
        img1=findViewById(R.id.opc_edit_img);
        img2=findViewById(R.id.opc_img_img);
        title=findViewById(R.id.opc_txt_title);

        vehicle=findViewById(R.id.opc_auto_vehiclelic);
        search=findViewById(R.id.opc_img_serch);
        getVeh=findViewById(R.id.opc_btn_get);

        opc_ly_sche=findViewById(R.id.opc_ly_sche);
        opc_img_sche=findViewById(R.id.opc_img_sche);
        anima= (AnimationDrawable) opc_img_sche.getDrawable();
        //获取地图控件引用
        map=(MapView)findViewById(R.id.opc_map_loction);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        map.onCreate(savedInstanceState);

        //Google地图相关
        //类型查询
        rg=findViewById(R.id.loction_rg_type);
        gaodemap =findViewById(R.id.loction_gaodemap);
        googlemap =findViewById(R.id.loction_googlemap);
        rg.setOnCheckedChangeListener(this);

        google_map=findViewById(R.id.map);
        //设置WebView属性,依次如下
        //支持js,不支持缩放
        //同时绑定Java对象
        google_map.getSettings().setJavaScriptEnabled(true);
        google_map.getSettings().setSupportZoom(false);
        google_map.getSettings().setDefaultTextEncodingName("UTF-8");
        google_map.loadUrl("file:///android_asset/google_mapInit.html");


        if(aMap==null){
            aMap=map.getMap();
            initMapStyle();
            initLocation();
        }

        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                aMap.clear();
                longitude =latLng.longitude;
                latitude =latLng.latitude;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_marker));
                markerOptions.position(latLng);
                aMap.addMarker(markerOptions);
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
            }
        });

        search.setOnClickListener(this);
        getVeh.setOnClickListener(this);
        //vehicle.addTextChangedListener(this);
        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getVehicleLic();

        vehicle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                getVeh.callOnClick();
                return true;
            }
        });

        if(getIntent().getStringExtra("VehicleLic")!=null) {
            if (getIntent().getStringExtra("VehicleLic").length() != 0) {
                vehicle.setText(getIntent().getStringExtra("VehicleLic"));
                vehicle.setSelection(vehicle.getText().length());
                if (getIntent().getIntExtra("IsOnline", 0) == 1) {
                    getPoi();
                    opc_ly_sche.setVisibility(View.VISIBLE);
                    map.setVisibility(View.GONE);
                    anima.start();
                } else {
                    Toast.makeText(OpcEleFLocation.this,"车辆不在线",Toast.LENGTH_SHORT).show();
                }

            }
        }


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.opc_eleflocation;
    }

    private void initLocation() {
        //定位相关初始化
        mapClient=new AMapLocationClient(this);
        mapOption=new AMapLocationClientOption();
        //设置定位监听
        mapClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battert_Saving为低功耗模式，Device_Sensors是仅设备模式
        mapOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位时间间隔
        mapOption.setInterval(2000);
        //设置单次定位
            /*mapOption.setOnceLocation(true);
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。
            // 如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mapOption.setOnceLocationLatest(true);*/
        //设置定位参数
        mapClient.setLocationOption(mapOption);
        //启动定位
        mapClient.startLocation();


            /*aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    if(location!=null){

                        location.getLatitude();
                        location.getLongitude();
                        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date=new Date(location.getTime());
                        //定位的时间
                        df.format(date);
                    }
                }
            });*/
    }

    private void initMapStyle() {
        //设置地图的类型
        //aMap.setMapType(AMap.MAP_TYPE_NAVI);
        //设置显示定位按钮并且可以点击
        UiSettings settings=aMap.getUiSettings();
        //设置定位监听，要实现LoactionSource接口
        aMap.setLocationSource(this);
        //是否显示定位按钮
        settings.setMyLocationButtonEnabled(true);
        //初始化定位蓝点样式类
        //style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        style=new MyLocationStyle();
        //LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER:连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
        //LOCATION_TYPE_FOLLOW_NO_CENTER:连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
        //LOCATION_TYPE_MAP_ROTATE_NO_CENTER:连续定位、蓝点不会移动到地图中心点，地图依照设备方向旋转，并且蓝点会跟随设备移动。
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效
        style.interval(2000);

        //自定义定位蓝点
        //图标
        style.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.navi_map_gps_locked));
        //定位锚点
        //style.anchor((float) 0.0,(float) 0.3);
        //精度圈边框颜色
        style.strokeColor(Color.parseColor("#331B85FF"));
        //精度圈填充颜色
        style.radiusFillColor(Color.parseColor("#111B85FF"));
        //精度圈宽度
        //style.strokeWidth(5);

        //设置定位蓝点的Style
        aMap.setMyLocationStyle(style);
        //设置默认定位按钮是否显示，非必需设置。
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);
        //显示定位层，并且可以触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        //设置地图的缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomBy(6));
        /*aMap.showIndoorMap(true);
        aMap.setMapType(AMap.MAP_TYPE_NAVI);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
        if(mapClient!=null) {
            mapClient.stopLocation();
            mapClient.onDestroy();
        }
    }

    //获取车辆列表
    private void getVehicleLic(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("Key","");

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetVehiclelicByKey", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<String> list=new ArrayList<String>();
                    list=parase(result);
                    if(list!=null){
                        autoVehLic=list;
                        handler.sendEmptyMessage(0x001);
                    }
                }
            }
        });
    }

    //获取车辆位置
    private void getPoi(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("VehicleLic",vehicle.getText().toString());

        WebServiceUtils.callWebService(WebServiceUtils.OperaCenter_URL, "QueryNowGpsDataByVehicleLic", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<String> list=new ArrayList<String>();
                    list=parase(result);
                    if(list.size()!=0){
                        locat=list;
                        handler.sendEmptyMessage(0x002);
                    }
                    else
                    {
                        handler.sendEmptyMessage(0x403);
                    }
                }
                else{
                    handler.sendEmptyMessage(0x404);
                }
            }
        });
    }

    private List<String> parase(SoapObject result){
        List<String> list=new ArrayList<>();
        SoapObject soap= (SoapObject) result.getProperty(0);
        if(soap==null) {
            return null;
        }
        for (int i=0;i<soap.getPropertyCount();i++){
            list.add(soap.getProperty(i).toString());
        }
        return list;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(aMapLocation!=null && mListener!=null){
            if(aMapLocation!=null && aMapLocation.getErrorCode()==0){
                //定位成功回调信息，设置相关消息

                //获取定位来源
                aMapLocation.getLocationType();
                //获取纬度
                aMapLocation.getLatitude();
                //获取经度
                aMapLocation.getLongitude();
                //获取精度信息
                aMapLocation.getAccuracy();
                SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date=new Date(aMapLocation.getTime());
                //定位的时间
                df.format(date);
                //mListener.onLocationChanged(aMapLocation);
                //如果不设置标志位，此时拖动地图，他会不断的移动到当前位置
                if(isFirst){
                    //将地图移到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude())));
                    //点击定位按钮，能够将地图的中心点移动到定位点
                    mListener.onLocationChanged(aMapLocation);
                    //获取定位信息

                    //设置标志位
                    isFirst=false;

                }
            }
            else{
                //显示错误信息
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener=onLocationChangedListener;
        /*if(!isFirst){
            isChange=!isChange;
            if (isChange){
                style.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);
                aMap.setMyLocationStyle(style);

            }
            else {
                style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
                aMap.setMyLocationStyle(style);

            }

        }*/
    }

    @Override
    public void deactivate() {
        mListener=null;
        if(mapClient!=null){
            mapClient.stopLocation();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.opc_img_serch:
                title.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);
                img2.setVisibility(View.VISIBLE);
                vehicle.setVisibility(View.VISIBLE);
                break;
            case R.id.opc_btn_get:
                if(longitude !=0&& latitude !=0) {
                    Intent intent = getIntent();
                    intent.putExtra("Lon", longitude);
                    intent.putExtra("Lat", latitude);
                    setResult(0x002, intent);
                    finish();
                }else{
                    Toast.makeText(OpcEleFLocation.this,"地址不能为空，请选择...",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        getVeh.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(vehicle.getText().length()!=0){
            getVeh.setEnabled(true);
        }

    }

    //Google地图相关
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.loction_gaodemap:
                setChecked();
                gaodemap.setChecked(true);
                mapType=1;
                map.setVisibility(View.VISIBLE);
                if (isPoi){
                    if (!isShowMapTag){
                        isShowMapTag2=true;
                        showGaodeMap();
                    }
                }
                break;
            case R.id.loction_googlemap:
                setChecked();
                googlemap.setChecked(true);
                mapType=2;
                google_map.setVisibility(View.VISIBLE);
                if(isPoi){
                    if (!isShowMapTag2){
                        isShowMapTag2=true;
                        //showGoogleMap();
                        google_map.addJavascriptInterface(new SharpJS(),"sharp");
                        google_map.loadUrl("file:///android_asset/google_mapTag.html");
                    }
                }
                break;
        }
    }

    public void setChecked(){
        gaodemap.setChecked(false);
        googlemap.setChecked(false);
        map.setVisibility(View.GONE);
        google_map.setVisibility(View.GONE);
    }

    public void showGaodeMap(){
        if(locat.size()>6) {
            //getVeh.setEnabled(true);
            double lat = 0, lng = 0;
            Double dlat = new Double(locat.get(5));
            Double dlng = new Double(locat.get(6));
            latitude =dlat;
            longitude =dlng;
            lat = dlat.doubleValue();
            lng = dlng.doubleValue();
            markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            //点标记标题及内容
            markerOptions.title("详细信息");
            markerOptions.snippet("定位时间："+locat.get(4)+"\n"+
                    "经度："+locat.get(6)+"\n"+
                    "纬度："+locat.get(5)+"\n"+
                    "电源状态："+locat.get(0)+"\n"+
                    "ACC："+locat.get(1)+"\n"+
                    "速度："+locat.get(8));
            //点标记是否可拖动
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point));
            //将Maeker设置为贴地显示，可以双指下拉地图查看效果
            markerOptions.setFlat(true);
            //添加标记
            marker=aMap.addMarker(markerOptions);
            //将中心点移动到车辆点
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
            //Toast.makeText(getApplicationContext(),"获取成功",Toast.LENGTH_LONG).show();
        }
        else {
            //getVeh.setEnabled(false);
            Toast.makeText(OpcEleFLocation.this,"没有查询到位置信息",Toast.LENGTH_SHORT).show();
        }

    }
    /*public void showGoogleMap(){
        double lat=0,lng=0;
        Double dlat=new Double(locat.get(0));
        Double dlng=new Double(locat.get(1));
        lat=dlat.doubleValue();
        lng=dlng.doubleValue();

        //标记地图的点
        google_map.loadUrl("http://www.google.cn/maps?q="+lat+","+lng+"(zxhl)&z=17&hl=cn");
    }*/

    //自定义一个js业务类
    public class SharpJS{
        @JavascriptInterface
        public void showLatLng(){
            google_map.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String json = buildJson(getLatLng());
                        google_map.loadUrl("javascript:showTag('" + json + "')");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });



        }

        //将获取的坐标写入到JsonObject中在添加到JsonArray数组中
        public String buildJson(List<LatLngInfo> latLngs)throws Exception{
            JSONArray jsonArray=new JSONArray();
            for(LatLngInfo latLng:latLngs){
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("lat",latLng.getLat());
                jsonObject.put("lng",latLng.getLng());
                jsonArray.put(jsonObject);
            }
            return jsonArray.toString();
        }

        //定义一个获取坐标的方法 返回的是List<LatLngInfo>
        public List<LatLngInfo> getLatLng(){
            List<LatLngInfo> latLngInfos=new ArrayList<LatLngInfo>();
            LatLngInfo latLngInfo=new LatLngInfo();

            if(locat.size()>6) {
                double lat = 0, lng = 0;
                Double dlat = new Double(locat.get(5));
                Double dlng = new Double(locat.get(6));
                lat = dlat.doubleValue();
                lng = dlng.doubleValue();
                latLngInfo.setLat(lat);
                latLngInfo.setLng(lng);
            }
            else {
                Toast.makeText(OpcEleFLocation.this,"没有查询到位置信息",Toast.LENGTH_SHORT).show();
            }


            latLngInfos.add(latLngInfo);
            return latLngInfos;
        }
    }
}
