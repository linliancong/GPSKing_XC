package com.zxhl.gpsking;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.entity.CarInfo;
import com.zxhl.util.AdapterUtil;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.SwipeRefreshView;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/1/17.
 */

public class QuerySyYycl extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    //控件
    private ListView list;
    private ImageView img;
    private TextView text;
    private SwipeRefreshView refresh;

    //顶部操作框
    private ImgTxtLayout back;
    private EditText img1;
    private ImageView img2;
    private TextView title;
    private AutoCompleteTextView vehicle;
    private ImageView search;
    private Button getVeh;

    private List<String> autoVehLic=new ArrayList<>();
    private ArrayAdapter<String> adapter;

    //查询等待
    private RelativeLayout yycl_ly_sche;
    private ImageView yycl_img_sche;
    private AnimationDrawable anima;

    private SharedPreferenceUtils sp;
    private Context context;

    private List<List<String>> info;
    private AdapterUtil adapterUtil;
    private ArrayList<CarInfo> carInfos;

    private int page=1;
    private int count=0;
    private static final int PAGE_COUNT=5;

    //是否用车牌号查询
    private boolean isVehicleLic=false;
    //是否在刷新
    private boolean isRefresh=false;
    //数据加载完毕
    private boolean isData=false;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x403:
                    list.setVisibility(View.GONE);
                    yycl_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    break;
                case 0x404:
                    list.setVisibility(View.GONE);
                    yycl_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    Toast.makeText(context,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x001:
                    page=1;
                    count=info.size()/PAGE_COUNT;
                    if(info.size()%PAGE_COUNT>0){
                        count+=1;
                    }
                    showInfo();
                    list.setVisibility(View.VISIBLE);
                    yycl_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    isRefresh=false;
                    isData=true;
                    if(refresh.isRefreshing()){
                        refresh.setRefreshing(false);
                        //refresh.setLoading(false);
                    }
                    img.setVisibility(View.GONE);
                    text.setVisibility(View.GONE);
                    break;
                case 0x003:
                    adapter=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
                case 0x004:
                    showInfo();
                    isRefresh=false;
                    if(refresh.isRefreshing()){
                        refresh.setRefreshing(false);
                        //refresh.setLoading(false);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.query_yycl);

        context=QuerySyYycl.this;

        init();
        /*yycl_ly_sche.setVisibility(View.VISIBLE);
        anima.start();
        getOrderRecord();*/
        getVehicleLic();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_yycl;
    }

    public void init(){
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);
        back=findViewById(R.id.yycl_imgtxt_title);
        list=findViewById(R.id.yycl_list);
        img=findViewById(R.id.yycl_img);
        text=findViewById(R.id.yycl_text);
        refresh=findViewById(R.id.yycl_refresh);

        //顶部操作栏
        back=findViewById(R.id.yycl_imgtxt_title);
        img1=findViewById(R.id.yycl_edit_img);
        img2=findViewById(R.id.yycl_img_img);
        title=findViewById(R.id.yycl_txt_title);
        vehicle=findViewById(R.id.yycl_auto_vehiclelic);
        search=findViewById(R.id.yycl_img_serch);
        getVeh=findViewById(R.id.yycl_btn_get);

        yycl_ly_sche=findViewById(R.id.yycl_ly_sche);
        yycl_img_sche=findViewById(R.id.yycl_img_sche);
        anima= (AnimationDrawable) yycl_img_sche.getDrawable();

        carInfos=new ArrayList<>();

        search.setOnClickListener(this);
        getVeh.setOnClickListener(this);
        //vehicle.addTextChangedListener(this);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        vehicle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                getVeh.callOnClick();
                return true;
            }
        });

        //小圈圈的颜色。转一圈换一种颜色，每一圈耗时1s。
        refresh.setColorSchemeColors(getResources().getColor(R.color.carrot),getResources().getColor(R.color.turquoise),getResources().getColor(R.color.pomegranate));
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isData) {
                    if (!isRefresh) {
                        isRefresh = true;
                        if (info.size() > PAGE_COUNT && info.size() > page * PAGE_COUNT) {
                            isVehicleLic = false;
                            page += 1;
                            handler.sendEmptyMessage(0x004);
                            //getSampleRecord();
                            //显示或隐藏刷新进度条
                            refresh.setRefreshing(true);
                        } else {
                            isRefresh = false;
                            refresh.setRefreshing(false);
                        }
                    }
                }else {
                    refresh.setRefreshing(false);
                }
            }
        });

        //设置一页显示的条目
        //refresh.setItemCount(PAGE_COUNT);

        // 手动调用,通知系统去测量
        /*refresh.measure(0, 0);
        refresh.setRefreshing(true);*/

       /* refresh.setOnLoadMoreListener(new SwipeRefreshView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                            if (!isRefresh) {
                                isRefresh = true;
                                if (info.size() > PAGE_COUNT && info.size() > page * PAGE_COUNT) {
                                    isVehicleLic = false;
                                    page += 1;
                                    handler.sendEmptyMessage(0x004);
                                    //getSampleRecord();
                                    //显示或隐藏刷新进度条
                                    refresh.setRefreshing(true);
                                    //加载完设置不显示
                                    refresh.setLoading(true);
                                } else {
                                    isData=true;
                                    isRefresh = false;
                                    refresh.setRefreshing(false);
                                    //加载完设置不显示
                                    refresh.setLoading(false);
                                }
                            }
                    }
                },2000);
            }
        });*/


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.yycl_img_serch:
                title.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);
                img2.setVisibility(View.VISIBLE);
                vehicle.setVisibility(View.VISIBLE);
                break;
            case R.id.yycl_btn_get:
                ShowKeyboard.hideKeyboard(vehicle);
                if(vehicle.getText().length()!=0) {
                    int permiss = 0;
                    for (int i = 0; i < autoVehLic.size(); i++) {
                        if (vehicle.getText().toString().equalsIgnoreCase(autoVehLic.get(i))) {
                            permiss = 1;
                            break;
                        }
                    }
                    if (permiss == 1) {
                        isData = false;
                        isVehicleLic = true;
                        list.setVisibility(View.GONE);
                        yycl_ly_sche.setVisibility(View.VISIBLE);
                        anima.start();
                        getOrderRecord();
                        title.setVisibility(View.VISIBLE);
                        search.setVisibility(View.VISIBLE);
                        img1.setVisibility(View.GONE);
                        img2.setVisibility(View.GONE);
                        vehicle.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(QuerySyYycl.this, "机号输入有误或者您没有权限操作", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    list.setVisibility(View.GONE);
                    yycl_ly_sche.setVisibility(View.VISIBLE);
                    anima.start();
                    getOrderRecord();
                    title.setVisibility(View.VISIBLE);
                    search.setVisibility(View.VISIBLE);
                    img1.setVisibility(View.GONE);
                    img2.setVisibility(View.GONE);
                    vehicle.setVisibility(View.GONE);
                }
                break;
        }

    }

    //获取样机车辆列表
    private void getOrderRecord(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("VehicleLic",vehicle.getText().toString());
        //vehicle.setText("");

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetOrderRecord", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<List<String>> list=new ArrayList<>();
                    list=parase(result);
                    if(list.size()!=0){
                        info=list;
                        handler.sendEmptyMessage(0x001);
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

    /**
     *解析SoapObject对象
     *@param result
     * @return
     * */
    private List<List<String>> parase(SoapObject result){
        List<List<String>> lists=new ArrayList<>();
        List<String> list;
        SoapObject soap= (SoapObject) result.getProperty(0);
        if(soap==null) {
            return null;
        }
        for (int i=0;i<soap.getPropertyCount();i++){
            SoapObject soapObject= (SoapObject) soap.getProperty(i);
            list=new ArrayList<>();
            list.add(soapObject.getProperty(0).toString());
            list.add(soapObject.getProperty(1).toString());
            list.add(soapObject.getProperty(2).toString());
            lists.add(list);
        }
        return lists;
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
                    list=parases(result);
                    if(list!=null){
                        autoVehLic=list;
                        handler.sendEmptyMessage(0x003);
                    }
                }
            }
        });
    }

    private List<String> parases(SoapObject result){
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

    public void showInfo(){
        int page_last=0;
        //if(isVehicleLic)
        {
            carInfos = new ArrayList<>();
        }
        Collections.reverse(carInfos);
        for(int i=0;i<page*PAGE_COUNT;i++) {
            if(i==info.size()){
                break;
            }else {
                carInfos.add(new CarInfo(info.get(i).get(0), info.get(i).get(1), info.get(i).get(2), "剩余天数：", "最后位置：","车牌号："));
            }
        }

        Collections.reverse(carInfos);

        //if(page==1)
        {
            adapterUtil=new AdapterUtil<CarInfo>(carInfos,R.layout.query_clxx_item){
                @Override
                public void bindView(ViewHolder holder, CarInfo obj) {
                    holder.setText(R.id.clxx_item_vehicle_title,obj.getVehicle_title());
                    holder.setText(R.id.clxx_item_vehicle,obj.getVehicle());
                    holder.setText(R.id.clxx_item_time_title,obj.getTime_title());
                    holder.setText(R.id.clxx_item_time,obj.getTime());
                    holder.setText(R.id.clxx_item_info_title,obj.getInfo_title());
                    holder.setText(R.id.clxx_item_info,obj.getInfo());
                }
            };
            list.setAdapter(adapterUtil);
            list.setSelection(PAGE_COUNT-1);
        }
        /*else {
            adapterUtil.notifyDataSetChanged();
        }*/

        if(page==count){
            page_last=info.size()%PAGE_COUNT;
            list.setSelection(page_last-1);
        }
        else
        {
            list.setSelection(PAGE_COUNT-1);
        }

        /*list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //这里处理点击，暂时不用
            }
        });*/

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
}
