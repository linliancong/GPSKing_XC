package com.zxhl.gpsking;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.entity.Icon;
import com.zxhl.util.AdapterUtil;
import com.zxhl.util.Constants;
import com.zxhl.util.SharedPreferenceUtils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/4.
 */

public class QuerySy extends Fragment implements View.OnClickListener {

    private View view;
    private Context context;
    private GridView grid_icon;
    private AdapterUtil adapter=null;
    private ArrayList<Icon> mData=null;
    private int tag=0;

    private SharedPreferenceUtils sp;

    public QuerySy(){
    }
    @SuppressLint("ValidFragment")
    public QuerySy(Context context){
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(view==null) {
            view = inflater.inflate(R.layout.sy_query, container, false);
            init();
        }
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    public void init(){
        grid_icon= (GridView) view.findViewById(R.id.grid_icon);
        sp=new SharedPreferenceUtils(context, Constants.SAVE_USER);

        mData=new ArrayList<Icon>();
        mData.add(new Icon(R.drawable.clwz,"车辆位置"));
        mData.add(new Icon(R.drawable.ccdh,"查车导航"));
        mData.add(new Icon(R.drawable.gjhf,"轨迹回放"));
        mData.add(new Icon(R.drawable.gkxx,"工况信息"));
        mData.add(new Icon(R.drawable.bbtj,"报表统计"));
        mData.add(new Icon(R.drawable.yjcl,"样机车辆"));
        mData.add(new Icon(R.drawable.gpsyc,"GPS异常"));
        mData.add(new Icon(R.drawable.scxx,"锁车信息"));
        mData.add(new Icon(R.drawable.bytx,"保养提醒"));
        mData.add(new Icon(R.drawable.sbxx,"设备信息"));
        mData.add(new Icon(R.drawable.yycl,"预约车辆"));
        mData.add(new Icon(R.drawable.jk,"遥控设备"));
        if(sp.getRoleID().equals("1")) {
            mData.add(new Icon(R.drawable.bjxx,"报警信息"));
            mData.add(new Icon(R.drawable.yyzx, "运营中心"));
            mData.add(new Icon(0, ""));
            mData.add(new Icon(0, ""));
        }


        adapter=new AdapterUtil<Icon>(mData,R.layout.item_grid_icon) {
            @Override
            public void bindView(ViewHolder holder, Icon obj) {
                holder.setImageResource(R.id.img_icon,obj.getmId());
                holder.setText(R.id.txt_icon,obj.getmName());
                //holder.setVisibility(R.id.view_icon,View.VISIBLE);
            }
        };

        grid_icon.setAdapter(adapter);

        grid_icon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        //车辆位置
                        Intent it0=new Intent(context,QuerySyLocation.class);
                        startActivity(it0);
                        break;
                    case 1:
                        //查车导航
                        Intent it1=new Intent(context,QuerySyNavi.class);
                        startActivity(it1);
                        break;
                    case 2:
                        //轨迹回放
                        Intent it2=new Intent(context,QuerySyTrackPlayback.class);
                        startActivity(it2);
                        break;
                    case 3:
                        //工况信息
                        Intent it3=new Intent(context,QuerySyGkxx.class);
                        startActivity(it3);
                        break;
                    case 4:
                        //报表统计
                        Intent it4=new Intent(context,QuerySyBbtj.class);
                        startActivity(it4);
                        break;
                    case 5:
                        //样机车辆
                        Intent it5=new Intent(context,QuerySyYjcl.class);
                        startActivity(it5);
                        break;
                    case 6:
                        //GPS异常
                        Intent it6=new Intent(context,QuerySyGpsyc.class);
                        startActivity(it6);
                        break;
                    case 7:
                        //锁车信息
                        Intent it7=new Intent(context,QuerySyScxx.class);
                        startActivity(it7);
                        break;
                    case 8:
                        //保养提醒
                        Intent it8=new Intent(context,QuerySyBytx.class);
                        startActivity(it8);
                        break;
                    case 9:
                        //设备信息
                        Intent it9=new Intent(context,QuerySySbxx.class);
                        startActivity(it9);
                        break;
                    case 10:
                        //预约车辆
                        Intent it10=new Intent(context,QuerySyYycl.class);
                        startActivity(it10);
                        break;
                    case 11:
                        //遥控设备
                        Intent it11=new Intent(context,QuerySyRemote.class);
                        startActivity(it11);
                        break;
                    case 12:
                        //报警设备
                        Intent it12=new Intent(context,QuerySyBjxx.class);
                        startActivity(it12);
                        break;
                    case 13:
                        //运营中心
                        if(sp.getRoleID().equals("1")) {
                            Intent it13=new Intent(context,OperatingCenter.class);
                            startActivity(it13);
                        }
                        else {}

                        break;
                }

            }
        });


    }
}
