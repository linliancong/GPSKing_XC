package com.zxhl.gpsking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.QRCodeUtil;
import com.zxhl.util.StatusBarUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by Administrator on 2018/1/24.
 */

public class SettingSyGyShare extends StatusBarUtil implements View.OnClickListener {
    private ImgTxtLayout back;
    private ImageView code;

    private Bitmap bitmap;

    private Button more;

    private RelativeLayout visable;
    private TextView save;
    private TextView rest;

    private Context context;
    private int style=0;
    private File files;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context=SettingSyGyShare.this;
        back=findViewById(R.id.setting_imgtxt_share);
        code=findViewById(R.id.setting_img_share);
        more=findViewById(R.id.setting_btn_more_share);

        visable=findViewById(R.id.setting_ly_more_share);
        save=findViewById(R.id.setting_txt_save_share);
        rest=findViewById(R.id.setting_txt_rest_share);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //以下为生成二维码的代码
        TwoDimensionCode();
        visable.setVisibility(View.GONE);

        more.setOnClickListener(this);
        save.setOnClickListener(this);
        rest.setOnClickListener(this);
        visable.setOnClickListener(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setting_gy_share;
    }

    public void TwoDimensionCode(){
        bitmap = QRCodeUtil.createQRCodeBitmap(Constants.APK_PATH,
                300, "UTF-8", "", "2",
                Color.parseColor("#000000"),Color.parseColor("#000000"),
                Color.parseColor("#000000"), Color.parseColor("#000000"),
                Color.parseColor("#ffffff"), null,
                BitmapFactory.decodeResource(getResources(), R.drawable.gpsking_logo2),
                0.2F);

        code.setBackground(new BitmapDrawable(getResources(), bitmap));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.setting_btn_more_share:
                visable.setVisibility(View.VISIBLE);
                break;
            case R.id.setting_txt_save_share:
                visable.setVisibility(View.GONE);
                saveImage(bitmap);
                break;
            case R.id.setting_txt_rest_share:
                visable.setVisibility(View.GONE);
                resetStyle();
                break;
            case R.id.setting_ly_more_share:
                visable.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 保存图片
     */
    private void saveImage(Bitmap bitmap) {
        File filesDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
            //路径1：storage/emulated/0/Android/data/包名/files
            //filesDir = context.getExternalFilesDir("");
            filesDir=new File("storage/emulated/0/gpsking");
        }else{//手机内部存储
            //路径2：data/user/0/包名/files
            //filesDir = context.getFilesDir();
            filesDir=new File("data/user/0/gpsking");
        }
        if(!filesDir.exists()){
            filesDir.mkdirs();
        }

        FileOutputStream fos = null;
        try {
            File file = new File(filesDir,"DownloadCode"+style+".png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,fos);
            Toast.makeText(context,"保存在："+file.toString(),Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri=Uri.fromFile(file);
            intent.setData(uri);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(fos != null){
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetStyle(){
        Random random = new Random();
        if(style==random.nextInt(5)&&style<5) {
            style +=1;
        }
        else {
            style=random.nextInt(5);
        }
        switch (style) {
            case 0:
                bitmap = QRCodeUtil.createQRCodeBitmap(Constants.APK_PATH,
                        300, "UTF-8", "", "2",
                        Color.parseColor("#000000"),Color.parseColor("#000000"),
                        Color.parseColor("#000000"), Color.parseColor("#000000"),
                        Color.parseColor("#ffffff"), null,
                        BitmapFactory.decodeResource(getResources(), R.drawable.gpsking_logo2),
                        0.2F);
                //style=1;
                break;
            case 1:
                bitmap = QRCodeUtil.createQRCodeBitmap(Constants.APK_PATH,
                        300, "UTF-8", "", "2",
                        Color.parseColor("#e67e22"),Color.parseColor("#e74c3c"),
                        Color.parseColor("#f1c40f"), Color.parseColor("#1abc9c"),
                        Color.parseColor("#ffffff"), null,
                        BitmapFactory.decodeResource(getResources(), R.drawable.gpsking_logo2),
                        0.2F);
                //style=2;
            break;
            case 2:
                bitmap = QRCodeUtil.createQRCodeBitmap(Constants.APK_PATH,
                        300, "UTF-8", "", "2",
                        Color.parseColor("#03A9F4"),Color.parseColor("#03A9F4"),
                        Color.parseColor("#c0392b"), Color.parseColor("#c0392b"),
                        Color.parseColor("#ffffff"), null,
                        null,
                        0.2F);
                //style=3;
                break;
            case 3:
                bitmap = QRCodeUtil.createQRCodeBitmap(Constants.APK_PATH,
                        300, "UTF-8", "", "2",
                        Color.parseColor("#16a085"),Color.parseColor("#16a085"),
                        Color.parseColor("#16a085"), Color.parseColor("#16a085"),
                        Color.parseColor("#ffffff"), null,
                        BitmapFactory.decodeResource(getResources(), R.drawable.gpsking_logo2),
                        0.2F);
                //style=4;
                break;
            case 4:
                bitmap = QRCodeUtil.createQRCodeBitmap(Constants.APK_PATH,
                        300, "UTF-8", "", "2",
                        Color.parseColor("#2ecc71"),Color.parseColor("#d35400"),
                        Color.parseColor("#d35400"), Color.parseColor("#2ecc71"),
                        Color.parseColor("#ffffff"), null,
                        null,
                        0.2F);
                //style=5;
                break;
            case 5:
                bitmap = QRCodeUtil.createQRCodeBitmap(Constants.APK_PATH,
                        300, "UTF-8", "", "2",
                        Color.parseColor("#f39c12"),Color.parseColor("#27ae60"),
                        Color.parseColor("#f39c12"), Color.parseColor("#27ae60"),
                        Color.parseColor("#ffffff"), null,
                        BitmapFactory.decodeResource(getResources(), R.drawable.gpsking_logo2),
                        0.2F);
                //style=2;
                break;
        }

        code.setBackground(new BitmapDrawable(getResources(), bitmap));
    }

}
