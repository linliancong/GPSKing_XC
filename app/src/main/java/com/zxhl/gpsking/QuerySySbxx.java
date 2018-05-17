package com.zxhl.gpsking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.util.Map;

/**
 * Created by Administrator on 2018/1/15.
 */

public class QuerySySbxx extends StatusBarUtil implements View.OnClickListener,TextWatcher{

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

    //点击下拉的布局
    private RelativeLayout jqxx;
    private RelativeLayout jzxx;
    private RelativeLayout zdxx;
    private RelativeLayout cjxx;
    private RelativeLayout jxs;
    private RelativeLayout jxs2;
    private RelativeLayout jxs3;

    //显示、隐藏数据的布局
    private LinearLayout v_jqxx;
    private LinearLayout v_jzxx;
    private LinearLayout v_zdxx;
    private LinearLayout v_cjxx;
    private LinearLayout v_jxs;
    private LinearLayout v_jxs2;
    private LinearLayout v_jxs3;

    //图片变换ImageView
    private ImageView img_jqxx;
    private ImageView img_jzxx;
    private ImageView img_zdxx;
    private ImageView img_cjxx;
    private ImageView img_jxs;
    private ImageView img_jxs2;
    private ImageView img_jxs3;

    //绑定数据的TextView
    //机器信息
    private TextView txt_jh;
    private TextView txt_jqzl;
    private TextView txt_jxxh;
    private TextView txt_yqbh;
    // 机主信息
    private TextView txt_jzxm;
    // 终端信息
    private TextView txt_zclx;
    private TextView txt_ssfz;
    private TextView txt_zdxh;
    private TextView txt_sbbh;
    private TextView txt_sim;
    private TextView txt_clmm;
    private TextView txt_azrq;
    private TextView txt_zjr;
    private TextView txt_zcr;
    // 厂家信息
    private TextView txt_xsrq;
    private TextView txt_dqrq;
    private TextView txt_xslb;
    // 经销商信息
    private TextView txt_jxs;
    private TextView txt_jxslxr;
    private TextView txt_lxdh;
    private TextView txt_lxsj;
    private TextView txt_email;
    // 经销商信息2
    private TextView txt_jxs2;
    private TextView txt_jxslxr2;
    private TextView txt_lxdh2;
    private TextView txt_lxsj2;
    private TextView txt_email2;
    // 经销商信息2
    private TextView txt_jxs3;
    private TextView txt_jxslxr3;
    private TextView txt_lxdh3;
    private TextView txt_lxsj3;
    private TextView txt_email3;

    //是否显示数据标志
    private boolean tag1=false;
    private boolean tag2=false;
    private boolean tag3=false;
    private boolean tag4=false;
    private boolean tag5=false;
    private boolean tag6=false;
    private boolean tag7=false;

    //查询相关
    private RelativeLayout sbxx_ly_sche;
    private ImageView sbxx_img_sche;
    private AnimationDrawable anima;

    private SharedPreferenceUtils sp;
    private Map<String,String> map;

    private boolean isData=false;
    private boolean isData2=false;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x403:
                    anima.stop();
                    sbxx_ly_sche.setVisibility(View.GONE);
                    Toast.makeText(QuerySySbxx.this,"没有查询到数据，请稍后重试",Toast.LENGTH_SHORT).show();
                    break;
                case 0x404:
                    anima.stop();
                    sbxx_ly_sche.setVisibility(View.GONE);
                    Toast.makeText(QuerySySbxx.this,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x002:
                    showWorkData();
                    anima.stop();
                    sbxx_ly_sche.setVisibility(View.GONE);
                    tag1=imgChange(false,img_jqxx,v_jqxx);
                    tag2=imgChange(false,img_jzxx,v_jzxx);
                    tag3=imgChange(false,img_zdxx,v_zdxx);
                    tag4=imgChange(false,img_cjxx,v_cjxx);
                    tag5=imgChange(false,img_jxs,v_jxs);
                    if(isData) {
                        jxs2.setVisibility(View.VISIBLE);
                        tag6 = imgChange(false, img_jxs2, v_jxs2);
                    }
                    else
                    {
                        jxs2.setVisibility(View.GONE);
                        tag6 = imgChange(true, img_jxs2, v_jxs2);
                    }
                    if(isData2) {
                        jxs3.setVisibility(View.VISIBLE);
                        tag7 = imgChange(false, img_jxs3, v_jxs3);
                    }
                    else
                    {
                        jxs3.setVisibility(View.GONE);
                        tag7 = imgChange(true, img_jxs3, v_jxs3);
                    }
                    break;
                case 0x003:
                    adapter=new ArrayAdapter<String>(QuerySySbxx.this,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.query_sbxx);

        init();
        getVehicleLic();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_sbxx;
    }

