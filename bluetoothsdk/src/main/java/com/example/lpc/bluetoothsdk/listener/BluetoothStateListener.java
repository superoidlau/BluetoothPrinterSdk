package com.example.lpc.bluetoothsdk.listener;

/**
 * Description: 蓝牙连接状态改变监听
 * Created by lpc on 2017/2/14.
 */
public interface BluetoothStateListener {

    /**
     * 连接状态改变结果回调
     * */
    void onConnectStateChanged(int state);
}
