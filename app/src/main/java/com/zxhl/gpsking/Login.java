package com.zxhl.gpsking;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.zxhl.util.ApkVersionUtils;
import com.zxhl.util.AppManager;
import com.zxhl.util.Constants;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.StreamHandler;


/**
 * Created by Administrator on 2017/11/23.
 */

public class Login extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private Button btn_login;
    private MaterialEditText user;
    private MaterialEditText passwd;
    private long time=0;

    SharedPreferenceUtils sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.login);
        //AppManager.getAppManager().addActivity(Login.this);

        initView();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.login;
    }

    //登录就获取保存的账号密码
    @Override
    protected void onStart() {
        super.onStart();

        sp=new SharedPreferenceUtils(this,Constants.SAVE_USER);
        if(!sp.getIsFirst()) {
            user.setText(sp.getNickName());
            passwd.setText(sp.getPWD());
        }
        else
        {
            user.setText(sp.getNickName());
        }
    }

    //退出按钮
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()-time>2000) {
            Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
            time=System.currentTimeMillis();
        }
        else {
            super.onBackPressed();
        }
    }

    public void initView() {
        btn_login= (Button) findViewById(R.id.btn_login);
        user= (MaterialEditText) findViewById(R.id.editText_username);
        passwd= (MaterialEditText) findViewById(R.id.editText_password);

        btn_login.setOnClickListener(this);
        user.addTextChangedListener(this);
        passwd.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_login:
                if(!sp.getIsNetworkConnect())
                {
                    Toast.makeText(Login.this,"网络不可用，请检查连接",Toast.LENGTH_SHORT).show();
                }
                else {
                    submit();
                }
                break;
            default:
                break;
        }

    }

    private void submit(){
        final String username=user.getText().toString();
        final String pwd=passwd.getText().toString();

        HashMap<String,String> proper=new HashMap<String,String>();
        SharedPreferenceUtils utils;

        final PopupWindow pop=new PopupWindow();
        //设置宽高
        pop.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //设置焦点
        pop.setFocusable(true);
        View view= LayoutInflater.from(this).inflate(R.layout.pop_content,null);
        //设置显示的view
        pop.setContentView(view);
        //设置显示的位置
        pop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER,0,0);

        proper.put("NickName",username);
        proper.put("PWD",pwd);
        proper.put("Version", "GPSKing_"+ApkVersionUtils.getVerName(Login.this));
        //调用WebService接口
        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "LoginForGPSKing", proper, new WebServiceUtils.WebServiceCallBack() {
            //WebService接口返回的数据回调到这个方法中
            @Override
            public void callBack(SoapObject result) {
                SharedPreferenceUtils utils=new SharedPreferenceUtils(Login.this, Constants.SAVE_USER);
                if(result!=null)
                {
                    List<String> list=new ArrayList<String>();
                    list= parseSoap(result);
                    if(list.size()==0)
                    {
                        pop.dismiss();
                        utils.setIsFirst(true);
                        Toast.makeText(Login.this,"账号或密码错误！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //保存用户信息
                    utils.setNickName(username);
                    utils.setPWD(pwd);
                    utils.setOperatorID(list.get(0));
                    utils.setOperatorName(list.get(1));
                    utils.setVGroupID(list.get(2));
                    utils.setRolePermission(list.get(3));
                    utils.setRoleID(list.get(4));
                    utils.setIsFirst(false);
                    Intent it=new Intent(Login.this,HomePage.class);
                    startActivity(it);
                    pop.dismiss();
                    finish();
                    Toast.makeText(Login.this,"登录成功",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    pop.dismiss();
                    utils.setIsFirst(true);
                    Toast.makeText(Login.this,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /*
    *解析SoapObject对象
    *@param result
    * @return
    * */
    private List<String> parseSoap(SoapObject result)
    {
        List<String> list=new ArrayList<String>();
        SoapObject soapObject= (SoapObject) result.getProperty("LoginForGPSKingResult");
        if (soapObject==null)
        {
            return null;
        }
        for (int i=0;i<soapObject.getPropertyCount();i++)
        {
            list.add(soapObject.getProperty(i).toString());
        }
        return list;
    }

    /*
    *
    * 这三个方法都是处理文本框输入改变后的操作
    *
    * */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        btn_login.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(user.getText().toString().length()!=0&&passwd.getText().toString().length()!=0)
        {
            btn_login.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
