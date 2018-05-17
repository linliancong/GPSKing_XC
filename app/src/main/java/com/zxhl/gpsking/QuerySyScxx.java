package com.zxhl.gpsking;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.entity.CarInfo;
import com.zxhl.entity.LockInfo;
import com.zxhl.util.AdapterUtil;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.SwipeRefreshView;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by Administrator on 2018/1/17.
 */

public class QuerySyScxx extends StatusBarUtil implements View.OnClickListener,TextWatcher{

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
    private RelativeLayout scxx_ly_sche;
    private ImageView scxx_img_sche;
    private AnimationDrawable anima;

    private SharedPreferenceUtils sp;
    private Context context;

    private List<List<String>> info;
    private AdapterUtil adapterUtil;
    private ArrayList<LockInfo> lockInfos;



    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x403:
                    list.setVisibility(View.GONE);
                    scxx_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    break;
                case 0x404:
                    list.setVisibility(View.GONE);
                    scxx_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                    Toast.makeText(context,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x001:
                    showInfo();
                    list.setVisibility(View.VISIBLE);
                    scxx_ly_sche.setVisibility(View.GONE);
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
        //setContentView(R.layout.query_scxx);

        context=QuerySyScxx.this;

        init();

        getVehicleLic();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_scxx;
    }

    public void init(){
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);
        back=findViewById(R.id.scxx_imgtxt_title);
        list=findViewById(R.id.scxx_list);
        img=findViewById(R.id.scxx_img);
        text=findViewById(R.id.scxx_text);

        //顶部操作栏
        back=findViewById(R.id.scxx_imgtxt_title);
        img1=findViewById(R.id.scxx_edit_img);
        img2=findViewById(R.id.scxx_img_img);
        title=findViewById(R.id.scxx_txt_title);
        vehicle=findViewById(R.id.scxx_auto_vehiclelic);
        search=findViewById(R.id.scxx_img_serch);
        getVeh=findViewById(R.id.scxx_btn_get);

        scxx_ly_sche=findViewById(R.id.scxx_ly_sche);
        scxx_img_sche=findViewById(R.id.scxx_img_sche);
        anima= (AnimationDrawable) scxx_img_sche.getDrawable();

        lockInfos=new ArrayList<>();

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
            case R.id.scxx_img_serch:
                title.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);
                img2.setVisibility(View.VISIBLE);
                vehicle.setVisibility(View.VISIBLE);
                break;
            case R.id.scxx_btn_get:
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
                    list.setVisibility(View.GONE);
                    scxx_ly_sche.setVisibility(View.VISIBLE);
                    anima.start();
                    getVehicleLockInfo();
                    title.setVisibility(View.VISIBLE);
                    search.setVisibility(View.VISIBLE);
                    img1.setVisibility(View.GONE);
                    img2.setVisibility(View.GONE);
                    vehicle.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(QuerySyScxx.this,"机号输入有误或者您没有权限操作",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    //获取锁车信息
    private void getVehicleLockInfo(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("VehicleLic",vehicle.getText().toString());

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetVehicleLockInfo", proper, new WebServiceUtils.WebServiceCallBack() {
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
            list.add(soapObject.getProperty(0).toString());
            list.add(soapObject.getProperty(1).toString());
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

    public void showInfo(){
        lockInfos = new ArrayList<>();

        for(int i=0;i<info.size();i++) {
            if(i==info.size()){
                break;
            }else {
                lockInfos.add(new LockInfo(info.get(i).get(0), info.get(i).get(1)));
            }
        }

        adapterUtil=new AdapterUtil<LockInfo>(lockInfos,R.layout.query_scxx_item){
            @Override
            public void bindView(ViewHolder holder, LockInfo obj) {
                holder.setText(R.id.scxx_item_time,obj.getTime());
                holder.setText(R.id.scxx_item_info,obj.getInfo());
            }

        };
        list.setAdapter(adapterUtil);

        /*list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //这里处理点击，暂时不用adapterUtil=new AdapterUtil<LockInfo>(lockInfos,R.layout.query_scxx_item){
            @Override
            public void bindView(ViewHolder holder, LockInfo obj) {
                holder.setText(R.id.scxx_item_time,obj.getTime());
                holder.setText(R.id.scxx_item_info,obj.getInfo());
            }

        };
        list.setAdapter(adapterUtil);
            }
        });*/

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
}
