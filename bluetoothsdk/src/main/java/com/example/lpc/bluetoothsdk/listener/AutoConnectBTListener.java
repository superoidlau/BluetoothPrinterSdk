package com.example.lpc.bluetoothsdk.listener;

/**
 * Description: 自动连接蓝牙设备
 * Created by lpc on 2017/2/14.
 */
public interface AutoConnectBTListener {

    void onAutoConnectStarted();
    void onAutoConnectBT(String name, String address);

}
