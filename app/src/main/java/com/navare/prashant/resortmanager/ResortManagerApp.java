package com.navare.prashant.resortmanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.navare.prashant.resortmanager.util.ComputeNewTasksAlarmReceiver;

/**
 * Created by prashant on 22-Nov-15.
 */
public class ResortManagerApp extends Application {

    // Object for intrinsic database lock
    public static final Object sDatabaseLock = new Object();

    public static Context sContext;

    private static String sPrefTaskAlarmInitialized = "TaskAlarmInitialized";
    public static String sPrefOrganizationName = "OrganizationName";
    public static String sPrefTaskRefreshTime = "TaskRefreshTime";
    public static String sPrefTaskCount = "TaskCount";
    public static String sPrefItemCount = "ItemCount";
    private static String sPrefPurchaseValue = "PurchaseValue";

    public static long APP_PURCHASED = 0xdeadbeef;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(sPrefTaskAlarmInitialized, false)) {
            // Set the preferences flag to true
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(sPrefTaskAlarmInitialized, true);
            editor.putString(ResortManagerApp.sPrefTaskRefreshTime, "01:00");
            editor.commit();

            ComputeNewTasksAlarmReceiver alarmReceiver = new ComputeNewTasksAlarmReceiver();
            // Set up the daily alarm for computing new tasks
            alarmReceiver.setAlarm(sContext, true);

        }
    }

    static public void incrementTaskCount() {
        changeTaskCount(1);
    }

    static public void decrementTaskCount() {
        changeTaskCount(-1);
    }

    static private void changeTaskCount(long numTasks) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        long taskCount = prefs.getLong(sPrefTaskCount, 0);
        taskCount = taskCount + numTasks;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(sPrefTaskCount, taskCount);
        editor.commit();
    }

    static public void incrementItemCount() {
        changeItemCount(1);
    }

    static public void decrementItemCount() {
        changeItemCount(-1);
    }

    static private void changeItemCount(long numItems) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        long itemCount = prefs.getLong(sPrefItemCount, 0);
        itemCount = itemCount + numItems;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(sPrefItemCount, itemCount);
        editor.commit();
    }

    static public void setPurchaseValue(long purchaseValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(sPrefPurchaseValue, purchaseValue);
        editor.commit();
    }

    static public boolean isAppPurchased() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        long purchaseValue = prefs.getLong(sPrefPurchaseValue, 0);
        return purchaseValue == APP_PURCHASED;
    }
}