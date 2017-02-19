package com.example.lpc.bluetoothsdk.constant;

import java.util.UUID;

/**
 * Description:
 * Created by lpc on 2017/2/14.
 */
public class ConstantDefine {

    //定义当前的连接状态
    public static final int CONNECT_STATE_NONE = 0;         //什么都没有连接
    public static final int CONNECT_STATE_LISTENER = 1;     //侦听连接
    public static final int CONNECT_STATE_CONNECTING = 2;   //正在连接
    public static final int CONNECT_STATE_CONNECTED = 3;    //已经连接
    public static final int CONNECT_STATE_NULL = -1;        //


    //Handler的消息类型
    public static final int MESSAGE_STATE_READ = 1;
    public static final int MESSAGE_STATE_WRITE = 2;
    public static final int MESSAGE_STATE_CHANGE = 3;
    public static final int MESSAGE_DEVICE_INFO = 4;

    //Intent请求码
    public static final int REQUEST_CONNECT_BT = 0x111;     //请求连接蓝牙
    public static final int REQUEST_ENABLED_BT = 0x222;     //请求开启蓝牙

    //需要的UUID
    public static final String STRING_DEVICE_PRINTER = "00001101-0000-1000-8000-00805F9B34FB";

    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_DEVICE_ADDRESS = "device_address";
}
