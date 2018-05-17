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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/15.
 */

public class QuerySyGkxx extends StatusBarUtil implements View.OnClickListener,TextWatcher{

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
    private RelativeLayout dwsj;
    private RelativeLayout gzsj;
    private RelativeLayout gkbj;
    private RelativeLayout gzzt;
    private RelativeLayout gps;
    private RelativeLayout gksj;

    //显示、隐藏数据的布局
    private LinearLayout v_dwsj;
    private LinearLayout v_gzsj;
    private LinearLayout v_gkbj;
    private LinearLayout v_gzzt;
    private LinearLayout v_gps;
    private LinearLayout v_gksj;

    //图片变换ImageView
    private ImageView img_dwsj;
    private ImageView img_gzsj;
    private ImageView img_gkbj;
    private ImageView img_gzzt;
    private ImageView img_gps;
    private ImageView img_gksj;

    //绑定数据的TextView
    //定位数据
    private TextView txt_dwsj;
    private TextView txt_wz;
    private TextView txt_jd;
    private TextView txt_wd;
    // 工作时间
    private TextView txt_zsj;
    private TextView txt_jtsj;
    // 工况报警
    private TextView txt_yyyw;
    private TextView txt_swgr;
    private TextView txt_ryd;
    private TextView txt_jyyld;
    private TextView txt_xtdygd;
    private TextView txt_yrzs;
    private TextView txt_yyyl;
    private TextView txt_klds;
    private TextView txt_ysfl;
    private TextView txt_fdjtx;
    // 工作状态
    private TextView txt_gzzt;
    private TextView txt_sjc;
    private TextView txt_jk;
    // GPS状态数据
    private TextView txt_gpstx;
    private TextView txt_sjxh;
    private TextView txt_dlzx;
    private TextView txt_gpszs_acc;
    private TextView txt_yjsc_sdms;
    private TextView txt_gpssj;
    private TextView txt_gpsbj;
    private TextView txt_zdyqk;
    private TextView txt_zdbb;
    // 工况数据
    private TextView txt_zhgk;
    private TextView txt_dy;
    private TextView txt_sw;
    private TextView txt_yw;
    private TextView txt_jyyl;
    private TextView txt_fdjzs;
    private TextView txt_ryyw;

    //是否显示数据标志
    private boolean tag1=false;
    private boolean tag2=false;
    private boolean tag3=false;
    private boolean tag4=false;
    private boolean tag5=false;
    private boolean tag6=false;

    //查询相关
    private RelativeLayout gkxx_ly_sche;
    private ImageView gkxx_img_sche;
    private AnimationDrawable anima;

    private SharedPreferenceUtils sp;
    private Map<String,String> map;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x403:
                    anima.stop();
                    gkxx_ly_sche.setVisibility(View.GONE);
                    Toast.makeText(QuerySyGkxx.this,"没有查询到数据，请稍后重试",Toast.LENGTH_SHORT).show();
                    break;
                case 0x404:
                    anima.stop();
                    gkxx_ly_sche.setVisibility(View.GONE);
                    Toast.makeText(QuerySyGkxx.this,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x002:
                    showWorkData();
                    anima.stop();
                    gkxx_ly_sche.setVisibility(View.GONE);
                    tag1=imgChange(false,img_dwsj,v_dwsj);
                    tag2=imgChange(false,img_gzsj,v_gzsj);
                    tag3=imgChange(false,img_gkbj,v_gkbj);
                    tag4=imgChange(false,img_gzzt,v_gzzt);
                    tag5=imgChange(false,img_gps,v_gps);
                    tag6=imgChange(false,img_gksj,v_gksj);
                    break;
                case 0x003:
                    adapter=new ArrayAdapter<String>(QuerySyGkxx.this,R.layout.simple_autoedit_dropdown_item,R.id.tv_spinner,autoVehLic);
                    vehicle.setAdapter(adapter);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.query_gkxx);

