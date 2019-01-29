package com.devilsclaw.motomodbattery;

import java.io.Serializable;

public class GreybusInfo implements Serializable {
    public boolean exists      = false;
    public int     capacity    = 0;
    public int     status      = PowerInfo.BATTERY_STATUS_UNKNOWN;
    public String  status_text = PowerInfo.BATTERY_STATUS_TEXT_UNKNOWN;
}