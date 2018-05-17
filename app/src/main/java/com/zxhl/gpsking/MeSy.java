package com.zxhl.gpsking;



import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zxhl.util.Constants;
import com.zxhl.util.FileDownloadUtil.DownloadProgressListener;
import com.zxhl.util.FileDownloadUtil.FileDownloadered;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/12/4.
 */

public class MeSy extends Fragment implements View.OnClickListener {

    private View view;
    private int tag=0;
    private SharedPreferenceUtils sp;
    private Context context;
    HashMap<String,String> map=null;
    List<Map<String,String>> list=null;

    private RelativeLayout me_ly_tx;
    private RelativeLayout me_ly_zh;
    private RelativeLayout me_ly_xm;
    private RelativeLayout me_ly_lx;
    private RelativeLayout me_ly_fz;
    private RelativeLayout me_ly_gd;

    private ImageView me_img_tx;
    private TextView me_txt_tx;
    private TextView me_txt_tx2;

    private ImgTxtLayout me_imgtxt_zh;
    private ImgTxtLayout me_imgtxt_xm;
    private ImgTxtLayout me_imgtxt_lx;
    private ImgTxtLayout me_imgtxt_fz;

    private RelativeLayout me_ly_sche;
    private ImageView me_img_sche;
    private AnimationDrawable anima;

    private ShowAct showAct;

    private static final int REQUEST_CODE_PICK_IMAGE=1;
    private static final int REQUEST_CODE_CAPTURE_CAMEIA=2;
    private static final int CODE_RESULT_REQUEST=3;

    String text;

    public static boolean state=false;
    public static boolean state2=false;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x404:
                    me_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    Toast.makeText(context,"服务器有点问题，我们正在全力修复！",Toast.LENGTH_SHORT).show();
                    break;
                case 0x0010:
                    showAct= (ShowAct) getActivity();
                    showAct.callBack(0x0010);
                            /*Toast.makeText(context,"登录超时，请重新登录",Toast.LENGTH_SHORT).show();
                            Intent it=new Intent(context,Login.class);
                            startActivity(it);
                            int pid=Process.myPid();
                            Process.killProcess(pid);*/