        init();
        getVehicleLic();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_gkxx;
    }

    private void init(){
        sp=new SharedPreferenceUtils(this, Constants.SAVE_USER);
        map=new HashMap<>();

        gkxx_ly_sche=findViewById(R.id.gkxx_ly_sche);
        gkxx_img_sche=findViewById(R.id.gkxx_img_sche);
        anima= (AnimationDrawable) gkxx_img_sche.getDrawable();

        //顶部操作栏
        back=findViewById(R.id.gkxx_imgtxt_title);
        img1=findViewById(R.id.gkxx_edit_img);
        img2=findViewById(R.id.gkxx_img_img);
        title=findViewById(R.id.gkxx_txt_title);
        vehicle=findViewById(R.id.gkxx_auto_vehiclelic);
        search=findViewById(R.id.gkxx_img_serch);
        getVeh=findViewById(R.id.gkxx_btn_get);

        //点击下拉的布局
        dwsj=findViewById(R.id.gkxx_ry_dwsj);
        gzsj=findViewById(R.id.gkxx_ry_gzsj);
        gkbj=findViewById(R.id.gkxx_ry_gkbj);
        gzzt=findViewById(R.id.gkxx_ry_gzzt);
        gps=findViewById(R.id.gkxx_ry_gps);
        gksj=findViewById(R.id.gkxx_ry_gksj);

        //显示、隐藏数据的布局
        v_dwsj=findViewById(R.id.gkxx_ly_dwsjmx);
        v_gzsj=findViewById(R.id.gkxx_ly_gzsjmx);
        v_gkbj=findViewById(R.id.gkxx_ly_gkbjmx);
        v_gzzt=findViewById(R.id.gkxx_ly_gzztmx);
        v_gps=findViewById(R.id.gkxx_ly_gpsmx);
        v_gksj=findViewById(R.id.gkxx_ly_gksjmx);

        //图片变换ImageView
        img_dwsj=findViewById(R.id.gkxx_img_dwsj);
        img_gzsj=findViewById(R.id.gkxx_img_gzsj);
        img_gkbj=findViewById(R.id.gkxx_img_gkbj);
        img_gzzt=findViewById(R.id.gkxx_img_gzzt);
        img_gps=findViewById(R.id.gkxx_img_gps);
        img_gksj=findViewById(R.id.gkxx_img_gksj);

        //绑定数据的TextView
        //定位数据
        txt_dwsj=findViewById(R.id.gkxx_txt_dwsj);
        txt_wz=findViewById(R.id.gkxx_txt_wz);
        txt_jd=findViewById(R.id.gkxx_txt_jd);
        txt_wd=findViewById(R.id.gkxx_txt_wd);
        // 工作时间
        txt_zsj=findViewById(R.id.gkxx_txt_zsj);
        txt_jtsj=findViewById(R.id.gkxx_txt_jtsj);
        // 工况报警
        txt_yyyw=findViewById(R.id.gkxx_txt_yyyw);
        txt_swgr=findViewById(R.id.gkxx_txt_swgr);
        txt_ryd=findViewById(R.id.gkxx_txt_ryd);
        txt_jyyld=findViewById(R.id.gkxx_txt_jyyld);
        txt_xtdygd=findViewById(R.id.gkxx_txt_xtdygd);
        txt_yrzs=findViewById(R.id.gkxx_txt_yrzs);
        txt_yyyl=findViewById(R.id.gkxx_txt_yyyl);
        txt_klds=findViewById(R.id.gkxx_txt_klds);
        txt_ysfl=findViewById(R.id.gkxx_txt_ysfl);
        txt_fdjtx=findViewById(R.id.gkxx_txt_fdjtx);
        // 工作状态
        txt_gzzt=findViewById(R.id.gkxx_txt_gzzt);
        txt_sjc=findViewById(R.id.gkxx_txt_sjc);
        txt_jk=findViewById(R.id.gkxx_txt_jk);
        // GPS状态数据
        txt_gpstx=findViewById(R.id.gkxx_txt_gpstx);
        txt_sjxh=findViewById(R.id.gkxx_txt_sjxh);
        txt_dlzx=findViewById(R.id.gkxx_txt_dlzx);
        txt_gpszs_acc=findViewById(R.id.gkxx_txt_gpszs);
        txt_yjsc_sdms=findViewById(R.id.gkxx_txt_yjsc);
        txt_gpssj=findViewById(R.id.gkxx_txt_gpssj);
        txt_gpsbj=findViewById(R.id.gkxx_txt_gpsbj);
        txt_zdyqk=findViewById(R.id.gkxx_txt_zdyqk);
        txt_zdbb=findViewById(R.id.gkxx_txt_zdbb);
        // 工况数据
        txt_zhgk=findViewById(R.id.gkxx_txt_zhgk);
        txt_dy=findViewById(R.id.gkxx_txt_dy);
        txt_sw=findViewById(R.id.gkxx_txt_sw);
        txt_yw=findViewById(R.id.gkxx_txt_yw);
        txt_jyyl=findViewById(R.id.gkxx_txt_jyyl);
        txt_fdjzs=findViewById(R.id.gkxx_txt_fdjzs);
        txt_ryyw=findViewById(R.id.gkxx_txt_ryjw);

        dwsj.setOnClickListener(this);
        gzsj.setOnClickListener(this);
        gkbj.setOnClickListener(this);
        gzzt.setOnClickListener(this);
        gps.setOnClickListener(this);
        gksj.setOnClickListener(this);

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
            case R.id.gkxx_ry_dwsj:
                tag1=imgChange(tag1,img_dwsj,v_dwsj);
                break;
            case R.id.gkxx_ry_gzsj:
                tag2=imgChange(tag2,img_gzsj,v_gzsj);
                break;
            case R.id.gkxx_ry_gkbj:
                tag3=imgChange(tag3,img_gkbj,v_gkbj);
                break;
            case R.id.gkxx_ry_gzzt:
                tag4=imgChange(tag4,img_gzzt,v_gzzt);
                break;
            case R.id.gkxx_ry_gps:
                tag5=imgChange(tag5,img_gps,v_gps);
                break;
            case R.id.gkxx_ry_gksj:
                tag6=imgChange(tag6,img_gksj,v_gksj);
                break;
            case R.id.gkxx_img_serch:
                title.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);
                img2.setVisibility(View.VISIBLE);
                vehicle.setVisibility(View.VISIBLE);
                break;
            case R.id.gkxx_btn_get:
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
                    gkxx_ly_sche.setVisibility(View.VISIBLE);
                    GetWorkData();
                    title.setVisibility(View.VISIBLE);
                    search.setVisibility(View.VISIBLE);
                    img1.setVisibility(View.GONE);
                    img2.setVisibility(View.GONE);
                    vehicle.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(QuerySyGkxx.this,"机号输入有误或者您没有权限操作",Toast.LENGTH_SHORT).show();
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


        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetWorkDataForOperator", prepro, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null)
                {
                    map=parseSoap(result);
                    if(map.size()==0)
                    {
                        handler.sendEmptyMessage(0x403);
                    }
                    else
                    {
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
        SoapObject soapObject= (SoapObject) result.getProperty("GetWorkDataForOperatorResult");
        if(soapObject==null)
        {
            return map;
        }
        for (int i=0;i<soapObject.getPropertyCount();i++){
            //查询回来的数据为user:lin的格式，故先截取
            str=soapObject.getProperty(i).toString().split(":");
            if (str.length>=2) {
                if(str[0].equals("最后定位时间")||str[0].equals("工作")||str[0].equals("最后工况时间")){
                    str=soapObject.getProperty(i).toString().split(",");
                }
                map.put(str[0],str[1]);
            }
            else
            {
                map.put(str[0],"");
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
        TextView text1=findViewById(R.id.gkxx_v_gps1);
        TextView text2=findViewById(R.id.gkxx_v_gps2);
        //显示数据
        //定位数据
        txt_dwsj.setText(map.get("最后定位时间:"));
        txt_wz.setText(map.get("最后位置"));
        txt_jd.setText(map.get("经度"));
        txt_wd.setText(map.get("纬度"));
        // 工作时间
        txt_zsj.setText(map.get("总工作小时"));
        txt_jtsj.setText(map.get("今天工作小时"));
        // 工况报警
        txt_yyyw.setText(map.get("液压油温报警"));
        txt_swgr.setText(map.get("水温过热报警"));
        txt_ryd.setText(map.get("燃油低报警"));
        txt_jyyld.setText(map.get("机油压力低报警"));
        txt_xtdygd.setText(map.get("系统电压过低报警"));
        txt_yrzs.setText(map.get("预热指示"));
        txt_yyyl.setText(map.get("液压油滤报警"));
        txt_klds.setText(map.get("空滤堵塞报警"));
        if(map.get("车台类型").equals("28")) {
            findViewById(R.id.gkxx_v_gkbj1).setVisibility(View.VISIBLE);
            findViewById(R.id.gkxx_v_gkbj2).setVisibility(View.VISIBLE);
            txt_ysfl.setText(map.get("油水分离报警"));
            txt_fdjtx.setText(map.get("发动机通信异常"));
            txt_ysfl.setVisibility(View.VISIBLE);
            txt_fdjtx.setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.gkxx_v_gkbj1).setVisibility(View.GONE);
            findViewById(R.id.gkxx_v_gkbj2).setVisibility(View.GONE);
            txt_ysfl.setVisibility(View.GONE);
            txt_fdjtx.setVisibility(View.GONE);
        }
        // 工作状态
        if(map.get("车台类型").equals("28")) {
            findViewById(R.id.gkxx_v_gzzt).setVisibility(View.VISIBLE);
            txt_gzzt.setText(map.get("工作:"));
            txt_gzzt.setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.gkxx_v_gzzt).setVisibility(View.GONE);
            txt_gzzt.setVisibility(View.GONE);
        }
        txt_sjc.setText(map.get("锁解车状态"));
        txt_jk.setText(map.get("监控状态"));
        // GPS状态数据
        txt_gpstx.setText(map.get("GPS天线状态"));
        txt_sjxh.setText(map.get("手机信号强度"));
        txt_dlzx.setText(map.get("登录中心情况"));
        if(map.get("车台类型").equals("28")) {
            text1.setText("GPS自身状态模式");
            text2.setText("样机锁车时间");
            findViewById(R.id.gkxx_v_gps3).setVisibility(View.GONE);
            findViewById(R.id.gkxx_v_gps4).setVisibility(View.GONE);
            findViewById(R.id.gkxx_v_gps5).setVisibility(View.GONE);
            findViewById(R.id.gkxx_v_gps6).setVisibility(View.GONE);
            txt_gpszs_acc.setText(map.get("GPS自身状态模式"));
            txt_yjsc_sdms.setText(map.get("样机锁车时间"));
            txt_gpssj.setVisibility(View.GONE);
            txt_gpsbj.setVisibility(View.GONE);
            txt_zdyqk.setVisibility(View.GONE);
            txt_zdbb.setVisibility(View.GONE);
        }
        else {
            text1.setText("ACC");
            text2.setText("省电模式");
            findViewById(R.id.gkxx_v_gps3).setVisibility(View.VISIBLE);
            findViewById(R.id.gkxx_v_gps4).setVisibility(View.VISIBLE);
            findViewById(R.id.gkxx_v_gps5).setVisibility(View.VISIBLE);
            findViewById(R.id.gkxx_v_gps6).setVisibility(View.VISIBLE);
            txt_gpszs_acc.setText(map.get("ACC"));
            txt_yjsc_sdms.setText(map.get("省电模式"));
            txt_gpssj.setText(map.get("GPS数据"));
            txt_gpsbj.setText(map.get("GPS报警"));
            txt_zdyqk.setText(map.get("主电源情况"));
            txt_zdbb.setText(map.get("终端版本号"));
            txt_gpssj.setVisibility(View.VISIBLE);
            txt_gpsbj.setVisibility(View.VISIBLE);
            txt_zdyqk.setVisibility(View.VISIBLE);
            txt_zdbb.setVisibility(View.VISIBLE);
        }
        // 工况数据
        txt_zhgk.setText(map.get("最后工况时间:"));
        txt_dy.setText(map.get("电压"));
        txt_sw.setText(map.get("水温"));
        txt_yw.setText(map.get("油温"));
        txt_jyyl.setText(map.get("机油压力"));
        txt_fdjzs.setText(map.get("发动机转速"));
        txt_ryyw.setText(map.get("燃油油位"));

    }
}
