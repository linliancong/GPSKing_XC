package com.zxhl.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.zxhl.gpsking.R;
import com.zxhl.gpsking.SettingSy;
import com.zxhl.util.FileDownloadUtil.DownloadProgressListener;
import com.zxhl.util.FileDownloadUtil.FileDownloadered;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2017/12/19.
 */

public class DownloadService extends Service{

    private boolean quit=false;
    private boolean stop=false;
    private boolean state=false;
    private myBinder binder=new myBinder();
    private DownloadService dl;

    private int MaxPro;
    private File file=null;
    private NotificationManager manager=null;
    private NotificationCompat.Builder bd=null;

    private Intent intent;

    private AudioManager mAudio;
    private int status=-1;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    //stop();
                    if(!quit) {
                        int size = msg.getData().getInt("size");
                        float num = (float) size / MaxPro;
                        int result = (int) (num * 100);
                        bd.setProgress(100, result, false);
                        bd.setContentTitle("正在下载");
                        bd.setContentText("下载" + result + "%");
                        intent.putExtra("operator",2);
                        PendingIntent pendingIntent=PendingIntent.getService(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                        bd.setContentIntent(pendingIntent);
                        manager.notify(1, bd.build());
                        if (MaxPro == size) {
                            //Toast.makeText(context,"文件下载成功",Toast.LENGTH_SHORT).show();
                            update();
                            //manager.cancel(1);
                        }
                    }
                    break;
                case 0x002:
                    manager.cancel(1);
                    bd.setContentText("点击重新下载");
                    bd.setContentTitle("下载失败");
                    intent.putExtra("operator",1);
                    PendingIntent pendingIntent=PendingIntent.getService(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    bd.setContentIntent(pendingIntent);
                    bd.setProgress(0,0,false);
                    manager.notify(1,bd.build());
                    state=false;
                    break;
            }
        }
    };

    public class myBinder extends Binder{
        public boolean getStop()
        {
            return stop;
        }
        public boolean getQuit()
        {
            return quit;
        }
        public boolean getState(){
            return state;
        }
    }


    //绑定的时候调用
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //创建的时候
    @Override
    public void onCreate() {
        super.onCreate();
        //只有当Android版本为O之后才需要控制系统声音
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            status = mAudio.getRingerMode();
            switch (status){
                case AudioManager.RINGER_MODE_NORMAL:
                    //普通模式
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    //振动模式
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    //静音模式
                    break;
            }
        }
        /*//以下语句的设置是为了解决在Android7.0以后安装apk的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }*/
        getNotification();
        dl=new DownloadService();
        //download();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(DownloadService.this, DownloadService.class);
            intent.setAction("com.zxhl.util.DOWNLOADSERVICE");

        } else {
            intent = new Intent();
            intent.setAction("com.zxhl.util.DOWNLOADSERVICE");

        }
    }

    //start调用的时候开启
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            //当前Android O版本不能控制声音，故而先用系统的方法，将声音调成振动
            if(status==AudioManager.RINGER_MODE_NORMAL) {
                mAudio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        }
        if (intent.getIntExtra("operator",-1)==0)
        {
            download();
        }
        else if(intent.getIntExtra("operator",-1)==1)
        {
            //this.quit=false;
            //exit();
            //manager.cancel(1);
            download();
        }
        else if (intent.getIntExtra("operator",-1)==2)
        {
            /*if(state){
                download();
            }
            else {
                exit();
                this.state=true;
            }*/
            exit();
            this.quit=true;

            bd.setContentText("点击继续下载");
            bd.setContentTitle("暂停下载");
            intent.putExtra("operator",3);
            PendingIntent pendingIntent=PendingIntent.getService(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            bd.setContentIntent(pendingIntent);
            bd.setProgress(0,0,false);
            manager.notify(1,bd.build());
        }
        else if(intent.getIntExtra("operator",-1)==3){
            this.quit=false;
            //manager.cancel(1);
            download();

        }
        //Toast.makeText(DownloadService.this,"flage:"+flags+",startid:"+startId,Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    //被关闭
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.quit=true;
        exit();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //退出恢复用户的铃声模式
            mAudio.setRingerMode(status);
        }
        manager.cancel(1);
    }

    //断开连接
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    //重新连接
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }


    //更新软件的相关操作
    private DownLoadTask task;

    //下载
    private void download(){
        task=new DownLoadTask();
        new Thread(task).start();
    }

    //退出
    public void exit(){
        if(task!=null){
            task.exit();
        }
    }

    //安装APP
    private void update(){
        bd.setContentText("点击安装");
        bd.setContentTitle("下载完成");
        Intent intent=new Intent(Intent.ACTION_VIEW);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            //当前Android O版本不能控制声音，下载完成将声音恢复
            mAudio.setRingerMode(status);
            // 由于没有在Activity环境下启动Activity,设置下面的标签；给目标应用一个临时的授权。
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri= FileProvider.getUriForFile(this,"com.zxhl.gpsking",new File(file,"GPSKing.apk"));
            intent.setData(uri);
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            // 由于没有在Activity环境下启动Activity,设置下面的标签；给目标应用一个临时的授权。
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri= FileProvider.getUriForFile(this,"com.zxhl.gpsking",new File(file,"GPSKing.apk"));
            intent.setDataAndType(uri,"application/vnd.android.package-archive");
        }
        else {
            intent.setDataAndType(Uri.fromFile(new File(file, "GPSKing.apk")), "application/vnd.android.package-archive");
        }
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,intent,0);
        bd.setContentIntent(pendingIntent);
        bd.setProgress(0,0,false);
        manager.notify(1,bd.build());
    }


    private final class DownLoadTask implements Runnable{
        String path=Constants.APK_PATH;
        FileDownloadered down=null;
        public DownLoadTask(){
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                file=getApplicationContext().getExternalFilesDir("");
            }
            else {
                file=getApplicationContext().getFilesDir();
            }
        }
        @Override
        public void run() {
            try {
                down=new FileDownloadered(getApplicationContext(),path,file,6);
                //设置进度条的最大刻度
                MaxPro=down.getFileSize();
                //pro.setMax(down.getFileSize());
                down.download(new DownloadProgressListener() {
                    @Override
                    public void onDownloadSize(int downloadedSize) {
                        Message msg = new Message();
                        msg.what = 0x001;
                        msg.getData().putInt("size", downloadedSize);
                        handler.sendMessage(msg);
                    }
                });
            }catch (Exception e)
            {
                e.printStackTrace();
                handler.sendEmptyMessage(0x002);
            }
        }

        public void exit(){
            if(down!=null){
                down.exit();
            }
        }
    }

    //定义Notification
    public void getNotification() {
        manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap bt = BitmapFactory.decodeResource(getResources(), R.drawable.gpsking_logo);
        //检查更新,当SDK大于等于Android6.0时必须添加在来源渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知渠道的id
            String CHANNEL_ID = "my_channel_01";
            // 用户可以看到的通知渠道的名字.
            CharSequence name = "测试";
            // 用户可以看到的通知渠道的描述
            String description = "test";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(false);
            mChannel.setVibrationPattern(new long[]{0l});

            //当前Android O版本不能控制声音，故而先用系统的方法，将声音调成振动
            if(status==AudioManager.RINGER_MODE_NORMAL) {
                mAudio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
            //当前Android O版本不能控制声音，故而先用系统的方法，将声音关闭
            //mAudio.setStreamVolume(AudioManager.STREAM_NOTIFICATION,0,AudioManager.FLAG_PLAY_SOUND);
            /*AudioAttributes.Builder audioAttributesBuilder = new AudioAttributes.Builder();
            audioAttributesBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            AudioAttributes audio = audioAttributesBuilder.build();
            Uri path = Uri.parse("android.resource://com.zxhl.gpsking/" + R.raw.kong);
            mChannel.setSound(null,null);*/
            //最后在notificationmanager中创建该通知渠道
            manager.createNotificationChannel(mChannel);

           /* Logs.i("SOUND:",mChannel.getSound().toString());
            Logs.i("SOUND:",mChannel.getAudioAttributes().toString());*/

            bd = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setTicker("开始下载")
                    .setContentTitle("正在下载")
                    .setContentText("下载")
                    .setWhen(System.currentTimeMillis())
                    .setProgress(100, 0, false)
                    .setLargeIcon(bt)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.gpsking_logo);
        }
        else
        {
            bd = new NotificationCompat.Builder(this)
                    .setTicker("开始下载")
                    .setContentTitle("正在下载")
                    .setContentText("下载")
                    .setWhen(System.currentTimeMillis())
                    .setProgress(100, 0, false)
                    .setLargeIcon(bt)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.gpsking_logo);
        }
    }


}
