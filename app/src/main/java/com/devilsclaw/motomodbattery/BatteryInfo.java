package com.devilsclaw.motomodbattery;

import java.io.Serializable;

public class BatteryInfo implements Serializable {
    public boolean present                  = false;
    public int     capacity                 = 0;
    public int     status                   = PowerInfo.BATTERY_STATUS_UNKNOWN;
    public String  status_text              = PowerInfo.BATTERY_STATUS_TEXT_UNKNOWN;
    public String  charge_rate              = "";
    public String  charge_type              = "";
    public boolean charging_enabled         = false;
    public boolean battery_charging_enabled = false;
}
