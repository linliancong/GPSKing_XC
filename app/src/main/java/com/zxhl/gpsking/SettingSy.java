package com.zxhl.gpsking;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.util.ApkVersionUtils;
import com.zxhl.util.Constants;
import com.zxhl.util.DownloadService;
import com.zxhl.util.FileDownloadUtil.DownloadProgressListener;
import com.zxhl.util.FileDownloadUtil.FileDownloadered;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.WebServiceUtils;

import org.apache.http.conn.BasicEofSensorWatcher;
import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/12/1.
 */

public class SettingSy extends Fragment implements View.OnClickListener {

    private View view;
    private Context context;
    private int tag=0;

    private RelativeLayout setting_ly_vehicle;
    private RelativeLayout setting_ly_gy;
    private RelativeLayout setting_ly_jcgx;
    private RelativeLayout setting_ly_wtfk;
    private RelativeLayout setting_ly_xgmm;
    private RelativeLayout setting_ly_tc;

    //private View ad_view;
    private AlertDialog alert;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    private ShowAct showAct;

    private int verCode_s=0;
    private int verCode;

    //服务所需的变量
    private Intent intent=null;

    public SettingSy(){
    }
    @SuppressLint("ValidFragment")
    public SettingSy(Context context){
        this.context=context;
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x404:
                    Toast.makeText(context,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x0001:
                    showAct = (ShowAct) getActivity();
                    showAct.callBack(0x0001);
                    break;
                case 0x0002:
                    showAct = (ShowAct) getActivity();
                    showAct.callBack(0x0002);
                    break;
                case 0x0003:
                    showAct = (ShowAct) getActivity();
                    showAct.callBack(0x0003);
                    break;
                case 0x0004:
                    if(verCode_s!=0) {
                        if (verCode >= verCode_s) {
                            Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show();
                        } else {
                            View view = getAlert(R.layout.ad_update);
                            view.findViewById(R.id.ad_btn_update_cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alert.dismiss();
                                }
                            });

                            view.findViewById(R.id.ad_btn_update_confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //检查更新,当SDK大于等于21时即大于Android5.0时调用这个
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        intent = new Intent(context, DownloadService.class);
                                        intent.setAction("com.zxhl.util.DOWNLOADSERVICE");
                                        intent.putExtra("operator",0);
                                        getActivity().startService(intent);
                                    } else {
                                        intent = new Intent();
                                        intent.setAction("com.zxhl.util.DOWNLOADSERVICE");
                                        intent.putExtra("operator",0);
                                        getActivity().startService(intent);
                                    }
                                    alert.dismiss();
                                }
                            });
                        }
                    }
                    break;
            }
        }
    };



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(view==null) {
            view = inflater.inflate(R.layout.sy_setting, container, false);
            init();
        }
        return view;
    }

    @Override
    public void onDestroy() {
        if(intent!=null) {
            getActivity().stopService(intent);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.setting_ly_vehicle:
                Intent it0=new Intent(context,SettingSyVehicle.class);
                startActivity(it0);
                break;
            case R.id.setting_ly_gy:
                Intent it1=new Intent(context,SettingSyGy.class);
                startActivity(it1);
                break;
            case R.id.setting_ly_jcgx:
                HashMap<String,String> proper=new HashMap<String,String>();
                WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetVerCode", proper, new WebServiceUtils.WebServiceCallBack() {
                    @Override
                    public void callBack(SoapObject result) {
                        if(result!=null) {
                            List<String> list = new ArrayList<String>();
                            Integer it = new Integer(result.getProperty(0).toString());
                            verCode_s = it.intValue();
                            handler.sendEmptyMessage(0x0004);
                        }
                        else{
                            handler.sendEmptyMessage(0x404);
                        }

                    }
                });
                break;
            case R.id.setting_ly_wtfk:
                Intent it2=new Intent(context,SettingSyWtfk.class);
                startActivity(it2);
                break;
            case R.id.setting_ly_xgmm:
                View ad_view2= getAlert(R.layout.ad_input_pass);
                final EditText editText= (EditText) ad_view2.findViewById(R.id.ad_edit_pass);
                ad_view2.findViewById(R.id.ad_btn_pass_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //handler.sendEmptyMessage(0x0001);
                        alert.dismiss();
                    }
                });
                ad_view2.findViewById(R.id.ad_btn_pass_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //handler.sendEmptyMessage(0x0002);
                        SharedPreferenceUtils sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);
                        if(sp.getPWD().equals(editText.getText().toString())){
                            handler.sendEmptyMessage(0x0003);
                            alert.dismiss();
                        }
                        else {
                            alert.dismiss();
                            View view=getAlert(R.layout.ad_pass_erro);
                            TextView txt= (TextView) view.findViewById(R.id.ad_txt_erro2);
                            //String name=editText.getText().toString();
                            if(editText.getText().toString().equals("")){
                                txt.setText("原密码不能为空。");
                            }
                            view.findViewById(R.id.ad_btn_erro_confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alert.dismiss();
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.setting_ly_tc:
                View ad_view1=getAlert(R.layout.ad_exit);
                ad_view1.findViewById(R.id.ad_ly_user).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handler.sendEmptyMessage(0x0001);
                        alert.dismiss();
                    }
                });
                ad_view1.findViewById(R.id.ad_ly_exit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handler.sendEmptyMessage(0x0002);
                        alert.dismiss();
                    }
                });
                break;
        }

    }

    //初始化
    public void init(){
        verCode=ApkVersionUtils.getVerCode(context);
        setting_ly_vehicle=(RelativeLayout) view.findViewById(R.id.setting_ly_vehicle);
        setting_ly_gy= (RelativeLayout) view.findViewById(R.id.setting_ly_gy);
        setting_ly_jcgx= (RelativeLayout) view.findViewById(R.id.setting_ly_jcgx);
        setting_ly_wtfk= (RelativeLayout) view.findViewById(R.id.setting_ly_wtfk);
        setting_ly_xgmm= (RelativeLayout) view.findViewById(R.id.setting_ly_xgmm);
        setting_ly_tc= (RelativeLayout) view.findViewById(R.id.setting_ly_tc);

        setting_ly_vehicle.setOnClickListener(this);
        setting_ly_gy.setOnClickListener(this);
        setting_ly_jcgx.setOnClickListener(this);
        setting_ly_wtfk.setOnClickListener(this);
        setting_ly_xgmm.setOnClickListener(this);
        setting_ly_tc.setOnClickListener(this);
    }

    //定义接口
    public interface ShowAct{
        public void callBack(int result);
    }
    //定义接口回调
    public void getData(ShowAct act){
        act.callBack(tag);
    }

    //定义弹窗方法
    public View getAlert(int mLayout){
        View ad_view;
        //初始化Builder
        builder=new AlertDialog.Builder(context);
        //完成相关设置
        inflater=getActivity().getLayoutInflater();
        ad_view=inflater.inflate(mLayout,null,false);
        builder.setView(ad_view);
        builder.setCancelable(true);
        alert=builder.create();
        alert.show();
        return ad_view;
    }


}
