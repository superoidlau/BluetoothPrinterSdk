package com.example.lpc.bluetoothsdk.listener;

/**
 * Description: 蓝牙连接结果监听
 * Created by lpc on 2017/2/14.
 */
public interface BluetoothConnectListener {

    /**
     * 连接OK结果回调
     * */
    void onBTDeviceConnected(String address, String name);

    /**
     * 断开连接结果回调
     * */
    void onBTDeviceDisconnected();

    /**
     * 连接失败结果回调
     * */
    void onBTDeviceConnectFailed();
}
