package com.example.lpc.bluetoothsdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.lpc.bluetoothsdk.listener.BluetoothConnectListener;
import com.example.lpc.bluetoothsdk.listener.BluetoothStateListener;
import com.example.lpc.bluetoothsdk.listener.DiscoveryDevicesListener;
import com.example.lpc.bluetoothsdk.listener.IReceiveDataListener;
import com.example.lpc.bluetoothsdk.service.BluetoothService;
import com.example.lpc.bluetoothsdk.constant.ConstantDefine;
import com.example.lpc.bluetoothsdk.util.BitmapUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
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
    private DiscoveryDevicesListener mDiscoveryDevicesListener = null;

    private BluetoothService mBTService;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isServiceRunning = false;

    private List<BluetoothDevice> mDeviceList = null;
    private DiscoveryReceiver mReceiver;
    private boolean isRegister = false;

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

        Log.e("lpc","--- startDiscovery()" );
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
        if (mBTService.getState() == ConstantDefine.CONNECT_STATE_NONE){
            mBTService.start();
            isServiceRunning = true;
        }
    }

    public int getServiceState(){
        if (mBTService != null){
            return mBTService.getState();
        }else {
            return -1;
        }
    }

    public void stopService(){
        if (mBTService != null){
            mBTService.stop();
            isServiceRunning = false;
        }
        if (isRegister){
            mContext.unregisterReceiver(mReceiver);
            isRegister = false;
        }

        mDeviceList = null;
    }

    public boolean isServiceRunning(){
        return isServiceRunning;
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

    //写入数据
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

    public void setDiscoveryDeviceListener(DiscoveryDevicesListener listener){
        this.mDiscoveryDevicesListener = listener;

        mDeviceList = new ArrayList<>();
        if (mReceiver == null){
            mReceiver = new DiscoveryReceiver();
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, intentFilter);

        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, intentFilter);
        isRegister = true;
        mDeviceList.clear();

        if (isDiscoverying()){
            cancelDiscovery();
        }

        startDiscovery();

        if (mDiscoveryDevicesListener != null){
            mDiscoveryDevicesListener.startDiscovery();
        }

    }

    //发现蓝牙设备广播
    public class DiscoveryReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDiscoveryDevicesListener != null){
                    Log.e("lpc >>>", "onReceive --- device.toString: " + device.getName() + ":" + device.getAddress());
                    mDiscoveryDevicesListener.discoveryNew(device);
                }
                mDeviceList.add(device);
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if (mDiscoveryDevicesListener != null){
                    Log.e("lpc >>>", "onReceive --- mDeviceList.size() = " + mDeviceList.size());
                    mDiscoveryDevicesListener.discoveryFinish(mDeviceList);
                }
            }
        }
    }

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //读取数据
                case ConstantDefine.MESSAGE_STATE_READ:
                    byte[] data = (byte[]) msg.obj;
                    if (data != null && data.length > 0){
                        if (mReceiveDataListener != null){
                            mReceiveDataListener.onReceiveData(data);
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
                default:
                    break;
            }
        }
    };

    //************* 打印相关 ****************//
    //打印文本
    public void printText(String content){
        if (content.length() > 0){
            byte[] send;
            try{
                send = content.getBytes("GB2312");
            }catch (UnsupportedEncodingException ex){
                send = content.getBytes();
            }
            write(send);
        }
    }

    public void setLF(){
        write("\n".getBytes());
    }

    //打印图片
    public void printImage(Bitmap bitmap){
        byte[] data = BitmapUtils.bitmapToByte(bitmap, bitmap.getWidth(), 0);
        Log.i("lpc", "--> printImage() -- data.length = " + data.length);
        write(data);
    }


}
