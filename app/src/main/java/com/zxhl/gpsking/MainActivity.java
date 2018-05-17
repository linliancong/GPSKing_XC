package com.zxhl.gpsking;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.util.ApkVersionUtils;
import com.zxhl.util.Constants;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends StatusBarUtil {
    private ImageView img;
    private boolean state=false;
    private int tag=0;

    private TextView banq;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x001:
                    finish();
                    break;
                case 0x002:
                    Intent intent=new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                    break;
                case 0x003:
                    Intent intent2=new Intent(MainActivity.this, HomePage.class);
                    startActivity(intent2);
                    finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        img= (ImageView) findViewById(R.id.img);
        /*if(Build.VERSION.SDK_INT>=26 || Build.VERSION.SDK_INT==21||Build.VERSION.SDK_INT==22) {
            scaleImage(this, img, R.mipmap.gpsking_index_new);
        }*/

        banq=findViewById(R.id.txt_bq);
        Typeface type=Typeface.createFromAsset(getAssets(),"fonts/标准仿宋体简.ttf");
        banq.setTypeface(type);


        //判断网络状态
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //如果仅仅是用来判断网络连接
        // 则可以使用 cm.getActiveNetworkInfo().isAvailable();
        if (cm != null)  {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                state=true;
            }
        }

        SharedPreferenceUtils sp=new SharedPreferenceUtils(MainActivity.this,Constants.SAVE_USER);
        sp.setIsNetworkConnect(state);
        if(sp.getIsFirst()||!state) {
            handler.sendEmptyMessageDelayed(0x002, 1000);
        }
        else {
            HashMap<String,String> proper=new HashMap<String,String>();
            proper.put("NickName",sp.getNickName());
            proper.put("PWD",sp.getPWD());
            proper.put("Version", "GPSKing_"+ ApkVersionUtils.getVerName(MainActivity.this));
            WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "LoginForGPSKing", proper, new WebServiceUtils.WebServiceCallBack() {
                @Override
                public void callBack(SoapObject result) {
                    if(result!=null) {
                        List<String> list = new ArrayList<String>();
                        list= parseSoap(result);
                        if(list.size()==0)
                        {
                            tag=0;
                        }
                        else {
                            tag=1;
                        }
                    }
                    else
                    {
                        tag =0;
                    }

                    if(tag==1) {
                        handler.sendEmptyMessageDelayed(0x003, 1000);
                    }
                    else {
                        handler.sendEmptyMessageDelayed(0x002, 1000);
                    }
                }
            });

        }

    }

    /*
   *解析SoapObject对象
   *@param result
   * @return
   * */
    private List<String> parseSoap(SoapObject result)
    {
        List<String> list=new ArrayList<String>();
        SoapObject soapObject= (SoapObject) result.getProperty("LoginForGPSKingResult");
        if (soapObject==null)
        {
            return null;
        }
        for (int i=0;i<soapObject.getPropertyCount();i++)
        {
            list.add(soapObject.getProperty(i).toString());
        }
        return list;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    public static int scaleImage(final Activity activity, final View view, int drawableResId) {

        // 获取屏幕的高宽
        Point outSize = new Point();
        activity.getWindow().getWindowManager().getDefaultDisplay().getSize(outSize);

        // 解析将要被处理的图片
        Bitmap resourceBitmap = BitmapFactory.decodeResource(activity.getResources(), drawableResId);

        if (resourceBitmap == null) {
            return 0x001;
        }

        // 开始对图片进行拉伸或者缩放

        // 使用图片的缩放比例计算将要放大的图片的高度
        int bitmapScaledHeight = Math.round(resourceBitmap.getHeight() * outSize.x * 1.0f / resourceBitmap.getWidth());

        // 以屏幕的宽度为基准，如果图片的宽度比屏幕宽，则等比缩小，如果窄，则放大
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(resourceBitmap, outSize.x, bitmapScaledHeight, false);

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                //这里防止图像的重复创建，避免申请不必要的内存空间
                if (scaledBitmap.isRecycled())
                    //必须返回true
                    return true;


                // 当UI绘制完毕，我们对图片进行处理
                int viewHeight = view.getMeasuredHeight();


                // 计算将要裁剪的图片的顶部以及底部的偏移量
                int offset = (scaledBitmap.getHeight() - viewHeight) / 2;


                // 对图片以中心进行裁剪，裁剪出的图片就是非常适合做引导页的图片了
                Bitmap finallyBitmap = Bitmap.createBitmap(scaledBitmap, 0, offset, scaledBitmap.getWidth(),
                        scaledBitmap.getHeight() - offset * 2);


                if (!finallyBitmap.equals(scaledBitmap)) {//如果返回的不是原图，则对原图进行回收
                    scaledBitmap.recycle();
                    System.gc();
                }


                // 设置图片显示
                view.setBackground(new BitmapDrawable(activity.getResources(), finallyBitmap));
                return true;
            }
        });
        return 0x002;
    }

}
