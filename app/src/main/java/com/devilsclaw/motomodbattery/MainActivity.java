package com.devilsclaw.motomodbattery;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_BATTERY_STATS = 1;
    public int mkFolder(String folderName){ // make a folder under Environment.DIRECTORY_DCIM
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
            //Log.d("myAppName", "Error: external storage is unavailable");
            return 0;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //Log.d("myAppName", "Error: external storage is read only.");
            return 0;
        }
        //Log.d("myAppName", "External storage is not read only or unavailable");

        // request permission when it is not granted. 
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Log.d("myAppName", "permission:WRITE_EXTERNAL_STORAGE: NOT granted!");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),folderName);
        int result = 0;
        if (folder.exists()) {
            //Log.d("myAppName","folder exist:"+folder.toString());
            result = 2; // folder exist
        }else{
            try {
                if (folder.mkdir()) {
                    //Log.d("myAppName", "folder created:" + folder.toString());
                    result = 1; // folder created
                } else {
                    //Log.d("myAppName", "creat folder fails:" + folder.toString());
                    result = 0; // creat folder fails
                }
            }catch (Exception ecp){
                ecp.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }

    public void dcLog(String log) {
        try {
            String filename = "dclog.txt";
            File root = new File(Environment.getExternalStorageDirectory(),"/dclog");
            if (!root.exists()) {
                if(!root.mkdir()) {
                    //showNotification("dclog","failed to make directory");
                }
            }
            File flog = new File(root, filename);
            FileWriter w = new FileWriter(flog);
            w.append(log);
            w.flush();
            w.close();
            //showNotification("dclog","woot");
        } catch(Exception e) {
            //showNotification("dclog",e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startForegroundService(new Intent(this, MotoMobBatteryService.class));
        //mkFolder("dctest");
        //dcLog("This is a test\n");

    }
}
