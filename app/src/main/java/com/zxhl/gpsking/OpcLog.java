package com.zxhl.gpsking;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.entity.CodeHistory;
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
 * Created by Administrator on 2018/1/29.
 */

public class OpcLog extends StatusBarUtil implements View.OnClickListener,TextWatcher,RadioGroup.OnCheckedChangeListener{

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
    private RelativeLayout log_ly_sche;
    private ImageView log_img_sche;
    private AnimationDrawable anima;

    private SharedPreferenceUtils sp;
    private Context context;

    private List<List<String>> info;
    private List<List<String>> info1;
    private List<List<String>> info2;
    private AdapterUtil adapterUtil;
    private ArrayList<CodeHistory> Log;

    private int page=1;
    private int count=0;
    private int count1=0;
    private int count2=0;
    private static final int PAGE_COUNT=20;

    //是否用车牌号查询
    private boolean isVehicleLic=false;
    //是否在刷新
    private boolean isRefresh=false;
    //数据加载完毕
    private boolean isData=false;

    //类型查询
    private RadioGroup rg;
    private RadioButton app;
    private RadioButton pc;

    //是否有数据
    private boolean isLog=false;
    //记录是否显示过
    private boolean isShowMapTag1=false;
    private boolean isShowMapTag2=false;
    //现在显示的是什么记录1、APP2、PC
    private int logType =1;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x403:
                    list.setVisibility(View.GONE);
                    log_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    break;
                case 0x404:
                    list.setVisibility(View.GONE);
                    log_ly_sche.setVisibility(View.GONE);
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
                    if(logType==1){
                        info1=info;
                        count1=count;
                        isShowMapTag1=true;
                    }else{
                        info2=info;
                        count2=count;
                        isShowMapTag2=true;
                    }
                    showInfo();
                    list.setVisibility(View.VISIBLE);
                    log_ly_sche.setVisibility(View.GONE);
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
        //setContentView(R.layout.query_log);

        context=OpcLog.this;

        init();
        getVehicleLic();
        if(getIntent().getStringExtra("VehicleLic")!=null) {
            if (getIntent().getStringExtra("VehicleLic").length() != 0) {
                vehicle.setText(getIntent().getStringExtra("VehicleLic"));
                vehicle.setSelection(vehicle.getText().length());
                list.setVisibility(View.GONE);
                log_ly_sche.setVisibility(View.VISIBLE);
                anima.start();
                getCodeHistory();
            }
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.opc_log;
    }

    public void init(){
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);
        back=findViewById(R.id.log_imgtxt_title);
        list=findViewById(R.id.log_list);
        img=findViewById(R.id.log_img);
        text=findViewById(R.id.log_text);
        refresh=findViewById(R.id.log_refresh);

        //顶部操作栏
        back=findViewById(R.id.log_imgtxt_title);
        img1=findViewById(R.id.log_edit_img);
        img2=findViewById(R.id.log_img_img);
        title=findViewById(R.id.log_txt_title);
        vehicle=findViewById(R.id.log_auto_vehiclelic);
        search=findViewById(R.id.log_img_serch);
        getVeh=findViewById(R.id.log_btn_get);

        log_ly_sche=findViewById(R.id.log_ly_sche);
        log_img_sche=findViewById(R.id.log_img_sche);
        anima= (AnimationDrawable) log_img_sche.getDrawable();

        //类型查询
        rg=findViewById(R.id.log_rg_type);
        app =findViewById(R.id.log_app);
        pc =findViewById(R.id.log_pc);
        rg.setOnCheckedChangeListener(this);
        app.setChecked(true);

        Log =new ArrayList<>();

