package com.example.lpc.bluetoothsdk;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.example.lpc.bluetoothsdk.listener.AutoConnectBTListener;
import com.example.lpc.bluetoothsdk.listener.BluetoothConnectListener;
import com.example.lpc.bluetoothsdk.listener.BluetoothStateListener;
import com.example.lpc.bluetoothsdk.listener.IReceiveDataListener;

/**
 * Description: 接口管理类
 * Created by lpc on 2017/2/14.
 */
public class BluetoothSdkManager {

    private Context mContext;

    private BluetoothAdapter mBluetoothAdapter;
    private String mConnectDeviceName;
    private String mConnectDeviceAddress;

    //listener
    private BluetoothConnectListener mConnectListener = null;
    private BluetoothStateListener mStateListener = null;
    private IReceiveDataListener mReceiveDataListener = null;
    private AutoConnectBTListener mAutoConnectListener = null;

    public BluetoothSdkManager(Context context){
        this.mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 判断设备是否支持蓝牙
     * */
    public boolean isBluetoothSupported(){
        if (mBluetoothAdapter == null ){
            return false;
        }
        return true;
    }

    /**
     * 判断蓝牙是否可用
     * */
    public boolean isBluetoothEnabled(){
        return mBluetoothAdapter.isEnabled();
    }

}
