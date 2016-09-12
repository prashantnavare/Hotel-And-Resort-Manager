package com.navare.prashant.resortmanager.util;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;

/**
 * Created by prashant on 05-Nov-15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ComputeNewTasksJobService extends JobService {

    private JobParameters params;
    private ComputeNewTasksTask computeNewTasksTask = new ComputeNewTasksTask();

    @Override
    public boolean onStartJob(JobParameters params) {
        this.params = params;
        Log.d("NewTasksJobService", "starting the computeNewTasks job");
        computeNewTasksTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("NewTasksJobService", "System calling to stop the computeNewTasks job");
        computeNewTasksTask.cancel(true);
        return false;
    }

    private class ComputeNewTasksTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("ComputeNewTasksTask", "Clean up the computeNewTasks task and call jobFinished...");
            jobFinished(params, false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("ComputeNewTasksTask", "calling hospital inentory content provider...");
            getContentResolver().call(ResortManagerContentProvider.COMPUTE_NEW_TASKS_URI, "computeNewTasks", null, null);
            return null;
        }
    }
}


