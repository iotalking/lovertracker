package com.iotalking.lovertracker.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.iotalking.lovertracker.MainActivity;
import com.iotalking.lovertracker.service.WakeupService;

/**
 * Created by funny on 2018/3/20.
 */

public class WakeupReceiver extends BroadcastReceiver {
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
                Intent i = new Intent();
                i.setClass(context,WakeupService.class);
                context.startService(i);
            }
        }
    }
}
