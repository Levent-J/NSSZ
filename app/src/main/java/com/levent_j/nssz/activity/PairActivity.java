package com.levent_j.nssz.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
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
    @Bind(R.id.btn_next)
    Button next;
    @Bind(R.id.btn_search)
    Button search;
    @Bind(R.id.txt)
    TextView txt;
//    @Bind(R.id.rv_new_list)
//    RecyclerView mNewDeviceRecycler;
//    @Bind(R.id.rv_paired_list)
//    RecyclerView mPairedDeviceRecycler;
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
        next.setOnClickListener(this);
        search.setOnClickListener(this);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //准备链接设备，关闭查找
            mSearchBtAdapter.cancelDiscovery();
            //获取mac地址
            String info = ((TextView)view).getText().toString();
            String address = info.substring(info.length()-17);
            //通过mac地址进行操作
            ConnectDevice(address);
        }
    };

    private void ConnectDevice(String address) {
        //通过mac地址得到设备
        mDevice = mBtAdapter.getRemoteDevice(address);
        //用UUID得到socket
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            Toa("链接失败！");
        }

        //开始建立连接
        try {
            mSocket.connect();
            Toa("链接"+mDevice.getName()+"成功！");
        } catch (IOException e) {
            Toa("链接断开！");
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e1) {
                Toa("链接失败！");
            }
            return;
        }

        //打开接收线程
        try {
            inputStream = mSocket.getInputStream();
        } catch (IOException e) {
            Toa("接收数据失败!");
            return;
        }

        if (!bThread){
            ReadThread.start();
            bThread = true;
        }else {
            bRun = true;
        }


    }

    //消息处理队列
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (Flag){
                txt.setText(showMessage);
                Flag = false;
            }else {
                txt.setText(showMessage);
                Toa(""+txt.getText());
                Flag = true;
            }


        }
    };

    //接收数据线程
    private Thread ReadThread = new Thread(){
        @Override
        public void run() {
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            //接收线程
            while(true){
                try{
                    while(inputStream.available()==0){
                        while(bRun == false){}
                    }
                    while(true){

                        num = inputStream.read(buffer);         //读入数据
                        n=0;

                        String s0 = new String(buffer,0,num);
                        saveMessage+=s0;    //保存收到数据
                        for(i=0;i<num;i++){
                            if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
                                buffer_new[n] = 0x0a;
                                i++;
                            }else{
                                buffer_new[n] = buffer[i];
                            }
                            n++;
                        }
                        String s = new String(buffer_new,0,n);
                        showMessage+=s;   //写入接收缓存

                        if(inputStream.available()==0)break;  //短时间没有数据才跳出进行显示
                    }
                    //发送显示消息，进行显示刷新

                    handler.sendMessage(handler.obtainMessage());
                } catch (IOException e){}
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED){
                    mNewAdapter.add(device.getName()+"\n"+device.getAddress());
                }else {
                    mPairedAdapter.add(device.getName()+"\n"+device.getAddress());
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
            case R.id.btn_next:
                startActivity(new Intent(PairActivity.this,DeviceListActivity.class));
                break;
            case R.id.btn_search:
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
