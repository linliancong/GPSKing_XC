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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class OpcOilEleControl extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private ImgTxtLayout back;
    private AutoCompleteTextView vehicle;
    private TextView veh_info;
    private RelativeLayout visble;
    private LinearLayout yjdyd;
    private LinearLayout ejdyd;
    private LinearLayout yjhfyd;
    private LinearLayout ejhfyd;
    private ImageView opc_cancel;

    private ListView list;
    private LinearLayout scroll_visble;
    private ArrayList<Logs> data;
    private AdapterUtil<Logs> adapter_log;

    private Button oilelecontrol_js;
    private Button oilelecontrol_jk;
    private Button oilelecontrol_sc;
    private Button oilelecontrol_ssdw;
    private Button oilelecontrol_yjgl;
    private Button oilelecontrol_log;

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

    private RelativeLayout oilelecontrol_ly_sche;
    private ImageView oilelecontrol_img_sche;
    private AnimationDrawable anima;


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x404:
                    oilelecontrol_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    data.add(new Logs("服务器有点问题，我们正在全力修复！"));
                    adapter_log.notifyDataSetChanged();
                    break;
                case 0x001:
                    adapter=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
                case 0x002:
                    oilelecontrol_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    visble.setVisibility(View.VISIBLE);
                    scroll_visble.setVisibility(View.VISIBLE);
                    veh_info.setText("【"+vehicle.getText()+"】 车台在线，可执行以下操作：");
                    break;
                case 0x003:
                    oilelecontrol_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    visble.setVisibility(View.GONE);
                    veh_info.setText("没有找到 【"+vehicle.getText()+"】 车台的在线信息，可能原因：\n1、该车台不在线。\n2、您输入的车牌号有误。\n3、您没有权限操作该车台。");
                    break;
                case 0x004:
                    oilelecontrol_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    if(remoteLock.equals("1")||remoteLock.equals("2")) {
                        data.add(new Logs("【"+vehicle.getText()+"】：下发 【" + command + "】指令成功！"));
                        adapter_log.notifyDataSetChanged();
                    }else{
                        data.add(new Logs("【"+vehicle.getText()+"】：下发 【" + command + "】指令失败！失败原因："+error));
                        adapter_log.notifyDataSetChanged();
                    }
                    break;
                case 0x005:
                    oilelecontrol_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    data.add(new Logs("【"+vehicle.getText()+"】：下发 【" + command + "】指令失败！请稍后再试。"));
                    adapter_log.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=OpcOilEleControl.this;

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
        return R.layout.opc_oilelecontrol;
    }

    public void init(){
        back=findViewById(R.id.oilelecontrol_back);
        vehicle=findViewById(R.id.oilelecontrol_vehicle);
        veh_info =findViewById(R.id.oilelecontrol_txt_veh);
        visble=findViewById(R.id.oilelecontrol_rl_opc);
        yjdyd =findViewById(R.id.oilelecontrol_ly_yjdyd);
        ejdyd =findViewById(R.id.oilelecontrol_ly_ejdyd);
        yjhfyd =findViewById(R.id.oilelecontrol_ly_yjhfyd);
        ejhfyd =findViewById(R.id.oilelecontrol_ly_ejhfyd);
        oilelecontrol_js=findViewById(R.id.oilelecontrol_js);
        oilelecontrol_jk=findViewById(R.id.oilelecontrol_jk);
        oilelecontrol_sc =findViewById(R.id.oilelecontrol_sc);
        oilelecontrol_ssdw=findViewById(R.id.oilelecontrol_ssdw);
        oilelecontrol_yjgl=findViewById(R.id.oilelecontrol_yjgl);
        oilelecontrol_log=findViewById(R.id.oilelecontrol_log);
        opc_cancel=findViewById(R.id.opc_cancel);
        opc_cancel.setVisibility(View.GONE);
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);

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

        oilelecontrol_ly_sche =findViewById(R.id.oilelecontrol_ly_sche);
        oilelecontrol_img_sche =findViewById(R.id.oilelecontrol_img_sche);
        anima= (AnimationDrawable) oilelecontrol_img_sche.getDrawable();

        autoVehLic=new ArrayList<>();

        vehicle.addTextChangedListener(this);
        opc_cancel.setOnClickListener(this);

        yjdyd.setOnClickListener(this);
        ejdyd.setOnClickListener(this);
        yjhfyd.setOnClickListener(this);
        ejhfyd.setOnClickListener(this);

        oilelecontrol_js.setOnClickListener(this);
        oilelecontrol_jk.setOnClickListener(this);
        oilelecontrol_sc.setOnClickListener(this);
        oilelecontrol_ssdw.setOnClickListener(this);
        oilelecontrol_yjgl.setOnClickListener(this);
        oilelecontrol_log.setOnClickListener(this);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        vehicle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                oilelecontrol_ly_sche.setVisibility(View.VISIBLE);
                scroll_visble.setVisibility(View.GONE);
                anima.start();
                getVheicleIsOnline();
                ShowKeyboard.hideKeyboard(vehicle);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.oilelecontrol_ly_yjdyd:
                dialog.show();
                text.setText("确认下发 【一级断油断电】 指令？");
                type="1";
                command="一级断油断电";
                break;
            case R.id.oilelecontrol_ly_ejdyd:
               dialog.show();
                text.setText("确认下发 【二级断油断电】 指令？");
                type="3";
                command="二级断油断电";
                break;
            case R.id.oilelecontrol_ly_yjhfyd:
                dialog.show();
                text.setText("确认下发 【一级恢复油电】 指令？");
                type="2";
                command="一级恢复油电";
                break;
            case R.id.oilelecontrol_ly_ejhfyd:
                dialog.show();
                text.setText("确认下发 【二级恢复油电】 指令？");
                type="4";
                command="二级恢复油电";
                break;
            case R.id.ad_btn_remotelock_cancel:
                dialog.dismiss();
                break;
            case R.id.ad_btn_remotelock_confirm:
                RemoteControl(type);
                oilelecontrol_ly_sche.setVisibility(View.VISIBLE);
                anima.start();
                dialog.dismiss();
                break;
            case R.id.opc_cancel:
                vehicle.setText("");
                opc_cancel.setVisibility(View.GONE);
                break;
            //服务直达
            case R.id.oilelecontrol_js:
                //解锁
                Intent it1=new Intent(context,OpcUnLock.class);
                it1.putExtra("VehicleLic",vehicle.getText().toString());
                it1.putExtra("IsOnline",pression);
                startActivity(it1);
                finish();
                break;
            case R.id.oilelecontrol_jk:
                //监控
                Intent it3=new Intent(context,OpcMonitor.class);
                it3.putExtra("VehicleLic",vehicle.getText().toString());
                it3.putExtra("IsOnline",pression);
                startActivity(it3);
                finish();
                break;
            case R.id.oilelecontrol_sc:
                //锁车
                Intent it2=new Intent(context,OpcLock.class);
                it2.putExtra("VehicleLic",vehicle.getText().toString());
                it2.putExtra("IsOnline",pression);
                startActivity(it2);
                finish();
                break;
            case R.id.oilelecontrol_ssdw:
                //实时定位
                Intent it4=new Intent(context,OpcLocation.class);
                it4.putExtra("VehicleLic",vehicle.getText().toString());
                it4.putExtra("IsOnline",pression);
                startActivity(it4);
                break;
            case R.id.oilelecontrol_yjgl:
                //样机管理
                Intent it5=new Intent(context,OpcLockTime.class);
                it5.putExtra("VehicleLic",vehicle.getText().toString());
                it5.putExtra("IsOnline",pression);
                startActivity(it5);
                finish();
                break;
            case R.id.oilelecontrol_log:
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
    public void RemoteControl(String type){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("VehicleLic",vehicle.getText().toString());
        proper.put("OptType",type);

        WebServiceUtils.callWebService(WebServiceUtils.OperaCenter_URL, "RemoteControlByVehicleLic", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<String> list=new ArrayList<String>();
                    SoapObject soapObject= (SoapObject) result.getProperty(0);
                    if(soapObject!=null){
                        //解锁车、监控：LockResult
                        remoteLock=soapObject.getProperty("controlState").toString();
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
