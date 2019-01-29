package com.devilsclaw.motomodbattery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startForegroundService(new Intent(this, MotoModBatteryService.class));
        Switch sw_toggle = findViewById(R.id.efficiency_toggle);
        sw_toggle.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Toast.makeText(getApplicationContext(),""+isChecked,Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_TOGGLE);
                intent.putExtra("value",isChecked);
                sendBroadcast(intent);
            }
        });
        SeekBarWithNumber sb_low = findViewById(R.id.efficiency_trigger_low);
        sb_low.setOnSeekBarChangeListener(new SeekBarWithNumber.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(),""+seekBar.getProgress(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_TRIGGER_LOW);
                intent.putExtra("value",seekBar.getProgress());
                sendBroadcast(intent);
            }
        });
        SeekBarWithNumber sb_high = findViewById(R.id.efficiency_trigger_high);
        sb_high.setOnSeekBarChangeListener(new SeekBarWithNumber.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(),""+seekBar.getProgress(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(MotoModBatteryService.ServiceReceiver.ACTION_EFFIENCY_TRIGGER_HIGH);
                intent.putExtra("value",seekBar.getProgress());
                sendBroadcast(intent);
            }
        });
    }
}
