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

import java.util.List;

/**
 * Created by funny on 2018/3/20.
 */

public class WakeupReceiver extends BroadcastReceiver {
    private static final String TAG = "WakeupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals(WakeupService.MOVE_TO_FRONT_ACTION)){
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.AppTask> tasks = am.getAppTasks();
                    if(tasks.size() > 0){
                        tasks.get(0).moveToFront();
                    }else{
                        Intent i = new Intent();
                        i.setClass(context,MainActivity.class);
                        context.startActivity(i);
                    }
                }else if(action.equals(WakeupService.WAKEUP_ALARM_ACTION)){
                    Wakeup(context);
                }else if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
                    broadcastStart(context);
                }else if(action.equals(Intent.ACTION_USER_PRESENT)){
                    broadcastStart(context);
                }else if(action.equals(Intent.ACTION_USER_UNLOCKED)){
                    broadcastStart(context);
                }else if(action.equals(Intent.ACTION_SCREEN_OFF)){
                    broadcastStartAlaram(context);
                }else if(action.equals(Intent.ACTION_SCREEN_ON)){
                    Intent i = new Intent(WakeupService.WAKEUP_ALARM_STOP_ACTION);
                    i.setClass(context,WakeupService.class);
                    context.startService(i);
                }
            }
        }
    }
    void broadcastStart(Context context){
        Intent i = new Intent(WakeupService.START_ACTION);
        i.setClass(context,WakeupService.class);
        context.startService(i);
    }
    void broadcastStartAlaram(Context context){
        Intent i = new Intent(WakeupService.WAKEUP_ALARM_ACTION);
        i.setClass(context,WakeupService.class);
        context.startService(i);
    }
    private void Wakeup(Context c){
        Log.i(TAG,"Wakeup");
        PowerManager mPowerManager = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "Ping");
        wakeLock.acquire();
        wakeLock.release();
        wakeLock = null;
        broadcastStartAlaram(c);
    }
}
