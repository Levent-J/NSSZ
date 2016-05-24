package com.levent_j.nssz.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.levent_j.nssz.R;
import com.levent_j.nssz.base.BaseActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import butterknife.Bind;

/**
 * Created by levent_j on 16-5-5.
 */
public class PairActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.fab_discovery)
    FloatingActionButton mDiscovery;
    @Bind(R.id.txt)
    TextView txt;
    @Bind(R.id.lv_new_list)
    ListView mNewListView;
    @Bind(R.id.lv_paired_list)
    ListView mPairedListView;

    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号

    //对接受数据进行准备
    private InputStream inputStream;
    private String showMessage = "";
    private String saveMessage = "";

    //设备及Socket
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;

    private boolean bRun = true;
    private boolean bThread = false;

    private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    //搜索相关
    private BluetoothAdapter mSearchBtAdapter;
    private ArrayAdapter<String> mPairedAdapter;
    private ArrayAdapter<String> mNewAdapter;

    private boolean Flag = true;
    private String mAddress;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pair;
    }

    @Override
    protected void init() {
        if (mBtAdapter ==null){
            Toa("该手机不支持蓝牙");
        }

        new Thread(){
            @Override
            public void run() {
                super.run();
                if (!mBtAdapter.isEnabled()){
                    mBtAdapter.enable();
                }
            }
        }.start();

        //初始化设备存储数组
        mPairedAdapter = new ArrayAdapter<String>(this,R.layout.device_name);
        mNewAdapter = new ArrayAdapter<String>(this,R.layout.device_name);

        //设置列表
        mPairedListView.setAdapter(mPairedAdapter);
        mNewListView.setAdapter(mNewAdapter);

        //设置共同的监听事件
        mPairedListView.setOnItemClickListener(mDeviceClickListener);

        //注册接收器
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver,filter);

        mSearchBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void setListener() {
        mDiscovery.setOnClickListener(this);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //准备链接设备，关闭查找
            mSearchBtAdapter.cancelDiscovery();
            //获取mac地址
            String info = ((TextView)view).getText().toString();
            String address = info.substring(info.length()-17);
            mAddress = address;
            //通过mac地址进行操作
            Intent intent = new Intent(PairActivity.this,LoginActivity.class);
            intent.putExtra("address",mAddress);
            startActivity(intent);
        }
    };





    private boolean isFinal(String data) {
        //判断
        return true;
    }



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED){
                    mNewAdapter.add(device.getName()+"\n"+device.getAddress());
                }else {
                    mPairedAdapter.add("设备名："+device.getName()+"\n"+"MAC地址："+device.getAddress());
                }
            }else  if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if (mNewAdapter.getCount() == 0){
                    String noDevices = "未找到新设备";
                    mNewAdapter.add(noDevices);
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_discovery:
                //搜索
                if (!mBtAdapter.isEnabled()){
                    Toa("打开蓝牙中……");
                }else {
                    if (mSocket==null){
                        //进行搜索
                        SearchDevices();
                    }else {
                        //关闭Socket
                        try {
                            inputStream.close();
                            mSocket.close();
                            mSocket = null;
                            bRun = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                break;
        }
    }

    private void SearchDevices() {
        if (mSearchBtAdapter.isDiscovering()){
            mSearchBtAdapter.cancelDiscovery();
        }
        mSearchBtAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
