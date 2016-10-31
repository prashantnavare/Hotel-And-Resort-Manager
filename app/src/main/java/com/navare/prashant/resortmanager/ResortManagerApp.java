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
    public static String sPrefRoomCount = "RoomCount";
    public static String sPrefPendingReservationCount = "PendingReservationCount";
    public static String sPrefCheckedInReservationCount = "CheckedInReservationCount";
    public static String sPrefHistoricalReservationCount = "HistoricalReservationCount";
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

    static public void incrementRoomCount() {
        changeRoomCount(1);
    }

    static public void decrementRoomCount() {
        changeRoomCount(-1);
    }

    static private void changeRoomCount(long numRooms) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        long roomCount = prefs.getLong(sPrefRoomCount, 0);
        roomCount = roomCount + numRooms;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(sPrefRoomCount, roomCount);
        editor.commit();
    }

    static public void incrementPendingReservationCount() {
        changePendingReservationCount(1);
    }

    static public void decrementPendingReservationCount() {
        changePendingReservationCount(-1);
    }

    static private void changePendingReservationCount(long numReservations) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        long reservationCount = prefs.getLong(sPrefPendingReservationCount, 0);
        reservationCount = reservationCount + numReservations;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(sPrefPendingReservationCount, reservationCount);
        editor.commit();
    }

    static public void incrementCheckedInReservationCount() {
        changeCheckedInReservationCount(1);
    }

    static public void decrementCheckedInReservationCount() {
        changeCheckedInReservationCount(-1);
    }

    static private void changeCheckedInReservationCount(long numReservations) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        long reservationCount = prefs.getLong(sPrefCheckedInReservationCount, 0);
        reservationCount = reservationCount + numReservations;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(sPrefCheckedInReservationCount, reservationCount);
        editor.commit();
    }

    static public void incrementHistoricalReservationCount() { changeHistoricalReservationCount(1);
    }

    static public void decrementHistoricalReservationCount() {
        changeHistoricalReservationCount(-1);
    }

    static private void changeHistoricalReservationCount(long numReservations) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        long reservationCount = prefs.getLong(sPrefHistoricalReservationCount, 0);
        reservationCount = reservationCount + numReservations;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(sPrefHistoricalReservationCount, reservationCount);
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