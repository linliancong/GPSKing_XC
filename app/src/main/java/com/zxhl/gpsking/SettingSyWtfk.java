package com.zxhl.gpsking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

/**
 * Created by Administrator on 2017/12/15.
 */

public class SettingSyWtfk extends StatusBarUtil implements TextWatcher{

    private ImgTxtLayout setting_imgtxt_back_wtfk;
    private Button setting_btn_send_wtfk;
    private EditText setting_edit_question_wtfk;
    private EditText setting_edit_lxfs_wtfk;
    private TextView setting_txt_fkjl_wtfk;
    private Spinner setting_spinner_wtfk;

    private ArrayAdapter<String> adapter=null;
    private int tag;

    private SharedPreferenceUtils sp;
    private int state=0;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==0x001){
                if(state==1){
                    //success
                    Toast.makeText(getApplicationContext(),"反馈成功",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    //faild
                    Toast.makeText(getApplicationContext(),"反馈失败，请稍后再试",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.setting_wtfk);

        init();

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setting_wtfk;
    }

    private void init() {
        sp=new SharedPreferenceUtils(SettingSyWtfk.this, Constants.SAVE_USER);
        setting_imgtxt_back_wtfk= (ImgTxtLayout) findViewById(R.id.setting_imgtxt_back_wtfk);
        setting_btn_send_wtfk= (Button) findViewById(R.id.setting_btn_send_wtfk);
        setting_edit_question_wtfk= (EditText) findViewById(R.id.setting_edit_question_wtfk);
        setting_edit_lxfs_wtfk= (EditText) findViewById(R.id.setting_edit_lxfs_wtfk);
        setting_txt_fkjl_wtfk= (TextView) findViewById(R.id.setting_txt_fkjl_wtfk);
        setting_spinner_wtfk= (Spinner) findViewById(R.id.setting_spinner_wtfk);


        adapter=new ArrayAdapter<String>(this,R.layout.simple_spinner_item,R.id.tv_spinner);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapter.add("很少出现");
        adapter.add("一天一次");
        adapter.add("一天多次");
        adapter.add("始终出现");

        setting_edit_question_wtfk.addTextChangedListener(this);
        setting_edit_lxfs_wtfk.addTextChangedListener(this);

        setting_spinner_wtfk.setAdapter(adapter);

        setting_spinner_wtfk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //ArrayAdapter<String> adp= (ArrayAdapter<String>) parent.getAdapter();
                //Toast.makeText(getApplicationContext(),adp.getItem(position),Toast.LENGTH_SHORT).show();
                tag=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setting_imgtxt_back_wtfk.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setting_txt_fkjl_wtfk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"点击了反馈记录",Toast.LENGTH_SHORT).show();
                Intent intent1=new Intent(SettingSyWtfk.this,SettingSyWtfkJl.class);
                startActivity(intent1);
            }
        });

        //在这里处理提交
        setting_btn_send_wtfk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowKeyboard.hideKeyboard(setting_edit_question_wtfk);
                ShowKeyboard.hideKeyboard(setting_edit_lxfs_wtfk);
                Integer it=new Integer(tag);
                HashMap<String,String> proper=new HashMap<String,String>();
                proper.put("OperatorID",sp.getOperatorID());
                proper.put("NickName",sp.getNickName());
                proper.put("Frequency",it.toString());
                proper.put("Contact",setting_edit_lxfs_wtfk.getText().toString());
                proper.put("Text",setting_edit_question_wtfk.getText().toString());
                WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "SendQuestion", proper, new WebServiceUtils.WebServiceCallBack() {
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

            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        setting_btn_send_wtfk.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(setting_edit_question_wtfk.getText().length()!=0&&setting_edit_lxfs_wtfk.getText().length()!=0){
            setting_btn_send_wtfk.setEnabled(true);
        }

    }
}
