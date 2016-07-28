package com.levent_j.nssz.activity;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.levent_j.nssz.R;
import com.levent_j.nssz.base.BaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by levent_j on 16-5-5.
 */
public class LoginActivity extends BaseActivity{
    @Bind(R.id.btn_login)
    Button login;
    @Bind(R.id.et_username)
    EditText username;
    @Bind(R.id.et_userpassword)
    EditText userpassword;
    @Bind(R.id.til_username)
    TextInputLayout usernameWraper;
    @Bind(R.id.til_userpassword)
    TextInputLayout userpasswordWraper;

    private String mAddress;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {
        mAddress = getIntent().getStringExtra("address");
    }

    @OnClick(R.id.btn_login)
    public void onLogin(){
        //管理员账号密码均为admin
        String mUsername = usernameWraper.getEditText().getText().toString().trim();
        String mUserpassword = userpasswordWraper.getEditText().getText().toString().trim();
        if (TextUtils.isEmpty(mUsername)){
            usernameWraper.setError("用户名不能为空");
            return;
        }else {
            usernameWraper.setErrorEnabled(false);
        }
        if (TextUtils.isEmpty(mUserpassword)){
            userpasswordWraper.setError("密码不能为空");
            return;
        }else {
            userpasswordWraper.setErrorEnabled(false);
        }

        if (!mUsername.equals("admin")){
            usernameWraper.setError("用户名错误");
            return;
        }else {
            usernameWraper.setErrorEnabled(false);
        }
        if (!mUserpassword.equals("admin")){
            userpasswordWraper.setError("密码错误");
            return;
        }else {
            userpasswordWraper.setErrorEnabled(false);
        }

        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("address",mAddress);
        startActivity(intent);
    }
}
