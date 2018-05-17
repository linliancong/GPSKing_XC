package com.zxhl.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/11/29.
 */

public class NetWorkBroadcastReceiver extends BroadcastReceiver {

    private SharedPreferenceUtils sp;
    @Override
    public void onReceive(Context context, Intent intent) {
        sp=new SharedPreferenceUtils(context,Constants.SAVE_USER);
        //检测API是否小于23，23之后getNetworkInfo(int networkType)被弃用
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
            ConnectivityManager cm= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取wifi信息
            NetworkInfo wifiNet=cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动网络信息
            NetworkInfo mobileNet=cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(!wifiNet.isConnected()&& !mobileNet.isConnected()){
                sp.setIsNetworkConnect(false);
                Toast.makeText(context,"未连接到网络，请检查连接",Toast.LENGTH_SHORT).show();
            }
            else{
                sp.setIsNetworkConnect(true);
                //Toast.makeText(context,"网络已连接",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取所有的连接信息
            Network[] net = cm.getAllNetworks();
            int state = 0;
            for (int i = 0; i < net.length; i++) {
                NetworkInfo networkInfo = cm.getNetworkInfo(net[i]);
                if (networkInfo.isConnected() &&
                        (networkInfo.getTypeName().equals("WIFI")
                                || networkInfo.getTypeName().equals("MOBILE"))) {
                    state = 1;
                    break;
                }
            }

            if (state == 1) {
                sp.setIsNetworkConnect(true);
                //Toast.makeText(context, "网络已连接", Toast.LENGTH_SHORT).show();
            } else {
                sp.setIsNetworkConnect(false);
                Toast.makeText(context, "未连接到网络，请检查连接", Toast.LENGTH_SHORT).show();
            }
        }
        }
}
