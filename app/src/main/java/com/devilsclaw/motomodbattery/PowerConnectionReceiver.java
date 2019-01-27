package com.devilsclaw.motomodbattery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

public class PowerConnectionReceiver extends BroadcastReceiver {
    private Icon[] icon_percent = new Icon[101];
    public NotificationManager manager = null;
    NotificationChannel chan1 = null;
    public static final String PRIMARY_CHANNEL = "default";
    Context context = null;

    //once using the moto mod battery to charge
    //when to stop
    int     efficiency_trigger_level_stop   = 81;

    //when to start using the moto mod battery to charge
    int     efficiency_trigger_level_start  = 80;

    boolean efficiency_trigger = false;
    boolean efficiency_enabled = true;

    public class gb_info {
        public boolean exists      = false;
        public int     capacity    = 0;
        public int     status      = power_info.BATTERY_STATUS_UNKNOWN;
        public String  status_text = power_info.BATTERY_STATUS_TEXT_UNKNOWN;
    }

    public class battery_info {
        public boolean present                  = false;
        public int     capacity                 = 0;
        public int     status                   = power_info.BATTERY_STATUS_UNKNOWN;
        public String  status_text              = power_info.BATTERY_STATUS_TEXT_UNKNOWN;
        public String  charge_rate              = "";
        public String  charge_type              = "";
        public boolean charging_enabled         = false;
        public boolean battery_charging_enabled = false;
    }

    public class usb_info {
        public boolean online         = false;
        public boolean present        = false;
        public boolean charge_present = false;
    }

    class power_info {
        public static final String BATTERY_STATUS_TEXT_UNKNOWN     = "Unknown";
        public static final String BATTERY_STATUS_TEXT_CHARGING    = "Charging";
        public static final String BATTERY_STATUS_TEXT_DISCHARGING = "Discharging";
        public static final String BATTERY_STATUS_TEXT_NOTCHARGING = "Not charging";
        public static final String BATTERY_STATUS_TEXT_FULL        = "Full";
        public static final int    BATTERY_STATUS_UNKNOWN          = 0;
        public static final int    BATTERY_STATUS_CHARGING         = 1;
        public static final int    BATTERY_STATUS_DISCHARGING      = 2;
        public static final int    BATTERY_STATUS_NOTCHARGING      = 3;
        public static final int    BATTERY_STATUS_FULL             = 4;
        gb_info greybus;
        battery_info battery;
        usb_info usb;
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

    public void setApplicationContext(Context context) {
        this.context = context;
        create_icons();
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        chan1 = new NotificationChannel(PRIMARY_CHANNEL, "silent", NotificationManager.IMPORTANCE_LOW);
        chan1.setLightColor(Color.GREEN);
        chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        manager.createNotificationChannel(chan1);
    }

    private gb_info get_gb_info() {
        Process p;
        gb_info info = new gb_info();
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
                                case power_info.BATTERY_STATUS_TEXT_UNKNOWN:
                                    info.status = power_info.BATTERY_STATUS_UNKNOWN;
                                    info.status_text = power_info.BATTERY_STATUS_TEXT_UNKNOWN;
                                    break;
                                case power_info.BATTERY_STATUS_TEXT_CHARGING:
                                    info.status = power_info.BATTERY_STATUS_CHARGING;
                                    info.status_text = power_info.BATTERY_STATUS_TEXT_CHARGING;
                                    break;
                                case power_info.BATTERY_STATUS_TEXT_DISCHARGING:
                                    info.status = power_info.BATTERY_STATUS_DISCHARGING;
                                    info.status_text = power_info.BATTERY_STATUS_TEXT_DISCHARGING;
                                    break;
                                case power_info.BATTERY_STATUS_TEXT_NOTCHARGING:
                                    info.status = power_info.BATTERY_STATUS_NOTCHARGING;
                                    info.status_text = power_info.BATTERY_STATUS_TEXT_NOTCHARGING;
                                    break;
                                case power_info.BATTERY_STATUS_TEXT_FULL:
                                    info.status = power_info.BATTERY_STATUS_FULL;
                                    info.status_text = power_info.BATTERY_STATUS_TEXT_FULL;
                                    break;
                                default:
                                    info.status = power_info.BATTERY_STATUS_UNKNOWN;
                                    info.status_text = power_info.BATTERY_STATUS_TEXT_UNKNOWN;
                            }
                        }

