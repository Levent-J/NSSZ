package com.levent_j.nssz.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.levent_j.nssz.R;
import com.levent_j.nssz.activity.MainActivity;
import com.levent_j.nssz.adapter.DeviceAdapter;
import com.levent_j.nssz.base.BaseFragment;
import com.levent_j.nssz.entry.Device;
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
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.rv_devices)
    RecyclerView recyclerView;

    private DeviceAdapter deviceAdapter;
    private List<Device> deviceList;

    private Timer timer;
    private TimerTask timerTask;
    private Handler checkhandler;

    private boolean isChecking = false;

    private Vibrator vibrator;

    //对接受数据进行准备
    private InputStream inputStream;

    //设备及Socket
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;

    private boolean bRun = true;
    private boolean bThread = false;

    //接收到的数据
    private int[] details = new int[5];

    public static int Temperature = 100;
    public static int Humidity = 100;

    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号


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
//        super.onViewCreated(view, savedInstanceState);
        /**开启振动*/
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        /**实例化adapter与list*/
        deviceAdapter = new DeviceAdapter(getContext());
        deviceList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.space);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        recyclerView.setAdapter(deviceAdapter);
        /**初始化检测设备的task*/
        timer = new Timer();
        checkhandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                checkDevices();
                super.handleMessage(msg);
            }
        };
        initTask();


        //TODO:暂时以假数据测试，之后要去掉注释的
//        ConnectDevice(MainActivity.mAddress);


        //TODO:测试用填充假数据
        loadFakeData();

        /**开启线程，每3秒发送一次数据*/
//        Thread sendHandler = new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                while (true){
//                    sendMessage();
//                    try {
//                        sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        };
//        sendHandler.start();


    }

    private void loadFakeData() {
        deviceList.clear();
        for (int i=0;i<5;i++){
            Device device = new Device();
            device.setDeviceNumber(i);
            device.setState((int) (2 + Math.random() * (4 - 2 + 1)));
            device.setTemperature((int) (25 + Math.random() * (30 - 25 + 1)));
            device.setTemperatureDecimal((int) (0 + Math.random() * (100 - 0 + 1)));
            device.setHumidity((int) (40 + Math.random() * (60 - 40 + 1)));
            deviceList.add(device);
            Fakehandler.sendMessage(Fakehandler.obtainMessage());
        }

    }

    private Handler Fakehandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            deviceAdapter.updateDeviceList(deviceList);
            recyclerView.setAdapter(deviceAdapter);
        }
    };

    private void initTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                checkhandler.sendMessage(message);
            }
        };
    }
    private void initTimer() {
        timer = new Timer();
    }

    private void checkDevices() {
        for (Device device:deviceList){
            //对报警限制做判断
            if (device.getState()==3){
                Toa("标签卡"+device.getDeviceNumber()+"受到了震动！！！");
                vibrator.vibrate(2000);
            }
            if (device.getState()==4){
                Toa("标签卡"+device.getDeviceNumber()+"被非法移动了！！！");
                vibrator.vibrate(2000);
            }
            //温度湿度检测另算
            if (device.getTemperature()>=Temperature){
                Toa("标签卡"+device.getDeviceNumber()+"温度过高！！！");
                vibrator.vibrate(2000);
            }
            if (device.getHumidity()>=Humidity){
                Toa("标签卡"+device.getDeviceNumber()+"湿度过高！！！");
                vibrator.vibrate(2000);
            }
        }
    }

    private void loadBtBata() {
        Device device = new Device();
        device.setState(details[0]);
        device.setDeviceNumber(details[1]);
        device.setTemperature(details[2]);
        device.setTemperatureDecimal(details[3]);
        device.setHumidity(details[4]);

        //判断一下是否存在
        if (isExist(details[1])){
            int index = getIndex(details[1]);
            deviceList.set(index,device);
        }else {
            deviceList.add(device);
        }

        deviceAdapter.updateDeviceList(deviceList);
        recyclerView.setAdapter(deviceAdapter);
    }

    private int getIndex(int i) {
        for (int j=0;j<deviceList.size();j++){
            if (i==deviceList.get(j).getDeviceNumber()){
                return j;
            }
        }
        return 0;
    }

    private boolean isExist(int name) {
        for (Device d:deviceList){
            if (d.getDeviceNumber()==name){
                return true;
            }
        }
        return false;
    }

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
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            loadBtBata();
        }
    };

    //接收数据线程
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
                        Log.e("DATA","num"+num);

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
                        Log.e("DATA","0=<"+Integer.parseInt(String.valueOf(buffer_cache[0]))+">1=<"+Integer.parseInt(String.valueOf(buffer_cache[1]))+">");
                        if ((Integer.parseInt(String.valueOf(buffer_cache[0]))==13)&&(Integer.parseInt(String.valueOf(buffer_cache[1]))==10)
                                &&(Integer.parseInt(String.valueOf(buffer_cache[7]))==10)&&(Integer.parseInt(String.valueOf(buffer_cache[8]))==13)){
                            for (int j=2;j<7;j++){
                                details[j-2]=buffer_cache[j];
                            }

                            handler.sendMessage(handler.obtainMessage());
                        }
                    }
                } catch (IOException e){

                }
            }
        }
    };

    @OnClick(R.id.fab)
    public void onClickCheckBtn(){
        if (!isChecking){
            Toa("START");
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
            initTimer();
            initTask();


            timer.scheduleAtFixedRate(timerTask, 1000, 10000);
            isChecking = true;
        }else {
            Toa("STOP");
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            timer.cancel();
            isChecking = false;
        }
    }
}
