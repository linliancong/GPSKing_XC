package com.zxhl.gpsking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zxhl.util.AppManager;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/12/15.
 */

public class SettingSyPWD extends StatusBarUtil implements TextWatcher{

    private ImgTxtLayout setting_imgtxt_update_pwd;
    private Button setting_btn_update_pwd;
    private EditText setting_edit_user_pwd;
    private EditText setting_edit_newpass_pwd;
    private EditText setting_edit_newpass2_pwd;

    private SharedPreferenceUtils sp;
    private Context mContext;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private View view;

    private TextView txt;

    private long state=0;


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    if(state==1){
                        //密码更改成功
                        dialog.show();
                        txt=(TextView) view.findViewById(R.id.ad_txt_erro2);
                        txt.setText("密码修改成功，请返回重新登录。");
                    }
                    else{
                        //更改密码失败
                        dialog.show();
                        txt=(TextView) view.findViewById(R.id.ad_txt_erro2);
                        txt.setText("密码修改失败，请稍后再试。");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.setting_passwd);

        mContext=SettingSyPWD.this;
        sp=new SharedPreferenceUtils(mContext, Constants.SAVE_USER);

        //提示信息相关设置
        builder=new AlertDialog.Builder(mContext);
        inflater=getLayoutInflater();
        view=inflater.inflate(R.layout.ad_pass_erro,null,false);
        builder.setView(view);
        dialog=builder.create();

        setting_imgtxt_update_pwd= (ImgTxtLayout) findViewById(R.id.setting_imgtxt_update_pwd);
        setting_btn_update_pwd= (Button) findViewById(R.id.setting_btn_update_pwd);
        setting_edit_user_pwd= (EditText) findViewById(R.id.setting_edit_user_pwd);
        setting_edit_newpass_pwd=(EditText) findViewById(R.id.setting_edit_newpass_pwd);
        setting_edit_newpass2_pwd=(EditText) findViewById(R.id.setting_edit_newpass2_pwd);

        setting_edit_newpass_pwd.addTextChangedListener(this);
        setting_edit_newpass2_pwd.addTextChangedListener(this);

        setting_edit_user_pwd.setText(sp.getNickName());

        setting_btn_update_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setting_edit_newpass_pwd.getText().toString().equals(setting_edit_newpass2_pwd.getText().toString()))
                {
                    if(setting_edit_newpass_pwd.getText().toString().equals(sp.getPWD()))
                    {
                        dialog.show();
                        txt=(TextView) view.findViewById(R.id.ad_txt_erro2);
                        txt.setText("新密码和原密码相同，请重新输入。");
                    }
                    //在这里做修改密码的操作
                    else{
                        HashMap<String,String> proper=new HashMap<String,String>();
                        proper.put("OperatorID",sp.getOperatorID());
                        proper.put("PWD",setting_edit_newpass_pwd.getText().toString());
                        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "ChangePassword", proper, new WebServiceUtils.WebServiceCallBack() {
                            @Override
                            public void callBack(SoapObject result) {
                                if(result!=null) {
                                    List<String> list = new ArrayList<String>();
                                    Integer it=new Integer(result.getProperty(0).toString());
                                    state =it.intValue();
                                }
                                else
                                {
                                    state =0;
                                }
                                handler.sendEmptyMessage(0x001);
                            }
                        });
                        //finish();
                    }
                }
                else{
                    dialog.show();
                    txt=(TextView) view.findViewById(R.id.ad_txt_erro2);
                    txt.setText("两次输入的密码不相同，请重新输入。");
                }

            }
        });

        setting_imgtxt_update_pwd.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        view.findViewById(R.id.ad_btn_erro_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state==1){
                    Intent intent=new Intent(SettingSyPWD.this,Login.class);
                    startActivity(intent);
                    finish();
                    AppManager.getAppManager().finishActivity();
                }
                dialog.dismiss();
            }
        });


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setting_passwd;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        setting_btn_update_pwd.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(setting_edit_newpass_pwd.getText().toString().length()!=0 && setting_edit_newpass2_pwd.getText().toString().length()!=0){
            setting_btn_update_pwd.setEnabled(true);
        }

    }

}