                        if(value == null) {
                            //return greybus does not exist state
                            info = new gb_info();
                        }
                    }
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }
        }
        return info;
    }

    private usb_info get_usb_info() {
        Process p;
        usb_info info = new usb_info();
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
                        info = new usb_info();
                    }
                }
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
        }
        return info;
    }

    private battery_info get_battery_info() {
        battery_info info = new battery_info();
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
                            case power_info.BATTERY_STATUS_TEXT_UNKNOWN:
                                info.status = power_info.BATTERY_STATUS_UNKNOWN;
                                info.status_text = power_info.BATTERY_STATUS_TEXT_UNKNOWN;
                                break;
                            case power_info.BATTERY_STATUS_TEXT_CHARGING:
                                info.status = power_info.BATTERY_STATUS_CHARGING;
                                info.status_text = power_info.BATTERY_STATUS_TEXT_CHARGING;
                                break;
                            case power_info.BATTERY_STATUS_TEXT_DISCHARGING:
                                info.status = power_info.BATTERY_STATUS_DISCHARGING;
                                info.status_text = power_info.BATTERY_STATUS_TEXT_DISCHARGING;
                                break;
                            case power_info.BATTERY_STATUS_TEXT_NOTCHARGING:
                                info.status = power_info.BATTERY_STATUS_NOTCHARGING;
                                info.status_text = power_info.BATTERY_STATUS_TEXT_NOTCHARGING;
                                break;
                            case power_info.BATTERY_STATUS_TEXT_FULL:
                                info.status = power_info.BATTERY_STATUS_FULL;
                                info.status_text = power_info.BATTERY_STATUS_TEXT_FULL;
                                break;
                            default:
                                info.status = power_info.BATTERY_STATUS_UNKNOWN;
                                info.status_text = power_info.BATTERY_STATUS_TEXT_UNKNOWN;
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
                        info = new battery_info();
                    }

                }
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
        }
        return info;
    }

    private power_info get_power_info() {
        power_info info = new power_info();
        info.greybus = get_gb_info();
        info.usb = get_usb_info();
        info.battery = get_battery_info();
        return info;
    }

    private void create_icons() {
        for(int idx = 0;idx < icon_percent.length;idx++) {
            int icon_size = 8 * 12;
            Bitmap bm = Bitmap.createBitmap(icon_size,icon_size,Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            Paint paint = new Paint(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            if(idx < 100) {
                canvas.drawText(String.format("% 2d%%", idx), -10, 50, paint);
            } else {
                canvas.drawText(String.format("% 3d", idx), -10, 50, paint);
            }
            canvas.drawText("BAT", 0, 90, paint);
            icon_percent[idx] = Icon.createWithBitmap(bm);
        }
    }

    public Icon get_icon(int icon_idx) {
        if(icon_idx <= 100) {
            return icon_percent[icon_idx];
        }
        return null;
    }

    public void showNotification(String title, String body,String icon_percent,String icon_text) {
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
        Notification.Builder builder = new Notification.Builder(context, PRIMARY_CHANNEL);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setSmallIcon(icon);
        builder.setLargeIcon(icon);
        builder.setAutoCancel(true);
        builder.setOngoing(true);

        Intent efficiency_toggle = new Intent(context, MainActivity.class);
        efficiency_toggle.setAction("EFFICIENCY_TOGGLE");
        efficiency_toggle.putExtra(Notification.EXTRA_NOTIFICATION_ID, 0);
        PendingIntent pending_efficiency_toggle = PendingIntent.getBroadcast(context, 0, efficiency_toggle, 0);
        Notification.Action.Builder action_efficiency_toggle = new Notification.Action.Builder(icon,"EFFICIENCY ON",pending_efficiency_toggle);
        builder.addAction(action_efficiency_toggle.build());

        Intent efficiency_level = new Intent(context, MainActivity.class);
        efficiency_level.setAction("EFFICIENCY_LEVEL");
        efficiency_level.putExtra(Notification.EXTRA_NOTIFICATION_ID, 1);
        PendingIntent pending_efficiency_level = PendingIntent.getBroadcast(context, 0, efficiency_level, 0);
        Notification.Action.Builder action_efficiency_level = new Notification.Action.Builder(icon,"EFFICIENCY LEVEL",pending_efficiency_level);
        builder.addAction(action_efficiency_level.build());
        manager.notify(1000,builder.build());
    }

    private boolean set_charging_enabled(boolean enabled) {
        int _enabled = (enabled)?1:0;
        Process p;
        try {
            // Preform su to get root privledges
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



    public void set_efficiency_trigger_level_start(int efficiency_trigger_level_start) {
        this.efficiency_trigger_level_start = efficiency_trigger_level_start;
        do_gb_stuff();
    }

    public void set_efficiency_trigger_level_stop(int efficiency_trigger_level_stop) {
        this.efficiency_trigger_level_stop = efficiency_trigger_level_stop;
        do_gb_stuff();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()) {
            case Intent.ACTION_BATTERY_CHANGED:
            case Intent.ACTION_POWER_CONNECTED:
            case Intent.ACTION_POWER_DISCONNECTED:
                do_gb_stuff();
                break;
            default:
                Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();

        }
    }

    private void do_gb_stuff() {
        power_info info = get_power_info();
        String icon_percent;
        if(info.greybus.exists) {
            if(info.usb.charge_present ||
                    (( efficiency_trigger) && info.battery.capacity < efficiency_trigger_level_stop) ||
                    ((!efficiency_trigger) && info.battery.capacity < efficiency_trigger_level_start)
            ) {
                if(info.battery.capacity < efficiency_trigger_level_start) {
                    efficiency_trigger = true;
                } else if(info.usb.charge_present || info.battery.capacity >= efficiency_trigger_level_stop) {
                    efficiency_trigger = false;
                }
                if(!info.battery.charging_enabled) {
                    set_charging_enabled(true);
                }
            } else {
                if(info.battery.charging_enabled) {
                    set_charging_enabled(false);
                }
            }
            if(info.greybus.capacity < 100) {
                icon_percent = String.format("% 2d%%", info.greybus.capacity);
            } else {
                icon_percent = String.format("% 3d", info.greybus.capacity);
            }
            String icon_text;
            switch (info.greybus.status) {
                case power_info.BATTERY_STATUS_UNKNOWN:
                    icon_text = "UNK";
                    break;
                case power_info.BATTERY_STATUS_CHARGING:
                    icon_text = "CRG";
                    break;
                case power_info.BATTERY_STATUS_DISCHARGING:
                    icon_text = "DIS";
                    break;
                case power_info.BATTERY_STATUS_NOTCHARGING:
                    icon_text = "NOT";
                    break;
                case power_info.BATTERY_STATUS_FULL:
                    icon_text = "FUL";
                    break;
                default:
                    icon_text = "BAT";
            }
            showNotification(String.format("Moto Mod battery %d%%", info.greybus.capacity), info.greybus.status_text, icon_percent, icon_text);
        } else {
            manager.cancelAll();
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
}
