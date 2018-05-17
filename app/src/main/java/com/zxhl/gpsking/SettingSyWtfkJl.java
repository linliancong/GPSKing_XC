package com.zxhl.gpsking;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxhl.entity.JLData;
import com.zxhl.util.AdapterUtil;
import com.zxhl.util.Constants;
import com.zxhl.util.ImgTxtLayout;
import com.zxhl.util.SharedPreferenceUtils;
import com.zxhl.util.StatusBarUtil;
import com.zxhl.util.WebServiceUtils;

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Administrator on 2017/12/21.
 */

public class SettingSyWtfkJl extends StatusBarUtil{

    private ListView list;
    private ImgTxtLayout setting_imgtxt_back_wtfkjl;

    private MyAdapter adapter;
    private List<Map<String,String>> mapList=new ArrayList<Map<String,String>>();

    private LayoutInflater inflater;

    private SharedPreferenceUtils sp;

    private List<Map<String,String>> hashMap;

    private ImageView img;
    private TextView txt;

    private RelativeLayout ly_sche;
    private ImageView img_sche;
    private AnimationDrawable anima;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.GONE);
                    txt.setVisibility(View.GONE);
                    showQuestion();
                    break;
                case 0x002:
                    ly_sche.setVisibility(View.GONE);
                    anima.stop();
                    img.setVisibility(View.VISIBLE);
                    txt.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.setting_wtfk_fkjl);

        setting_imgtxt_back_wtfkjl= (ImgTxtLayout) findViewById(R.id.setting_imgtxt_back_wtfkjl);
        list= (ListView) findViewById(R.id.setting_wtfk_list_fkjl);
        img= (ImageView) findViewById(R.id.setting_wtfk_img_fkjl);
        txt= (TextView) findViewById(R.id.setting_wtfk_txt_fkjl);
        inflater=getLayoutInflater();
        //inflater=LayoutInflater.from(SettingSyWtfkJl.this);

        ly_sche=findViewById(R.id.ly_sche);
        img_sche=findViewById(R.id.img_sche);
        anima= (AnimationDrawable) img_sche.getDrawable();

        sp=new SharedPreferenceUtils(getApplicationContext(), Constants.SAVE_USER);
        hashMap=new ArrayList<Map<String, String>>();
        HashMap<String,String> proper=new HashMap<String, String>();
        proper.put("OperatorID",sp.getOperatorID());

        adapter=new MyAdapter(this,mapList);
        list.setAdapter(adapter);

        /*list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"你点击了~"+position,Toast.LENGTH_SHORT).show();
            }
        });*/

        setting_imgtxt_back_wtfkjl.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ly_sche.setVisibility(View.VISIBLE);
        anima.start();


        //获取消息记录
        WebServiceUtils.callWebService(WebServiceUtils.WEB_SERVER_URL, "GetQuestion", proper, new WebServiceUtils.WebServiceCallBack() {
            @Override
            public void callBack(SoapObject result) {
                if(result!=null)
                {
                    List<Map<String,String>> map=new ArrayList<Map<String, String>>();
                    map=parseSoap(result);
                    if(map.size()==0){
                        handler.sendEmptyMessage(0x002);
                        return;
                    }
                    hashMap=map;
                    handler.sendEmptyMessage(0x001);
                }
                else
                    handler.sendEmptyMessage(0x002);
            }
        });


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setting_wtfk_fkjl;
    }

    private void showQuestion() {
        Map<String,String> map;
        String str;
        String date;
        String time;


        for(int i=0;i<hashMap.size();i++)
        {
            str=hashMap.get(i).get("time");
            Date oldTime;
            Date newTime=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("MM月dd日");
            SimpleDateFormat sdf2=new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdf3=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            SimpleDateFormat sdf4=new SimpleDateFormat("yyyy年MM月dd日");
            SimpleDateFormat sdf5=new SimpleDateFormat("yyyy");
            try {
                oldTime=sdf3.parse(str);
                String str1=sdf5.format(oldTime);
                String str2=sdf5.format(newTime);
                if(str1.equals(str2)) {
                    date = sdf.format(oldTime);
                }
                else {
                    date=sdf4.format(oldTime);
                }
                time=sdf2.format(oldTime);
                if(getDateDiff(oldTime)==0){
                    time="昨天"+"\t"+time;
                }
                else if(getDateDiff(oldTime)==2)
                {
                    time=date+"\t"+time;
                }
                map=new HashMap<String, String>();
                map.put("type","0");
                map.put("time",time);
                mapList.add(map);
                map=new HashMap<String, String>();
                map.put("type","1");
                map.put("time",date);
                map.put("text",hashMap.get(i).get("text"));
                mapList.add(map);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
        list.setSelection(list.getCount()-1);
    }

    /**
    *@return 0:昨天 1：今天 2：之前
    * */
    public int getDateDiff(Date oldDate){
        long d=1000*24*60*60;
        /*long h=1000*60*60;
        long m=1000*60;*/
        //当前时间毫秒数
        long current=System.currentTimeMillis();
        //今天零点零分零秒的毫秒数
        long zero=current/(1000*3600*24)*(1000*3600*24)- TimeZone.getDefault().getRawOffset();
        //总共相差的毫秒数
        //long diff=newDate.getTime()-oldDate.getTime();
        /*//相差的天数、小时数、分钟数
        long day=diff/d;
        long hour=diff%d/h;
        long min=diff%d%h/m;
        long hours=diff/h;*/
        //今天相差的毫秒数
        //long diff2=newDate.getTime()-zero;


        if(oldDate.getTime()>zero){
            return 1;
        }
        else {
            //昨天的23:59:59
            long dif=zero-d-1;
            if(oldDate.getTime()<dif)
            {
                return 2;
            }
            else {
                return 0;
            }
        }

    }

    public class MyAdapter extends ArrayAdapter{
        private List<Map<String,String>> list;
        private Context context;
        private int type;
        private ListView listView;

        public MyAdapter(Context context,List<Map<String,String>> list){
            super(context,0);
            this.context=context;
            this.list=list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder=null;
            Map<String,String> map=(Map<String,String>)list.get(position);
            if(map.get("type").equals("0")){
                if(convertView==null){
                    convertView=inflater.inflate(R.layout.setting_wtfk_fkjl_time,parent,false);
                    viewHolder=new ViewHolder();
                    viewHolder.time= (TextView) convertView.findViewById(R.id.setting_wtfk_item_time);
                    convertView.setTag(viewHolder);
                }
                else{
                    viewHolder= (ViewHolder) convertView.getTag();
                }
                //防止滑动过快convertView返回的是上一个
                if(viewHolder.time==null){
                    convertView=inflater.inflate(R.layout.setting_wtfk_fkjl_time,parent,false);
                    viewHolder=new ViewHolder();
                    viewHolder.time= (TextView) convertView.findViewById(R.id.setting_wtfk_item_time);
                    convertView.setTag(viewHolder);
                }
                viewHolder.time.setText(map.get("time"));
            }
            else
            {
                if(convertView==null){
                    convertView=inflater.inflate(R.layout.setting_wtfk_fkjl_item,parent,false);
                    viewHolder=new ViewHolder();
                    viewHolder.text= (TextView) convertView.findViewById(R.id.setting_wtfk_item_txt);
                    viewHolder.txt_time= (TextView) convertView.findViewById(R.id.setting_wtfk_item_txt2);
                    convertView.setTag(viewHolder);
                }
                else{
                    viewHolder= (ViewHolder) convertView.getTag();
                }
                //防止滑动过快convertView返回的是上一个
                if(viewHolder.text==null || viewHolder.txt_time==null){
                    convertView=inflater.inflate(R.layout.setting_wtfk_fkjl_item,parent,false);
                    viewHolder=new ViewHolder();
                    viewHolder.text= (TextView) convertView.findViewById(R.id.setting_wtfk_item_txt);
                    viewHolder.txt_time= (TextView) convertView.findViewById(R.id.setting_wtfk_item_txt2);
                    convertView.setTag(viewHolder);
                }
                viewHolder.text.setText(map.get("text"));
                viewHolder.txt_time.setText(map.get("time"));
            }
            Log.i("现在的索引是","-----------"+position+"-----------------");
            return convertView;
        }

        public final class ViewHolder{
            TextView time;
            TextView text;
            TextView txt_time;
        }
    }

    public List<Map<String,String>> parseSoap(SoapObject result){
        List<Map<String,String>> list1=new ArrayList<Map<String, String>>();
        Map<String,String> map;
        SoapObject soapObject= (SoapObject) result.getProperty("GetQuestionResult");
        if(soapObject==null){
            return null;
        }
        for(int i=0;i<soapObject.getPropertyCount();i++){
            map=new HashMap<String, String>();
            map.put("time",soapObject.getProperty(i).toString());
            map.put("text",soapObject.getProperty(i+1).toString());
            list1.add(map);
            i++;
        }
        return list1;
    }
}
