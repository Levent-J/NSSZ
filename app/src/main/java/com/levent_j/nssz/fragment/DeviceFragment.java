package com.levent_j.nssz.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
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
import com.wang.avi.AVLoadingIndicatorView;

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
    @Bind(R.id.loading) AVLoadingIndicatorView loading;

    /**设备列表*/
    private DeviceAdapter deviceAdapter;
    private List<Device> deviceList;

    /**报警检测*/
    private Timer checkTimer;
    private TimerTask checkTImeTask;
    private boolean isChecking = false;

    private Timer overTimer1;
    private TimerTask overTimeTask1;
    private Timer overTimer2;
    private TimerTask overTimeTask2;
    private Timer overTimer3;
    private TimerTask overTimeTask3;

    /**超时检测时间间隔*/
    private static int OVER_TIME_DELAY = 40000;

    /**判断三个标签卡是否存在*/
    private boolean isExist1 = false;
    private boolean isExist2 = false;
    private boolean isExist3 = false;
    private boolean isFirst = true;
    private boolean isTimeRuning1 = false;
    private boolean isTimeRuning2 = false;
    private boolean isTimeRuning3 = false;

    /**报警检测，温服、湿度*/
    public static int TEMP_MAX = 100;
    public static int TEMP_MIN = 0;
    public static int HUM_MAX = 100;
    public static int HUM_MIN = 0;

    /**报警声播放器*/
    private MediaPlayer mediaPlayer;

    /**振动器*/
    private Vibrator vibrator;

    /**蓝牙输入流*/
    private InputStream inputStream;

    /**蓝牙串口uuid*/
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";//SPP服务UUID号

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
        mediaPlayer = MediaPlayer.create(getContext(),R.raw.bao);
//        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
//        getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        deviceList = new ArrayList<>();

        /**实例化adapter与list*/
        deviceAdapter = new DeviceAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.space)));
        recyclerView.setAdapter(deviceAdapter);

        recyclerView.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);

        /**初始化检测设备的task*/
        initCheckTask();

        startConnectThread();

//        Thread fake = new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                startOvertimeListener();
//                while (true){
//                    fakeHandler.sendMessage(fakeHandler.obtainMessage());
//                    try {
//                        sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        fake.start();
    }

    private void startConnectThread(){
        Thread connectThread = new Thread(){
            @Override
            public void run() {
                super.run();
                ConnectDevice(MainActivity.mDeviceMacAddress);
            }
        };
        connectThread.start();
    }

