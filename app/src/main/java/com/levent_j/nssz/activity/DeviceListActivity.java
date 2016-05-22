package com.levent_j.nssz.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.levent_j.nssz.R;
import com.levent_j.nssz.base.BaseActivity;

/**
 * Created by levent_j on 16-5-22.
 */
public class DeviceListActivity extends Activity{
//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_device_list;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  //设置窗口显示模式为窗口方式
        setContentView(R.layout.activity_device_list);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = ViewGroup.LayoutParams.FILL_PARENT;
        lp.gravity = Gravity.TOP;
        getWindow().setAttributes(lp);
    }

//    @Override
//    protected void init() {
//
//    }

//    @Override
//    protected void setListener() {
//
//    }
}
