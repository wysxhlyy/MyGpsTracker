package com.example.mario.mygpstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by mario on 2017/1/6.
 */

public class BatteryReceiver extends BroadcastReceiver {


    private BroadcastData broadcastData;

    public void onReceive(Context context, Intent intent) {

        int curretBattery=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int batteryScale=intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        float batteryPercent=curretBattery/(float)batteryScale;

        broadcastData.setBattery(batteryPercent*100+" %");

    }

    public interface BroadcastData{
        public void setBattery(String content);
    }

    public void setBroadcastData(BroadcastData broadcastData){
        this.broadcastData=broadcastData;
    }

}
