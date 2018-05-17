package com.zxhl.gpsking;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.util.ApkVersionUtils;
import com.zxhl.util.AppManager;
import com.zxhl.util.CheckPermissionsActivity;
import com.zxhl.util.DownloadService;
import com.zxhl.util.FragmentUtils;
import com.zxhl.util.MyFragmentPagerAdapter;
import com.zxhl.util.NetWorkBroadcastReceiver;
import com.zxhl.util.WebServiceUtils;


import org.kobjects.pim.VCard;
import org.ksoap2.serialization.SoapObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/11/24.
 */

public class HomePage extends CheckPermissionsActivity implements RadioGroup.OnCheckedChangeListener,ViewPager.OnPageChangeListener,MeSy.ShowAct,SettingSy.ShowAct{

    public static final int PAG_ONE=0;
    public static final int PAG_TWO=1;
    public static final int PAG_THREE=2;
    public static final int PAG_FOUR=3;

    private RadioGroup rg_tab_bar;
    private RadioButton rb_home;
    private RadioButton rb_query;
    private RadioButton rb_me;
    private RadioButton rb_setting;

    private View view_home;
    private View view_query;
    private View view_me;
    private View view_setting;
    private TextView txt_topbar;

    private ViewPager vpager;
    private MyFragmentPagerAdapter mAdapter=null;

    private long mTime=0;
    NetWorkBroadcastReceiver net;


    private Intent intent;

