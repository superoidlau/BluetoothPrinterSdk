package com.example.lpc.bluetoothprintersdk;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.lpc.bluetoothsdk.BluetoothSdkManager;

import java.util.List;

/**
 * Description:
 * Created by lpc on 2017/3/26.
 */
public class DeviceAdapter extends BaseAdapter{

    private Context mContext;
    private List<BluetoothDevice> mListDevices;
    private LayoutInflater mInflater;

    private BluetoothSdkManager manager;

    public DeviceAdapter(Context context, List<BluetoothDevice> list,BluetoothSdkManager manager){
        mInflater = LayoutInflater.from(context);
        mListDevices = list;
        this.manager = manager;
    }


    @Override
    public int getCount() {
        return mListDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mListDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_list_device, null);
            holder.mTextDeviceName = (TextView) convertView.findViewById(R.id.item_device_name);
            holder.mTextDeviceAddress = (TextView) convertView.findViewById(R.id.item_device_address);
            holder.mBtnConnect = (Button) convertView.findViewById(R.id.item_connect);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTextDeviceName.setText(mListDevices.get(position).getName());
        holder.mTextDeviceAddress.setText(mListDevices.get(position).getAddress());
        holder.mBtnConnect.setOnClickListener(new BtnListener(position));

        holder.mBtnConnect.setFocusable(false);
        holder.mBtnConnect.setFocusableInTouchMode(false);


        return convertView;
    }

    class ViewHolder{
        TextView mTextDeviceName;
        TextView mTextDeviceAddress;
        Button mBtnConnect;
    }

    class BtnListener implements View.OnClickListener{

        private int position;

        public BtnListener(int position){
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.item_connect:
                    //点击连接该蓝牙设备
                    BluetoothDevice device = mListDevices.get(position);
                    if (manager != null && manager.isServiceAvailable()&& manager.isServiceRunning()){
                        manager.connect(device);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
