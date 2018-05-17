package com.zxhl.gpsking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/4.
 */

public class MeSyGd extends StatusBarUtil implements View.OnClickListener{

    private RelativeLayout me_ly_gsmc;
    private RelativeLayout me_ly_lxdh;
    private RelativeLayout me_ly_qq;
    private RelativeLayout me_ly_email;
    private RelativeLayout me_ly_sfz;
    private RelativeLayout me_ly_jg;
    private RelativeLayout me_ly_jtdz;
    private RelativeLayout me_ly_jtdh;
    private RelativeLayout me_ly_bz;


    private ImgTxtLayout me_imgtxt_gsmc;
    private ImgTxtLayout me_imgtxt_lxdh;
    private ImgTxtLayout me_imgtxt_qq;
    private ImgTxtLayout me_imgtxt_email;
    private ImgTxtLayout me_imgtxt_sfz;
    private ImgTxtLayout me_imgtxt_jg;
    private ImgTxtLayout me_imgtxt_jtdz;
    private ImgTxtLayout me_imgtxt_jtdh;
    private ImgTxtLayout me_imgtxt_bz;

    private ImgTxtLayout me_imgtxt_title;

    HashMap<String,String> map=null;
    List<Map<String,String>> listM=null;

    private Context context;
    SharedPreferenceUtils sp;
    private int tag=-1;
    private MeSy meSy;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    Toast.makeText(MeSyGd.this,"修改成功",Toast.LENGTH_SHORT).show();
                   /* homePage=new HomePage();
                    broadcastHP=homePage.new MyBroadcastHP();
                    IntentFilter filter=new IntentFilter();
                    filter.addAction("com.zxhl.gpsking.MYBROADCASTHP");
                    registerReceiver(broadcastHP,filter);*/
                    //sendBroadcast(new Intent("com.zxhl.gpsking.MYBROADCASTHP"));
                    break;
                case 0x002:
                    Toast.makeText(MeSyGd.this,"修改失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.me_gd);
        context=getApplicationContext();

        init();
        getIntents();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.me_gd;
    }