    private int verCode_s=0;
    private int verCode=0;
    private AlertDialog alert;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    private NotificationManager manager;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if(verCode_s!=0) {
                        if(verCode_s > verCode) {
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
                                        intent = new Intent(HomePage.this, DownloadService.class);
                                        intent.setAction("com.zxhl.util.DOWNLOADSERVICE");
                                        intent.putExtra("operator",0);
                                        startService(intent);
                                    } else {
                                        intent = new Intent();
                                        intent.setAction("com.zxhl.util.DOWNLOADSERVICE");
                                        intent.putExtra("operator",0);
                                        startService(intent);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        initStatusBar();

       /* myBroadcastHP=new MyBroadcastHP();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.zxhl.gpsking.MYBROADCASTHP");
        registerReceiver(myBroadcastHP,filter);*/

        AppManager.getAppManager().addActivity(HomePage.this);

        manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mAdapter=new MyFragmentPagerAdapter(getSupportFragmentManager(),HomePage.this);
        bindView();
        rb_home.setChecked(true);

        verCode= ApkVersionUtils.getVerCode(this);
        HashMap<String,String> proper=new HashMap<String,String>();
        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetVerCode", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null) {
                    List<String> list = new ArrayList<String>();
                    Integer it=new Integer(result.getProperty(0).toString());
                    verCode_s =it.intValue();
                }
                else
                {
                    verCode_s =0;
                }
                handler.sendEmptyMessage(1);
            }
        });

    }

    public void bindView() {
        //设置菜单上方的区块
        view_home=findViewById(R.id.view_home);
        view_query=findViewById(R.id.view_query);
        view_me=findViewById(R.id.view_me);
        view_setting=findViewById(R.id.view_setting);
        //顶部标题栏
        txt_topbar= (TextView) findViewById(R.id.txt_topbar);

        //按钮
        rg_tab_bar= (RadioGroup) findViewById(R.id.rg_tab_bar);
        rg_tab_bar.setOnCheckedChangeListener(this);
        //获取第一个按钮，并设置其状态为选中
        rb_home= (RadioButton) findViewById(R.id.rb_home);
        rb_query= (RadioButton) findViewById(R.id.rb_query);
        rb_me= (RadioButton) findViewById(R.id.rb_me);
        rb_setting= (RadioButton) findViewById(R.id.rb_setting);

        //viewPager相关的设置
        vpager= (ViewPager) findViewById(R.id.vpager);
        vpager.setAdapter(mAdapter);
        //vpager.setCurrentItem(0);
        vpager.addOnPageChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.rb_home:
                txt_topbar.setText("首页");
                setSelected();
                view_home.setSelected(true);
                vpager.setCurrentItem(PAG_ONE);
                break;
            case R.id.rb_query:
                txt_topbar.setText("查询");
                setSelected();
                view_query.setSelected(true);
                vpager.setCurrentItem(PAG_TWO);
                break;
            case R.id.rb_me:
                txt_topbar.setText("我的");
                setSelected();
                view_me.setSelected(true);
                vpager.setCurrentItem(PAG_THREE);
                break;
            case R.id.rb_setting:
                txt_topbar.setText("设置");
                setSelected();
                view_setting.setSelected(true);
                vpager.setCurrentItem(PAG_FOUR);
                break;
            default:break;
        }
    }

    public void setSelected(){
        view_home.setSelected(false);
        view_query.setSelected(false);
        view_me.setSelected(false);
        view_setting.setSelected(false);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //state状态有3个，0：什么都没做，1：正在滑动，2：滑动完毕
        // 由于ViewPager 放在 RadioButton 后，所以RadioButton 的点击事件会失效。
        if (state==2)
        {
            switch (vpager.getCurrentItem()){
                case HomePage.PAG_ONE:
                    rb_home.setChecked(true);
                    break;
                case HomePage.PAG_TWO:
                    rb_query.setChecked(true);
                    break;
                case HomePage.PAG_THREE:
                    rb_me.setChecked(true);
                    break;
                case HomePage.PAG_FOUR:
                    rb_setting.setChecked(true);
                    break;
            }

        }

    }

    //解决onBackPressed不被执行的问题
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //拦截返回键
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            //判断触摸UP事件才会进行返回事件处理
            if (event.getAction() == KeyEvent.ACTION_UP) {
                onBackPressed();
            }
            //只要是返回事件，直接返回true，表示消费掉
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()-mTime>2000)
        {
            Toast.makeText(getApplicationContext(),"再按一次退出",Toast.LENGTH_SHORT).show();
            mTime=System.currentTimeMillis();
        }
        else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onResume() {
       /* if(net==null)
        {
            net=new NetWorkBroadcastReceiver();
        }
        IntentFilter it=new IntentFilter("android.intent.action.NETWORK_CONNECT_STATE");
        registerReceiver(net,it);*/
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(intent!=null) {
            stopService(intent);
        }
        super.onDestroy();
    }

    @Override
    public void callBack(int result) {
        switch (result){
            //首页
            case 0x1000:
                break;
            case 0x2000:
                break;
            case 0x3000:
                break;
            //查询
            case 0x0100:
                break;
            case 0x0200:
                break;
            case 0x0300:
                break;
            //我的
            case 0x0010:
                Toast.makeText(getApplicationContext(),"登录超时，请重新登录",Toast.LENGTH_SHORT).show();
                Intent it=new Intent(getApplicationContext(),Login.class);
                startActivity(it);
                finish();
                break;
            case 0x0020:
                break;
            case 0x0030:
                Toast.makeText(getApplicationContext(),"没有读取到数据",Toast.LENGTH_SHORT).show();
                Intent it2=new Intent(getApplicationContext(),Login.class);
                startActivity(it2);
                finish();
                break;
            //设置
            case 0x0001:
                Intent it3=new Intent(HomePage.this,Login.class);
                startActivity(it3);
                //AppManager.getAppManager().finishActivity();
                finish();
                break;
            case 0x0002:
                finish();
                break;
            case 0x0003:
                Intent it4=new Intent(getApplicationContext(),SettingSyPWD.class);
                startActivity(it4);
                break;
        }

    }

   /* public class MyBroad extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
                Toast.makeText(context,"安装完成",Toast.LENGTH_SHORT).show();
                manager.cancel(1);
            }else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
                Toast.makeText(context,"替换完成",Toast.LENGTH_SHORT).show();
                manager.cancel(1);
            }
        }
    }*/


    public View getAlert(int mLayout){
        View ad_view;
        //初始化Builder
        builder=new AlertDialog.Builder(this);
        //完成相关设置
        inflater=getLayoutInflater();
        ad_view=inflater.inflate(mLayout,null,false);
        builder.setView(ad_view);
        builder.setCancelable(true);
        alert=builder.create();
        alert.show();
        return ad_view;
    }

    private void initStatusBar() {
        Window win = getWindow();
        ViewGroup contentFrameLayout = findViewById(Window.ID_ANDROID_CONTENT);
        View parentView = contentFrameLayout.getChildAt(0);
        //KITKAT也能满足，只是SYSTEM_UI_FLAG_LIGHT_STATUS_BAR（状态栏字体颜色反转）只有在6.0才有效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            parentView.setFitsSystemWindows(true);
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            // 部分机型的statusbar会有半透明的黑色背景
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(Color.WHITE);// SDK21
        }
    }

}


