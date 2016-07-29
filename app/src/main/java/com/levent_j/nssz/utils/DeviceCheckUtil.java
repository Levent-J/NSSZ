package com.levent_j.nssz.utils;

import com.levent_j.nssz.entry.Device;

import java.util.List;

/**
 * Created by levent_j on 16-7-29.
 */
public class DeviceCheckUtil {
    public static int getIndex(int number, List<Device> deviceList) {
        for (int j=0;j<deviceList.size();j++){
            if (number==deviceList.get(j).getDeviceNumber()){
                return j;
            }
        }
        return 0;
    }

    public static boolean isExist(int number,List<Device> deviceList) {
        for (Device d:deviceList){
            if (d.getDeviceNumber()==number){
                return true;
            }
        }
        return false;
    }
}
