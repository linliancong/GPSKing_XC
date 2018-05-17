package com.zxhl.util;

import android.os.Handler;
import android.os.Message;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.HttpsTransportSE;

import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/11/24.
 */

public class WebServiceUtils {
    //WebService服务的地址
    public static final String WEB_SERVER_URL="http://117.28.255.123:9999/APPWebService/Service1.asmx";
    public static final String OperaCenter_URL="http://117.28.255.123:9999/APPControlService/GpsKingService.asmx";

    //开始线程池，含有3个线程
    private static final ExecutorService executor= Executors.newFixedThreadPool(3);
    //命名空间
    private static final String NAMESPACE="http://tempuri.org/";

    /*
    *
    * @param url
    *           WebService服务器的地址
    *@param methodName
	*           WebService的调用方法名
	* @param proper
	*           WebService的参数
	* @param webServiceCallBack
	*           回调接口
    *
    * */
    public static void callWebService(String url, final String methodName,
                                      HashMap<String,String> proper,
                                      final WebServiceCallBack webServiceCallBack) {
        //创建SoapObject对象
        final SoapObject soapObject=new SoapObject(NAMESPACE,methodName);

        //添加查询参数
        if(proper!=null)
        {
            for (Iterator<Map.Entry<String, String>> it = proper.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry<String,String> entry= it.next();
                soapObject.addProperty(entry.getKey(),entry.getValue());
            }
        }
        //实例化SoapSerializationEnvelope对象，设置Soap协议版本号
        final SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        //设置调用.net开发的WebService
        envelope.setOutputSoapObject(soapObject);
        envelope.dotNet=true;
        //创建HttpsTransportSE对象，传递WebService服务器地址,设置超时2分钟
        final HttpTransportSE httpsTransportSE=new HttpTransportSE(url,1000*60*2);
        httpsTransportSE.debug=true;
        //用于子线程与主线程通信
        final Handler hanlder=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //将返回值回调到callBack的参数中
                webServiceCallBack.callBack((SoapObject) msg.obj);
            }
        };

        //开启线程去访问WebService
        executor.submit(new Runnable() {
            @Override
            public void run() {
                SoapObject resultSoap=null;
                try{
                    httpsTransportSE.call(NAMESPACE+methodName,envelope);
                    if(envelope.getResponse()!=null)
                    {
                        //获取服务器响应返回的SoapObject
                        resultSoap= (SoapObject) envelope.bodyIn;
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }finally {
                    //将获取的消息利用Handler发送到主线程
                    hanlder.sendMessage(hanlder.obtainMessage(0,resultSoap));
                }
            }
        });


    }

    public interface WebServiceCallBack{
        public void callBack(SoapObject result);
    }
}
