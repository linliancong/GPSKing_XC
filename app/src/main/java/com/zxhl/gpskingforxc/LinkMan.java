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

import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Administrator on 2018/3/13.
 */

public class LinkMan extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private ImgTxtLayout back;
    private TextView name;
    private TextView phone;
    private TextView email;
    private Spinner agent;

    private Button add;

    private Context context;
    private int state=0;

    private ArrayAdapter<String> adapter =null;
    private List<String> franchiserID =new ArrayList<>();
    private List<String> franchiserName =new ArrayList<>();

    private int mFranchiserPosition =0;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    if(state==1){
                        Toast.makeText(context,"添加联系人成功",Toast.LENGTH_SHORT).show();
                        finish();
                    }else {
                        Toast.makeText(context,"添加联系人失败，请稍后重试",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0x003:
                    adapter =new ArrayAdapter<String>(context, R.layout.simple_spinner_item,R.id.tv_spinner, franchiserName);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

                    agent.setAdapter(adapter);

                    agent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            mFranchiserPosition =position;

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

        getFranchiser();

        init();

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_linkman;
    }

    public void init(){
        context=LinkMan.this;

        back=findViewById(R.id.back);
        name =findViewById(R.id.name);
        phone =findViewById(R.id.phone);
        email =findViewById(R.id.email);
        agent =findViewById(R.id.agent);

        add=findViewById(R.id.add);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        add.setOnClickListener(this);

        name.addTextChangedListener(this);
        phone.addTextChangedListener(this);
        email.addTextChangedListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                    HashMap<String, String> proper = new HashMap<String, String>();
                    proper.put("LinkManName", name.getText().toString());
                    proper.put("LinkManMobile", phone.getText().toString());
                    proper.put("Email", email.getText().toString());
                    proper.put("FranchiserID", franchiserID.get(mFranchiserPosition));
                    WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "AddLinkManInfo", proper, new WebServiceUtils.WebServiceCallBack() {
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
        if(name.getText().toString().length()>0&& phone.getText().toString().length()>0&& email.getText().toString().length()>0){
            add.setEnabled(true);
        }

    }

    public void getFranchiser(){
        HashMap<String, String> proper = new HashMap<String, String>();
        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetAPPAgent", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if (result != null) {
                    List<String> list = new ArrayList<String>();
                    list = parases(result);
                    if (list != null) {
                        for (int i=0;i<list.size();i++) {
                            String[] str=list.get(i).split(":");
                            franchiserID.add(str[0]);
                            franchiserName.add(str[1]);
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
