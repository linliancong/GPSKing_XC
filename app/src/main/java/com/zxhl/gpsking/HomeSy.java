package com.zxhl.gpsking;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;
import com.zxhl.util.Constants;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.sax.SAXSource;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Administrator on 2017/12/4.
 */

public class HomeSy extends Fragment implements View.OnClickListener {

    private View view;
    private Context context;
    private int tag=0;

    //折线图相关设置
    private LineChartView lineChart;
    private List<String> data=null;
    private List<String> score=null;
    private List<PointValue> mPointValue=new ArrayList<>();
    private List<AxisValue> mAxisXValue=new ArrayList<>();
    private List<AxisValue> mAxisYValue=new ArrayList<>();

    //控件
    private TextView Yesterday;
    private TextView MonthTotal;
    private TextView YearTotal;
    private List<String> WorkHoursTotal;

    private AutoCompleteTextView VehicleLic;
    private EditText BeginTime;
    private EditText EndTime;

    private RelativeLayout home_ly_sche;
    private ImageView home_img_sche;
    private AnimationDrawable anima;

    //自动完成框的相关变量
    private List<String> autoVehLic;
    private ArrayAdapter<String> adapter;

    private SharedPreferenceUtils sp;

    //提示设置默认显示机号
    private RelativeLayout rl_setting_visible;
    private TextView txt_setting;
    private ImageView img_cancel;

