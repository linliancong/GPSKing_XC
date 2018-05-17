package com.zxhl.gpsking;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.entity.LinkMan;
import com.zxhl.entity.VehicleAlarm;
import com.zxhl.util.Constants;
import com.zxhl.util.EmailUtil.MailSendInfo;
import com.zxhl.util.EmailUtil.MailSenderUtils;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/2/2.
 */

public class SendAlarmInfo extends StatusBarUtil implements View.OnClickListener,RadioGroup.OnCheckedChangeListener{

    private ImgTxtLayout back;
    private Button send;
    private Button close;
    public TextView Mobile;
    public TextView DeviceNum;
    public TextView MModelName;
    public TextView VehicleLic;
    public TextView GPSDateTime;
    public TextView Position;
    public TextView OperatorName;
    public TextView DealType;
    public TextView OwnerName;
    public TextView GroupName;
    public String FranchiserID="";
    public String VehicleID="";

    private ArrayList<LinkMan> linkMan;
    public ArrayList<VehicleAlarm> vehicleAlarms;
    private Context context;

    private int isLinkMan=0;
    private String msg="";
    private int sendType=1;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private View view;

    private static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private static final String KEY_PHONENUM = "phone_num";

    private SharedPreferenceUtils sp;
    private int tag=-1;
    private Intent intent;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    break;
                case 0x002:
                    Toast.makeText(context,"邮件发送成功",Toast.LENGTH_SHORT).show();
                    saveAlarmInfo();
                    break;
                case 0x003:
                    Toast.makeText(context,"邮件发送失败",Toast.LENGTH_SHORT).show();
                    break;
                case 0x004:
                    Toast.makeText(context,"短信发送成功",Toast.LENGTH_SHORT).show();
                    saveAlarmInfo();
                    break;
                case 0x005:
                    Toast.makeText(context,"短信发送失败",Toast.LENGTH_SHORT).show();
                    break;
                case 0x006:
                    //处理成功
                    intent.putExtra("state","2");
                    setResult(0x001,intent);
                    break;
                case 0x007:
                    //处理失败
                    intent.putExtra("state","1");
                    setResult(0x001,intent);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        getData();

