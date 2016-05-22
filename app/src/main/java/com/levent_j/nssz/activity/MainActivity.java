package com.levent_j.nssz.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.levent_j.nssz.Entry.Device;
import com.levent_j.nssz.R;
import com.levent_j.nssz.adapter.DeviceAdapter;
import com.levent_j.nssz.base.BaseActivity;
import com.levent_j.nssz.utils.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;
    @Bind(R.id.nav_view)
    NavigationView navigationView;
    @Bind(R.id.rv_devices)
    RecyclerView recyclerView;

    private DeviceAdapter deviceAdapter;
    private List<Device> deviceList;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler;

    private boolean isChecking = false;
    private String mAddress;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        deviceAdapter = new DeviceAdapter(this);
        deviceList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.space);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        recyclerView.setAdapter(deviceAdapter);

        timer = new Timer();
        loadBtBata();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                checkDevices();
                super.handleMessage(msg);
            }
        };
        initTask();
    }

    private void initTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
    }
    private void initTimer() {
        timer = new Timer();
    }

    private void checkDevices() {
        for (Device device:deviceList){
            //对报警限制做判断
            Log.d("Task","Checking!");
        }
    }

    private void loadBtBata() {
        //TODO:从蓝牙获取数据
        mAddress = getIntent().getStringExtra("address");
        //从Intent中获取数据


        deviceList.clear();
        for (int i=0;i<10;i++){
            Device device = new Device();
            device.setName("XX"+i);
            device.setV(i);
            device.setI(i);
            device.setT(i);
            device.setW(i);
            device.setMove(true);
            deviceList.add(device);
        }
        deviceAdapter.updateDeviceList(deviceList);
        recyclerView.setAdapter(deviceAdapter);


    }

    @Override
    protected void setListener() {
        fab.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,SetActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {

        } else if (id == R.id.nav_about) {
            startActivity(new Intent(MainActivity.this,AboutActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                //开启检测
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
                break;
        }
    }



}
