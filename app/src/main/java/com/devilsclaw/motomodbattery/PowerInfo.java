package com.devilsclaw.motomodbattery;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class PowerInfo {
    static final String BATTERY_STATUS_TEXT_UNKNOWN     = "Unknown";
    static final String BATTERY_STATUS_TEXT_CHARGING    = "Charging";
    static final String BATTERY_STATUS_TEXT_DISCHARGING = "Discharging";
    static final String BATTERY_STATUS_TEXT_NOTCHARGING = "Not charging";
    static final String BATTERY_STATUS_TEXT_FULL        = "Full";
    static final int    BATTERY_STATUS_UNKNOWN          = 0;
    static final int    BATTERY_STATUS_CHARGING         = 1;
    static final int    BATTERY_STATUS_DISCHARGING      = 2;
    static final int    BATTERY_STATUS_NOTCHARGING      = 3;
    static final int    BATTERY_STATUS_FULL             = 4;

    public GreybusInfo greybus;
    public BatteryInfo battery;
    public UsbInfo     usb;

    public static class GreybusInfo {
        public boolean exists      = false;
        public int     capacity    = 0;
        public int     status      = BATTERY_STATUS_UNKNOWN;
        public String  status_text = BATTERY_STATUS_TEXT_UNKNOWN;
    }

    public static class BatteryInfo {
        public boolean present                  = false;
        public int     capacity                 = 0;
        public int     status                   = BATTERY_STATUS_UNKNOWN;
        public String  status_text              = BATTERY_STATUS_TEXT_UNKNOWN;
        public String  charge_rate              = "";
        public String  charge_type              = "";
        public boolean charging_enabled         = false;
        public boolean battery_charging_enabled = false;
    }

    public static class UsbInfo {
        public boolean online         = false;
        public boolean present        = false;
        public boolean charge_present = false;
    }

    private static boolean greybbus_exists() {
        Process p;
        try {
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

    private static GreybusInfo get_greybus_info() {
        Process p;
        GreybusInfo info = new GreybusInfo();
        info.exists = greybbus_exists();
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

    private static UsbInfo get_usb_info() {
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

    private static BatteryInfo get_battery_info() {
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

    public static PowerInfo get_power_info() {
        PowerInfo info = new PowerInfo();
        info.greybus = get_greybus_info();
        info.usb = get_usb_info();
        info.battery = get_battery_info();
        return info;
    }
}
