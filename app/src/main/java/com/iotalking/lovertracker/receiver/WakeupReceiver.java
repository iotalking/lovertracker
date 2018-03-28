package com.iotalking.lovertracker.receiver;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

import com.iotalking.lovertracker.MainActivity;
import com.iotalking.lovertracker.service.WakeupService;

/**
 * Created by funny on 2018/3/20.
 */

public class WakeupReceiver extends BroadcastReceiver {
    private static final String TAG = "WakeupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            String action = intent.getAction();
            if(action.equals(WakeupService.MOVE_TO_FRONT_ACTION)){
                if(intent.hasExtra("taskId")){
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    am.moveTaskToFront(intent.getIntExtra("taskId",0),0);
                }
            }else if(action.equals(WakeupService.WAKEUP_ALARM_ACTION)){
                Wakeup(context);
                Log.i(TAG,"WAKEUP_ALARM_ACTION");
            }else if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
                Intent i = new Intent(WakeupService.WAKEUP_ALARM_ACTION);
                i.setClass(context,WakeupService.class);
                context.startService(i);
                Log.i(TAG,"WAKEUP_ALARM_ACTION");
            }else if(action.equals(Intent.ACTION_SCREEN_OFF)){
                Intent i = new Intent(WakeupService.WAKEUP_ALARM_ACTION);
                i.setClass(context,WakeupService.class);
                context.startService(i);
            }
        }
    }
    private void Wakeup(Context c){
        PowerManager mPowerManager = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "Ping");
        wakeLock.acquire();
        wakeLock.release();
        wakeLock = null;
    }
}
