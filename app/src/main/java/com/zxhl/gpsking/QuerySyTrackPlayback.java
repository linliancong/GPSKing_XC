package com.zxhl.gpsking;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.bigkoo.pickerview.TimePickerView;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.apache.http.conn.BasicEofSensorWatcher;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/1/8.
 */

public class QuerySyTrackPlayback extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private MapView map;
    private ImgTxtLayout back;

    private SharedPreferenceUtils sp;
    private List<String> locat=new ArrayList<>();
    private List<String> autoVehLic=new ArrayList<>();
    private ArrayAdapter<String> adapter;

    //地图底部自定义控件
    //搜索
    private RelativeLayout search_ly;
    private AutoCompleteTextView vehicle;
    private EditText beginTime;
    private EditText endTime;
    private Button getVeh;
    //播放和隐藏
    private RelativeLayout bottom_ly;
    private RelativeLayout bottom_ly1;
    private LinearLayout play_ly;
    private Button start;
    private Button pause;
    private Button stop;
    private SeekBar seekBar;
    private TextView num;
    private ImgTxtLayout hind;

    private AnimationDrawable anima;
    private ImageView query_img_sche;
    private RelativeLayout query_ly_sche;

    private Thread thread;
    private Thread synNotify;
    private Thread synSeekBar;
    private static boolean isWait=false;
    private static boolean isNotify=false;
    private static boolean isSeek=true;
    private static boolean isSeekRun=false;
    //线程运行标记
    private boolean threadTag=true;
    //线程中for循环运行标记
    private boolean threadForTag=true;


    private static int proCount=0;
    private static int anInt=0;



    //声明地图控制器
    private AMap aMap;
    //声明定位蓝点
    private MyLocationStyle style;
    //是第一次运行标志
    private boolean isFirst=true;
    //是否切换视角
    private boolean isChange=false;
    //定义地图画线条
    private Polyline polyline;
    private PolylineOptions polylineOptions;
    //定义地图点
    private List<LatLng> latLngs;
    //定义地图图层
    private Marker marker;
    private List<Marker> markers;
    private MarkerOptions markerOptions;

    private Calendar selectedDate;

    Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x403:
                    query_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    Toast.makeText(QuerySyTrackPlayback.this,"没有查询到位置信息，请稍后重试",Toast.LENGTH_SHORT).show();
                    break;
                case 0x404:
                    query_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    Toast.makeText(QuerySyTrackPlayback.this,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x001:
                    adapter=new ArrayAdapter<String>(QuerySyTrackPlayback.this,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
                case 0x002:
                    playbackTrack();
                    break;
                case 0x003:
                    int size=msg.getData().getInt("size");
                    if(size==seekBar.getMax()){
                        isWait=true;
                    }
                    seekBar.setProgress(size);
                    num.setText(size+"/"+seekBar.getMax());
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.query_track);

        init();
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        map.onCreate(savedInstanceState);

        if(aMap==null){
            aMap=map.getMap();
            aMap.getUiSettings().setZoomControlsEnabled(false);
            //设置地图的缩放级别
            aMap.moveCamera(CameraUpdateFactory.zoomBy(3));
        }

        getVehicleLic();

        vehicle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                getVeh.callOnClick();
                return true;
            }
        });


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_track;
    }

    private void init() {
        sp=new SharedPreferenceUtils(this, Constants.SAVE_USER);
        back=(ImgTxtLayout)findViewById(R.id.query_imgtxt_title_tra);
        //搜索
        search_ly=findViewById(R.id.query_ly_bottom_search);
        vehicle=findViewById(R.id.query_auto_vehiclelic_tra);
        beginTime=findViewById(R.id.query_edit_BeginTime);
        endTime=findViewById(R.id.query_edit_EndTime);
        getVeh=findViewById(R.id.query_btn_get_tra);
        //播放和隐藏
        bottom_ly=findViewById(R.id.query_ly_bottom);
        bottom_ly1=findViewById(R.id.query_ly_bottom1);
        play_ly=findViewById(R.id.query_ly_play);
        start=findViewById(R.id.query_btn_start);
        pause=findViewById(R.id.query_btn_pause);
        stop=findViewById(R.id.query_btn_stop);
        seekBar =findViewById(R.id.query_pro_play);
        num=findViewById(R.id.query_txt_num);
        hind=findViewById(R.id.query_imgtxt_bottom_tra);

        query_img_sche=findViewById(R.id.query_img_sche);
        query_ly_sche=findViewById(R.id.query_ly_sche);
        anima= (AnimationDrawable) query_img_sche.getDrawable();

        //获取地图控件引用
        map=(MapView)findViewById(R.id.query_map_loction_tra);
        markerOptions=new MarkerOptions();




        //设置默认显示时间
        selectedDate=Calendar.getInstance();
        Date today=new Date();
        Date olyday=new Date(today.getTime()-3*24*60*60*1000);
        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");
        String end=sf.format(today);
        String begin=sf.format(olyday);
        beginTime.setText(begin);
        endTime.setText(end);

        //设置线程同步加锁操作
        Object lock=new Object();
        thread=new Thread(new PlayBack(lock));
        synNotify=new Thread(new PlayBackSyn(lock));
        synSeekBar=new Thread(new PlaySeekBar(lock));


        getVeh.setOnClickListener(this);
        start.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        bottom_ly.setOnClickListener(this);
        bottom_ly1.setOnClickListener(this);
        beginTime.setOnClickListener(this);
        endTime.setOnClickListener(this);

        vehicle.addTextChangedListener(this);
        beginTime.addTextChangedListener(this);
        endTime.addTextChangedListener(this);
        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        hind.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num.setText(progress+"/"+seekBar.getMax());
                proCount=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeek=false;
                isWait=true;
                isNotify=true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(markerOptions!=null) {
                    polylineOptions=new PolylineOptions();
                    aMap.clear();
                }
                isSeekRun=true;
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
        threadForTag=false;
        threadTag=false;
        Log.d("Thread111", "Activity --onDestroy中线程是否活着:" + thread.isAlive());
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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

    //获取车辆轨迹点
    private void getTrackPlay(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("VehicleLic",vehicle.getText().toString());
        proper.put("BeginTime",beginTime.getText().toString());
        proper.put("EndTime",endTime.getText().toString());

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetTrackPlay", proper, new WebServiceUtils.WebServiceCallBack() {
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.query_btn_get_tra:
                ShowKeyboard.hideKeyboard(vehicle);
                int permiss=0;
                for(int i=0;i<autoVehLic.size();i++)
                {
                    if(vehicle.getText().toString().equalsIgnoreCase(autoVehLic.get(i))){
                        permiss=1;
                        break;
                    }
                }
                if(permiss==1){
                    query_ly_sche.setVisibility(View.VISIBLE);
                    anima.start();
                    isWait=true;
                    threadForTag=false;
                    getTrackPlay();
                }
                else{
                    Toast.makeText(QuerySyTrackPlayback.this,"机号输入有误或者您没有权限操作",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.query_ly_bottom:
                search_ly.setVisibility(View.VISIBLE);
                bottom_ly1.setVisibility(View.VISIBLE);
                play_ly.setVisibility(View.GONE);
                hind.setVisibility(View.GONE);
                break;
            case R.id.query_ly_bottom1:
                search_ly.setVisibility(View.GONE);
                bottom_ly1.setVisibility(View.GONE);
                hind.setVisibility(View.VISIBLE);
                if(latLngs!=null) {
                    play_ly.setVisibility(View.VISIBLE);
                }
                else {
                    play_ly.setVisibility(View.GONE);
                }
                break;

            case R.id.query_btn_start:
                if(markerOptions!=null) {
                    polylineOptions=new PolylineOptions();
                    aMap.clear();
                }
                if(!thread.isAlive()) {
                    thread.start();
                }
                if (isWait) {
                    isNotify = true;
                    isWait=false;
                }
                anInt = 0;
                if(!threadForTag){
                    threadForTag=true;
                }

                if (!synNotify.isAlive()) {
                    synNotify.start();
                }
                if(!synSeekBar.isAlive()) {
                    synSeekBar.start();
                }
                break;
            case R.id.query_btn_stop:
                isWait=true;
                break;
            case R.id.query_btn_pause:
                if(isWait) {
                    isNotify = true;
                    isWait=false;
                }
                break;
            case R.id.query_edit_BeginTime:
                ShowKeyboard.hideKeyboard(vehicle);
                Date etoday=new Date();
                Date olyday=new Date(etoday.getTime()-3*24*60*60*1000);
                SimpleDateFormat esf=new SimpleDateFormat("yyyy");
                SimpleDateFormat esf2=new SimpleDateFormat("MM");
                SimpleDateFormat esf3=new SimpleDateFormat("dd");
                Integer eyear=new Integer(esf.format(olyday));
                Integer emonth=new Integer(esf2.format(olyday));
                Integer eday=new Integer(esf3.format(olyday));
                selectedDate.set(eyear.intValue(),emonth.intValue()-1,eday.intValue());
                TimePicker(1);
                break;
            case R.id.query_edit_EndTime:
                ShowKeyboard.hideKeyboard(vehicle);
                Date today=new Date();
                SimpleDateFormat sf=new SimpleDateFormat("yyyy");
                SimpleDateFormat sf2=new SimpleDateFormat("MM");
                SimpleDateFormat sf3=new SimpleDateFormat("dd");
                Integer year=new Integer(sf.format(today));
                Integer month=new Integer(sf2.format(today));
                Integer day=new Integer(sf3.format(today));
                selectedDate.set(year.intValue(),month.intValue()-1,day.intValue());
                TimePicker(0);
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
        if(vehicle.getText().length()!=0&&beginTime.getText().length()!=0&&endTime.getText().length()!=0){
            getVeh.setEnabled(true);
        }

    }

    private void playbackTrack() {
        search_ly.setVisibility(View.GONE);
        bottom_ly1.setVisibility(View.GONE);
        play_ly.setVisibility(View.VISIBLE);
        hind.setVisibility(View.VISIBLE);
        double lat=0,lng=0;
        Double dlat,dlng;
        polylineOptions=new PolylineOptions();
        latLngs=new ArrayList<>();
        for(int i=0;i<locat.size();i+=3) {
            dlat = new Double(locat.get(i+2));
            dlng = new Double(locat.get(i+1));
            lat = dlat.doubleValue();
            lng = dlng.doubleValue();
            latLngs.add(new LatLng(lat,lng));
        }
        seekBar.setMax(latLngs.size());
        seekBar.setProgress(1);
        num.setText("0/"+seekBar.getMax());
        if(markers!=null){
            aMap.clear();
        }

        if(!thread.isAlive()) {
            thread.start();
        }
        if (isWait) {
            isNotify = true;
            isWait=false;
        }
        anInt=0;
        if(!threadForTag){
            threadForTag=true;
        }
        if (!synNotify.isAlive()) {
            synNotify.start();
        }
        if(!synSeekBar.isAlive()) {
            synSeekBar.start();
        }
        query_ly_sche.setVisibility(View.GONE);
        anima.stop();
        aMap.moveCamera(CameraUpdateFactory.zoomBy(4));
    }

    private class PlayBack implements Runnable{
        private Object lock;

        public PlayBack(Object lock){
            this.lock=lock;
        }

        @Override
        public void run() {
            try {
                while (threadTag) {
                    for (anInt=0;anInt<latLngs.size();anInt++) {
                        if(!threadForTag){
                            break;
                        }
                        if(anInt==0){
                            markers=new ArrayList<>();
                        }
                        QuerySyTrackPlayback play=new QuerySyTrackPlayback();
                        play.synWait(lock);
                        Message msg=new Message();
                        msg.what=0x003;
                        msg.getData().putInt("size",anInt+1);
                        handler.sendMessage(msg);


                        //设置数据源
                        polylineOptions.add(latLngs.get(anInt));
                        //polylineOptions.addAll(latLngs);
                        //设置线条宽度
                        polylineOptions.width(4);
                        //设置线条颜色
                        polylineOptions.color(Color.RED);
                        //将线条加到地图上
                        polyline=aMap.addPolyline(polylineOptions);


                        //设置点
                        markerOptions.position(latLngs.get(anInt));
                        //设置图片
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point));
                        //将Maeker设置为贴地显示，可以双指下拉地图查看效果
                        markerOptions.setFlat(true);
                        //添加标记
                        marker=aMap.addMarker(markerOptions);
                        marker.setObject(anInt);
                        markers.add(marker);
                        //设置标记点显示
                        for (int i=0;i<markers.size();i++){
                            Marker mk=markers.get(i);
                            mk.setVisible(false);
                            if(i==0){
                                mk.setVisible(true);
                            }
                            if(i==markers.size()-1){
                                mk.setVisible(true);
                            }

                        }


                        //将中心点移动到车辆点
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLngs.get(anInt)));
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private class PlayBackSyn implements Runnable{
        int count=0;
        private Object lock;

        public PlayBackSyn(Object lock){
            this.lock=lock;
        }

        @Override
        public void run() {
            try {
                while (threadTag) {
                    QuerySyTrackPlayback play=new QuerySyTrackPlayback();
                    play.synNotify(lock);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class PlaySeekBar implements Runnable{
        int count=0;
        private Object lock;

        public PlaySeekBar(Object lock){
            this.lock=lock;
        }

        @Override
        public void run() {
            try {
                while (threadTag) {
                    QuerySyTrackPlayback play=new QuerySyTrackPlayback();
                    play.synWait2(lock);
                    while (isSeekRun) {
                        for (int j = 0; j < proCount; j++) {
                            if(!threadForTag){
                                break;
                            }
                            if(j==0){
                                //设置点
                                markerOptions.position(latLngs.get(j));
                                //设置图片
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point));
                                //将Maeker设置为贴地显示，可以双指下拉地图查看效果
                                markerOptions.setFlat(true);
                                //添加标记
                                aMap.addMarker(markerOptions);
                            }
                            polylineOptions.add(latLngs.get(j));
                            //设置线条宽度
                            polylineOptions.width(4);
                            //设置线条颜色
                            polylineOptions.color(Color.RED);
                            //将线条加到地图上
                            aMap.addPolyline(polylineOptions);
                            if (j == proCount - 1) {
                                anInt = j+1;
                                aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLngs.get(j)));

                                isSeek=true;
                                isWait=false;
                                isNotify=true;
                            }
                        }
                        isSeekRun=false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void synWait(Object lock) {
        try {
            synchronized (lock) {
                while (isWait) {
                    lock.wait();
                    //isWait=false;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void synWait2(Object lock) {
        try {
            synchronized (lock) {
                while (isSeek) {
                    lock.wait();
                    //isWait=false;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void synNotify(Object lock) {
        try {
            synchronized (lock) {
                while (isNotify) {
                    isNotify=false;
                    lock.notifyAll();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void TimePicker(final int state){
        TimePickerView time=new TimePickerView.Builder(QuerySyTrackPlayback.this, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                if(state==1) {
                    beginTime.setText(getTime(date));
                }
                else {
                    endTime.setText(getTime(date));
                }
            }
        })
                .setDate(selectedDate)
                .setType(new boolean[]{true,true,true,false,false,false})
                .setLabel("","","","","","")
                .build();
        //精确到秒的时间显示
        //time.setDate(Calendar.getInstance());
        time.show();
    }

    //可根据需要自行截取数据显示
    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

}
