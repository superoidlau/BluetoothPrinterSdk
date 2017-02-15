package com.example.lpc.bluetoothsdk.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.EventLogTags;
import android.util.Log;

import com.example.lpc.bluetoothsdk.constant.ConstantDefine;

import java.io.IOException;
import java.util.UUID;

/**
 * Description: 连接蓝牙设备
 * Created by lpc on 2017/2/14.
 */
@SuppressLint("NewApi")
public class BluetoothService {

    private static final String TAG = "BluetoothService";

    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private int mState;
    private boolean isAndroidDevice = ConstantDefine.DEVICE_ANDROID;

    //连接所需的UUID
    private final UUID UUID_DEVICE_ANDROID = UUID.fromString(ConstantDefine.STRING_DEVICE_ANDROID);
    private final UUID UUID_DEVICE_PRINTER = UUID.fromString(ConstantDefine.STRING_DEVICE_PRINTER);
    private final static String MY_NAME = "BluetoothSDK";

    public BluetoothService(Handler handler){
        this.mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = ConstantDefine.CONNECT_STATE_NONE;
    }

    public synchronized void setState(int state){
        Log.i(TAG, "setState: " + mState + "----->" + state);

        this.mState = state;
        //连接状态改变
        mHandler.obtainMessage(ConstantDefine.MESSAGE_STATE_CHANGE, state,-1).sendToTarget();
    }

    public synchronized int getState(){
        return mState;
    }


    //--------相关操作线程类---------//
    //连接为服务器
    public class AcceptThread extends  Thread {
        private final BluetoothServerSocket mBluetoothServerSocket;
        private boolean isRunning = true;

        public AcceptThread(boolean isAndroid) {
            BluetoothServerSocket tempServerSocket = null;
            try {
                if (isAndroid) {
                    tempServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_NAME, UUID_DEVICE_ANDROID);
                } else {
                    tempServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_NAME, UUID_DEVICE_PRINTER);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            mBluetoothServerSocket = tempServerSocket;
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;

            while (mState != ConstantDefine.CONNECT_STATE_CONNECTED && isRunning) {
                try {
                    socket = mBluetoothServerSocket.accept();
                } catch (IOException ex) {
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case ConstantDefine.CONNECT_STATE_LISTENER:
                            case ConstantDefine.CONNECT_STATE_CONNECTING:
                                //进行连接
                                break;
                            case ConstantDefine.CONNECT_STATE_NONE:
                            case ConstantDefine.CONNECT_STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel(){
            try{
                mBluetoothServerSocket.close();
                isRunning = false;
            }catch (IOException ex){}
        }
    }

    //与远程蓝牙设备进行连接
    public class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device){
            BluetoothSocket tempSocket = null;
            this.mmDevice = device;

            try{
                if (BluetoothService.this.isAndroidDevice){
                    tempSocket = device.createRfcommSocketToServiceRecord(UUID_DEVICE_ANDROID);
                }else {
                    tempSocket = device.createRfcommSocketToServiceRecord(UUID_DEVICE_PRINTER);
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }

            mmSocket = tempSocket;
        }

        @Override
        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try{
                mmSocket.connect();
            }catch (IOException ex){
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }

                return;
            }


            synchronized (BluetoothService.this){

            }

            // TODO: 2017/2/15 连接完成后，两个设备之间进行数据传输

        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException ex){

            }
        }
    }

    //两个连接成功的设备之间进行数据传输
    public class ConnectedThread extends Thread{



        @Override
        public void run() {

        }
    }

}