//    private Handler fakeHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            loadFakeData();
//        }
//    };
//
//    private void loadFakeData() {
//
//        overTimeTask1.cancel();
//
//        loading.setVisibility(View.GONE);
//        recyclerView.setVisibility(View.VISIBLE);
//        deviceList.clear();
//        for (int i=0;i<3;i++){
//            Device device = new Device();
//            device.setDeviceNumber(i+1);
//            device.setState((int) (2 + Math.random() * (4 - 2 + 1)));
//            device.setTemperature((int) (25 + Math.random() * (30 - 25 + 1)));
//            device.setTemperatureDecimal((int) (0 + Math.random() * (10 - 0 + 1)));
//            device.setHumidity((int) (40 + Math.random() * (60 - 40 + 1)));
//            deviceList.add(device);
//        }
//        deviceAdapter.updateDeviceList(deviceList);
//
//        startOvertimeListener(1);
//        startOvertimeListener(2);
//        startOvertimeListener(3);
//
//    }

    private void startOvertimeListener(int id) {
        if (id==1){
            overTimer1 = new Timer();
            overTimeTask1 = new TimerTask() {
                @Override
                public void run() {
                    overTimer1.cancel();
                    isTimeRuning1 =false;
                    overTimeHandler1.sendMessage(overTimeHandler1.obtainMessage());
                }
            };
            overTimer1.schedule(overTimeTask1,OVER_TIME_DELAY);
            isTimeRuning1 = true;
        }else if (id==2){
            overTimer2 = new Timer();
            overTimeTask2 = new TimerTask() {
                @Override
                public void run() {
                    overTimer2.cancel();
                    isTimeRuning2 = false;
                    overTimeHandler2.sendMessage(overTimeHandler2.obtainMessage());
                }
            };
            overTimer2.schedule(overTimeTask2,OVER_TIME_DELAY);
            isTimeRuning2 = true;
        }else {
            overTimer3 = new Timer();
            overTimeTask3 = new TimerTask() {
                @Override
                public void run() {
                    overTimer3.cancel();
                    isTimeRuning3 = false;
                    overTimeHandler3.sendMessage(overTimeHandler3.obtainMessage());
                }
            };
            overTimer3.schedule(overTimeTask3,OVER_TIME_DELAY);
            isTimeRuning3 = true;
        }
    }

    private Handler overTimeHandler1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isChecking&&isExist1){
                showDialog("标签卡1异常!");
            }
        }
    };

    private Handler overTimeHandler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isChecking&&isExist2){
                showDialog("标签卡2异常!");
            }
        }
    };

    private Handler overTimeHandler3 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isChecking&&isExist3){
                showDialog("标签卡3异常!");
            }
        }
    };

    private Handler checkHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            checkDevices();
        }
    };

    private Handler dialogHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showDialog();
        }
    };

    /**处理获取到的设备信息*/
    private Handler loadDateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mDeviceDetail[0]==2){

                initDataByNum(mDeviceDetail[4]);

                //温度湿度
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
            }else if (mDeviceDetail[0]==3){

                initDataByNum(mDeviceDetail[1]);

                if (DeviceCheckUtil.isExist(mDeviceDetail[1],deviceList)){
                    int index = DeviceCheckUtil.getIndex(mDeviceDetail[1],deviceList);
                    Device device = deviceList.get(index);
                    device.setState(3);
                    deviceList.set(index,device);
                }else {
                    Device device = new Device();
                    device.setDeviceNumber(mDeviceDetail[1]);
                    device.setState(3);
                    deviceList.add(device);
                }

            }else {

                initDataByNum(mDeviceDetail[1]);

                if (DeviceCheckUtil.isExist(mDeviceDetail[1],deviceList)){
                    int index = DeviceCheckUtil.getIndex(mDeviceDetail[1],deviceList);
                    Device device = deviceList.get(index);
                    device.setState(4);
                    deviceList.set(index,device);
                }else {
                    Device device = new Device();
                    device.setDeviceNumber(mDeviceDetail[1]);
                    device.setState(4);
                    deviceList.add(device);
                }
            }

            deviceAdapter.updateDeviceList(deviceList);

            loading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

        }
    };

    private void initDataByNum(int id){
        setExist(id);
        if (isFirst){
            startOvertimeListener(id);
            isFirst = false;
        }else {
            //根据标签卡号取消检测超时任务
            cancelTimerByNum(id);
        }
    }

    private void setExist(int id){
        if (id==1){
            isExist1=true;
        }else if (id==2){
            isExist2=true;
        }else {
            isExist3=true;
        }
    }

    private void cancelTimerByNum(int id){
        //根据标签卡号取消检测超时任务
        Log.e("DATA","cancel id:"+id);
        if (id==1){
            if (isTimeRuning1){
                overTimer1.cancel();
            }
        }else if (id==2){
            if (isTimeRuning2){
                overTimer2.cancel();
            }
        }else {
            if (isTimeRuning3){
                overTimer3.cancel();
            }
        }
        //进行下一次检测
        startOvertimeListener(id);
    }

    private void initCheckTask(){
        checkTimer = new Timer();
        checkTImeTask = new TimerTask() {
            @Override
            public void run() {
                checkHandler.sendMessage(checkHandler.obtainMessage());
            }
        };
    }

    private void checkDevices() {
        for (Device device:deviceList){

            //对报警限制做判断
            if (device.getState()==3){
                showDialog("标签卡"+device.getDeviceNumber()+"受到了震动！");
                reventState(device.getDeviceNumber());
            }
            if (device.getState()==4){
                showDialog("标签卡"+device.getDeviceNumber()+"被非法移动了！");
                reventState(device.getDeviceNumber());
            }
            //温度湿度检测另算
            if ((device.getTemperature()+device.getTemperatureDecimal()/10)>= TEMP_MAX){
                showDialog("标签卡"+device.getDeviceNumber()+"温度过高！");
                closeCheck();
            }
            if (device.getHumidity()>= HUM_MAX){
                showDialog("标签卡"+device.getDeviceNumber()+"湿度过高！");
                closeCheck();
            }
            if ((device.getTemperature()+device.getTemperatureDecimal()/10)<= TEMP_MIN){
                showDialog("标签卡"+device.getDeviceNumber()+"温度过低！");
                closeCheck();
            }
            if (device.getHumidity()<= HUM_MIN){
                showDialog("标签卡"+device.getDeviceNumber()+"湿度过低！");
                closeCheck();
            }
        }
    }

    private void closeCheck(){
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));

        checkTimer.cancel();
        isChecking = false;
    }

    private void reventState(int id){
        int index = DeviceCheckUtil.getIndex(id,deviceList);
        Device device = deviceList.get(index);
        device.setState(2);
        deviceList.set(index,device);
    }

//    public void sendMessage(){
//        try{
//            OutputStream os = mSocket.getOutputStream();   //蓝牙连接输出流
//            //要发送的数据
//            String s = "12345";
//            byte[] bos = s.getBytes();
//            os.write(bos);
//        }catch(IOException e){
//
//        }
//    }

    public void ConnectDevice(String address) {
        /**通过mac地址得到设备*/
        mDevice = mBtAdapter.getRemoteDevice(address);
        Log.e("MacAddress",MainActivity.mDeviceMacAddress);
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
            dialogHandler.sendMessage(dialogHandler.obtainMessage());
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e1) {
                Log.e("Socket->close error",e.getMessage());
            }
            return;
        }
        /**打开接收线程*/
        try {
            inputStream = mSocket.getInputStream();
        } catch (IOException e) {
            return;
        }

        if (!bThread){
            ReadThread.start();
            bThread = true;
        }else {
            bRun = true;
        }

    }

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
                        loading.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        startConnectThread();
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

    public void showDialog(String s){
        startMusic();
        new AlertDialog.Builder(getContext())
                .setTitle("警告").setMessage(s)
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        stopMusic();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**控制警报声*/
    public void startMusic(){
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
    }
    public void stopMusic(){
        mediaPlayer.pause();
    }


    @OnClick(R.id.fab)
    public void onClickCheckBtn(View view){

        if (!isChecking){
            Snackbar.make(view,"开启检测",Snackbar.LENGTH_SHORT).show();

            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));

            initCheckTask();
            checkTimer.scheduleAtFixedRate(checkTImeTask, 1000, 2000);
            isChecking = true;
        }else {
            Snackbar.make(view,"关闭检测",Snackbar.LENGTH_SHORT).show();

            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7393e2")));

            checkTimer.cancel();
            isChecking = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loadDateHandler.removeCallbacksAndMessages(null);
        checkHandler.removeCallbacksAndMessages(null);
        dialogHandler.removeCallbacksAndMessages(null);
        overTimeHandler1.removeCallbacksAndMessages(null);
        overTimeHandler2.removeCallbacksAndMessages(null);
        overTimeHandler3.removeCallbacksAndMessages(null);

        if (mSocket!=null){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
