package com.zxhl.gpsking;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.MapView;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.ShowKeyboard;
import com.zxhl.util.StatusBarUtil;

/**
 * Created by Administrator on 2018/3/6.
 */

public class GoogleMap extends StatusBarUtil implements View.OnClickListener,TextWatcher{
    private WebView map;
    private ImgTxtLayout back;
    private EditText img1;
    private ImageView img2;
    private TextView title;

    private AutoCompleteTextView vehicle;
    private ImageView search;
    private Button getVeh;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        //设置WebView属性,转载的网页
        //初始化地图
        //map.loadUrl("http://www.google.cn/maps/@35.780287,104.1361118,4z/data=!3m1!1e3");
        //标记地图的点
        map.loadUrl("http://www.google.cn/maps?q=35.780287,104.1361118(zxhl)&z=17&hl=cn");
        //设置WebView属性,运行执行js脚本
        map.getSettings().setJavaScriptEnabled(true);
        map.setWebChromeClient(new WebChromeClient(){
            //这里设置获取到的网站title
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });

        map.setWebViewClient(new WebViewClient() {
            //在webview里打开新链接
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

    }

    private void init() {
        map=findViewById(R.id.map);
        back=findViewById(R.id.query_imgtxt_title);
        img1=findViewById(R.id.query_edit_img);
        img2=findViewById(R.id.query_img_img);
        title=findViewById(R.id.query_txt_title);

        vehicle=findViewById(R.id.query_auto_vehiclelic);
        search=findViewById(R.id.query_img_serch);
        getVeh=findViewById(R.id.query_btn_get);


        getVeh.setOnClickListener(this);
        vehicle.addTextChangedListener(this);
        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        vehicle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                getVeh.callOnClick();
                return true;
            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.query_googlemap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.query_img_serch:
                title.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);
                img2.setVisibility(View.VISIBLE);
                vehicle.setVisibility(View.VISIBLE);
                break;
            case R.id.query_btn_get:
                ShowKeyboard.hideKeyboard(vehicle);
                map.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                search.setVisibility(View.VISIBLE);
                img1.setVisibility(View.GONE);
                img2.setVisibility(View.GONE);
                vehicle.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