        search.setOnClickListener(this);
        getVeh.setOnClickListener(this);

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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.log_img_serch:
                title.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);
                img2.setVisibility(View.VISIBLE);
                vehicle.setVisibility(View.VISIBLE);
                break;
            case R.id.log_btn_get:
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
                        log_ly_sche.setVisibility(View.VISIBLE);
                        anima.start();
                        if(logType==1) {
                            getCodeHistory();
                        }else {
                            getCodeHistory_PC();
                        }
                        title.setVisibility(View.VISIBLE);
                        search.setVisibility(View.VISIBLE);
                        img1.setVisibility(View.GONE);
                        img2.setVisibility(View.GONE);
                        vehicle.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(OpcLog.this, "机号输入有误或者您没有权限操作", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    list.setVisibility(View.GONE);
                    log_ly_sche.setVisibility(View.VISIBLE);
                    anima.start();
                    if(logType==1) {
                        getCodeHistory();
                    }else {
                        getCodeHistory_PC();
                    }
                    title.setVisibility(View.VISIBLE);
                    search.setVisibility(View.VISIBLE);
                    img1.setVisibility(View.GONE);
                    img2.setVisibility(View.GONE);
                    vehicle.setVisibility(View.GONE);
                }
                break;
        }

    }

    //获取指令下发记录列表
    private void getCodeHistory(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("VehicleLic",vehicle.getText().toString());
        //vehicle.setText("");

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetCodeHistory", proper, new WebServiceUtils.WebServiceCallBack() {
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

    //获取指令下发记录列表（PC）
    private void getCodeHistory_PC(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("VehicleLic",vehicle.getText().toString());
        //vehicle.setText("");

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetCodeHistory_PC", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<List<String>> list=new ArrayList<>();
                    list=parase2(result);
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
        String type="";
        String[] types=new String[]{};
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
            types=soapObject.getProperty(3).toString().split(",");
            if(types.length==1) {
                switch (types[0]) {
                    case "锁车监控0":
                        type = "全部解锁";
                        break;
                    case "锁车监控1":
                        type = "一级锁车";
                        break;
                    case "锁车监控2":
                        type = "二级锁车";
                        break;
                    case "锁车监控3":
                        type = "一级解锁";
                        break;
                    case "锁车监控4":
                        type = "二级解锁";
                        break;
                    case "锁车监控5":
                        type = "退出监控";
                        break;
                    case "锁车监控6":
                        type = "进入监控";
                        break;
                    case "断油断电1":
                        type = "一级断油电";
                        break;
                    case "断油断电2":
                        type = "一级恢复油电";
                        break;
                    case "断油断电3":
                        type = "二级断油电";
                        break;
                    case "断油断电4":
                        type = "二级恢复油电";
                        break;
                    case "车辆位置查询":
                        type = "车辆位置查询";
                        break;
                }
            }
            else
            {
                switch (types[1]) {
                    case "0":
                        type="退出样机模式";
                        break;
                    case "1":
                        type="进入样机模式";
                        break;
                }
            }
            list.add(type);
            list.add(soapObject.getProperty(4).toString());
            lists.add(list);
        }
        return lists;
    }

    /**
     *解析SoapObject对象
     *@param result
     * @return
     * */
    private List<List<String>> parase2(SoapObject result){
        List<List<String>> lists=new ArrayList<>();
        List<String> list;
        String type="";
        String[] types=new String[]{};
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
            list.add(soapObject.getProperty(3).toString());
            list.add(soapObject.getProperty(4).toString());
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
        Log = new ArrayList<>();

        if(logType==1){
            info=info1;
            count=count1;
        }else {
            info=info2;
            count=count2;
        }

        Collections.reverse(Log);
        for(int i=0;i<page*PAGE_COUNT;i++) {
            if(i==info.size()){
                break;
            }else {
                Log.add(new CodeHistory(info.get(i).get(0), info.get(i).get(1), info.get(i).get(2), info.get(i).get(3), info.get(i).get(4)));
            }
        }

        Collections.reverse(Log);

        adapterUtil=new AdapterUtil<CodeHistory>(Log,R.layout.opc_log_item){
            @Override
            public void bindView(ViewHolder holder, CodeHistory obj) {
                holder.setText(R.id.log_item_vehicle,obj.getVehicleLic());
                holder.setText(R.id.log_item_type,obj.getOperaProject());
                holder.setText(R.id.log_item_result,obj.getOperaResult());
                holder.setText(R.id.log_item_time,obj.getOperatorTime());
                holder.setText(R.id.log_item_operator,obj.getOperatorName());
            }

        };
        list.setAdapter(adapterUtil);
        list.setSelection(PAGE_COUNT-1);

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

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.log_app:
                setChecked();
                app.setChecked(true);
                logType =1;
                if(isShowMapTag1) {
                    showInfo();
                }else {
                    list.setVisibility(View.GONE);
                    log_ly_sche.setVisibility(View.VISIBLE);
                    anima.start();
                    getCodeHistory();
                }
                break;
            case R.id.log_pc:
                setChecked();
                pc.setChecked(true);
                logType =2;
                if(isShowMapTag2) {
                    showInfo();
                }else {
                    list.setVisibility(View.GONE);
                    log_ly_sche.setVisibility(View.VISIBLE);
                    anima.start();
                    getCodeHistory_PC();
                }
                break;
        }
    }

    public void setChecked(){
        app.setChecked(false);
        pc.setChecked(false);
    }
}
