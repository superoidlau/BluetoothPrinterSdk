# BluetoothPrinterSdk
热敏蓝牙打印机SDK

## Dependency

[![](https://jitpack.io/v/Forecheng/BluetoothPrinterSdk.svg)](https://jitpack.io/#Forecheng/BluetoothPrinterSdk)

1.Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2.Add it in module build.gradle:
```
compile 'com.github.Forecheng:BluetoothPrinterSdk:-SNAPSHOT'
```
## Functions
主要方法：

|id|name|return|desc
|--|:--:|--|:--:|
|1|isBluetoothSupported()|boolean|判断设备是否支持蓝牙|
|2|isBluetoothEnabled()|boolean|判断蓝牙是否已开启|
|3|setupService()|void|开启蓝牙服务|
|4|stopService()|void|停止服务|
|5|connect(String address)|void|使用mac地址蓝牙|
|6|connect(BluetoothDevice device)|void|传入设备实例进行连接|
|7|disconnect()|void|断开蓝牙设备连接|
|8|write(byte[] data)|void|向连接流中写入数据|
|9|printText(String content)|void|打印文字|
|10|printImage(Bitmap bitmap)|void|打印图片|

几种回调：

|id|name|desc|
|--|:--:|:--:|
|1|setBlueStateListener(BluetoothStateListener listener)|蓝牙连接状态回调|
|2|setReceiveDataListener(IReceiveDataListener listener)|接收蓝牙数据回调|
|3|setBluetoothConnectListener(BluetoothConnectListener listener)|连接结果回调|
|4|setDiscoveryDeviceListener(DiscoveryDevicesListener listener)|搜索蓝牙设备结果回调|

## Usage
主要使用的类：BluetoothSdkManager
```java
BluetoothSdkManager manager = new BluetoothSdkManager(context);
```
1.所有的接口都使用`manager`进行调用;

2.在`onStart()`中开启蓝牙服务：开始监听蓝牙设备的连接;
```java
@Override
protected void onStart() {
    super.onStart();
    if (manager != null) {
        manager.setupService();
    }
}
```
3.调用`setDiscoveryDeviceListener`进行设备搜索，不需调用`startDiscovery()`;
