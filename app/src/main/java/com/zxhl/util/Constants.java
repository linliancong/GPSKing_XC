package com.zxhl.util;

/**
 * Created by Administrator on 2017/11/24.
 */

public class Constants {
    public static final String SERVICE_IP="117.28.255.123";   //服务器IP
    public static final int SERVER_PORT=9999;               //端口
    public static final int REGISTER_FAIL=0;                //注册失败
    public static final String MSGKEY="message";            //消息秘钥
    public static final String IP_PORT="ip_port";           //保存ip和port的文件名
    public static final String SAVE_USER="save_user";       //保存用户信息的文件名
    public static final String DATABASE="TXServer";         //连接的数据库名称
    public static final String PHOTO_SAVE="http://117.28.255.123:9999/WebServiceClient/UpLoadPhotoServlet"; //头像保存deServlet
    public static final String PHOTO_PATH="http://117.28.255.123:9999/APPWebService/Image/";//图片在服务器的路径
    public static final String APK_PATH="http://117.28.255.123:9999/APPWebService/GPSKing_App/APK/GPSKing.apk";//APP在服务器的路径

    public static byte[] hexStr2Bytes(String src){
        int m=0,n=0;
        int l=src.length()/2;

        byte[] ret=new byte[l];
        for (int i=0;i<l;i++)
        {
            m=i*2+1;
            n=m+1;
            ret[i]=uniteBytes(src.substring(i*2+m),src.substring(m,n));
        }
        return ret;
    }

    public static byte uniteBytes(String src0,String src1){
        byte b0=Byte.decode("0x"+src0).byteValue();
        b0=(byte)(b0<<4);
        byte b1 = Byte.decode("0x" + src1).byteValue();
        byte ret=(byte)(b0|b1);
        return ret;
    }

}
