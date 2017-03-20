package com.example.lpc.bluetoothprintersdk;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView mListDevices;
    private Button mBtnSearchDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        initViews();

        initListener();
    }

    public void initViews(){
        mListDevices = (ListView) findViewById(R.id.deviceList);
        mBtnSearchDevices = (Button) findViewById(R.id.scan);
    }

    public void initListener(){
        mBtnSearchDevices.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //单击进行蓝牙设备的搜索
            case R.id.scan:
                break;
            default:
                break;
        }
    }
}
