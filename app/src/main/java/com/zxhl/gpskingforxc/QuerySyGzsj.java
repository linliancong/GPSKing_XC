package com.zxhl.gpskingforxc;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;
import com.zxhl.entity.WorkTimeInfo;
import com.zxhl.util.AdapterUtil;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Administrator on 2018/6/21.
 */

public class QuerySyGzsj extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private ImgTxtLayout back;
    private ListView list;
    private AutoCompleteTextView vehicle;
    private EditText time;
    private Button query;

    //自动完成框的相关变量
    private List<String> autoVehLic;
    private ArrayAdapter<String> adapter;

    private SharedPreferenceUtils sp;
    private Context context;

    //查询相关
    private RelativeLayout ly_sche;
    private ImageView img_sche;
    private AnimationDrawable anima;

    private String DateTime;
    private Calendar selectedDate;

    private WorkTimeInfo workTimeInfo;
    private ArrayList<WorkTimeInfo> workTimeInfos=new ArrayList<>();
    private AdapterUtil<WorkTimeInfo> adapters;

    private float value=0;
    private float values=0;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x403:
                    anima.stop();
                    ly_sche.setVisibility(View.GONE);
                    list.setVisibility(View.GONE);
                    Toast.makeText(context,"没有查询到数据，请稍后重试",Toast.LENGTH_SHORT).show();
                    break;
                case 0x404:
                    anima.stop();
                    ly_sche.setVisibility(View.GONE);
                    list.setVisibility(View.GONE);
                    Toast.makeText(context,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x001:
                    adapter=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
                case 0x002:
                    list.setVisibility(View.VISIBLE);
                    anima.stop();
                    ly_sche.setVisibility(View.GONE);
                    adapters=new AdapterUtil<WorkTimeInfo>(workTimeInfos,R.layout.query_gzsj_item) {
                        @Override
                        public void bindView(ViewHolder holder, WorkTimeInfo obj) {
                            holder.setText(R.id.beginTime,obj.getBeginTime());
                            holder.setText(R.id.endTime,obj.getEndTime());
                            holder.setText(R.id.workTime,obj.getWorkTime());
                            if(obj.getBeginTime().equals("总时间")){
                                holder.setTextColor(R.id.beginTime,"#000000");
                            }
                        }
                    };
                    list.setAdapter(adapters);

                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        getVehicleLic();

    }

    private void init() {
        context=QuerySyGzsj.this;
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);

        back=findViewById(R.id.gzsj_back);
        list=findViewById(R.id.gzsj_list);
        vehicle=findViewById(R.id.gzsj_vehicle);
        time=findViewById(R.id.gzsj_time);
        query=findViewById(R.id.gzsj_query);

        ly_sche=findViewById(R.id.gzsj_ly_sche);
        img_sche=findViewById(R.id.gzsj_img_sche);
        anima= (AnimationDrawable) img_sche.getDrawable();

        //设置默认显示时间
        selectedDate= Calendar.getInstance();
        Date today=new Date();
        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");
        String end=sf.format(today);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
        DateTime =format1.format(today);
        time.setText(end);

        time.setOnClickListener(this);
        query.setOnClickListener(this);
        vehicle.addTextChangedListener(this);
        time.addTextChangedListener(this);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_gzsj;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.gzsj_time:
                ShowKeyboard.hideKeyboard(vehicle);
                Date today=new Date();
                SimpleDateFormat sf=new SimpleDateFormat("yyyy");
                SimpleDateFormat sf2=new SimpleDateFormat("MM");
                SimpleDateFormat sf3=new SimpleDateFormat("dd");
                Integer year=new Integer(sf.format(today));
                Integer month=new Integer(sf2.format(today));
                Integer day=new Integer(sf3.format(today));
                selectedDate.set(year.intValue(),month.intValue()-1,day.intValue());
                TimePicker();
                break;
            case R.id.gzsj_query:
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
                    anima.start();
                    ly_sche.setVisibility(View.VISIBLE);
                    GetWorkHour();
                }
                else{
                    Toast.makeText(context,"机号输入有误或者您没有权限操作",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        query.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(vehicle.getText().length()>0&& time.getText().length()>0){
            query.setEnabled(true);
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

    //查询工作时间
    private void GetWorkHour(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("VehicleLic",vehicle.getText().toString());
        proper.put("Time", DateTime);

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetWorkHourInfo", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    workTimeInfos=paraseGetWork(result);
                    if(workTimeInfos.size()!=0){
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

    private ArrayList<WorkTimeInfo> paraseGetWork(SoapObject result){
        ArrayList<WorkTimeInfo> workTimes=new ArrayList<>();
        SoapObject soap= (SoapObject) result.getProperty(0);
        values=0;
        if(soap==null) {
            return null;
        }
        for (int i=0;i<soap.getPropertyCount();i+=3){
            WorkTimeInfo workTime=new WorkTimeInfo();
            workTime.setBeginTime(soap.getProperty(i).toString());
            workTime.setEndTime(soap.getProperty(i+1).toString());
            workTime.setWorkTime(soap.getProperty(i+2).toString());
            value=new Float(workTime.getWorkTime());
            values+=value;
            workTimes.add(workTime);
        }
        //统计当天工作总时间
        WorkTimeInfo workTime=new WorkTimeInfo();
        workTime.setBeginTime("总时间");
        workTime.setEndTime("");
        //设置小数点个数
        BigDecimal bd=new BigDecimal((double)values);
        bd =bd.setScale(2,BigDecimal.ROUND_HALF_UP);//保留两位小数，四舍五入
        values= bd.floatValue();
        workTime.setWorkTime(values+"");
        workTimes.add(workTime);
        return workTimes;
    }


    public void TimePicker(){
        final TimePickerView times=new TimePickerView.Builder(context, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                    time.setText(getTime(date));
            }
        })
                .setDate(selectedDate)
                .setType(new boolean[]{true,true,true,false,false,false})
                .setLabel("","","","","","")
                .build();
        //精确到秒的时间显示
        //time.setDate(Calendar.getInstance());
        times.show();
    }

    //可根据需要自行截取数据显示
    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
        DateTime =format1.format(date);
        return format.format(date);
    }



}
