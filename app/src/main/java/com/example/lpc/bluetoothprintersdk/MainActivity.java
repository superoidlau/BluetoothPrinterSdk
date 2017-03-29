package com.example.lpc.bluetoothprintersdk;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lpc.bluetoothsdk.BluetoothSdkManager;
import com.example.lpc.bluetoothsdk.constant.ConstantDefine;
import com.example.lpc.bluetoothsdk.listener.BluetoothConnectListener;
import com.example.lpc.bluetoothsdk.listener.BluetoothStateListener;
import com.example.lpc.bluetoothsdk.listener.DiscoveryDevicesListener;
import com.example.lpc.bluetoothsdk.listener.IReceiveDataListener;
import com.example.lpc.bluetoothsdk.util.BitmapUtils;
import com.example.lpc.bluetoothsdk.util.PrintUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private ListView mList;
    private Button mBtnSearchDevices;
    private Button mBtnPrintTest;

    private BluetoothSdkManager manager;

    private List<BluetoothDevice> mListDevices;
    private DeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        mContext = this;

        initViews();

        initListener();
    }

    public void initViews() {
        mList = (ListView) findViewById(R.id.deviceList);
        mBtnSearchDevices = (Button) findViewById(R.id.scan);
        mBtnPrintTest = (Button) findViewById(R.id.printTest);

        mListDevices = new ArrayList<>();
        manager = new BluetoothSdkManager(this);


        if (!manager.isBluetoothSupported()) {
            Toast.makeText(this, "此设备不支持蓝牙...", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!manager.isBluetoothEnabled()) {
                Toast.makeText(this, "蓝牙没有开启，正在强制开启...", Toast.LENGTH_SHORT).show();
                manager.getBluetoothAdapter().enable();
            }
        }
    }

    public void initListener() {
        mBtnSearchDevices.setOnClickListener(this);
        mBtnPrintTest.setOnClickListener(this);

        //接收蓝牙数据回调
        manager.setReceiveDataListener(new IReceiveDataListener() {
            @Override
            public void onReceiveData(byte[] data) {

            }
        });

        //连接状态结果回调
        manager.setBlueStateListener(new BluetoothStateListener() {
            @Override
            public void onConnectStateChanged(int state) {
                switch (state) {
                    case ConstantDefine.CONNECT_STATE_NONE:
                        Log.i("main", "  -----> none <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_LISTENER:
                        Log.i("main", "  -----> listener <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_CONNECTING:
                        Log.i("main", "  -----> connecting <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_CONNECTED:
                        Log.i("main", "  -----> connected <----");
                        break;
                }
            }
        });

        manager.setBluetoothConnectListener(new BluetoothConnectListener() {
            @Override
            public void onBTDeviceConnected(String address, String name) {
                Toast.makeText(MainActivity.this, "已连接到名称为" + name + "的设备", Toast.LENGTH_SHORT).show();
                mBtnPrintTest.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBTDeviceDisconnected() {
                Toast.makeText(MainActivity.this, "连接已经断开，请重新尝试连接...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBTDeviceConnectFailed() {
                Toast.makeText(MainActivity.this, "连接失败，请重新连接...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (manager != null) {
            manager.setupService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.stopService();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //单击进行蓝牙设备的搜索
            case R.id.scan:
                startSearchBT();
                break;
            case R.id.printTest:
                printTest();
            default:
                break;
        }
    }

    //开始扫描蓝牙设备
    public void startSearchBT() {
        //如果正在扫描的话先取消掉当前的扫描
        if (manager.isDiscoverying()) {
            manager.cancelDiscovery();
        } else {
            //扫描蓝牙设备回调
            manager.setDiscoveryDeviceListener(new DiscoveryDevicesListener() {
                @Override
                public void startDiscovery() {
                    Toast.makeText(MainActivity.this, "开始搜索蓝牙设备...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void discoveryNew(BluetoothDevice device) {
                    Toast.makeText(MainActivity.this, "发现新的蓝牙设备...", Toast.LENGTH_SHORT).show();
                    mListDevices.add(device);
                }

                @Override
                public void discoveryFinish(List<BluetoothDevice> list) {
                    Log.e("main >>>", "startSearchBT --- discoveryFinish() --- list.size(): " + list.size() );
                    Toast.makeText(MainActivity.this, "搜索完成，共发现 <" + list.size() + ">" + "个蓝牙设备", Toast.LENGTH_SHORT).show();
                    if (!list.isEmpty()){
                        mAdapter = new DeviceAdapter(mContext, list, manager);
                        mList.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }

                }
            });
        }
    }

    //打印测试
    public void printTest() {
        manager.printText("可以正常打印出这句话吗？\n");
        manager.printText("Hello World.\n");

        manager.printImage(markImage());
    }

    private Bitmap markImage() {
        try {
            Resources res = mContext.getResources();

            Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.logo);
            Bitmap bitmapOrg = null;// BitmapFactory.decodeFile(picPath);

            if (bmp != null) {

                bitmapOrg = BitmapUtils.resizeImage(bmp, 48 * 8, 48 * 4);

            }
            return bitmapOrg;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
