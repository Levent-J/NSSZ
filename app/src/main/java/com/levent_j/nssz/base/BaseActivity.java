package com.levent_j.nssz.base;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import butterknife.ButterKnife;

/**
 * Created by levent_j on 16-5-5.
 */

public abstract class BaseActivity extends AppCompatActivity {
    public String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        init();
        setListener();
        TAG = this.getClass().getSimpleName();
    }

    protected abstract int getLayoutId();

    protected abstract void init();

    protected abstract void setListener();

    protected void msg(String s) {
        Log.d(TAG, s);
    }

    protected void Toa(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

}