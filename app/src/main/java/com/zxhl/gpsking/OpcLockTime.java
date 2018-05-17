package com.zxhl.gpsking;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zxhl.entity.Logs;
import com.zxhl.util.AdapterUtil;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/1/29.
 */

public class OpcLockTime extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private ImgTxtLayout back;
    private AutoCompleteTextView vehicle;
    private TextView veh_info;
    private RelativeLayout visble;
    private LinearLayout yjsc;
    private LinearLayout tcyj;
    private EditText time;
    private ImageView opc_cancel;

    private Button locktime_js;
    private Button locktime_jk;
    private Button locktime_ydkz;
    private Button locktime_ssdw;
    private Button locktime_sc;
    private Button locktime_log;

    private ListView list;
    private LinearLayout scroll_visble;
    private ArrayList<Logs> data;
    private AdapterUtil<Logs> adapter_log;

    private SharedPreferenceUtils sp;
    private Context context;

    private List<String> autoVehLic;
    private String remoteLock;
    private String error;
    private ArrayAdapter<String> adapter;
    private int isOnline =0;
    private String type;
    private String command;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private View view;
    private TextView text;
    private int pression=0;
    private int length=0;

    private RelativeLayout locktime_ly_sche;
    private ImageView locktime_img_sche;
    private AnimationDrawable anima;


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x404:
                    locktime_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    //scroll_visble.setVisibility(View.VISIBLE);
                    if(type.equals("1")&&time.getText().length()==0) {
                        data.add(new Logs("延迟锁车时间为空，请输入时间后重试！"));
                    }else {
                        data.add(new Logs("服务器有点问题，我们正在全力修复！"));
                    }
                    adapter_log.notifyDataSetChanged();
                    break;
                case 0x001:
                    adapter=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
                case 0x002:
                    locktime_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    visble.setVisibility(View.VISIBLE);
                    scroll_visble.setVisibility(View.VISIBLE);
                    veh_info.setText("【"+vehicle.getText()+"】 车台在线，可执行以下操作：");
                    break;
                case 0x003:
                    locktime_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    visble.setVisibility(View.GONE);
                    veh_info.setText("没有找到 【"+vehicle.getText()+"】 车台的在线信息，可能原因：\n1、该车台不在线。\n2、您输入的车牌号有误。\n3、您没有权限操作该车台。");
                    break;
                case 0x004:
                    locktime_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    if(remoteLock.equals("1")) {
                        if(type.equals("0")) {
                            data.add(new Logs("【" + vehicle.getText() + "】：下发 【" + command + "】指令成功！"));
                        }else {
                            data.add(new Logs("【" + vehicle.getText() + "】：下发 【" + command + "】 延迟 【" + time.getText() + "】 分钟指令成功！"));
                        }
                        adapter_log.notifyDataSetChanged();
                    }else{
                        if(type.equals("0")) {
                            data.add(new Logs("【" + vehicle.getText() + "】：下发 【" + command + "】指令失败！失败原因：" + error));
                        }else {
                            data.add(new Logs("【" + vehicle.getText() + "】：下发 【" + command + "】 延迟 【" + time.getText() + "】 分钟指令失败！失败原因：" + error));
                        }
                        adapter_log.notifyDataSetChanged();
                    }
                    break;
                case 0x005:
                    locktime_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    if(type.equals("0")) {
                        data.add(new Logs("【" + vehicle.getText() + "】：下发 【" + command + "】指令失败！请稍后再试。"));
                    }else {
                        data.add(new Logs("【" + vehicle.getText() + "】：下发 【" + command + "】 延迟 【" + time.getText() + "】 分钟 指令失败！请稍后再试。"));
                    }
                    adapter_log.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=OpcLockTime.this;

        init();
        getVehicleLic();
        view=getAlert(R.layout.ad_remotelock);
        if(getIntent().getStringExtra("VehicleLic")!=null) {
            if (getIntent().getStringExtra("VehicleLic").length() != 0) {
                length=1;
                vehicle.setText(getIntent().getStringExtra("VehicleLic"));
                vehicle.setSelection(vehicle.getText().length());
                if (getIntent().getIntExtra("IsOnline", 0) == 1) {
                    pression = 1;
                    handler.sendEmptyMessage(0x002);
                } else {
                    handler.sendEmptyMessage(0x003);
                }
            }
        }

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.opc_locktime;
    }

    public void init(){
        back=findViewById(R.id.locktime_back);
        vehicle=findViewById(R.id.locktime_vehicle);
        veh_info =findViewById(R.id.locktime_txt_veh);
        visble=findViewById(R.id.locktime_rl_opc);
        yjsc =findViewById(R.id.locktime_ly_yjsc);
        tcyj =findViewById(R.id.locktime_ly_tcyj);
        time=findViewById(R.id.locktime_time);
        locktime_sc=findViewById(R.id.locktime_sc);
        locktime_js=findViewById(R.id.locktime_js);
        locktime_jk=findViewById(R.id.locktime_jk);
        locktime_ydkz=findViewById(R.id.locktime_ydkz);
        locktime_ssdw=findViewById(R.id.locktime_ssdw);
        locktime_log=findViewById(R.id.locktime_log);
        opc_cancel=findViewById(R.id.opc_cancel);
        opc_cancel.setVisibility(View.GONE);

        //日志
        list=findViewById(R.id.opc_list);
        scroll_visble=findViewById(R.id.scroll);
        data=new ArrayList<Logs>();

        adapter_log=new AdapterUtil<Logs>(data,R.layout.opc_list_item) {
            @Override
            public void bindView(ViewHolder holder, Logs obj) {
                holder.setText(R.id.list_log,obj.getData());
            }
        };
        list.setAdapter(adapter_log);
        ShowKeyboard.hideKeyboard(vehicle);

        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);

        locktime_ly_sche =findViewById(R.id.locktime_ly_sche);
        locktime_img_sche =findViewById(R.id.locktime_img_sche);
        anima= (AnimationDrawable) locktime_img_sche.getDrawable();

        autoVehLic=new ArrayList<>();

        vehicle.addTextChangedListener(this);
        opc_cancel.setOnClickListener(this);

        yjsc.setOnClickListener(this);
        tcyj.setOnClickListener(this);

        locktime_sc.setOnClickListener(this);
        locktime_js.setOnClickListener(this);
        locktime_jk.setOnClickListener(this);
        locktime_ydkz.setOnClickListener(this);
        locktime_ssdw.setOnClickListener(this);
        locktime_log.setOnClickListener(this);


        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        vehicle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                locktime_ly_sche.setVisibility(View.VISIBLE);
                anima.start();
                scroll_visble.setVisibility(View.GONE);
                getVheicleIsOnline();
                ShowKeyboard.hideKeyboard(vehicle);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.locktime_ly_yjsc:
                ShowKeyboard.hideKeyboard(time);
                dialog.show();
                text.setText("确认下发 【样机锁车】 延迟 【"+time.getText()+"】 分钟 指令？");
                type="1";
                command="样机锁车";
                break;
            case R.id.locktime_ly_tcyj:
                ShowKeyboard.hideKeyboard(time);
                dialog.show();
                text.setText("确认下发 【退出样机】指令？");
                type="0";
                command="退出样机";
                break;
            case R.id.ad_btn_remotelock_cancel:
                dialog.dismiss();
                break;
            case R.id.ad_btn_remotelock_confirm:
                RemoteLock(type);
                locktime_ly_sche.setVisibility(View.VISIBLE);
                anima.start();
                //scroll_visble.setVisibility(View.GONE);
                dialog.dismiss();
                break;
            case R.id.opc_cancel:
                vehicle.setText("");
                opc_cancel.setVisibility(View.GONE);
                break;
            //服务直达
            case R.id.locktime_sc:
                //锁车
                Intent it5=new Intent(context,OpcLock.class);
                it5.putExtra("VehicleLic",vehicle.getText().toString());
                it5.putExtra("IsOnline",pression);
                startActivity(it5);
                finish();
                break;
            case R.id.locktime_js:
                //解锁
                Intent it1=new Intent(context,OpcUnLock.class);
                it1.putExtra("VehicleLic",vehicle.getText().toString());
                it1.putExtra("IsOnline",pression);
                startActivity(it1);
                finish();
                break;
            case R.id.locktime_jk:
                //监控
                Intent it2=new Intent(context,OpcMonitor.class);
                it2.putExtra("VehicleLic",vehicle.getText().toString());
                it2.putExtra("IsOnline",pression);
                startActivity(it2);
                finish();
                break;
            case R.id.locktime_ydkz:
                //油电控制
                Intent it3=new Intent(context,OpcOilEleControl.class);
                it3.putExtra("VehicleLic",vehicle.getText().toString());
                it3.putExtra("IsOnline",pression);
                startActivity(it3);
                finish();
                break;
            case R.id.locktime_ssdw:
                //实时定位
                Intent it4=new Intent(context,OpcLocation.class);
                it4.putExtra("VehicleLic",vehicle.getText().toString());
                it4.putExtra("IsOnline",pression);
                startActivity(it4);
                break;
            case R.id.locktime_log:
                //指令下发记录
                Intent it6=new Intent(context,OpcLog.class);
                it6.putExtra("VehicleLic",vehicle.getText().toString());
                it6.putExtra("IsOnline",pression);
                startActivity(it6);
                break;
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

    //获取车辆是否在线
    public void getVheicleIsOnline(){
        HashMap<String,String> proper=new HashMap<String,String>();
        proper.put("VehicleLic",vehicle.getText().toString());
        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetVehicleIsOnline", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null) {
                    List<String> list = new ArrayList<String>();
                    Integer it=new Integer(result.getProperty(0).toString());
                    isOnline =it.intValue();
                    if(isOnline ==1) {
                        for(int i=0;i<autoVehLic.size();i++)
                        {
                            if(vehicle.getText().toString().equalsIgnoreCase(autoVehLic.get(i))){
                                pression=1;
                                break;
                            }
                        }
                        if(pression==1) {
                            handler.sendEmptyMessage(0x002);
                        }else {
                            handler.sendEmptyMessage(0x003);
                        }
                    }
                    else {
                        handler.sendEmptyMessage(0x003);
                    }
                }
                else
                {
                    handler.sendEmptyMessage(0x404);
                }
            }
        });
    }

    //下发锁车指令
    public void RemoteLock(String type){
        HashMap<String,String> proper=new HashMap<>();
        if(type.equals("0")){
            proper.put("Time", "0");
        }
        else {
            proper.put("Time", time.getText().toString());
        }
        proper.put("OperatorID", sp.getOperatorID());
        proper.put("VehicleLic", vehicle.getText().toString());
        proper.put("LockLevel", type);

        WebServiceUtils.callWebService(WebServiceUtils.OperaCenter_URL, "SampleLockByVehicleLic", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<String> list=new ArrayList<String>();
                    SoapObject soapObject= (SoapObject) result.getProperty(0);
                    if(soapObject!=null){
                        //油电控制：controlState
                        remoteLock=soapObject.getProperty("LockResult").toString();
                        error=soapObject.getProperty("error").toString();
                        handler.sendEmptyMessage(0x004);
                    }
                    else
                    {
                        handler.sendEmptyMessage(0x005);
                    }
                }
                else{
                    handler.sendEmptyMessage(0x404);
                }
            }
        });

    }

    //定义弹窗方法
    public View getAlert(int mLayout){
        View ad_view;
        //初始化Builder
        builder=new AlertDialog.Builder(context);
        //完成相关设置
        inflater=getLayoutInflater();
        ad_view=inflater.inflate(mLayout,null,false);
        builder.setView(ad_view);
        builder.setCancelable(true);
        dialog=builder.create();
        ad_view.findViewById(R.id.ad_btn_remotelock_cancel).setOnClickListener(this);
        ad_view.findViewById(R.id.ad_btn_remotelock_confirm).setOnClickListener(this);
        text=ad_view.findViewById(R.id.ad_txt_remotelock2);
        return ad_view;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if(length==0) {
            veh_info.setText("功能可搜索车台，查看可执行的操作。试一试吧！");
            visble.setVisibility(View.GONE);
            scroll_visble.setVisibility(View.GONE);
            opc_cancel.setVisibility(View.GONE);
            pression=0;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        length=0;
        if(vehicle.getText().length()>0){
            opc_cancel.setVisibility(View.VISIBLE);
        }
        else
        {
            opc_cancel.setVisibility(View.GONE);
        }
    }
}
