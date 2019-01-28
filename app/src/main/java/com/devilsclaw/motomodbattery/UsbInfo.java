package com.devilsclaw.motomodbattery;

import java.io.Serializable;

public class UsbInfo implements Serializable {
    public boolean online         = false;
    public boolean present        = false;
    public boolean charge_present = false;
}
