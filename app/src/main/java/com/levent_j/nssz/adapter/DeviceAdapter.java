package com.levent_j.nssz.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.levent_j.nssz.Entry.Device;
import com.levent_j.nssz.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by levent_j on 16-5-6.
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.mViewHolder>{
    private Context context;
    private List<Device> deviceList;
    private final LayoutInflater layoutInflater;

    public DeviceAdapter(Context context){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        deviceList = new ArrayList<>();
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_device,null);
        return new mViewHolder(view);
    }

    @Override
    public void onBindViewHolder(mViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.Dname.setText("设备名："+device.getName());
        holder.Dv.setText("电压"+device.getV());
        holder.Di.setText("电流"+device.getI());
        holder.Dt.setText("温度"+device.getT());
        holder.Dw.setText("湿度"+device.getW());
        holder.Dmove.setText(device.isMove()?"移动":"未移动");
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void updateDeviceList(List<Device> list){
        deviceList.clear();
        deviceList.addAll(list);
        notifyDataSetChanged();
    }

    class mViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.tv_device_name)
        TextView Dname;
        @Bind(R.id.tv_device_v)
        TextView Dv;
        @Bind(R.id.tv_device_i)
        TextView Di;
        @Bind(R.id.tv_device_t)
        TextView Dt;
        @Bind(R.id.tv_device_w)
        TextView Dw;
        @Bind(R.id.tv_device_move)
        TextView Dmove;

        public mViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