                    break;
                case 0x0020:
                    showAct= (ShowAct) getActivity();
                    showAct.callBack(0x0020);
                    break;
                case 0x0030:
                    showAct= (ShowAct) getActivity();
                    showAct.callBack(0x0030);
                    break;
                case 0x0040:
                    me_ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    if(state) {
                        state=false;
                        Intent it_gd = new Intent();
                        it_gd.setClass(context, MeSyGd.class);
                        Bundle bd = new Bundle();
                        ArrayList bundlist = new ArrayList();
                        list.add(map);
                        bundlist.add(list);
                        bd.putParcelableArrayList("list", bundlist);
                        it_gd.putExtras(bd);
                        startActivity(it_gd);
                    }
                    break;
                case 0x0050:
                    getUserInfo();
                    break;
            }
        }
    };

    public MeSy(){

    }
    @SuppressLint("ValidFragment")
    public MeSy(Context context){
        this.context=context;
    }

    private MyBroadcastMeSy broad;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(view==null)
        {
            view = inflater.inflate(R.layout.sy_me, container, false);
            sp = new SharedPreferenceUtils(context, Constants.SAVE_USER);
            init();
            broad=new MyBroadcastMeSy();
            IntentFilter filter=new IntentFilter();
            filter.addAction("com.zxhl.gpsking.MYBROADCASTMESY");
            getActivity().registerReceiver(broad,filter);

            new Thread(){
                @Override
                public void run() {
                    while (true)
                    {
                        if(state2) {
                            state2=false;
                            handler.sendEmptyMessage(0x0050);
                        }
                    }
                }
            }.start();
        }

        list = new ArrayList<Map<String, String>>();
        me_ly_sche.setVisibility(View.VISIBLE);
        anima.start();
        getUserInfo();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.me_ly_tx:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                break;
            case R.id.me_ly_zh:

                break;
            case R.id.me_ly_xm:
                Intent it_xm=new Intent();
                it_xm.setClass(context,MeSyUpdate.class);
                Bundle bd_xm=new Bundle();
                bd_xm.putString("STR",map.get("姓名"));
                bd_xm.putInt("VALUE",1);
                it_xm.putExtras(bd_xm);
                startActivity(it_xm);
                break;
            case R.id.me_ly_lx:
                break;
            case R.id.me_ly_fz:
                break;
            case R.id.me_ly_gd:
                state=true;
                list=new ArrayList<>();
                getUserInfo();
                break;
        }

    }

    public void init(){
        if(tag==0) {
            me_ly_tx= (RelativeLayout) view.findViewById(R.id.me_ly_tx);
            me_ly_zh= (RelativeLayout) view.findViewById(R.id.me_ly_zh);
            me_ly_xm= (RelativeLayout) view.findViewById(R.id.me_ly_xm);
            me_ly_lx= (RelativeLayout) view.findViewById(R.id.me_ly_lx);
            me_ly_fz= (RelativeLayout) view.findViewById(R.id.me_ly_fz);
            me_ly_gd= (RelativeLayout) view.findViewById(R.id.me_ly_gd);

            me_img_tx= (ImageView) view.findViewById(R.id.me_img_tx);
            me_txt_tx= (TextView) view.findViewById(R.id.me_txt_tx);
            me_txt_tx2= (TextView) view.findViewById(R.id.me_txt_tx2);

            me_imgtxt_zh= (ImgTxtLayout) view.findViewById(R.id.me_imgtxt_zh);
            me_imgtxt_xm= (ImgTxtLayout) view.findViewById(R.id.me_imgtxt_xm);
            me_imgtxt_lx= (ImgTxtLayout) view.findViewById(R.id.me_imgtxt_lx);
            me_imgtxt_fz= (ImgTxtLayout) view.findViewById(R.id.me_imgtxt_fz);

            me_ly_sche=view.findViewById(R.id.me_ly_sche);
            me_img_sche=view.findViewById(R.id.me_img_sche);
            anima= (AnimationDrawable) me_img_sche.getDrawable();

            me_ly_tx.setOnClickListener(this);
            me_ly_zh.setOnClickListener(this);
            me_ly_xm.setOnClickListener(this);
            me_ly_lx.setOnClickListener(this);
            me_ly_fz.setOnClickListener(this);
            me_ly_gd.setOnClickListener(this);
            tag=1;
        }

    }

    public void getUserInfo(){
        HashMap<String,String> prepro=new HashMap<String,String>();
        prepro.put("OperatorID",sp.getOperatorID());
        if(!readImage()){
            downloadImage();
        }

        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetOperatorInfo", prepro, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null)
                {
                    map=parseSoap(result);
                    if(map.size()==0)
                    {
                        handler.sendEmptyMessage(0x0010);
                    }
                    me_txt_tx.setText(map.get("姓名"));
                    me_txt_tx2.setText(map.get("帐号"));
                    me_imgtxt_zh.setText(map.get("帐号"));
                    me_imgtxt_xm.setText(map.get("姓名"));
                    me_imgtxt_lx.setText(map.get("类型"));
                    me_imgtxt_fz.setText(map.get("分组"));
                    handler.sendEmptyMessage(0x0040);
                    list.add(map);
                }else{
                    handler.sendEmptyMessage(0x404);
                }
            }
        });
    }

    /*
    *解析SoapObject对象
    *@param result
    * @return
    * */
    public HashMap<String,String> parseSoap(SoapObject result){
        HashMap<String,String> map=new HashMap<String,String>();
        String[] str=new String[2];
        SoapObject soapObject= (SoapObject) result.getProperty("GetOperatorInfoResult");
        if(soapObject==null)
        {
            return map;
        }
        for (int i=0;i<soapObject.getPropertyCount();i++){
            //查询回来的数据为user:lin的格式，故先截取
            str=soapObject.getProperty(i).toString().split(":");
            if (str.length==2) {
                map.put(str[0],str[1]);
            }
            else
            {
                map.put(str[0],"");
            }
        }
        return map;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public interface ShowAct{
        public void callBack(int result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode==RESULT_OK &&data!=null) {
            if (data != null) {
                cropPhoto(data.getData());
            }
        }
        else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
            Uri uri = data.getData();
            //to do find the path of pic
        }else if(requestCode==CODE_RESULT_REQUEST)
        {
            if (data!=null) {
                setImageToHeadView(data);
            }
        }
    }

    /**
    *裁剪图片
    * */
    public void cropPhoto(Uri uri){
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");

        //设置裁剪
        intent.putExtra("crop","true");

        // aspectX , aspectY :宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX , outputY : 裁剪图片宽高
        intent.putExtra("outputX", me_img_tx.getWidth());
        intent.putExtra("outputY", me_img_tx.getHeight());
        intent.putExtra("return-data", true);

        startActivityForResult(intent, CODE_RESULT_REQUEST);
    }

    public void setImageToHeadView(Intent intent){
        Bundle bd=intent.getExtras();
        if(bd!=null){
            Bitmap photo=bd.getParcelable("data");
            me_img_tx.setImageBitmap(photo);
            saveImage(photo);
            sendImage(photo);
        }
    }

    /**
     * 保存图片
     */
    private void saveImage(Bitmap bitmap) {
        File filesDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
            //路径1：storage/sdcard/Android/data/包名/files
            filesDir = context.getExternalFilesDir("");
        }else{//手机内部存储
            //路径2：data/data/包名/files
            filesDir = context.getFilesDir();
        }
        FileOutputStream fos = null;
        try {
            File file = new File(filesDir,sp.getOperatorID()+".png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,fos);
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

    /**
    * 读取图片
    * */
    private boolean readImage() {
        File filesDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
            //路径1：storage/sdcard/Android/data/包名/files
            filesDir = context.getExternalFilesDir("");
        }else{//手机内部存储
            //路径2：data/data/包名/files
            filesDir = context.getFilesDir();
        }
        File file = new File(filesDir,sp.getOperatorID()+".png");
        if(file.exists()){
            //存储--->内存
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            me_img_tx.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    /**
    *上传头像到服务器
    * */
    private void sendImage(Bitmap bt){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bt.compress(Bitmap.CompressFormat.PNG, 100,stream);
        byte[] bytes=stream.toByteArray();
        String name=sp.getOperatorID()+".png";
        String img=new String(Base64.encodeToString(bytes,Base64.DEFAULT));
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.add("img",img);
        params.add("name",name);
        client.post(Constants.PHOTO_SAVE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, org.apache.http.Header[] headers, byte[] bytes) {
                Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(context,"保存失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     *下载头像到本地
     * */
    public void downloadImage(){
        String path=Constants.PHOTO_PATH + sp.getOperatorID()+".png";
        File saveDir=null;
        FileDownloadered loader;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
            //路径1：storage/sdcard/Android/data/包名/files
            saveDir = context.getExternalFilesDir("");
        }else{//手机内部存储
            //路径2：data/data/包名/files
            saveDir = context.getFilesDir();
        }
        File file=new File(saveDir,sp.getOperatorID()+".png");
        try {
            loader = new FileDownloadered(context, path, saveDir, 3);
            loader.download(new DownloadProgressListener() {
                @Override
                public void onDownloadSize(int downloadedSize) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        if (file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            me_img_tx.setImageBitmap(bitmap);
        }

    }

    public static class MyBroadcastMeSy extends BroadcastReceiver {
        public final String board="com.zxhl.gpsking.MYBROADCASTMESY";
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(board)){
                state2=true;
                //Toast.makeText(context,"ces",Toast.LENGTH_SHORT).show();

            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(broad!=null){
            getActivity().unregisterReceiver(broad);
        }
    }
}
