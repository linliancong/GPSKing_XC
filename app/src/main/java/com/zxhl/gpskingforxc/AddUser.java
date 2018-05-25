package com.zxhl.gpskingforxc;

import android.content.Context;
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
import android.widget.Spinner;
import android.widget.TextView;
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


/**
 * Created by Administrator on 2018/3/13.
 */

public class AddUser extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private ImgTxtLayout back;
    private TextView name;
    private TextView username;
    private TextView pwd;
    private TextView pwd2;
    private Spinner group;
    private Spinner role;

    private Button add;

    private Context context;
    private int state=0;

    private ArrayAdapter<String> adapter=null;
    private ArrayAdapter<String> adapter2=null;
    private List<String> roleID=new ArrayList<>();
    private List<String> roleName=new ArrayList<>();
    private List<String> groupID=new ArrayList<>();
    private List<String> groupName=new ArrayList<>();

    private int mRolePosition=0;
    private int mGroupPosition=0;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    if(state==1){
                        Toast.makeText(context,"注册成功",Toast.LENGTH_SHORT).show();
                        finish();
                    }else {
                        Toast.makeText(context,"注册失败，请稍后重试",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0x002:
                    adapter=new ArrayAdapter<String>(context, R.layout.simple_spinner_item,R.id.tv_spinner,roleName);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

                    role.setAdapter(adapter);

                    role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            mRolePosition=position;

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    break;
                case 0x003:
                    adapter2=new ArrayAdapter<String>(context, R.layout.simple_spinner_item,R.id.tv_spinner,groupName);
                    adapter2.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

                    group.setAdapter(adapter2);

                    group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            mGroupPosition=position;

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    break;
            }
        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.opc_user);

        getRole();
        getGroup();

        init();

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.opc_user;
    }

    public void init(){
        context=AddUser.this;

        back=findViewById(R.id.back);
        name =findViewById(R.id.name);
        username =findViewById(R.id.username);
        pwd =findViewById(R.id.pwd);
        pwd2 =findViewById(R.id.pwd2);
        group =findViewById(R.id.group);
        role =findViewById(R.id.role);

        add=findViewById(R.id.add);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        add.setOnClickListener(this);

        name.addTextChangedListener(this);
        username.addTextChangedListener(this);
        pwd.addTextChangedListener(this);
        pwd2.addTextChangedListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                if (pwd.getText().toString().equals(pwd2.getText().toString())) {
                    HashMap<String, String> proper = new HashMap<String, String>();
                    proper.put("OperatorName", name.getText().toString());
                    proper.put("NickName", username.getText().toString());
                    proper.put("Pwd", pwd2.getText().toString());
                    proper.put("GroupID", groupID.get(mGroupPosition));
                    proper.put("RoleID", roleID.get(mRolePosition));
                    WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "AddOperatorInfo", proper, new WebServiceUtils.WebServiceCallBack() {
                        @Override
                        public void callBack(SoapObject result) {
                            if (result != null) {
                                List<String> list = new ArrayList<String>();
                                Integer it = new Integer(result.getProperty(0).toString());
                                state = it.intValue();
                            } else {
                                state = 0;
                            }
                            handler.sendEmptyMessage(0x001);
                        }
                    });
                }else {
                    Toast.makeText(context,"两次输入的密码不匹配，请重新输入",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }



    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        add.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(name.getText().toString().length()>0&& username.getText().toString().length()>0&& pwd.getText().toString().length()>0
                && pwd2.getText().toString().length()>0){
            add.setEnabled(true);
        }

    }

    public void getRole(){
        HashMap<String, String> proper = new HashMap<String, String>();
        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetAPPRole", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if (result != null) {
                    List<String> list = new ArrayList<String>();
                    list = parases(result);
                    if (list != null) {
                        for (int i=0;i<list.size();i++) {
                            String[] str=list.get(i).split(":");
                            roleID.add(str[0]);
                            roleName.add(str[1]);
                        }
                        handler.sendEmptyMessage(0x002);
                    }
                }
            }
        });
    }

    public void getGroup(){
        HashMap<String, String> proper = new HashMap<String, String>();
        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetAPPGroup", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if (result != null) {
                    List<String> list = new ArrayList<String>();
                    list = parases(result);
                    if (list != null) {
                        for (int i=0;i<list.size();i++) {
                            String[] str=list.get(i).split(":");
                            groupID.add(str[0]);
                            groupName.add(str[1]);
                        }
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
}