        getLinkMan();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.send_alarminfo;
    }

    private void init() {

        context=SendAlarmInfo.this;
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);
        view=getAlert(R.layout.ad_select_send);
        back=findViewById(R.id.alarm_imgtxt_title);
        send=findViewById(R.id.alarm_send);
        close=findViewById(R.id.alarm_close);
        Mobile=findViewById(R.id.alarm_mobile);
        DeviceNum=findViewById(R.id.alarm_gps);
        MModelName=findViewById(R.id.alarm_model);
        VehicleLic=findViewById(R.id.alarm_vehicle);
        GPSDateTime=findViewById(R.id.alarm_time);
        Position=findViewById(R.id.alarm_position);
        OperatorName=findViewById(R.id.alarm_operator);
        DealType=findViewById(R.id.alarm_isdipose);
        OwnerName=findViewById(R.id.alarm_owner);
        GroupName=findViewById(R.id.alarm_group);
        linkMan=new ArrayList<>();

        close.setOnClickListener(this);
        send.setOnClickListener(this);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.alarm_close:
                finish();
                break;
            case R.id.alarm_send:
                //发送信息
                if(isLinkMan==0){
                    Toast.makeText(context,"没有找到联系人，请联系管理员添加联系人",Toast.LENGTH_SHORT).show();
                }
                else {
                    dialog.show();
                    msg= "众星互联GPS服务中心：【"+OwnerName.getText().toString()+"】" +
                            "【"+ VehicleLic.getText().toString()+"】" +
                            "机器于【"+GPSDateTime.getText().toString()+"】在" +
                            "【"+Position.getText().toString()+"】" +
                            "发生断电报警";
                }
                break;
            case R.id.ad_btn_send_cancel:
                dialog.dismiss();
                break;
            case R.id.ad_btn_send_confirm:
                if(sendType==1){
                    //1.发送电子邮件:
                    ArrayList<String> email=new ArrayList<String>();
                    for (int i=0;i<linkMan.size();i++) {
                        email.add(linkMan.get(i).getEmail());
                    }
                    sendEmail(email);

                    //测试
                    /*email.add("370329258@qq.com");
                    email.add("1070335034@qq.com");
                    sendEmail(email);*/
                }else if(sendType==2){
                    //2.发送短信（打开系统短信）
                    /*String phones="smsto:";
                    for (int i=0;i<linkMan.size();i++) {
                        if(linkMan.size()==1) {
                            phones += linkMan.get(i).getLinkManMobile();
                        }else {
                            phones=phones+";"+linkMan.get(i).getLinkManMobile();
                        }
                    }
                    Uri uri = Uri.parse(phones);
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    intent.putExtra("sms_body",msg);
                    startActivity(intent);
                    */

                    //2.发送短信（打开短信接口）
                    for (int i=0;i<linkMan.size();i++) {
                        sendSMS(linkMan.get(i).getLinkManMobile(),msg);
                    }

                    //测试
                    /*ArrayList<String> phone=new ArrayList<>();
                    phone.add("15860742081");
                    phone.add("15959291668");
                    for (int i=0;i<phone.size();i++) {
                        sendSMS(phone.get(i),msg);
                    }*/
                }
                dialog.dismiss();
                break;

        }

    }

    private void getData() {
        intent=getIntent();
        Bundle bd=intent.getExtras();
        vehicleAlarms= (ArrayList<VehicleAlarm>) bd.getSerializable("info");

        Mobile.setText(vehicleAlarms.get(0).getMobile());
        DeviceNum.setText(vehicleAlarms.get(0).getDeviceNum());
        MModelName.setText(vehicleAlarms.get(0).getMModelName());
        VehicleLic.setText(vehicleAlarms.get(0).getVehicleLic());
        GPSDateTime.setText(vehicleAlarms.get(0).getGPSDateTime());
        Position.setText(vehicleAlarms.get(0).getPosition());
        OperatorName.setText(vehicleAlarms.get(0).getOperatorName());
        OwnerName.setText(vehicleAlarms.get(0).getOwnerName());
        GroupName.setText(vehicleAlarms.get(0).getGroupName());
        if(vehicleAlarms.get(0).getDealType().equals("1")){
            DealType.setText("未处理");
        }else if(vehicleAlarms.get(0).getDealType().equals("2")) {
            DealType.setText("正在处理");
        }else if(vehicleAlarms.get(0).getDealType().equals("3")){
            DealType.setText("恢复正常");
        }
        FranchiserID=vehicleAlarms.get(0).getFranchiserID();
        VehicleID=vehicleAlarms.get(0).getVehicleID();
    }

    //获取联系人信息
    private void getLinkMan(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("FranchiserID",FranchiserID);

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetLinkMan", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null) {
                    ArrayList<LinkMan> link = new ArrayList<>();
                    link = parases(result);
                    if (link != null) {
                        linkMan = link;
                        isLinkMan = 1;
                        //handler.sendEmptyMessage(0x001);
                    }
                }
            }
        });
    }

    //发送信息记录
    private void saveAlarmInfo(){
        HashMap<String,String> proper=new HashMap<>();
        proper.put("OperatorID",sp.getOperatorID());
        proper.put("NoticeMode","2");
        proper.put("NoticeContent",msg);
        proper.put("FranchiserID",FranchiserID);
        proper.put("EmailTitle","断电报警通知");
        proper.put("UpFilePath","");
        if(sendType==1) {
            proper.put("Remark", "APP邮件");
        }else {
            proper.put("Remark", "APP短信");
        }
        proper.put("VehicleID",VehicleID);
        proper.put("BeginTime",GPSDateTime.getText().toString());

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "SaveAlarmInfo", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null) {
                    Integer it = new Integer(result.getProperty(0).toString());
                    tag = it.intValue();
                }

                if(tag==0){
                    handler.sendEmptyMessage(0x006);
                }
                else
                    handler.sendEmptyMessage(0x007);

            }
        });
    }

    private ArrayList<LinkMan> parases(SoapObject result){
        ArrayList<LinkMan> link=new ArrayList<>();
        SoapObject soap= (SoapObject) result.getProperty(0);
        if(soap==null) {
            return null;
        }
        for (int i=0;i<soap.getPropertyCount();i++){
            SoapObject soapObject= (SoapObject) soap.getProperty(i);
            link.add(new LinkMan(soapObject.getProperty("LinkManName").toString(),
                    soapObject.getProperty("LinkManMobile").toString(),
                    soapObject.getProperty("Email").toString()));
        }
        return link;
    }

    public void sendEmail(final ArrayList<String> email){
        //设置发送邮件的相关信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                MailSendInfo mailinfo=new MailSendInfo();
                mailinfo.setMailServerHost("smtp.163.com");
                mailinfo.setMailServerPort("25");
                mailinfo.setValidate(true);
                mailinfo.setUserName("gps579@163.com");
                mailinfo.setPassword("5935092");
                mailinfo.setFromAddress("gps579@163.com");
                //接收者
                mailinfo.setToAddress(email);
                //标题
                mailinfo.setSubject("众星互联GPS服务中心");
                mailinfo.setContent(msg);
                //发送邮件
                MailSenderUtils sms=new MailSenderUtils();
                boolean isSuccess=sms.sendTextMail(mailinfo);
                if(isSuccess){
                    handler.sendEmptyMessage(0x002);
                }else {
                    handler.sendEmptyMessage(0x003);
                }

            }
        }).start();
    }

    /**
     * 直接调用短信接口发短信
     *
     * @param phoneNumber
     * @param message
     */
    public void sendSMS(String phoneNumber, String message) {
        //处理返回的发送状态
        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent,
                0);
        // register the Broadcast Receivers
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        handler.sendEmptyMessage(0x004);
                        break;
                    /*case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        break;*/
                    default:
                        handler.sendEmptyMessage(0x005);
                        break;
                }
            }
        }, new IntentFilter(SENT_SMS_ACTION));


        //处理返回的接收状态
        String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
        // create the deilverIntent parameter
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0,
                deliverIntent, 0);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                /*Toast.makeText(context,
                        "收信人已经成功接收", Toast.LENGTH_SHORT)
                        .show();*/
            }
        }, new IntentFilter(DELIVERED_SMS_ACTION));


        // 获取短信管理器
        android.telephony.SmsManager smsManager = android.telephony.SmsManager
                .getDefault();
        // 拆分短信内容（手机短信长度限制）
        if (message.length() > 70) {
            List<String> divideContents = smsManager.divideMessage(message);
            for (String text : divideContents) {
                smsManager.sendTextMessage(phoneNumber, null, text, sentPI, deliverPI);
            }
        }else {
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
        }
    }


    //定义弹窗方法
    public View getAlert(int mLayout){
        View ad_view;
        //初始化Builder
        builder=new AlertDialog.Builder(context);
        //完成相关设置
        inflater=getLayoutInflater();
        ad_view=inflater.inflate(mLayout,null,false);
        builder.setView(ad_view);
        builder.setCancelable(true);
        dialog=builder.create();
        RadioGroup rg= ad_view.findViewById(R.id.ad_rg);
        rg.setOnCheckedChangeListener(this);
        ad_view.findViewById(R.id.ad_btn_send_cancel).setOnClickListener(this);
        ad_view.findViewById(R.id.ad_btn_send_confirm).setOnClickListener(this);
        return ad_view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton rb1=view.findViewById(R.id.email);
        RadioButton rb2=view.findViewById(R.id.sms);
        switch (checkedId){
            case R.id.email:
                sendType=1;
                rb1.setChecked(true);
                break;
            case R.id.sms:
                sendType=2;
                rb2.setChecked(true);
                break;
        }

    }
}
