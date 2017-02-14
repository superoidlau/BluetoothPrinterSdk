package com.example.lpc.bluetoothsdk.listener;

/**
 * Description: 接收远程设备数据
 * Created by lpc on 2017/2/14.
 */
public interface IReceiveDataListener {

    /**
     * 接收到的数据回调
     * */
    void onReceiveData(byte[] data);
}
