package com.zxhl.gpsking;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.entity.VehicleAlarm;
import com.zxhl.util.AdapterUtil;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/1/17.
 */

public class QuerySyBjxx extends StatusBarUtil implements View.OnClickListener,TextWatcher,RadioGroup.OnCheckedChangeListener{

    //控件
    private ListView list;
    private ImageView img;
    private TextView text;

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
    private RelativeLayout bjxx_ly_sche;
    private ImageView bjxx_img_sche;
    private AnimationDrawable anima;

    private SharedPreferenceUtils sp;
    private Context context;

    private List<List<String>> info;
    private List<List<String>> infoveh;
    private List<List<String>> infotype;
    private AdapterUtil adapterUtil;
    private ArrayList<VehicleAlarm> vehicleAlarm;
    private int positions=0;

    //类型查询
    private RadioGroup rg;
    private RadioButton rb_qb;
    private RadioButton rb_wcl;
    private RadioButton rb_ycl;


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x403:
                    list.setVisibility(View.GONE);
                    bjxx_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    break;
                case 0x404:
                    list.setVisibility(View.GONE);
                    bjxx_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    Toast.makeText(context,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x001:
                    showInfo(info);
                    list.setVisibility(View.VISIBLE);
                    bjxx_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.GONE);
                    text.setVisibility(View.GONE);
                    break;
                case 0x003:
                    adapter=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.query_bjxx);

        context=QuerySyBjxx.this;

        init();

        getVehicleLic();

        list.setVisibility(View.GONE);
        bjxx_ly_sche.setVisibility(View.VISIBLE);
        anima.start();
        getVehicleAlarmInfo();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_bjxx;
    }

    public void init(){
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);
        back=findViewById(R.id.bjxx_imgtxt_title);
        list=findViewById(R.id.bjxx_list);
        img=findViewById(R.id.bjxx_img);
        text=findViewById(R.id.bjxx_text);

        //顶部操作栏
        back=findViewById(R.id.bjxx_imgtxt_title);
        img1=findViewById(R.id.bjxx_edit_img);
        img2=findViewById(R.id.bjxx_img_img);
        title=findViewById(R.id.bjxx_txt_title);
        vehicle=findViewById(R.id.bjxx_auto_vehiclelic);
        search=findViewById(R.id.bjxx_img_serch);
        getVeh=findViewById(R.id.bjxx_btn_get);

        //类型查询
        rg=findViewById(R.id.bjxx_rg_type);
        rb_qb=findViewById(R.id.bjxx_rb_qb);
        rb_wcl=findViewById(R.id.bjxx_rb_wcl);
        rb_ycl=findViewById(R.id.bjxx_rb_ycl);
        rg.setOnCheckedChangeListener(this);

        bjxx_ly_sche=findViewById(R.id.bjxx_ly_sche);
        bjxx_img_sche=findViewById(R.id.bjxx_img_sche);
        anima= (AnimationDrawable) bjxx_img_sche.getDrawable();

        vehicleAlarm =new ArrayList<>();

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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bjxx_img_serch:
                title.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);
                img2.setVisibility(View.VISIBLE);
                vehicle.setVisibility(View.VISIBLE);
                break;
            case R.id.bjxx_btn_get:
                ShowKeyboard.hideKeyboard(vehicle);
                if(vehicle.getText().length()==0) {
                    list.setVisibility(View.GONE);
                    bjxx_ly_sche.setVisibility(View.VISIBLE);
                    anima.start();
                    getVehicleAlarmInfo();
                }else {
                    getAlarmInfoByVeh();
                }
                title.setVisibility(View.VISIBLE);
                search.setVisibility(View.VISIBLE);
                img1.setVisibility(View.GONE);
                img2.setVisibility(View.GONE);
                vehicle.setVisibility(View.GONE);
                break;
        }

    }

    //获取报警信息
    private void getVehicleAlarmInfo(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetAlarm", proper, new WebServiceUtils.WebServiceCallBack() {
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
            for(int j=0;j<soapObject.getPropertyCount();j++){
                if(soapObject.getProperty(j).toString().equals("anyType{}")){
                    list.add("");
                }
                else {
                    list.add(soapObject.getProperty(j).toString());
                }
            }
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

    //机号查询报警信息
    private void getAlarmInfoByVeh(){
        infoveh=new ArrayList<>();
        ArrayList<String> list1=new ArrayList<>();
        if(vehicle.getText().length()==0){
            infoveh=info;
        }else {
            for (int i = 0; i < info.size(); i++) {
                if (info.get(i).get(4).equalsIgnoreCase(vehicle.getText().toString())) {
                    list1 = (ArrayList<String>) info.get(i);
                    infoveh.add(list1);
                }
            }
        }
        showInfo(infoveh);
    }

    //类型查询报警信息
    private void getAlarmInfoByType(String type){
        infotype=new ArrayList<>();
        ArrayList<String> list1=new ArrayList<>();
        if(type.equals("0")){
            infotype=info;
        }else {
            for (int i = 0; i < info.size(); i++) {
                if (info.get(i).get(8).equals(type)) {
                    list1 = (ArrayList<String>) info.get(i);
                    infotype.add(list1);
                }
            }
        }
        showInfo(infotype);
    }

    public void showInfo(List<List<String>> info){
        vehicleAlarm = new ArrayList<>();

        for(int i=0;i<info.size();i++) {
            if(i==info.size()){
                break;
            }else {
                vehicleAlarm.add(new VehicleAlarm(info.get(i).get(0), info.get(i).get(1), info.get(i).get(2),
                        info.get(i).get(3), info.get(i).get(4), info.get(i).get(5),
                        info.get(i).get(6), info.get(i).get(7), info.get(i).get(8),
                        info.get(i).get(9), info.get(i).get(10), info.get(i).get(11)));
            }
        }

        adapterUtil=new AdapterUtil<VehicleAlarm>(vehicleAlarm,R.layout.query_bjxx_item){
            @Override
            public void bindView(ViewHolder holder, VehicleAlarm obj) {
                holder.setText(R.id.bjxx_item_time,obj.getGPSDateTime());
                holder.setText(R.id.bjxx_item_jh,obj.getVehicleLic());
                if(obj.getDealType().equals("1")){
                    holder.setText(R.id.bjxx_item_sfcl,"未处理");
                }else if(obj.getDealType().equals("2")) {
                    holder.setText(R.id.bjxx_item_sfcl,"正在处理");
                }else if(obj.getDealType().equals("3")){
                    holder.setText(R.id.bjxx_item_sfcl,"恢复正常");
                }
            }
        };
        list.setAdapter(adapterUtil);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                positions=position;
                Intent intent=new Intent(QuerySyBjxx.this,SendAlarmInfo.class);
                Bundle bd=new Bundle();
                ArrayList<VehicleAlarm> list=new ArrayList();
                list.add(vehicleAlarm.get(position));
                bd.putSerializable("info", (Serializable) list);
                intent.putExtras(bd);
                startActivityForResult(intent,0x001);
            }
        });

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0x001 && resultCode==0x001 && data.getExtras().getString("state").equals("2")){
            ArrayList<String> list1= (ArrayList<String>) info.get(positions);
            list1.set(7,sp.getOperatorName());
            list1.set(8,data.getExtras().getString("state").toString());
            info.set(positions,list1);
            showInfo(info);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.bjxx_rb_qb:
                setChecked();
                rb_qb.setChecked(true);
                getAlarmInfoByType("0");
                break;
            case R.id.bjxx_rb_wcl:
                setChecked();
                rb_wcl.setChecked(true);
                getAlarmInfoByType("1");
                break;
            case R.id.bjxx_rb_ycl:
                setChecked();
                rb_ycl.setChecked(true);
                getAlarmInfoByType("2");
                break;
        }
    }

    public void setChecked(){
        rb_qb.setChecked(false);
        rb_wcl.setChecked(false);
        rb_ycl.setChecked(false);
    }
}
