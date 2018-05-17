package com.zxhl.util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zxhl.gpsking.R;


/**
 * Created by Administrator on 2017/11/28.
 */

public class FragmentUtils extends Fragment implements View.OnClickListener{

    public static final int PAG_ONE=0;
    public static final int PAG_TWO=1;
    public static final int PAG_THREE=2;
    public static final int PAG_FOUR=3;

    private View view;
    private View view1;
    private View view2;
    private View view3;
    private View view4;
    private String content;
    private int tag;


    /*
    *首页控件
    * */
    private int tag1=0;

    /*
    *查询页控件
    * */
    private int tag2=0;

    /*
    *我的页控件
    * */
    private int tag3=0;
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


    /*
    *设置页控件
    * */
    private int tag4=0;

    public FragmentUtils(){}
    @SuppressLint("ValidFragment")
    public FragmentUtils(String content,int tag) {
        this.content = content;
        this.tag=tag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        switch (tag) {
            case PAG_ONE:
                view1 = inflater.inflate(R.layout.hp_content, container, false);
                init1();
                TextView txt_content1 = (TextView) view1.findViewById(R.id.hp_txt_content);
                txt_content1.setText(content);
                view=view1;
                break;
            case PAG_TWO:
                view2 = inflater.inflate(R.layout.hp_content, container, false);
                init2();
                TextView txt_content2 = (TextView) view2.findViewById(R.id.hp_txt_content);
                txt_content2.setText(content);
                view=view2;
                break;
            case PAG_THREE:
                view3 = inflater.inflate(R.layout.sy_me, container, false);
                init3();
                view=view3;
                break;
            case PAG_FOUR:
                view4 = inflater.inflate(R.layout.sy_setting, container, false);
                init4();
                view=view4;
                break;
        }
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    /*
    * 控件初始化操作，对应的页面
    * */
    public void init1(){

    }

    public void init2(){

    }

    public void init3(){
        if(tag3==0) {
            me_ly_tx= (RelativeLayout) view1.findViewById(R.id.me_ly_tx);
            me_ly_zh= (RelativeLayout) view1.findViewById(R.id.me_ly_zh);
            me_ly_xm= (RelativeLayout) view1.findViewById(R.id.me_ly_xm);
            me_ly_lx= (RelativeLayout) view1.findViewById(R.id.me_ly_lx);
            me_ly_fz= (RelativeLayout) view1.findViewById(R.id.me_ly_fz);
            me_ly_gd= (RelativeLayout) view1.findViewById(R.id.me_ly_gd);

            me_img_tx= (ImageView) view3.findViewById(R.id.me_img_tx);
            me_txt_tx= (TextView) view3.findViewById(R.id.me_txt_tx);
            me_txt_tx2= (TextView) view3.findViewById(R.id.me_txt_tx2);

            me_imgtxt_zh= (ImgTxtLayout) view3.findViewById(R.id.me_imgtxt_zh);
            me_imgtxt_xm= (ImgTxtLayout) view3.findViewById(R.id.me_imgtxt_xm);
            me_imgtxt_lx= (ImgTxtLayout) view3.findViewById(R.id.me_imgtxt_lx);
            me_imgtxt_fz= (ImgTxtLayout) view3.findViewById(R.id.me_imgtxt_fz);
        }

    }

    public void init4(){

    }
}