    //广播
    private static boolean state=false;
    private static boolean state2=true;
    private MyBroadcastHomeSy broad;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x403:
                    Toast.makeText(context,"没有查询到数据，请稍后重试",Toast.LENGTH_SHORT).show();
                    break;
                case 0x404:
                    Toast.makeText(context,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x001:
                    adapter=new ArrayAdapter<String>(context,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    VehicleLic.setAdapter(adapter);
                    break;
                case 0x002:
                    getAxisXLables();
                    getAxisPoints();
                    initLineChart();
                    home_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    break;
                case 0x003:
                    Yesterday.setText(WorkHoursTotal.get(0));
                    MonthTotal.setText(WorkHoursTotal.get(1));
                    YearTotal.setText(WorkHoursTotal.get(2));
                    break;
                case 0x004:
                    GetWorkHourTotal();
                    GetWorkHour();
                    rl_setting_visible.setVisibility(View.GONE);
                    break;
            }
        }
    };

    public HomeSy(){

    }
    @SuppressLint("ValidFragment")
    public HomeSy(Context context){
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(view==null) {
            view = inflater.inflate(R.layout.sy_home, container, false);
            init();
            //getVehicleLic();
            home_ly_sche.setVisibility(View.VISIBLE);
            anima.start();
            GetWorkHourTotal();
            GetWorkHour();

        }
        if(sp.getVehicleLic().equals("")){
            rl_setting_visible.setVisibility(View.VISIBLE);
        }
        else
        {
            rl_setting_visible.setVisibility(View.GONE);
        }

        new Thread(){
            @Override
            public void run() {
                while (true){
                    if(state){
                        state=false;
                        handler.sendEmptyMessage(0x004);
                    }
                }
            }
        }.start();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sy_home_BeginTime:
                TimePicker(1);
                break;
            case R.id.sy_home_EndTime:
                TimePicker(2);
                break;
            case R.id.sy_home_setting:
                Intent it0=new Intent(context,SettingSyVehicle.class);
                startActivity(it0);
                break;
            case R.id.sy_home_cancel:
                rl_setting_visible.setVisibility(View.GONE);
                break;
            case R.id.sy_home_VehicleLic:
                VehicleLic.setFocusable(true);
                break;
        }

    }

    public void init(){

        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);
        autoVehLic=new ArrayList<>();
        WorkHoursTotal=new ArrayList<>();
        broad=new MyBroadcastHomeSy();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.zxhl.gpsking.MYBROADCASTHOMESY");
        getActivity().registerReceiver(broad,filter);

        Yesterday= (TextView) view.findViewById(R.id.sy_home_Yesterday);
        MonthTotal=(TextView) view.findViewById(R.id.sy_home_MonthTotal);
        YearTotal=(TextView) view.findViewById(R.id.sy_home_YearTotal);
        VehicleLic= (AutoCompleteTextView) view.findViewById(R.id.sy_home_VehicleLic);
        BeginTime= (EditText) view.findViewById(R.id.sy_home_BeginTime);
        EndTime= (EditText) view.findViewById(R.id.sy_home_EndTime);
        rl_setting_visible= (RelativeLayout) view.findViewById(R.id.sy_home_visible);
        txt_setting= (TextView) view.findViewById(R.id.sy_home_setting);
        img_cancel= (ImageView) view.findViewById(R.id.sy_home_cancel);

        home_ly_sche=view.findViewById(R.id.home_ly_sche);
        home_img_sche=view.findViewById(R.id.home_img_sche);
        anima= (AnimationDrawable) home_img_sche.getDrawable();

        BeginTime.setOnClickListener(this);
        EndTime.setOnClickListener(this);
        img_cancel.setOnClickListener(this);
        txt_setting.setOnClickListener(this);
        VehicleLic.setOnClickListener(this);

        lineChart= (LineChartView) view.findViewById(R.id.sy_home_chart);
        /*data=new String[]{"10-22","11-22","12-22","1-22","6-22","5-23","5-22","6-22","5-23","5-22"};//X轴的标注
        score= new int[]{50,42,90,33,10,74,22,18,79,20};//图表的数据点*/


    }

    /**
    *设置X轴的显示
    * */
    public void getAxisXLables(){
        mAxisXValue=new ArrayList<>();
        for (int i=0;i<data.size();i++){
            mAxisXValue.add(new AxisValue(i).setLabel(data.get(i)));
        }
    }

    /**
     *设置每个点的显示
     * */
    public void getAxisPoints(){
        mPointValue=new ArrayList<>();
        float value;
        Float it;
        for (int i=0;i<score.size();i++){
            it=new Float(score.get(i));
            value=it.floatValue();
            mPointValue.add(new PointValue(i,value));
        }
    }

    /**
     * 对图标进行设置
    * */
    public void initLineChart(){
        //折线的颜色
        Line line=new Line(mPointValue).setColor(Color.parseColor("#e74c3c"));
        List<Line> lines=new ArrayList<>();
        //设置显示小数点
        LineChartValueFormatter formatter=new SimpleLineChartValueFormatter(2);
        line.setFormatter(formatter);
        //折线图上的每个数据点的形状，这里是圆形
        //有三种 ：ValueShape.SQUARE（矩形） ValueShape.DIAMOND（菱形）ValueShape.CIRCLE
        line.setShape(ValueShape.CIRCLE);
        //曲线是否平滑，即是曲线还是折线
        line.setCubic(true);
        //线条的粗细，默认是3
        line.setStrokeWidth(2);
        //是否填充曲线的面积
        line.setFilled(false);
        //曲线的数据坐标是否加上备注
        line.setHasLabels(true);
        //点击数据坐标提示数据（必须设置line.setHasLabels(true);）
        //line.setHasLabelsOnlyForSelected(true);
        //是否用线显示，如果为false 则没有曲线只有点显示
        line.setHasLines(true);
        //是否显示圆点，如果为false 则没有圆点显示只有点显示（每个数据点都是大的圆点）
        line.setHasPoints(true);
        // 设置节点颜色
        line.setPointColor(Color.parseColor("#ffc773"));
        // 设置节点半径
        line.setPointRadius(2);
        //添加到线的集合中
        lines.add(line);
        //将线画入到图中
        LineChartData data1=new LineChartData();
        data1.setLines(lines);


        //坐标轴
        //X轴
        Axis axisX=new Axis();
        //X轴字体市斜的显示还是直的，设置为true为斜的
        axisX.setHasTiltedLabels(true);
        //字体颜色
        //axisX.setTextColor(Color.RED);
        //axisX.setTextColor(Color.parseColor("#D6D6D9"));//灰色
        //表格名称
        axisX.setName("工作时间统计");
        //字体大小
        axisX.setTextSize(10);
        //最多几个坐标轴，mAxisValue.lenght
        axisX.setMaxLabelChars(7);
        //填充X的坐标名
        axisX.setValues(mAxisXValue);
        //X轴的位置在底部
        data1.setAxisXBottom(axisX);
        //设置X轴分割线
        //axisX.setHasLines(true);

        //Y轴 根据数据大小自动设置上限
        Axis axisY=new Axis();
        //Y轴分割线
        axisY.setHasLines(true);
        //Y轴标注
        //axisY.setName("AxisY");
        //字体大小
        axisY.setTextSize(10);

        //设置固定Y轴数据，从0-100
        for (int i=0;i<=24;i+=2){
            mAxisYValue.add(new AxisValue(i).setValue(i));
        }

        axisY.setValues(mAxisYValue);
        //Y轴设置在左边
        data1.setAxisYLeft(axisY);

        /*//更多设置
        // 设置反向覆盖区域颜色
        data1.setBaseValue(20);
        // 设置数据背景是否跟随节点颜色
        data1.setValueLabelBackgroundAuto(false);
        // 设置数据背景颜色
        data1.setValueLabelBackgroundColor(Color.BLUE);
        // 设置是否有数据背景
        data1.setValueLabelBackgroundEnabled(false);
        // 设置数据文字颜色
        data1.setValueLabelsTextColor(Color.BLACK);
        // 设置数据文字大小
        data1.setValueLabelTextSize(15);
        // 设置数据文字样式
        data1.setValueLabelTypeface(Typeface.MONOSPACE);
        //最后为图表设置数据，数据类型为LineChartData
        lineChart.setLineChartData(data1);*/


        //设置行为属性，支持缩放，滑动及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float)2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data1);
        lineChart.setVisibility(View.VISIBLE);


        Viewport v=new Viewport(lineChart.getMaximumViewport());
        //这里设置Y轴的值的范围
        v.bottom=0;
        v.top=24;
        lineChart.setMaximumViewport(v);
        //这里设置一开始加载的显示个数，配合axisX.setMaxLabelChars(7);使用，这里的7必须大于等于设置的范围，才会生效
        v.left=0;
        v.right=6;
        lineChart.setCurrentViewport(v);

    }

    public void TimePicker(final int state){
        TimePickerView time=new TimePickerView.Builder(context, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                if(state==1) {
                    BeginTime.setText(getTime(date));
                }
                else {
                    EndTime.setText(getTime(date));
                }
            }
        })
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

    //查询工作时间曲线
    private void GetWorkHour(){

        String count=sp.getDayCount();
        int counts=7;
        if(!count.equals("")){
            Integer it=new Integer(count);
            counts=it.intValue();
        }
        SimpleDateFormat simple=new SimpleDateFormat("yyyyMMdd");
        Date d=new Date();
        HashMap<String,String> proper=new HashMap<>();
        proper.put("VehicleLic",sp.getVehicleLic());
        proper.put("BeginTime",simple.format(new Date(d.getTime()-counts*24*60*60*1000)));
        proper.put("EndTime",simple.format(new Date(d.getTime()-1*24*60*60*1000)));
       /* //测试数据
        proper.put("VehicleLic","XH000181");
        proper.put("BeginTime","20171225");
        proper.put("EndTime","20180103");*/

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetWorkHour", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<List<String>> lists=new ArrayList<>();
                    lists=paraseGetWork(result);
                    if(lists.size()!=0){
                        data=lists.get(0);
                        score=lists.get(1);
                        handler.sendEmptyMessage(0x002);
                    }else
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

    //查询工作时间统计
    private void GetWorkHourTotal(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("VehicleLic",sp.getVehicleLic());
        /*//测试数据
        proper.put("VehicleLic","XH000181");
        proper.put("DateDay","20171231");*/


        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetWorkHourTotal", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null){
                    List<String> list=new ArrayList<String>();
                    list=parase(result);
                    if(list.size()!=0){
                        WorkHoursTotal=list;
                        handler.sendEmptyMessage(0x003);
                    }else
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

    private List<List<String>> paraseGetWork(SoapObject result){
        List<List<String>> lists=new ArrayList<>();
        List<String> listTime=new ArrayList<>();
        List<String> listWork=new ArrayList<>();
        SoapObject soap= (SoapObject) result.getProperty(0);
        if(soap==null) {
            return null;
        }
        for (int i=0;i<soap.getPropertyCount();i+=2){
            listTime.add(soap.getProperty(i).toString());
            listWork.add(soap.getProperty(i+1).toString());
        }
        lists.add(listTime);
        lists.add(listWork);
        return lists;
    }


    public static class MyBroadcastHomeSy extends BroadcastReceiver {
        public final String board="com.zxhl.gpsking.MYBROADCASTHOMESY";
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(board)){
                state=true;
                //Toast.makeText(context,"ces",Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(broad!=null){
            getActivity().unregisterReceiver(broad);
        }
    }
}
