package com.devilsclaw.motomodbattery;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    ServiceConnection serviceconnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {}

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Intent intent = new Intent();
            intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_GET_TOGGLE);
            sendBroadcast(intent);
            intent = new Intent();
            intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_GET_TRIGGER_LOW);
            sendBroadcast(intent);
            intent = new Intent();
            intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_GET_TRIGGER_HIGH);
            sendBroadcast(intent);
        }
    };

    Switch.OnCheckedChangeListener sw_toggle_listener = new Switch.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Intent intent = new Intent();
            intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_SET_TOGGLE);
            intent.putExtra("value",isChecked);
            sendBroadcast(intent);
        }
    };

    SeekBarWithNumber.OnSeekBarChangeListener sb_low_listener = new SeekBarWithNumber.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Intent intent = new Intent();
            intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_SET_TRIGGER_LOW);
            intent.putExtra("value",seekBar.getProgress());
            sendBroadcast(intent);
        }
    };

    SeekBarWithNumber.OnSeekBarChangeListener sb_high_listener = new SeekBarWithNumber.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Intent intent = new Intent();
            intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_SET_TRIGGER_HIGH);
            intent.putExtra("value",seekBar.getProgress());
            sendBroadcast(intent);
        }
    };

    BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null) {
                return;
            }
            String action = intent.getAction();
            if(action == null) {
                return;
            }
            switch(action) {
                case MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_REP_TOGGLE: {
                    Switch sw_toggle = findViewById(R.id.efficiency_toggle);
                    sw_toggle.setChecked(intent.getBooleanExtra("value",true));
                    break;
                }
                case MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_REP_TRIGGER_LOW: {
                    SeekBarWithNumber sb_low = findViewById(R.id.efficiency_trigger_low);
                    sb_low.setProgress(intent.getIntExtra("value",80));
                    break;
                }
                case MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_REP_TRIGGER_HIGH: {
                    SeekBarWithNumber sb_high = findViewById(R.id.efficiency_trigger_high);
                    sb_high.setProgress(intent.getIntExtra("value",81));
                    break;
                }
            }
        }
    };

    private void install_activity_receiver() {
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_REP_TOGGLE);
        intentfilter.addAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_REP_TRIGGER_LOW);
        intentfilter.addAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_REP_TRIGGER_HIGH);
        registerReceiver(broadcastreceiver,intentfilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        install_activity_receiver();

        startForegroundService(new Intent(this, MotoModBatteryService.class));
        bindService(new Intent(this, MotoModBatteryService.class),serviceconnection,Context.BIND_ABOVE_CLIENT);

        Switch sw_toggle = findViewById(R.id.efficiency_toggle);
        sw_toggle.setOnCheckedChangeListener(sw_toggle_listener);

        SeekBarWithNumber sb_low = findViewById(R.id.efficiency_trigger_low);
        sb_low.setOnSeekBarChangeListener(sb_low_listener);

        SeekBarWithNumber sb_high = findViewById(R.id.efficiency_trigger_high);
        sb_high.setOnSeekBarChangeListener(sb_high_listener);
    }
}
