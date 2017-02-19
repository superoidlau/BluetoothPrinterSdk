package com.example.lpc.bluetoothsdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.example.lpc.bluetoothsdk.listener.BluetoothConnectListener;
import com.example.lpc.bluetoothsdk.listener.BluetoothStateListener;
import com.example.lpc.bluetoothsdk.listener.IReceiveDataListener;
import com.example.lpc.bluetoothsdk.service.BluetoothService;
import com.example.lpc.bluetoothsdk.constant.ConstantDefine;

import java.util.ArrayList;
import java.util.Set;

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

    private BluetoothService mBTService;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isServiceRunning = false;

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

    //判断蓝牙服务是否可用
    public boolean isServiceAvailable() {
        return mBTService != null;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        return mBluetoothAdapter;
    }

    //开启远程蓝牙设备扫描
    public boolean startDiscovery(){
        return mBluetoothAdapter.startDiscovery();
    }

    //判断发现蓝牙设备进程是否正在运行
    public boolean isDiscoverying(){
        return mBluetoothAdapter.isDiscovering();
    }

    //取消设备发现进程
    public boolean cancelDiscovery(){
        return mBluetoothAdapter.cancelDiscovery();
    }

    public void setupService() {
        mBTService = new BluetoothService(mHandler);
    }

    public int getServiceState(){
        if (mBTService != null){
            return mBTService.getState();
        }else {
            return -1;
        }
    }

    public void startService(){
        if (mBTService != null){
            if (mBTService.getState() == ConstantDefine.CONNECT_STATE_NONE){
                mBTService.start();
                isServiceRunning = true;
            }
        }
    }

    public void stopService(){
        if (mBTService != null){
            mBTService.stop();
            isServiceRunning = false;
        }
    }

    //连接蓝牙设备
    public void connect(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (mBTService != null){
            mBTService.connect(device);
        }
    }

    public void connect(BluetoothDevice device){
        if (mBTService != null){
            mBTService.connect(device);
        }
    }

    //断开连接
    public void disconnect(){
        if (mBTService != null){
            mBTService.stop();
            isServiceRunning = false;
            if (mBTService.getState() == ConstantDefine.CONNECT_STATE_NONE){
                mBTService.start();
                isServiceRunning = true;
            }
        }
    }

    public void write(byte[] data){
        if (mBTService.getState() == ConstantDefine.CONNECT_STATE_CONNECTED){
            mBTService.write(data);
        }
    }

    //得到配对成功的设备集合
    public Set<BluetoothDevice> getPairingDevices(){
        return mBluetoothAdapter.getBondedDevices();
    }

    public String getConnectDeviceName(){
        return mConnectDeviceName;
    }

    public String getConnectDeviceAddress(){
        return mConnectDeviceAddress;
    }

    public void setBlueStateListener(BluetoothStateListener listener){
        this.mStateListener = listener;
    }

    public void setReceiveDataListener(IReceiveDataListener listener){
        this.mReceiveDataListener = listener;
    }

    public void setBluetoothConnectListener(BluetoothConnectListener listener){
        this.mConnectListener = listener;
    }


    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //读取数据
                case ConstantDefine.MESSAGE_STATE_READ:
                    byte[] datas = (byte[]) msg.obj;
                    if (datas != null && datas.length > 0){
                        if (mReceiveDataListener != null){
                            mReceiveDataListener.onReceiveData(datas);
                        }
                    }
                    break;
                case ConstantDefine.MESSAGE_STATE_WRITE:
                    break;
                case ConstantDefine.MESSAGE_DEVICE_INFO:
                    mConnectDeviceName = msg.getData().getString(ConstantDefine.KEY_DEVICE_NAME);
                    mConnectDeviceAddress = msg.getData().getString(ConstantDefine.KEY_DEVICE_ADDRESS);
                    if (mConnectListener != null){
                        mConnectListener.onBTDeviceConnected(mConnectDeviceAddress, mConnectDeviceName);
                    }
                    isConnected = true;
                    break;
                case ConstantDefine.MESSAGE_STATE_CHANGE:
                    if(mStateListener != null)
                        mStateListener.onConnectStateChanged(msg.arg1);
                    if(isConnected && msg.arg1 != ConstantDefine.CONNECT_STATE_CONNECTED) {
                        if(mConnectListener != null){
                            mConnectListener.onBTDeviceDisconnected();
                        }
                        isConnected = false;
                        mConnectDeviceName = null;
                        mConnectDeviceAddress = null;
                    }

                    if(!isConnecting && msg.arg1 == ConstantDefine.CONNECT_STATE_CONNECTING) {
                        isConnecting = true;
                    } else if(isConnecting) {
                        if(msg.arg1 != ConstantDefine.CONNECT_STATE_CONNECTED) {
                            if(mConnectListener != null)
                                mConnectListener.onBTDeviceConnectFailed();
                        }
                        isConnecting = false;
                    }
                    break;
            }
        }
    };

}
