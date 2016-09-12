package com.navare.prashant.resortmanager.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by prashant on 16-Nov-15.
 */
public class ComputeNewTasksBootReceiver extends BroadcastReceiver {
    private ComputeNewTasksAlarmReceiver alarm = new ComputeNewTasksAlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context, true);
        }
    }
}
