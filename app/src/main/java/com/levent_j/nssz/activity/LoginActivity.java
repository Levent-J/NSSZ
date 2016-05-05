package com.levent_j.nssz.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.levent_j.nssz.R;
import com.levent_j.nssz.base.BaseActivity;

import butterknife.Bind;

/**
 * Created by levent_j on 16-5-5.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.btn_login)
    Button login;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void setListener() {
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                break;
        }
    }
}
