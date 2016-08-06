package com.levent_j.nssz.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.levent_j.nssz.R;
import com.levent_j.nssz.activity.MainActivity;
import com.levent_j.nssz.adapter.DeviceAdapter;
import com.levent_j.nssz.base.BaseFragment;
import com.levent_j.nssz.entry.Device;
import com.levent_j.nssz.utils.DeviceCheckUtil;
import com.levent_j.nssz.utils.SpaceItemDecoration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by levent_j on 16-7-28.
 */
public class DeviceFragment extends BaseFragment{
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.rv_devices) RecyclerView recyclerView;

    /**设备列表*/
    private DeviceAdapter deviceAdapter;
    private List<Device> deviceList;

    /**报警检测*/
    private Timer timer;
    private TimerTask timerTask;
    private boolean isChecking = false;

    /**报警检测，温服、湿度*/
    public static int TEMP_MAX = 100;
    public static int TEMP_MIN = 0;
    public static int HUM_MAX = 100;
    public static int HUM_MIN = 0;

    /**振动器*/
    private Vibrator vibrator;

    /**蓝牙输入流*/
    private InputStream inputStream;

    /**蓝牙串口uuid*/
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号

    /**蓝牙设备及socket*/
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;

    /**蓝牙链接状态*/
    private boolean bRun = true;
    private boolean bThread = false;

    /**蓝牙接收数据缓存池*/
    private int[] mDeviceDetail = new int[5];

    private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    public static DeviceFragment newInstance(){
        return new DeviceFragment();
    }

    @Override
    protected int setRootViewId() {
        return R.layout.fragment_device;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /**开启振动*/
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        deviceList = new ArrayList<>();

        /**实例化adapter与list*/
        deviceAdapter = new DeviceAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.space)));
        recyclerView.setAdapter(deviceAdapter);

        /**初始化检测设备的task*/
        initCheckTask();

        //TODO:暂时以假数据测试，之后要去掉注释的
//        ConnectDevice(MainActivity.mDeviceMacAddress);

        //TODO:测试用填充假数据
        loadFakeData();
    }

    private void loadFakeData() {
        deviceList.clear();
        for (int i=0;i<5;i++){
            Device device = new Device();
            device.setDeviceNumber(i+1);
            device.setState((int) (2 + Math.random() * (4 - 2 + 1)));
            device.setTemperature((int) (25 + Math.random() * (30 - 25 + 1)));
            device.setTemperatureDecimal((int) (0 + Math.random() * (10 - 0 + 1)));
            device.setHumidity((int) (40 + Math.random() * (60 - 40 + 1)));
            deviceList.add(device);
//            Fakehandler.sendMessage(Fakehandler.obtainMessage());
        }
        deviceAdapter.updateDeviceList(deviceList);
        recyclerView.setAdapter(deviceAdapter);

    }

    private Handler Fakehandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            deviceAdapter.updateDeviceList(deviceList);
            recyclerView.setAdapter(deviceAdapter);
        }
    };

    private Handler checkHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            checkDevices();
            super.handleMessage(msg);
        }
    };



    /**处理获取到的设备信息*/
    private Handler loadDateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Device device = new Device();
            device.setState(mDeviceDetail[0]);
            device.setDeviceNumber(mDeviceDetail[4]);
            device.setTemperature(mDeviceDetail[1]);
            device.setTemperatureDecimal(mDeviceDetail[2]);
            device.setHumidity(mDeviceDetail[3]);

            //判断一下是否存在
            if (DeviceCheckUtil.isExist(mDeviceDetail[4],deviceList)){
                int index = DeviceCheckUtil.getIndex(mDeviceDetail[4],deviceList);
                deviceList.set(index,device);
            }else {
                deviceList.add(device);
            }

            deviceAdapter.updateDeviceList(deviceList);
            recyclerView.setAdapter(deviceAdapter);
        }
    };


    private void initCheckTask(){
        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                checkHandler.sendMessage(message);
            }
        };
    }

    private void checkDevices() {
        for (Device device:deviceList){
            //对报警限制做判断
            //TODO:测试方便，暂时注释了震动与移动检测
//            if (device.getState()==3){
//                Toa("标签卡"+device.getDeviceNumber()+"受到了震动！！！");
//                vibrator.vibrate(2000);
//            }
//            if (device.getState()==4){
//                Toa("标签卡"+device.getDeviceNumber()+"被非法移动了！！！");
//                vibrator.vibrate(2000);
//            }
            //温度湿度检测另算
            if ((device.getTemperature()+device.getTemperatureDecimal()/10)>= TEMP_MAX){
                Toa("标签卡"+device.getDeviceNumber()+"温度过高！");
                vibrator.vibrate(2000);
            }
            if (device.getHumidity()>= HUM_MAX){
                Toa("标签卡"+device.getDeviceNumber()+"湿度过高！");
                vibrator.vibrate(2000);
            }
            if ((device.getTemperature()+device.getTemperatureDecimal()/10)< TEMP_MIN){
                Toa("标签卡"+device.getDeviceNumber()+"温度过低！");
                vibrator.vibrate(2000);
            }
            if (device.getHumidity()< HUM_MIN){
                Toa("标签卡"+device.getDeviceNumber()+"湿度过低！");
                vibrator.vibrate(2000);
            }
        }
    }


