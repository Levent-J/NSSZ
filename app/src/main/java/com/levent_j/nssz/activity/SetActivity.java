package com.levent_j.nssz.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.levent_j.nssz.R;
import com.levent_j.nssz.base.BaseActivity;
import com.levent_j.nssz.fragment.DeviceFragment;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by levent_j on 16-5-5.
 */
public class SetActivity extends BaseActivity{

    @Bind(R.id.et_temperature)
    EditText mTemperature;
    @Bind(R.id.et_humidity)
    EditText mHumidity;
    @Bind(R.id.btn_confirm)
    Button mConfirm;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    protected void init() {
    }

    @OnClick(R.id.btn_confirm)
    public void Confirm(){
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mTemperature.getText())){
                    Toa("请填写正确温度");
                }else if (TextUtils.isEmpty(mHumidity.getText())){
                    Toa("请填写正确湿度");
                }else {
                    try {
                        DeviceFragment.Temperature = Integer.parseInt(mTemperature.getText().toString().trim());
                        DeviceFragment.Humidity = Integer.parseInt(mHumidity.getText().toString().trim());
                        finish();
                    }catch (NumberFormatException e){
                        Toa("请填写正确数值");
                    }

                }

            }
        });
    }

}
