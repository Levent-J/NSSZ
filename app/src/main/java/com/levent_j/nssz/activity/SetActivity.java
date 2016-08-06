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

    @Bind(R.id.et_temperature_max)
    EditText mTemperatureMax;
    @Bind(R.id.et_humidity_max)
    EditText mHumidityMax;
    @Bind(R.id.et_temperature_min)
    EditText mTemperatureMin;
    @Bind(R.id.et_humidity_min)
    EditText mHumidityMin;
    @Bind(R.id.btn_confirm)
    Button mConfirm;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    protected void init() {
        /**显示默认数值*/
        mTemperatureMax.setText(String.valueOf(DeviceFragment.TEMP_MAX));
        mHumidityMax.setText(String.valueOf(DeviceFragment.HUM_MAX));
        mTemperatureMin.setText(String.valueOf(DeviceFragment.TEMP_MIN));
        mHumidityMin.setText(String.valueOf(DeviceFragment.HUM_MIN));
    }

    @OnClick(R.id.btn_confirm)
    public void Confirm(){
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mTemperatureMax.getText())&&TextUtils.isEmpty(mTemperatureMin.getText())){
                    Toa("请填写正确温度");
                }else if (TextUtils.isEmpty(mHumidityMax.getText())&&TextUtils.isEmpty(mHumidityMin.getText())){
                    Toa("请填写正确湿度");
                }else {
                    try {
                        DeviceFragment.TEMP_MAX = Integer.parseInt(mTemperatureMax.getText().toString().trim());
                        DeviceFragment.HUM_MAX = Integer.parseInt(mHumidityMax.getText().toString().trim());
                        DeviceFragment.TEMP_MIN = Integer.parseInt(mTemperatureMin.getText().toString().trim());
                        DeviceFragment.HUM_MIN = Integer.parseInt(mHumidityMin.getText().toString().trim());
                        finish();
                    }catch (NumberFormatException e){
                        Toa("请填写正确数值");
                    }

                }

            }
        });
    }

}