//    private int getIndex(int i) {
//        for (int j=0;j<deviceList.size();j++){
//            if (i==deviceList.get(j).getDeviceNumber()){
//                return j;
//            }
//        }
//        return 0;
//    }

//    private boolean isExist(int name) {
//        for (Device d:deviceList){
//            if (d.getDeviceNumber()==name){
//                return true;
//            }
//        }
//        return false;
//    }

    public void sendMessage(){
        //TODO:这里可以换成我自己写死的发起请求的语句
        try{
            OutputStream os = mSocket.getOutputStream();   //蓝牙连接输出流
            //要发送的数据
            String s = "12345";
            byte[] bos = s.getBytes();
            os.write(bos);
        }catch(IOException e){
        }
    }

    public void ConnectDevice(String address) {
        /**通过mac地址得到设备*/
        mDevice = mBtAdapter.getRemoteDevice(address);
        /**用UUID得到socket*/
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            Log.e("Socket->get error",e.getMessage());
        }
        /**开始建立连接*/
        try {
            mSocket.connect();
        } catch (IOException e) {
            Log.e("Socket->connect error",e.getMessage());
            showDialog();
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e1) {
                Log.e("Socket->close error",e.getMessage());
                showDialog();
            }
            return;
        }
        /**打开接收线程*/
        try {
            inputStream = mSocket.getInputStream();
        } catch (IOException e) {
            showDialog();
            return;
        }

        /**开启发送数据线程，每3秒发送一次数据*/
        SendThread.start();

        if (!bThread){
            ReadThread.start();
            bThread = true;
        }else {
            bRun = true;
        }

    }

    /**发送数据的线程*/
    private Thread SendThread = new Thread(){
        @Override
        public void run() {
            super.run();
            while (true){
                sendMessage();
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    /**接收数据线程*/
    private Thread ReadThread = new Thread(){
        @Override
        public void run() {
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_cache = new byte[1024];
            int i = 0;
            int size = 0;
            bRun = true;
            //接收线程
            while(true){
                try{
                    while(inputStream.available()==0){
                        while(bRun == false){}
                    }
                    while(true){
                        //获取本次读取到的字节长度
                        num = inputStream.read(buffer);
                        Log.e("DATA","字节长度="+num);

                        //新方法
                        for (i=0;i<num;i++){
                            buffer_cache[size] = buffer[i];
                            Log.e("DATA","buffer = "+buffer[i]);
                            size++;
                        }


                        if(inputStream.available()==0)break;  //短时间没有数据才跳出进行显示
                    }
                    Log.e("DATA","size="+size);
                    //发送显示消息，进行显示刷新
                    //判断缓存区是否已满
                    if (size>=9){
                        size=0;
                        Log.e("DATA","0=<"+Integer.parseInt(String.valueOf(buffer_cache[0]))
                                +">1=<"+Integer.parseInt(String.valueOf(buffer_cache[1]))+">");
                        if ((Integer.parseInt(String.valueOf(buffer_cache[0]))==13)
                                &&(Integer.parseInt(String.valueOf(buffer_cache[1]))==10)
                                &&(Integer.parseInt(String.valueOf(buffer_cache[7]))==10)
                                &&(Integer.parseInt(String.valueOf(buffer_cache[8]))==13)){
                            for (int j=2;j<7;j++){
                                mDeviceDetail[j-2]=buffer_cache[j];
                            }

                            loadDateHandler.sendMessage(loadDateHandler.obtainMessage());
                        }
                    }
                } catch (IOException e){

                }
            }
        }
    };

    /**链接失败时弹出dialog*/
    public void showDialog(){
        new AlertDialog.Builder(getContext())
                .setTitle("提示").setMessage("与蓝牙设备链接失败，是否重新链接？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ConnectDevice(MainActivity.mDeviceMacAddress);
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setCancelable(false)
                .show();
    }


    @OnClick(R.id.fab)
    public void onClickCheckBtn(View view){
        if (!isChecking){
            Snackbar.make(view,"开启检测",Snackbar.LENGTH_SHORT).show();

            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
            initCheckTask();
            timer.scheduleAtFixedRate(timerTask, 1000, 10000);
            isChecking = true;
        }else {
            Snackbar.make(view,"关闭检测",Snackbar.LENGTH_SHORT).show();

            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            timer.cancel();
            isChecking = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkHandler.removeCallbacksAndMessages(null);
    }
}
