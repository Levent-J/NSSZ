package com.levent_j.nssz.activity;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.widget.EditText;

import com.levent_j.nssz.R;
import com.levent_j.nssz.base.BaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by levent_j on 16-5-5.
 */
public class LoginActivity extends BaseActivity{
    @Bind(R.id.et_username) EditText username;
    @Bind(R.id.et_userpassword) EditText password;
    @Bind(R.id.til_username) TextInputLayout usernameWraper;
    @Bind(R.id.til_userpassword) TextInputLayout passwordWraper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {
    }

    @OnClick(R.id.btn_login)
    public void onLogin(){
        String eUsername = username.getText().toString().trim();
        String ePassword = password.toString().trim();
        if (TextUtils.isEmpty(eUsername)){
            usernameWraper.setError("用户名不能为空");
            return;
        }else {
            usernameWraper.setErrorEnabled(false);
        }
        if (TextUtils.isEmpty(ePassword)){
            passwordWraper.setError("密码不能为空");
            return;
        }else {
            passwordWraper.setErrorEnabled(false);
        }

//        if (!eUsername.equals("admin")){
//            usernameWraper.setError("用户名错误");
//            return;
//        }else {
//            usernameWraper.setErrorEnabled(false);
//        }
//        if (!ePassword.equals("123456")){
//            passwordWraper.setError("密码错误");
//            return;
//        }else {
//            passwordWraper.setErrorEnabled(false);
//        }

        Intent intent = new Intent(LoginActivity.this,PairActivity.class);
        startActivity(intent);
    }
}
