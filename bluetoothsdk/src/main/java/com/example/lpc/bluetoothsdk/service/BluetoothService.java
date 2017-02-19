package com.example.lpc.bluetoothsdk.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.lpc.bluetoothsdk.constant.ConstantDefine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    //连接所需的UUID
    private final UUID UUID_DEVICE_PRINTER = UUID.fromString(ConstantDefine.STRING_DEVICE_PRINTER);
    private final static String MY_NAME = "BluetoothSDK";

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

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

    //开启线程，监听来自打印机的数据
    public synchronized void start(){
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(ConstantDefine.CONNECT_STATE_LISTENER);

        if (mAcceptThread == null){
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    //开启线程，连接远程的蓝牙设备
    public synchronized void connect(BluetoothDevice device){
        if (mState == ConstantDefine.CONNECT_STATE_CONNECTING){
            if (mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(ConstantDefine.CONNECT_STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        //告诉Activity连接成功的蓝牙设备信息
        Message message = mHandler.obtainMessage(ConstantDefine.MESSAGE_DEVICE_INFO);
        Bundle bundle = new Bundle();
        bundle.putString(ConstantDefine.KEY_DEVICE_NAME, device.getName());
        bundle.putString(ConstantDefine.KEY_DEVICE_ADDRESS, device.getAddress());
        message.setData(bundle);
        mHandler.sendMessage(message);

        setState(ConstantDefine.CONNECT_STATE_CONNECTED);

    }

    public synchronized void stop(){
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread.kill();
            mAcceptThread = null;
        }
        setState(ConstantDefine.CONNECT_STATE_NONE);
    }

    //向远程蓝牙设备发送数据
    public synchronized void write(byte[] data){
        ConnectedThread thread;

        synchronized (this){
            if (mState != ConstantDefine.CONNECT_STATE_CONNECTED){
                return;
            }
            thread = mConnectedThread;
        }

        thread.write(data);
    }

    //连接失败时重新开启
    private void connectionFailed() {
        BluetoothService.this.start();
    }

    //连接断开时重新开启
    private void connectionLost() {
        BluetoothService.this.start();
    }

    //--------相关操作线程类---------//
    //连接为服务器
    public class AcceptThread extends  Thread {
        private BluetoothServerSocket mBluetoothServerSocket;
        private boolean isRunning = true;

        public AcceptThread() {
            BluetoothServerSocket tempServerSocket = null;
            try {
                tempServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_NAME, UUID_DEVICE_PRINTER);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            mBluetoothServerSocket = tempServerSocket;
        }

        @Override
        public void run() {
            BluetoothSocket socket ;

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
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case ConstantDefine.CONNECT_STATE_NONE:
                            case ConstantDefine.CONNECT_STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }

        public void cancel(){
            try{
                mBluetoothServerSocket.close();
                mBluetoothServerSocket = null;
                isRunning = false;
            }catch (IOException ex){}
        }

        public void kill() {
            isRunning = false;
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
                tempSocket = device.createRfcommSocketToServiceRecord(UUID_DEVICE_PRINTER);

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
                connectionFailed();
                return;
            }

            synchronized (BluetoothService.this){
                mConnectThread = null;
            }

            // TODO: 2017/2/15 连接完成后，两个设备之间进行数据传输
            connected(mmSocket, mmDevice);
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    //两个连接成功的设备之间进行数据传输
    public class ConnectedThread extends Thread{

        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try{
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

            }catch (IOException ex){
                ex.printStackTrace();
            }

            mmInputStream = inputStream;
            mmOutputStream = outputStream;
        }

        @Override
        public void run() {
            byte[] bufferDatas = new byte[1024];
            //读取到的字节数
            int bytes;
            while(true){
                try{
                    bytes = mmInputStream.read(bufferDatas);
                    mHandler.obtainMessage(ConstantDefine.MESSAGE_STATE_READ, bytes, -1, bufferDatas).sendToTarget();
                }catch (IOException ex){
                    ex.printStackTrace();
                    // 读取数据失败处理
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] data){
            try{
                mmOutputStream.write(data);
                mHandler.obtainMessage(ConstantDefine.MESSAGE_STATE_WRITE, -1, -1, data).sendToTarget();
            }catch (IOException ex){

            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

}