    public void init(){
        sp=new SharedPreferenceUtils(MeSyGd.this, Constants.SAVE_USER);
        me_ly_gsmc= (RelativeLayout) findViewById(R.id.me_ly_gsmc);
        me_ly_lxdh= (RelativeLayout) findViewById(R.id.me_ly_lxdh);
        me_ly_qq= (RelativeLayout) findViewById(R.id.me_ly_qq);
        me_ly_email= (RelativeLayout) findViewById(R.id.me_ly_email);
        me_ly_sfz= (RelativeLayout) findViewById(R.id.me_ly_sfz);
        me_ly_jg= (RelativeLayout) findViewById(R.id.me_ly_jg);
        me_ly_jtdz= (RelativeLayout) findViewById(R.id.me_ly_jtdz);
        me_ly_jtdh= (RelativeLayout) findViewById(R.id.me_ly_jtdh);
        me_ly_bz= (RelativeLayout) findViewById(R.id.me_ly_bz);

        me_imgtxt_gsmc= (ImgTxtLayout) findViewById(R.id.me_imgtxt_gsmc);
        me_imgtxt_lxdh= (ImgTxtLayout) findViewById(R.id.me_imgtxt_lxdh);
        me_imgtxt_qq= (ImgTxtLayout) findViewById(R.id.me_imgtxt_qq);
        me_imgtxt_email= (ImgTxtLayout) findViewById(R.id.me_imgtxt_email);
        me_imgtxt_sfz= (ImgTxtLayout) findViewById(R.id.me_imgtxt_sfz);
        me_imgtxt_jg= (ImgTxtLayout) findViewById(R.id.me_imgtxt_jg);
        me_imgtxt_jtdz= (ImgTxtLayout) findViewById(R.id.me_imgtxt_jtdz);
        me_imgtxt_jtdh= (ImgTxtLayout) findViewById(R.id.me_imgtxt_jtdh);
        me_imgtxt_bz= (ImgTxtLayout) findViewById(R.id.me_imgtxt_bz);
        me_imgtxt_title= (ImgTxtLayout) findViewById(R.id.me_imgtxt_title);

        me_ly_gsmc.setOnClickListener(this);
        me_ly_lxdh.setOnClickListener(this);
        me_ly_qq.setOnClickListener(this);
        me_ly_email.setOnClickListener(this);
        me_ly_sfz.setOnClickListener(this);
        me_ly_jg.setOnClickListener(this);
        me_ly_jtdz.setOnClickListener(this);
        me_ly_jtdh.setOnClickListener(this);
        me_ly_bz.setOnClickListener(this);
        me_imgtxt_title.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,String> proper=new HashMap<>();
                proper.put("OperatorID",sp.getOperatorID());

                proper.put("Unit",me_imgtxt_gsmc.getText().toString());
                proper.put("ConnectTel",me_imgtxt_lxdh.getText().toString());
                proper.put("QQ",me_imgtxt_qq.getText().toString());
                proper.put("Email",me_imgtxt_email.getText().toString());
                proper.put("IDCard",me_imgtxt_sfz.getText().toString());
                proper.put("NativePlace",me_imgtxt_jg.getText().toString());
                proper.put("HomeAddress",me_imgtxt_jtdz.getText().toString());
                proper.put("HomePhone",me_imgtxt_jtdh.getText().toString());
                proper.put("Remark",me_imgtxt_bz.getText().toString());
                proper.put("State","1");

                WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "SetOperatorInfo", proper, new WebServiceUtils.WebServiceCallBack() {
                    @Override
                    public void callBack(SoapObject result) {
                        if(result!=null){
                            Integer it=new Integer(result.getProperty(0).toString());
                            tag=it.intValue();
                        }

                        if(tag==0){
                            handler.sendEmptyMessage(0x001);
                        }
                        else
                            handler.sendEmptyMessage(0x002);

                    }
                });
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.me_ly_gsmc:
                Intent it_gsmc=new Intent();
                it_gsmc.setClass(context,MeSyUpdate.class);
                Bundle bd_gsmc=new Bundle();
                bd_gsmc.putString("STR",map.get("公司名称"));
                bd_gsmc.putInt("VALUE",2);
                it_gsmc.putExtras(bd_gsmc);
                startActivityForResult(it_gsmc,0x002);
                break;
            case R.id.me_ly_lxdh:
                Intent it_lxdh=new Intent();
                it_lxdh.setClass(context,MeSyUpdate.class);
                Bundle bd_lxdh=new Bundle();
                bd_lxdh.putString("STR",map.get("联系电话"));
                bd_lxdh.putInt("VALUE",3);
                it_lxdh.putExtras(bd_lxdh);
                startActivityForResult(it_lxdh,0x003);
                break;
            case R.id.me_ly_qq:
                Intent it_qq=new Intent();
                it_qq.setClass(context,MeSyUpdate.class);
                Bundle bd_qq=new Bundle();
                bd_qq.putString("STR",map.get("QQ"));
                bd_qq.putInt("VALUE",4);
                it_qq.putExtras(bd_qq);
                startActivityForResult(it_qq,0x004);
                break;
            case R.id.me_ly_email:
                Intent it_email=new Intent();
                it_email.setClass(context,MeSyUpdate.class);
                Bundle bd_email=new Bundle();
                bd_email.putString("STR",map.get("Email"));
                bd_email.putInt("VALUE",5);
                it_email.putExtras(bd_email);
                startActivityForResult(it_email,0x005);
                break;
            case R.id.me_ly_sfz:
                Intent it_sfz=new Intent();
                it_sfz.setClass(context,MeSyUpdate.class);
                Bundle bd_sfz=new Bundle();
                bd_sfz.putString("STR",map.get("身份证号码"));
                bd_sfz.putInt("VALUE",6);
                it_sfz.putExtras(bd_sfz);
                startActivityForResult(it_sfz,0x006);
                break;
            case R.id.me_ly_jg:
                Intent it_jg=new Intent();
                it_jg.setClass(context,MeSyUpdate.class);
                Bundle bd_jg=new Bundle();
                bd_jg.putString("STR",map.get("籍贯"));
                bd_jg.putInt("VALUE",7);
                it_jg.putExtras(bd_jg);
                startActivityForResult(it_jg,0x007);
                break;
            case R.id.me_ly_jtdz:
                Intent it_jtdz=new Intent();
                it_jtdz.setClass(context,MeSyUpdate.class);
                Bundle bd_jtdz=new Bundle();
                bd_jtdz.putString("STR",map.get("家庭地址"));
                bd_jtdz.putInt("VALUE",8);
                it_jtdz.putExtras(bd_jtdz);
                startActivityForResult(it_jtdz,0x008);
                break;
            case R.id.me_ly_jtdh:
                Intent it_jtdh=new Intent();
                it_jtdh.setClass(context,MeSyUpdate.class);
                Bundle bd_jtdh=new Bundle();
                bd_jtdh.putString("STR",map.get("家庭电话"));
                bd_jtdh.putInt("VALUE",9);
                it_jtdh.putExtras(bd_jtdh);
                startActivityForResult(it_jtdh,0x009);
                break;
            case R.id.me_ly_bz:
                Intent it_bz=new Intent();
                it_bz.setClass(context,MeSyUpdate.class);
                Bundle bd_bz=new Bundle();
                bd_bz.putString("STR",map.get("备注"));
                bd_bz.putInt("VALUE",10);
                it_bz.putExtras(bd_bz);
                startActivityForResult(it_bz,0x010);
                break;
            default:break;
        }

    }

    public void getIntents(){
        Intent it=getIntent();
        Bundle bd=it.getExtras();
        ArrayList list=bd.getParcelableArrayList("list");
        ArrayList list1= (ArrayList) list.get(0);
        map= (HashMap<String, String>) list1.get(0);

        me_imgtxt_gsmc.setText(map.get("公司名称"));
        me_imgtxt_lxdh.setText(map.get("联系电话"));
        me_imgtxt_qq.setText(map.get("QQ"));
        me_imgtxt_email.setText(map.get("Email"));
        me_imgtxt_sfz.setText(map.get("身份证号码"));
        me_imgtxt_jg.setText(map.get("籍贯"));
        me_imgtxt_jtdz.setText(map.get("家庭地址"));
        me_imgtxt_jtdh.setText(map.get("家庭电话"));
        me_imgtxt_bz.setText(map.get("备注"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0x002 && resultCode==0x002){
            me_imgtxt_gsmc.setText(data.getExtras().getString("data"));
        }
        else if(requestCode==0x003 && resultCode==0x003){
            me_imgtxt_lxdh.setText(data.getExtras().getString("data"));
        }
        else if(requestCode==0x004 && resultCode==0x004){
            me_imgtxt_qq.setText(data.getExtras().getString("data"));
        }
        else if(requestCode==0x005 && resultCode==0x005){
            me_imgtxt_email.setText(data.getExtras().getString("data"));
        }
        else if(requestCode==0x006 && resultCode==0x006){
            me_imgtxt_sfz.setText(data.getExtras().getString("data"));
        }
        else if(requestCode==0x007 && resultCode==0x007){
            me_imgtxt_jg.setText(data.getExtras().getString("data"));
        }
        else if(requestCode==0x008 && resultCode==0x008){
            me_imgtxt_jtdz.setText(data.getExtras().getString("data"));
        }
        else if(requestCode==0x009 && resultCode==0x009){
            me_imgtxt_jtdh.setText(data.getExtras().getString("data"));
        }
        else if(requestCode==0x010 && resultCode==0x010){
            me_imgtxt_bz.setText(data.getExtras().getString("data"));
        }

    }

}