    private void init(){
        sp=new SharedPreferenceUtils(this, Constants.SAVE_USER);
        map=new HashMap<>();

        sbxx_ly_sche=findViewById(R.id.sbxx_ly_sche);
        sbxx_img_sche=findViewById(R.id.sbxx_img_sche);
        anima= (AnimationDrawable) sbxx_img_sche.getDrawable();

        //顶部操作栏
        back=findViewById(R.id.sbxx_imgtxt_title);
        img1=findViewById(R.id.sbxx_edit_img);
        img2=findViewById(R.id.sbxx_img_img);
        title=findViewById(R.id.sbxx_txt_title);
        vehicle=findViewById(R.id.sbxx_auto_vehiclelic);
        search=findViewById(R.id.sbxx_img_serch);
        getVeh=findViewById(R.id.sbxx_btn_get);

        //点击下拉的布局
        jqxx=findViewById(R.id.sbxx_ry_jjxx);
        jzxx=findViewById(R.id.sbxx_ry_jzxx);
        zdxx=findViewById(R.id.sbxx_ry_zdxx);
        cjxx=findViewById(R.id.sbxx_ry_cjxx);
        jxs=findViewById(R.id.sbxx_ry_jxs);
        jxs2=findViewById(R.id.sbxx_ry_jxs2);
        jxs3=findViewById(R.id.sbxx_ry_jxs3);

        //显示、隐藏数据的布局
        v_jqxx=findViewById(R.id.sbxx_ly_jqxxmx);
        v_jzxx=findViewById(R.id.sbxx_ly_gjzxxmx);
        v_zdxx=findViewById(R.id.sbxx_ly_zdxxmx);
        v_cjxx=findViewById(R.id.sbxx_ly_cjxxmx);
        v_jxs=findViewById(R.id.sbxx_ly_jxsmx);
        v_jxs2=findViewById(R.id.sbxx_ly_jxsmx2);
        v_jxs3=findViewById(R.id.sbxx_ly_jxsmx3);

        //图片变换ImageView
        img_jqxx=findViewById(R.id.sbxx_img_jqxx);
        img_jzxx=findViewById(R.id.sbxx_img_jzxx);
        img_zdxx=findViewById(R.id.sbxx_img_zdxx);
        img_cjxx=findViewById(R.id.sbxx_img_cjxx);
        img_jxs=findViewById(R.id.sbxx_img_jxs);
        img_jxs2=findViewById(R.id.sbxx_img_jxs2);
        img_jxs3=findViewById(R.id.sbxx_img_jxs3);

        //绑定数据的TextView
        //机器信息
        txt_jh=findViewById(R.id.sbxx_txt_jh);
        txt_jqzl=findViewById(R.id.sbxx_txt_jqzl);
        txt_jxxh =findViewById(R.id.sbxx_txt_jxxh);
        txt_yqbh =findViewById(R.id.sbxx_txt_yqbh);
        // 机主信息
        txt_jzxm =findViewById(R.id.sbxx_txt_jzxm);
        // 终端信息
        txt_zclx =findViewById(R.id.sbxx_txt_zclx);
        txt_ssfz =findViewById(R.id.sbxx_txt_ssfz);
        txt_zdxh =findViewById(R.id.sbxx_txt_zdxh);
        txt_sbbh =findViewById(R.id.sbxx_txt_sbbh);
        txt_sim =findViewById(R.id.sbxx_txt_sim);
        txt_clmm =findViewById(R.id.sbxx_txt_clmm);
        txt_azrq =findViewById(R.id.sbxx_txt_azrq);
        txt_zjr =findViewById(R.id.sbxx_txt_zjr);
        txt_zcr =findViewById(R.id.sbxx_txt_zcr);
        // 经销商信息
        txt_jxs =findViewById(R.id.sbxx_txt_jxs);
        txt_jxslxr =findViewById(R.id.sbxx_txt_jxslxr);
        txt_lxdh =findViewById(R.id.sbxx_txt_lxdh);
        txt_lxsj =findViewById(R.id.sbxx_txt_lxsj);
        txt_email =findViewById(R.id.sbxx_txt_email);
        // 经销商信息2
        txt_jxs2 =findViewById(R.id.sbxx_txt_jxs2);
        txt_jxslxr2 =findViewById(R.id.sbxx_txt_jxslxr2);
        txt_lxdh2 =findViewById(R.id.sbxx_txt_lxdh2);
        txt_lxsj2 =findViewById(R.id.sbxx_txt_lxsj2);
        txt_email2 =findViewById(R.id.sbxx_txt_email2);
        // 经销商信息3
        txt_jxs3 =findViewById(R.id.sbxx_txt_jxs3);
        txt_jxslxr3 =findViewById(R.id.sbxx_txt_jxslxr3);
        txt_lxdh3 =findViewById(R.id.sbxx_txt_lxdh3);
        txt_lxsj3 =findViewById(R.id.sbxx_txt_lxsj3);
        txt_email3 =findViewById(R.id.sbxx_txt_email3);
        // 厂家信息
        txt_xsrq=findViewById(R.id.sbxx_txt_xsrq);
        txt_dqrq =findViewById(R.id.sbxx_txt_dqrq);
        txt_xslb =findViewById(R.id.sbxx_txt_xslb);

        jqxx.setOnClickListener(this);
        jzxx.setOnClickListener(this);
        zdxx.setOnClickListener(this);
        cjxx.setOnClickListener(this);
        jxs.setOnClickListener(this);
        jxs2.setOnClickListener(this);
        jxs3.setOnClickListener(this);

        search.setOnClickListener(this);
        getVeh.setOnClickListener(this);
        vehicle.addTextChangedListener(this);
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
            case R.id.sbxx_ry_jjxx:
                tag1=imgChange(tag1,img_jqxx,v_jqxx);
                break;
            case R.id.sbxx_ry_jzxx:
                tag2=imgChange(tag2,img_jzxx,v_jzxx);
                break;
            case R.id.sbxx_ry_zdxx:
                tag3=imgChange(tag3,img_zdxx,v_zdxx);
                break;
            case R.id.sbxx_ry_cjxx:
                tag4=imgChange(tag4,img_cjxx,v_cjxx);
                break;
            case R.id.sbxx_ry_jxs:
                tag5=imgChange(tag5,img_jxs,v_jxs);
                break;
            case R.id.sbxx_ry_jxs2:
                tag6=imgChange(tag6,img_jxs2,v_jxs2);
                break;
            case R.id.sbxx_ry_jxs3:
                tag7=imgChange(tag7,img_jxs3,v_jxs3);
                break;
            case R.id.sbxx_img_serch:
                title.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);
                img2.setVisibility(View.VISIBLE);
                vehicle.setVisibility(View.VISIBLE);
                break;
            case R.id.sbxx_btn_get:
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
                    sbxx_ly_sche.setVisibility(View.VISIBLE);
                    GetWorkData();
                    title.setVisibility(View.VISIBLE);
                    search.setVisibility(View.VISIBLE);
                    img1.setVisibility(View.GONE);
                    img2.setVisibility(View.GONE);
                    vehicle.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(QuerySySbxx.this,"机号输入有误或者您没有权限操作",Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private boolean imgChange(boolean tag,ImageView view,LinearLayout layout) {
        Bitmap down= BitmapFactory.decodeResource(getResources(),R.mipmap.cs_arrow_down);
        Bitmap up= BitmapFactory.decodeResource(getResources(),R.mipmap.cs_arrow_up);
        if(tag){
            tag=!tag;
            view.setImageDrawable(new BitmapDrawable(getResources(),down));
            layout.setVisibility(View.GONE);
        }
        else {
            tag=!tag;
            view.setImageDrawable(new BitmapDrawable(getResources(),up));
            layout.setVisibility(View.VISIBLE);
        }
        return tag;
    }

    //获取工况信息
    public void GetWorkData(){
        HashMap<String,String> prepro=new HashMap<String,String>();
        prepro.put("OperatorID",sp.getOperatorID());
        prepro.put("VehicleLic",vehicle.getText().toString());


        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetVehicleDeviceInfoForOperator", prepro, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null)
                {
                    map=parseSoap(result);
                    if(map.size()==0)
                    {
                        handler.sendEmptyMessage(0x403);
                    }
                    else {
                        handler.sendEmptyMessage(0x002);
                    }
                }
                else{
                    handler.sendEmptyMessage(0x404);
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
                        autoVehLic=list;
                        handler.sendEmptyMessage(0x003);
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

    /**
    *解析SoapObject对象
    *@param result
    * @return
    * */
    public HashMap<String,String> parseSoap(SoapObject result){
        HashMap<String,String> map=new HashMap<String,String>();
        String[] str=new String[4];
        SoapObject soapObject= (SoapObject) result.getProperty("GetVehicleDeviceInfoForOperatorResult");
        if(soapObject==null)
        {
            return map;
        }
        for (int i=0;i<soapObject.getPropertyCount();i++){
            //查询回来的数据为user:lin的格式，故先截取
            str=soapObject.getProperty(i).toString().split(":");
            if (str.length>=2) {
                if(str[0].equals("安装日期")||str[0].equals("销售日期")||str[0].equals("到期日期")){
                    str=soapObject.getProperty(i).toString().split(",");
                }
                if(i>=27&&i<54)
                {
                    isData=true;
                    map.put(str[0]+"2",str[1]);
                }
                else if(i>=54){
                    isData2=true;
                    map.put(str[0]+"3", str[1]);
                }
                else {
                    isData=false;
                    isData2=false;
                    map.put(str[0], str[1]);
                }
            }
            else
            {
                if(i>=27&&i<54)
                {
                    isData=true;
                    map.put(str[0]+"2","");
                }
                else if(i>=54){
                    isData2=true;
                    map.put(str[0]+"3", "");
                }
                else {
                    isData=false;
                    isData2=false;
                    map.put(str[0], "");
                }
            }
        }
        return map;

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

    private void showWorkData(){
        //显示数据
        //机器信息
        txt_jh.setText(map.get("机号"));
        txt_jqzl.setText(map.get("机器种类"));
        txt_jxxh.setText(map.get("机械型号"));
        txt_yqbh.setText(map.get("引擎编号"));
        // 机主信息
        txt_jzxm.setText(map.get("机主姓名"));
        // 终端信息
        txt_zclx.setText(map.get("注册类型"));
        txt_ssfz.setText(map.get("所属分组"));
        txt_zdxh.setText(map.get("终端型号"));
        txt_sbbh.setText(map.get("设备编号"));
        txt_sim.setText(map.get("SIM卡号"));
        txt_clmm.setText(map.get("车辆密码"));
        txt_azrq.setText(map.get("安装日期:"));
        txt_zjr.setText(map.get("装机人"));
        txt_zcr.setText(map.get("注册人"));
        // 厂家信息
        txt_xsrq.setText(map.get("销售日期:"));
        txt_dqrq.setText(map.get("到期日期:"));
        txt_xslb.setText(map.get("销售类别"));
        // 经销商信息
        txt_jxs.setText(map.get("经销商"));
        txt_jxslxr.setText(map.get("经销商联系人"));
        txt_lxdh.setText(map.get("联系电话"));
        txt_lxsj.setText(map.get("联系手机"));
        txt_email.setText(map.get("Email"));

        // 经销商信息2
        txt_jxs2.setText(map.get("经销商2"));
        txt_jxslxr2.setText(map.get("经销商联系人2"));
        txt_lxdh2.setText(map.get("联系电话2"));
        txt_lxsj2.setText(map.get("联系手机2"));
        txt_email2.setText(map.get("Email2"));
        // 经销商信息3
        txt_jxs3.setText(map.get("经销商3"));
        txt_jxslxr3.setText(map.get("经销商联系人3"));
        txt_lxdh3.setText(map.get("联系电话3"));
        txt_lxsj3.setText(map.get("联系手机3"));
        txt_email3.setText(map.get("Email3"));

    }
}
