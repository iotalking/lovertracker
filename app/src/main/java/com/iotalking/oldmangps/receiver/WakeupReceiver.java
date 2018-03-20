package com.iotalking.oldmangps.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by funny on 2018/3/20.
 */

public class WakeupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            if(intent.getAction().equals("OldManGPSMoveToFront")){
                if(intent.hasExtra("taskId")){
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    am.moveTaskToFront(intent.getIntExtra("taskId",0),0);
                }
            }
        }
    }
}
