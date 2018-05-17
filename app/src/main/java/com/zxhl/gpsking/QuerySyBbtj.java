package com.zxhl.gpsking;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
 * Created by Administrator on 2018/1/16.
 */

public class QuerySyBbtj extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    //控件
    private AutoCompleteTextView VehicleLic;
    private EditText BeginTime;
    private EditText EndTime;
    private Button query;
    private LineChartView lineChart;
    private ImgTxtLayout gkxx_imgtxt_title;

    //自动完成框的相关变量
    private List<String> autoVehLic;
    private ArrayAdapter<String> adapter;

    //折线图相关设置
    private List<String> data=null;
    private List<String> score=null;
    private List<PointValue> mPointValue=new ArrayList<>();
    private List<AxisValue> mAxisXValue=new ArrayList<>();
    private List<AxisValue> mAxisYValue=new ArrayList<>();

    private SharedPreferenceUtils sp;

    //查询相关
    private RelativeLayout bbtj_ly_sche;
    private ImageView bbtj_img_sche;
    private AnimationDrawable anima;

    private Calendar selectedDate;


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x403:
                    anima.stop();
                    bbtj_ly_sche.setVisibility(View.GONE);
                    lineChart.setVisibility(View.GONE);
                    Toast.makeText(QuerySyBbtj.this,"没有查询到数据，请稍后重试",Toast.LENGTH_SHORT).show();
                    break;
                case 0x404:
                    anima.stop();
                    bbtj_ly_sche.setVisibility(View.GONE);
                    lineChart.setVisibility(View.GONE);
                    Toast.makeText(QuerySyBbtj.this,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x001:
                    adapter=new ArrayAdapter<String>(QuerySyBbtj.this,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    VehicleLic.setAdapter(adapter);
                    break;
                case 0x002:
                    getAxisXLables();
                    getAxisPoints();
                    initLineChart();
                    lineChart.setVisibility(View.VISIBLE);
                    anima.stop();
                    bbtj_ly_sche.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.query_bbtj);

        init();
        getVehicleLic();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_bbtj;
    }

    public void init(){
        sp=new SharedPreferenceUtils(QuerySyBbtj.this, Constants.SAVE_USER);

        VehicleLic=findViewById(R.id.bbtj_auto_vehiclelic);
        BeginTime=findViewById(R.id.bbtj_edit_BeginTime);
        EndTime=findViewById(R.id.bbtj_edit_EndTime);
        query=findViewById(R.id.bbtj_btn_get);
        lineChart=findViewById(R.id.bbtj_chart);
        gkxx_imgtxt_title=findViewById(R.id.gkxx_imgtxt_title);

        bbtj_ly_sche=findViewById(R.id.bbtj_ly_sche);
        bbtj_img_sche=findViewById(R.id.bbtj_img_sche);
        anima= (AnimationDrawable) bbtj_img_sche.getDrawable();

        //设置默认显示时间
        selectedDate=Calendar.getInstance();
        Date today=new Date();
        Date olyday=new Date(today.getTime()-7*24*60*60*1000);
        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");
        String end=sf.format(today);
        String begin=sf.format(olyday);
        BeginTime.setText(begin);
        EndTime.setText(end);

        BeginTime.setOnClickListener(this);
        EndTime.setOnClickListener(this);
        query.setOnClickListener(this);
        VehicleLic.addTextChangedListener(this);
        BeginTime.addTextChangedListener(this);
        EndTime.addTextChangedListener(this);

        gkxx_imgtxt_title.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        VehicleLic.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                query.callOnClick();
                return true;
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bbtj_btn_get:
                ShowKeyboard.hideKeyboard(VehicleLic);
                int permiss=0;
                for(int i=0;i<autoVehLic.size();i++)
                {
                    if(VehicleLic.getText().toString().equalsIgnoreCase(autoVehLic.get(i))){
                        permiss=1;
                        break;
                    }
                }
                if(permiss==1){
                    anima.start();
                    bbtj_ly_sche.setVisibility(View.VISIBLE);
                    GetWorkHour();
                }
                else{
                    Toast.makeText(QuerySyBbtj.this,"机号输入有误或者您没有权限操作",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bbtj_edit_BeginTime:
                ShowKeyboard.hideKeyboard(VehicleLic);
                Date etoday=new Date();
                Date olyday=new Date(etoday.getTime()-7*24*60*60*1000);
                SimpleDateFormat esf=new SimpleDateFormat("yyyy");
                SimpleDateFormat esf2=new SimpleDateFormat("MM");
                SimpleDateFormat esf3=new SimpleDateFormat("dd");
                Integer eyear=new Integer(esf.format(olyday));
                Integer emonth=new Integer(esf2.format(olyday));
                Integer eday=new Integer(esf3.format(olyday));
                selectedDate.set(eyear.intValue(),emonth.intValue()-1,eday.intValue());
                TimePicker(1);
                break;
            case R.id.bbtj_edit_EndTime:
                ShowKeyboard.hideKeyboard(VehicleLic);
                Date today=new Date();
                SimpleDateFormat sf=new SimpleDateFormat("yyyy");
                SimpleDateFormat sf2=new SimpleDateFormat("MM");
                SimpleDateFormat sf3=new SimpleDateFormat("dd");
                Integer year=new Integer(sf.format(today));
                Integer month=new Integer(sf2.format(today));
                Integer day=new Integer(sf3.format(today));
                selectedDate.set(year.intValue(),month.intValue()-1,day.intValue());
                TimePicker(2);
                break;
        }

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
        Line line=new Line(mPointValue).setColor(Color.parseColor("#ff8c31"));
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
        line.setStrokeWidth(3);
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
        line.setPointColor(Color.parseColor("#fff143"));
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
        TimePickerView time=new TimePickerView.Builder(QuerySyBbtj.this, new TimePickerView.OnTimeSelectListener() {
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
        HashMap<String,String> proper=new HashMap<>();
        proper.put("VehicleLic",VehicleLic.getText().toString());
        proper.put("BeginTime",BeginTime.getText().toString());
        proper.put("EndTime",EndTime.getText().toString());

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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        query.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(VehicleLic.getText().length()!=0&&BeginTime.getText().length()!=0&&EndTime.getText().length()!=0){
            query.setEnabled(true);
        }

    }
}
