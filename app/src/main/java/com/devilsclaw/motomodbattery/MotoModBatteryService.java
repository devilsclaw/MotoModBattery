package com.devilsclaw.motomodbattery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
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
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

public class MotoMobBatteryService extends Service {
    private Icon[] icon_percent = new Icon[101];
    private NotificationManager notificationmanager = null;
    private NotificationChannel notificationchannel = null;
    MotoModBatteryReceiver batter_receiver = null;
    private static final String CHANNEL_ID  = "default";
    private static final String CHANNEL_TAG = "silent";

    //once using the moto mod battery to charge
    //when to stop
    private int efficiency_trigger_level_high = 81;

    //when to start using the moto mod battery to charge
    private int efficiency_trigger_level_low = 80;

    private boolean efficiency_trigger = false;
    private boolean efficiency_enabled = true;

    private boolean set_charging_enabled(boolean enabled) {
        int _enabled = (enabled)?1:0;
        Process p;
        try {
            // Preform su to get root privileges
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            //battery_charging_enabled is not the one that controls charging
            os.writeBytes(String.format("echo %d > /sys/class/power_supply/battery/charging_enabled\n",_enabled));
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

    private void do_gb_stuff(PowerInfo info) {
        String icon_percent;
        if(!efficiency_enabled) {
            if(!info.battery.charging_enabled) {
                set_charging_enabled(true);
            }
        }
        if(info.greybus.exists) {
            if(efficiency_enabled) {
                if (info.usb.charge_present ||
                        ((efficiency_trigger) && info.battery.capacity < efficiency_trigger_level_high) ||
                        ((!efficiency_trigger) && info.battery.capacity < efficiency_trigger_level_low)
                ) {
                    if (info.battery.capacity < efficiency_trigger_level_low) {
                        efficiency_trigger = true;
                    } else if (info.usb.charge_present || info.battery.capacity >= efficiency_trigger_level_high) {
                        efficiency_trigger = false;
                    }
                    if (!info.battery.charging_enabled) {
                        set_charging_enabled(true);
                    }
                } else {
                    if (info.battery.charging_enabled) {
                        set_charging_enabled(false);
                    }
                }
            }
            if(info.greybus.capacity < 100) {
                icon_percent = String.format("% 2d%%", info.greybus.capacity);
            } else {
                icon_percent = String.format("% 3d", info.greybus.capacity);
            }
            String icon_text;
            switch (info.greybus.status) {
                case PowerInfo.BATTERY_STATUS_UNKNOWN:
                    icon_text = "UNK";
                    break;
                case PowerInfo.BATTERY_STATUS_CHARGING:
                    icon_text = "CRG";
                    break;
                case PowerInfo.BATTERY_STATUS_DISCHARGING:
                    icon_text = "DIS";
                    break;
                case PowerInfo.BATTERY_STATUS_NOTCHARGING:
                    icon_text = "NOT";
                    break;
                case PowerInfo.BATTERY_STATUS_FULL:
                    icon_text = "FUL";
                    break;
                default:
                    icon_text = "BAT";
            }
            showNotification(String.format("Moto Mod battery %d%%", info.greybus.capacity), info.greybus.status_text, icon_percent, icon_text);
        } else {
            notificationmanager.cancelAll();
            if(info.usb.charge_present) {
                if(!info.battery.charging_enabled) {
                    set_charging_enabled(true);
                }
            } else {
                if(info.battery.charging_enabled) {
                    set_charging_enabled(false);
                }
            }
        }
    }

    private Notification buildNotification(String title, String body, String icon_percent, String icon_text) {
        int icon_size = 8 * 12;
        Bitmap bm = Bitmap.createBitmap(icon_size,icon_size,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(icon_percent, -10, 50, paint);
        canvas.drawText(icon_text, 0, 90, paint);
        Icon icon = Icon.createWithBitmap(bm);
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setSmallIcon(icon);
        builder.setLargeIcon(icon);
        builder.setAutoCancel(true);
        builder.setOngoing(true);

//        Intent efficiency_toggle = new Intent(this, MainActivity.class);
//        efficiency_toggle.setAction("EFFICIENCY_TOGGLE");
//        efficiency_toggle.putExtra(Notification.EXTRA_NOTIFICATION_ID, 0);
//        PendingIntent pending_efficiency_toggle = PendingIntent.getBroadcast(this, 0, efficiency_toggle, 0);
//        Notification.Action.Builder action_efficiency_toggle = new Notification.Action.Builder(icon,"EFFICIENCY ON",pending_efficiency_toggle);
//        builder.addAction(action_efficiency_toggle.build());
//
//        Intent efficiency_level = new Intent(this, MainActivity.class);
//        efficiency_level.setAction("EFFICIENCY_LEVEL");
//        efficiency_level.putExtra(Notification.EXTRA_NOTIFICATION_ID, 1);
//        PendingIntent pending_efficiency_level = PendingIntent.getBroadcast(this, 0, efficiency_level, 0);
//        Notification.Action.Builder action_efficiency_level = new Notification.Action.Builder(icon,"EFFICIENCY LEVEL",pending_efficiency_level);
//        builder.addAction(action_efficiency_level.build());
        return builder.build();
    }

    private void showNotification(String title, String body, String icon_percent, String icon_text) {
        notificationmanager.notify(1,buildNotification(title,body,icon_percent,icon_text));
    }

    private void install_notification() {
        notificationmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationchannel = new NotificationChannel(CHANNEL_ID, CHANNEL_TAG, NotificationManager.IMPORTANCE_LOW);
        notificationchannel.setLightColor(Color.GREEN);
        notificationchannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationmanager.createNotificationChannel(notificationchannel);
    }

    public class ServiceReceiver extends BroadcastReceiver {
        public static final String ACTION_EFFIENCY_TOGGLE = "com.devilsclaw.action.ACTION_EFFIENCY_TOGGLE";
        public static final String ACTION_EFFIENCY_TRIGGER_LOW = "com.devilsclaw.action.ACTION_EFFIENCY_TRIGGER_LOW";
        public static final String ACTION_EFFIENCY_TRIGGER_HIGH = "com.devilsclaw.action.ACTION_EFFIENCY_TRIGGER_HIGH";
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case ServiceReceiver.ACTION_EFFIENCY_TOGGLE:
                    efficiency_trigger = false;
                    efficiency_enabled = intent.getBooleanExtra("value", true);
                    do_gb_stuff(batter_receiver.get_power_info());
                    Toast.makeText(context, String.format("efficiency_enabled = %s",efficiency_enabled), Toast.LENGTH_LONG).show();
                    break;
                case ServiceReceiver.ACTION_EFFIENCY_TRIGGER_LOW:
                    efficiency_trigger = false;
                    efficiency_trigger_level_low = intent.getIntExtra("value",80);
                    do_gb_stuff(batter_receiver.get_power_info());
                    Toast.makeText(context, String.format("efficiency_trigger_level_low = %d",efficiency_trigger_level_low), Toast.LENGTH_LONG).show();
                    break;
                case ServiceReceiver.ACTION_EFFIENCY_TRIGGER_HIGH:
                    efficiency_trigger = false;
                    efficiency_trigger_level_high = intent.getIntExtra("value",81);
                    do_gb_stuff(batter_receiver.get_power_info());
                    Toast.makeText(context, String.format("efficiency_trigger_level_high = %d",efficiency_trigger_level_high), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
    private void install_receiver() {
        batter_receiver = new MotoModBatteryReceiver();
        batter_receiver.setPassDate(new MotoModBatteryPassData() {
            @Override
            public void passData(Object data) {
                do_gb_stuff((PowerInfo) data);
            }
        });
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentfilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batter_receiver,intentfilter);

        ServiceReceiver service_receiver = new ServiceReceiver();
        intentfilter = new IntentFilter();
        intentfilter.addAction(ServiceReceiver.ACTION_EFFIENCY_TOGGLE);
        intentfilter.addAction(ServiceReceiver.ACTION_EFFIENCY_TRIGGER_LOW);
        intentfilter.addAction(ServiceReceiver.ACTION_EFFIENCY_TRIGGER_HIGH);
        registerReceiver(service_receiver,intentfilter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        install_notification();
        install_receiver();
        startForeground(1, buildNotification("Moto Mode battery", "", "MOD", "BAT"));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batter_receiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
