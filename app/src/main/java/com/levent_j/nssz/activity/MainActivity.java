package com.levent_j.nssz.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
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

import com.levent_j.nssz.base.BaseFragment;
import com.levent_j.nssz.entry.Device;
import com.levent_j.nssz.R;
import com.levent_j.nssz.adapter.DeviceAdapter;
import com.levent_j.nssz.base.BaseActivity;
import com.levent_j.nssz.fragment.AboutFragment;
import com.levent_j.nssz.fragment.DeviceFragment;
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

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Bind(R.id.nav_view) NavigationView navigationView;

    @Bind(R.id.drawer_layout) DrawerLayout drawer;



    /**蓝牙设备的mac地址*/
    public static String mAddress;

    /**管理fragment*/
    private FragmentManager fragmentManager;
    private BaseFragment[] fragments = new BaseFragment[2];
    private int current;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fl_container, DeviceFragment.newInstance()).commit();
        current = R.id.nav_main;
        setTitle("设备列表");
        navigationView.setCheckedItem(R.id.nav_main);

        /**通过获取mac地址来建立通信*/
        mAddress = getIntent().getStringExtra("address");

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();
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
            Intent intent = new Intent(MainActivity.this,SetActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int select = item.getItemId();

        if (select==current){
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        current=select;
        BaseFragment fragment = null;
        switch (select){
            case R.id.nav_main:
                setTitle("设备列表");
                if (fragments[0]==null){
                    fragments[0] = DeviceFragment.newInstance();
                }
                fragment = fragments[0];
                break;
            case R.id.nav_about:
                setTitle("关于");
                if (fragments[1]==null){
                    fragments[1] = AboutFragment.newInstance();
                }
                fragment = fragments[1];
                break;
        }

        fragmentManager.beginTransaction().replace(R.id.fl_container,fragment).commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
