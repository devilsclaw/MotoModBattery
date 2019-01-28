package com.devilsclaw.motomodbattery;

import java.io.Serializable;

public class PowerInfo implements Serializable {
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
    public GreybusInfo greybus;
    public BatteryInfo battery;
    public UsbInfo     usb;
}
