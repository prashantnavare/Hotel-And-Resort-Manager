package com.navare.prashant.resortmanager.util;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;

/**
 * Created by prashant on 16-Nov-15.
 */
public class ComputeNewTasksAlarmService extends IntentService {
    public ComputeNewTasksAlarmService() {
        super("ComputeNewTasksAlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // BEGIN_INCLUDE(service_onhandle)
        Log.i("NewTasksAlarmService", "Starting computeNewTasks()");
        getContentResolver().call(ResortManagerContentProvider.COMPUTE_NEW_TASKS_URI, "computeNewTasks", null, null);
        // Release the wake lock provided by the BroadcastReceiver.
        ComputeNewTasksAlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }
}
