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
public class PairActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.btn_next)
    Button next;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pair;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void setListener() {
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_next:
                startActivity(new Intent(PairActivity.this,LoginActivity.class));
                break;
        }
    }
}
