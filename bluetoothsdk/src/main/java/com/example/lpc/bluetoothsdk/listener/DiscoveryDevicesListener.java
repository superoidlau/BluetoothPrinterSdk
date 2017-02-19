package com.example.lpc.bluetoothsdk.listener;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Description: 发现蓝牙设备结果监听
 * Created by lpc on 2017/2/19.
 */
public interface DiscoveryDevicesListener {

    void startDisconvery();

    void discoveryNew(BluetoothDevice device);

    void discoveryFinish(List<BluetoothDevice> list);
}
