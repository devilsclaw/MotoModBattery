package com.devilsclaw.motomodbattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MotoModBatteryReceiver extends BroadcastReceiver {
    public static interface PassData {
        void passData(Object data);
    }

    PassData passdata = null;

    public void setPassDate(PassData passdata) {
        this.passdata = passdata;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BATTERY_CHANGED:
            case Intent.ACTION_POWER_CONNECTED:
            case Intent.ACTION_POWER_DISCONNECTED:
                if(passdata != null) {
                    passdata.passData(PowerInfo.get_power_info());
                }
                break;
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                context.startForegroundService(new Intent(context, MotoModBatteryService.class));
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                context.startForegroundService(new Intent(context, MotoModBatteryService.class));
                break;
        }
    }
}
