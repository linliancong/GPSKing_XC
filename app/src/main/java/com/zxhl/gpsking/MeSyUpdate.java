package com.zxhl.gpsking;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.bigkoo.pickerview.OptionsPickerView;
import com.zxhl.entity.AddressCh;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.LoadingAddressChUtils;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/12/7.
 */

public class MeSyUpdate extends StatusBarUtil implements TextWatcher,View.OnClickListener {

    private ImgTxtLayout me_imgtxt_update;
    private Button me_btn_update;
    private EditText me_txt_update;

    private String content;
    private int value;

    private String mProvinceName;
    private String mCityName;
    private String mCountName;

    private Intent it;
    private MeSy meSy;
    private int tag=-1;
    private SharedPreferenceUtils sp;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    Toast.makeText(MeSyUpdate.this,"修改成功",Toast.LENGTH_SHORT).show();
                    sendBroadcast(new Intent("com.zxhl.gpsking.MYBROADCASTMESY"));
                    break;
                case 0x002:
                    Toast.makeText(MeSyUpdate.this,"修改失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.me_info);
        //AppManager.getAppManager().addActivity(MeSyUpdate.this);
        init();


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.me_info;
    }

    public void init() {
        sp=new SharedPreferenceUtils(MeSyUpdate.this, Constants.SAVE_USER);
        me_imgtxt_update = (ImgTxtLayout) findViewById(R.id.me_imgtxt_update);
        me_btn_update = (Button) findViewById(R.id.me_btn_update);
        me_txt_update = (EditText) findViewById(R.id.me_txt_update);

        it = getIntent();
        Bundle bd = it.getExtras();
        content = bd.getString("STR");
        value = bd.getInt("VALUE");
        me_txt_update.setText(content);
        me_txt_update.setSelection(content.length());

        me_txt_update.addTextChangedListener(this);
        me_btn_update.setOnClickListener(this);
        me_imgtxt_update.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switch (value) {
            case 1:
                me_imgtxt_update.setText("姓名");
                break;
            case 2:
                me_imgtxt_update.setText("公司名称");
                break;
            case 3:
                me_imgtxt_update.setText("联系电话");
                break;
            case 4:
                me_imgtxt_update.setText("QQ");
                break;
            case 5:
                me_imgtxt_update.setText("Email");
                break;
            case 6:
                me_imgtxt_update.setText("身份证号");
                break;
            case 7:
                me_imgtxt_update.setText("籍贯");
                selectAddress(1);
                break;
            case 8:
                me_imgtxt_update.setText("家庭地址");
                selectAddress(2);
                break;
            case 9:
                me_imgtxt_update.setText("家庭电话");
                break;
            case 10:
                me_imgtxt_update.setText("备注");
                break;
            default:
                break;

        }
    }

    @Override
    public void onClick(View v) {
        switch (value) {
            case 1:
                //handler.sendEmptyMessage(0x001);
                HashMap<String,String> proper=new HashMap<>();
                proper.put("State","2");
                proper.put("OperatorID",sp.getOperatorID());
                proper.put("OperatorName",me_txt_update.getText().toString());
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
                break;
            case 2:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x002,it);
                break;
            case 3:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x003,it);
                break;
            case 4:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x004,it);
                break;
            case 5:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x005,it);
                break;
            case 6:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x006,it);
                break;
            case 7:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x007,it);
                break;
            case 8:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x008,it);
                break;
            case 9:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x009,it);
                break;
            case 10:
                it.putExtra("data",me_txt_update.getText().toString());
                setResult(0x010,it);
                break;
            default:
                break;
        }
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        me_btn_update.setEnabled(false);

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (me_txt_update.getText().toString().length() != 0) {
            me_btn_update.setEnabled(true);
        }

    }

    public void selectAddress(final int tag) {
        final AddressCh addressCh= LoadingAddressChUtils.loading(getApplicationContext());
        OptionsPickerView option = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                mProvinceName = addressCh.getProvince().get(options1);
                mCityName = addressCh.getCity().get(options1).get(options2);
                mCountName = addressCh.getCounty().get(options1).get(options2).get(options3);
                if(tag==1) {
                    strReplace();
                    me_txt_update.setText(mProvinceName + "-" + mCountName);
                    me_txt_update.setSelection(me_txt_update.getText().toString().length());
                }
                else{
                    me_txt_update.setText(mProvinceName + "\t" + mCityName + "\t" +mCountName+"\t");
                    me_txt_update.setSelection(me_txt_update.getText().toString().length());
                }

            }
        }).build();
        option.setPicker(addressCh.getProvince(), addressCh.getCity(),addressCh.getCounty());
        option.show();

    }

    private void strReplace() {
        //因为中文空格比英文空格多一个，所以使用的时候需要多打一个空格才可以去除
        //mCountName=mCountName.replaceAll("  ","");
        // \s 可以匹配空格、制表符、换页符等空白字符的其中任意一个
        mCountName=mCountName.replaceAll("\\s*","");
        if(!mProvinceName.contains("特别行政区")) {
            if (mCountName.contains("县") && !mCountName.contains("自治县") && mCountName.length()>2) {
                mCountName = mCountName.replaceAll("县", "");
            }
            if (mCountName.contains("区") && mCountName.length()>2) {
                mCountName = mCountName.replaceAll("区", "");
            }
        }
        else
        {
            mProvinceName=mProvinceName.replaceAll("特别行政区","");
            mCountName = mProvinceName;
        }
        if(mProvinceName.contains("省"))
        {
            mProvinceName=mProvinceName.replaceAll("省","");
        }
        if(mProvinceName.contains("壮族自治区"))
        {
            mProvinceName=mProvinceName.replaceAll("壮族自治区","");
        }
        if(mProvinceName.contains("维吾尔自治区"))
        {
            mProvinceName=mProvinceName.replaceAll("维吾尔自治区","");
        }
        if(mProvinceName.contains("回族自治区"))
        {
            mProvinceName=mProvinceName.replaceAll("回族自治区","");
        }
        if(mProvinceName.contains("自治区"))
        {
            mProvinceName=mProvinceName.replaceAll("自治区","");
        }
    }

}
