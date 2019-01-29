package com.devilsclaw.motomodbattery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.BatteryManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

public class MotoModBatteryReceiver extends BroadcastReceiver {

    MotoModBatteryPassData passdata = null;

    public void setPassDate(MotoModBatteryPassData passdata) {
        this.passdata = passdata;
    }

    private boolean gb_exists() {
        Process p;
        try {
            // Preform su to get root privledges
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("[ -d /sys/class/power_supply/gb_battery ]\n");
            os.writeBytes("exit\n");
            os.flush();
            try {
                p.waitFor();
                if (p.exitValue() != 255) {
                    if(p.exitValue() == 0) {
                        return true;
                    }
                }
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
        }
        return false;
    }

    private GreybusInfo get_gb_info() {
        Process p;
        GreybusInfo info = new GreybusInfo();
        info.exists = gb_exists();
        if(info.exists) {
            try {
                p = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
                os.writeBytes("cat /sys/class/power_supply/gb_battery/capacity\n");
                os.writeBytes("cat /sys/class/power_supply/gb_battery/status\n");
                os.writeBytes("exit\n");
                os.flush();
                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {
                        String value;

                        //get gerybus battery power info
                        value = is.readLine();
                        if (value != null) {
                            info.capacity = Integer.valueOf(value);
                        }

                        if (value != null) {
                            value = is.readLine();
                        }

                        if (value != null) {
                            switch (value) {
                                case PowerInfo.BATTERY_STATUS_TEXT_UNKNOWN:
                                    info.status = PowerInfo.BATTERY_STATUS_UNKNOWN;
                                    info.status_text = PowerInfo.BATTERY_STATUS_TEXT_UNKNOWN;
                                    break;
                                case PowerInfo.BATTERY_STATUS_TEXT_CHARGING:
                                    info.status = PowerInfo.BATTERY_STATUS_CHARGING;
                                    info.status_text = PowerInfo.BATTERY_STATUS_TEXT_CHARGING;
                                    break;
                                case PowerInfo.BATTERY_STATUS_TEXT_DISCHARGING:
                                    info.status = PowerInfo.BATTERY_STATUS_DISCHARGING;
                                    info.status_text = PowerInfo.BATTERY_STATUS_TEXT_DISCHARGING;
                                    break;
                                case PowerInfo.BATTERY_STATUS_TEXT_NOTCHARGING:
                                    info.status = PowerInfo.BATTERY_STATUS_NOTCHARGING;
                                    info.status_text = PowerInfo.BATTERY_STATUS_TEXT_NOTCHARGING;
                                    break;
                                case PowerInfo.BATTERY_STATUS_TEXT_FULL:
                                    info.status = PowerInfo.BATTERY_STATUS_FULL;
                                    info.status_text = PowerInfo.BATTERY_STATUS_TEXT_FULL;
                                    break;
                                default:
                                    info.status = PowerInfo.BATTERY_STATUS_UNKNOWN;
                                    info.status_text = PowerInfo.BATTERY_STATUS_TEXT_UNKNOWN;
                            }
                        }

                        if(value == null) {
                            //return greybus does not exist state
                            info = new GreybusInfo();
                        }
                    }
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }
        }
        return info;
    }

    private UsbInfo get_usb_info() {
        Process p;
        UsbInfo info = new UsbInfo();
        try {
            p = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
            os.writeBytes("cat /sys/class/power_supply/usb/online\n");
            os.writeBytes("cat /sys/class/power_supply/usb/present\n");
            os.writeBytes("cat /sys/class/power_supply/usb/chg_present\n");
            os.writeBytes("exit\n");
            os.flush();
            try {
                p.waitFor();
                if (p.exitValue() != 255) {
                    String value;
                    value = is.readLine();
                    if (value != null) {
                        info.online = Integer.valueOf(value) == 1;
                    }
                    if (value != null) {
                        value = is.readLine();
                    }
                    if (value != null) {
                        info.present = Integer.valueOf(value) == 1;
                    }

                    if (value != null) {
                        value = is.readLine();
                    }
                    if (value != null) {
                        info.charge_present = Integer.valueOf(value) == 1;
                    }

                    if(value == null) {
                        //return usb no present state
                        info = new UsbInfo();
                    }
                }
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
        }
        return info;
    }

    private BatteryInfo get_battery_info() {
        BatteryInfo info = new BatteryInfo();
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
            os.writeBytes("cat /sys/class/power_supply/battery/present\n");
            os.writeBytes("cat /sys/class/power_supply/battery/capacity\n");
            os.writeBytes("cat /sys/class/power_supply/battery/status\n");
            os.writeBytes("cat /sys/class/power_supply/battery/charge_rate\n");
            os.writeBytes("cat /sys/class/power_supply/battery/charge_type\n");
            os.writeBytes("cat /sys/class/power_supply/battery/charging_enabled\n");
            os.writeBytes("cat /sys/class/power_supply/battery/battery_charging_enabled\n");
            os.writeBytes("exit\n");
            os.flush();
            try {
                p.waitFor();
                if (p.exitValue() != 255) {
                    String value;
                    value = is.readLine();
                    if (value != null) {
                        info.present = Integer.valueOf(value) == 1;
                    }

                    if (value != null) {
                        value = is.readLine();
                    }
                    if (value != null) {
                        info.capacity = Integer.valueOf(value);
                    }

                    if (value != null) {
                        value = is.readLine();
                    }
                    if (value != null) {
                        switch (value) {
                            case PowerInfo.BATTERY_STATUS_TEXT_UNKNOWN:
                                info.status = PowerInfo.BATTERY_STATUS_UNKNOWN;
                                info.status_text = PowerInfo.BATTERY_STATUS_TEXT_UNKNOWN;
                                break;
                            case PowerInfo.BATTERY_STATUS_TEXT_CHARGING:
                                info.status = PowerInfo.BATTERY_STATUS_CHARGING;
                                info.status_text = PowerInfo.BATTERY_STATUS_TEXT_CHARGING;
                                break;
                            case PowerInfo.BATTERY_STATUS_TEXT_DISCHARGING:
                                info.status = PowerInfo.BATTERY_STATUS_DISCHARGING;
                                info.status_text = PowerInfo.BATTERY_STATUS_TEXT_DISCHARGING;
                                break;
                            case PowerInfo.BATTERY_STATUS_TEXT_NOTCHARGING:
                                info.status = PowerInfo.BATTERY_STATUS_NOTCHARGING;
                                info.status_text = PowerInfo.BATTERY_STATUS_TEXT_NOTCHARGING;
                                break;
                            case PowerInfo.BATTERY_STATUS_TEXT_FULL:
                                info.status = PowerInfo.BATTERY_STATUS_FULL;
                                info.status_text = PowerInfo.BATTERY_STATUS_TEXT_FULL;
                                break;
                            default:
                                info.status = PowerInfo.BATTERY_STATUS_UNKNOWN;
                                info.status_text = PowerInfo.BATTERY_STATUS_TEXT_UNKNOWN;
                        }
                    }
                    os.writeBytes("cat /sys/class/power_supply/battery/charge_rate\n");
                    os.writeBytes("cat /sys/class/power_supply/battery/charge_type\n");
                    os.writeBytes("cat /sys/class/power_supply/battery/charging_enabled\n");
                    os.writeBytes("cat /sys/class/power_supply/battery/battery_charging_enabled\n");
                    if (value != null) {
                        value = is.readLine();
                    }
                    if (value != null) {
                        info.charge_rate = value;
                    }

                    if (value != null) {
                        value = is.readLine();
                    }
                    if (value != null) {
                        info.charge_type = value;
                    }

                    if (value != null) {
                        value = is.readLine();
                    }
                    if (value != null) {
                        info.charging_enabled = Integer.valueOf(value) == 1;
                    }

                    if (value != null) {
                        value = is.readLine();
                    }
                    if (value != null) {
                        info.battery_charging_enabled = Integer.valueOf(value) == 1;
                    }

                    if(value == null) {
                        //return battery not present state
                        info = new BatteryInfo();
                    }

                }
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
        }
        return info;
    }

    private PowerInfo get_power_info() {
        PowerInfo info = new PowerInfo();
        info.greybus = get_gb_info();
        info.usb = get_usb_info();
        info.battery = get_battery_info();
        return info;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BATTERY_CHANGED:
            case Intent.ACTION_POWER_CONNECTED:
            case Intent.ACTION_POWER_DISCONNECTED:
                if(passdata != null) {
                    passdata.passData(get_power_info());
                }
                break;
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                context.startForegroundService(new Intent(context, MotoMobBatteryService.class));
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                context.startForegroundService(new Intent(context, MotoMobBatteryService.class));
                break;
            default:
                Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();

        }
    }
}
