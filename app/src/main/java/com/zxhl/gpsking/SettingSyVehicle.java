package com.zxhl.gpsking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

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
 * Created by Administrator on 2018/1/4.
 */

public class SettingSyVehicle extends StatusBarUtil implements TextWatcher{

    private ImgTxtLayout back;
    private AutoCompleteTextView vehicle;
    private AutoCompleteTextView count;
    private Button finish;
    private Context context;

    private SharedPreferenceUtils sp;

    private ArrayAdapter<String> adapter_v;
    private ArrayAdapter<String> adapter_c;

    private List<String> vehicle_list;
    private String[] count_str;

    private boolean state=false;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    adapter_v=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,vehicle_list);
                    vehicle.setAdapter(adapter_v);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.setting_vehicle);

        init();
        getVehicleLic();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setting_vehicle;
    }

    private void init() {
        context=SettingSyVehicle.this;
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);

        back= (ImgTxtLayout) findViewById(R.id.settingve_imgtxt_vehicle);
        vehicle= (AutoCompleteTextView) findViewById(R.id.settingve_auto_vehicle);
        count= (AutoCompleteTextView) findViewById(R.id.settingve_auto_count);
        finish= (Button) findViewById(R.id.settingve_btn_update);
        vehicle_list=new ArrayList<>();
        count_str=new String[]{"7","15","30","45"};

        adapter_c=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,count_str);

        count.setAdapter(adapter_c);

        vehicle.setText(sp.getVehicleLic());
        count.setText(sp.getDayCount());

        vehicle.addTextChangedListener(this);
        count.addTextChangedListener(this);

        vehicle.setSelection(vehicle.getText().length());
        count.setSelection(count.getText().length());

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowKeyboard.hideKeyboard(vehicle);
                int permiss=0;
                for(int i=0;i<vehicle_list.size();i++)
                {
                    if(vehicle.getText().toString().equalsIgnoreCase(vehicle_list.get(i))){
                        permiss=1;
                        break;
                    }
                }
                if(permiss==1){
                    sp.setVehicleLic(vehicle.getText().toString());
                    sp.setDayCount(count.getText().toString());
                    Toast.makeText(context,finish.getText().toString()+"成功",Toast.LENGTH_SHORT).show();
                    sendBroadcast(new Intent("com.zxhl.gpsking.MYBROADCASTHOMESY"));
                    finish();
                }
                else{
                    Toast.makeText(SettingSyVehicle.this,"机号输入有误或者您没有该车台的权限",Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                        vehicle_list=list;
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count1, int after) {
        finish.setEnabled(false);
        if(vehicle.getText().length()>0 || count.getText().length()>0){
            state=true;
        }

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(vehicle.getText().length()==0 && count.getText().length()==0){
            if(state) {
                finish.setText("解绑");
                finish.setEnabled(true);
            }
            else {
                finish.setEnabled(false);
            }
        }
        else {
            finish.setText("绑定");
            finish.setEnabled(true);
        }

    }
}
