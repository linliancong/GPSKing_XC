package com.zxhl.util;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/12/19.
 */

public class MyBroad extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            //Toast.makeText(context,"安装完成",Toast.LENGTH_SHORT).show();
            manager.cancel(1);
        }else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
            //Toast.makeText(context,"替换完成",Toast.LENGTH_SHORT).show();
            manager.cancel(1);
        }
    }
}
