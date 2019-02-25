package com.zxhl.gpskingforxc;

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
import android.widget.Toast;

import com.autonavi.amap.mapcore.Convert;
import com.zxhl.entity.Logs;
import com.zxhl.util.AdapterUtil;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/1/29.
 */

public class OpcElectronicFence extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private ImgTxtLayout back;
    private AutoCompleteTextView vehicle;
    private TextView veh_info;
    private RelativeLayout visble;
    private LinearLayout jqy;
    private LinearLayout cqy;
    private LinearLayout jc;
    private ImageView opc_cancel;

    private ListView list;
    private LinearLayout scroll_visble;
    private ArrayList<Logs> data;
    private AdapterUtil<Logs> adapter_log;

    private Button electronicfence_js;
    private Button electronicfence_jk;
    private Button electronicfence_sc;
    private Button electronicfence_ssdw;
    private Button electronicfence_yjgl;
    private Button electronicfence_log;

    private Button electronicfence_location;
    private EditText electronicfence_km;
    private String Lon="0.0000";
    private String Lat="0.0000";

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

    private RelativeLayout electronicfence_ly_sche;
    private ImageView electronicfence_img_sche;
    private AnimationDrawable anima;

    private SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x404:
                    electronicfence_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    data.add(new Logs("【"+sf.format(new Date())+"】："+"服务器有点问题，我们正在全力修复！"));
                    adapter_log.notifyDataSetChanged();
                    break;
                case 0x001:
                    adapter=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
                case 0x002:
                    electronicfence_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    visble.setVisibility(View.VISIBLE);
                    scroll_visble.setVisibility(View.VISIBLE);
                    veh_info.setText("【"+vehicle.getText()+"】 车台在线，可执行以下操作：");
                    break;
                case 0x003:
                    electronicfence_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    visble.setVisibility(View.GONE);
                    veh_info.setText("没有找到 【"+vehicle.getText()+"】 车台的在线信息，可能原因：\n1、该车台不在线。\n2、您输入的车牌号有误。\n3、您没有权限操作该车台。");
                    break;
                case 0x004:
                    electronicfence_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    if(remoteLock.equals("1")) {
                        data.add(new Logs("【"+sf.format(new Date())+"】："+"【"+vehicle.getText()+"】下发【" + command + "】指令成功！"));
                        adapter_log.notifyDataSetChanged();
                    }else{
                        data.add(new Logs("【"+sf.format(new Date())+"】："+"【"+vehicle.getText()+"】下发【" + command + "】指令失败！失败原因："+error));
                        adapter_log.notifyDataSetChanged();
                    }
                    break;
                case 0x005:
                    electronicfence_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    data.add(new Logs("【"+sf.format(new Date())+"】："+"【"+vehicle.getText()+"】下发【" + command + "】指令失败！请稍后再试。"));
                    adapter_log.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=OpcElectronicFence.this;

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
        return R.layout.opc_electronicfence;
    }

    public void init(){
        back=findViewById(R.id.electronicfence_back);
        vehicle=findViewById(R.id.electronicfence_vehicle);
        veh_info =findViewById(R.id.electronicfence_txt_veh);
        visble=findViewById(R.id.electronicfence_rl_opc);
        jqy =findViewById(R.id.electronicfence_ly_jqy);
        cqy =findViewById(R.id.electronicfence_ly_cqy);
        jc =findViewById(R.id.electronicfence_ly_jc);
        electronicfence_js=findViewById(R.id.electronicfence_js);
        electronicfence_jk=findViewById(R.id.electronicfence_jk);
        electronicfence_sc =findViewById(R.id.electronicfence_sc);
        electronicfence_ssdw=findViewById(R.id.electronicfence_ssdw);
        electronicfence_yjgl=findViewById(R.id.electronicfence_yjgl);
        electronicfence_log=findViewById(R.id.electronicfence_log);
        electronicfence_location=findViewById(R.id.electronicfence_location);
        electronicfence_km=findViewById(R.id.electronicfence_km);
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

        electronicfence_ly_sche =findViewById(R.id.electronicfence_ly_sche);
        electronicfence_img_sche =findViewById(R.id.electronicfence_img_sche);
        anima= (AnimationDrawable) electronicfence_img_sche.getDrawable();

        autoVehLic=new ArrayList<>();

        vehicle.addTextChangedListener(this);
        opc_cancel.setOnClickListener(this);

        jqy.setOnClickListener(this);
        cqy.setOnClickListener(this);
        jc.setOnClickListener(this);

        electronicfence_js.setOnClickListener(this);
        electronicfence_jk.setOnClickListener(this);
        electronicfence_sc.setOnClickListener(this);
        electronicfence_ssdw.setOnClickListener(this);
        electronicfence_yjgl.setOnClickListener(this);
        electronicfence_log.setOnClickListener(this);
        electronicfence_location.setOnClickListener(this);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        vehicle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                electronicfence_ly_sche.setVisibility(View.VISIBLE);
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
            case R.id.electronicfence_ly_jqy:
                //暂时屏蔽
                ShowKeyboard.hideKeyboard(electronicfence_km);
                if(electronicfence_km.getText().toString().length()>0) {
                    if (Double.valueOf(electronicfence_km.getText().toString()) >= 1.0) {
                        dialog.show();
                        text.setText("确认下发 【进区域电子围栏】 指令？");
                        type = "1";
                        command = "进区域电子围栏";
                    } else {
                        Toast.makeText(context, "范围不能小于1KM", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(context, "范围不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.electronicfence_ly_cqy:
                ShowKeyboard.hideKeyboard(electronicfence_km);
                if(electronicfence_km.getText().toString().length()>0) {
                    if (Double.valueOf(electronicfence_km.getText().toString()) >= 1.0) {
                        dialog.show();
                        text.setText("确认下发 【出区域电子围栏】 指令？");
                        type = "2";
                        command = "出区域电子围栏";
                    } else {
                        Toast.makeText(context, "范围不能小于1KM", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(context, "范围不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.electronicfence_ly_jc:
                ShowKeyboard.hideKeyboard(electronicfence_km);
                dialog.show();
                text.setText("确认下发 【解除电子围栏】 指令？");
                type = "0";
                command = "解除电子围栏";
                break;
            case R.id.electronicfence_location:
                Intent intent=new Intent(context,OpcEleFLocation.class);
                intent.putExtra("VehicleLic",vehicle.getText().toString());
                intent.putExtra("IsOnline",pression);
                startActivityForResult(intent,0x001);
                break;
            case R.id.ad_btn_remotelock_cancel:
                dialog.dismiss();
                break;
            case R.id.ad_btn_remotelock_confirm:
                RemoteControl(type);
                electronicfence_ly_sche.setVisibility(View.VISIBLE);
                anima.start();
                dialog.dismiss();
                break;
            case R.id.opc_cancel:
                vehicle.setText("");
                opc_cancel.setVisibility(View.GONE);
                break;
            //服务直达
            case R.id.electronicfence_js:
                //解锁
                Intent it1=new Intent(context,OpcUnLock.class);
                it1.putExtra("VehicleLic",vehicle.getText().toString());
                it1.putExtra("IsOnline",pression);
                startActivity(it1);
                finish();
                break;
            case R.id.electronicfence_jk:
                //监控
                Intent it3=new Intent(context,OpcMonitor.class);
                it3.putExtra("VehicleLic",vehicle.getText().toString());
                it3.putExtra("IsOnline",pression);
                startActivity(it3);
                finish();
                break;
            case R.id.electronicfence_sc:
                //锁车
                Intent it2=new Intent(context,OpcLock.class);
                it2.putExtra("VehicleLic",vehicle.getText().toString());
                it2.putExtra("IsOnline",pression);
                startActivity(it2);
                finish();
                break;
            case R.id.electronicfence_ssdw:
                //实时定位
                Intent it4=new Intent(context,OpcLocation.class);
                it4.putExtra("VehicleLic",vehicle.getText().toString());
                it4.putExtra("IsOnline",pression);
                startActivity(it4);
                break;
            case R.id.electronicfence_yjgl:
                //样机管理
                Intent it5=new Intent(context,OpcLockTime.class);
                it5.putExtra("VehicleLic",vehicle.getText().toString());
                it5.putExtra("IsOnline",pression);
                startActivity(it5);
                finish();
                break;
            case R.id.electronicfence_log:
                //指令下发记录
                Intent it6=new Intent(context,OpcLog.class);
                it6.putExtra("VehicleLic",vehicle.getText().toString());
                it6.putExtra("IsOnline",pression);
                startActivity(it6);
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==0x001 && resultCode==0x002){
            Lon=data.getDoubleExtra("Lon",0)+"";
            Lat=data.getDoubleExtra("Lat",0)+"";
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

    //下发电子围栏指令
    public void RemoteControl(String type){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("VehicleLic",vehicle.getText().toString());
        proper.put("LockLevel",type);
        proper.put("Lon",Lon);
        proper.put("Lat",Lat);
        proper.put("Km",electronicfence_km.getText().toString());

        WebServiceUtils.callWebService(WebServiceUtils.OperaCenter_URL2, "ElectronicFence", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<String> list=new ArrayList<String>();
                    SoapObject soapObject= (SoapObject) result.getProperty(0);
                    if(soapObject!=null){
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
