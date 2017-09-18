package com.navare.prashant.resortmanager;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.navare.prashant.resortmanager.util.ComputeNewTasksAlarmReceiver;

/**
 * Created by prashant on 22-Nov-15.
 */
public class ResortManagerApp extends Application {

    // Object for intrinsic database lock
    public static final Object sDatabaseLock = new Object();

    public static Context mAppContext;

    private static String sPrefTaskAlarmInitialized = "TaskAlarmInitialized";
    public static String sPrefOrganizationName = "OrganizationName";
    public static String sPrefTaskRefreshTime = "TaskRefreshTime";
    public static String sPrefTaskCount = "TaskCount";
    public static String sPrefItemCount = "ItemCount";
    public static String sPrefRoomCount = "RoomCount";
    public static String sPrefNewReservationCount = "NewReservationCount";
    public static String sPrefCheckedInReservationCount = "CheckedInReservationCount";
    public static String sPrefHistoricalReservationCount = "HistoricalReservationCount";
    private static String sPrefPurchaseValue = "PurchaseValue";

    public static long APP_PURCHASED = 0xdeadbeef;

    private static SharedPreferences mPrefs;
    private static SharedPreferences.Editor mEditor;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppContext = getApplicationContext();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPrefs.edit();

        if (!mPrefs.getBoolean(sPrefTaskAlarmInitialized, false)) {
            // Set the preferences flag to true
            mEditor.putBoolean(sPrefTaskAlarmInitialized, true);
            mEditor.putString(ResortManagerApp.sPrefTaskRefreshTime, "01:00");
            mEditor.commit();

            ComputeNewTasksAlarmReceiver alarmReceiver = new ComputeNewTasksAlarmReceiver();
            // Set up the daily alarm for computing new tasks
            alarmReceiver.setAlarm(mAppContext, true);
        }
    }

    // Task
    static public void incrementTaskCount() {
        changeTaskCount(1);
    }

    static public void decrementTaskCount() {
        changeTaskCount(-1);
    }

    static private void changeTaskCount(long numTasks) {
        long taskCount = mPrefs.getLong(sPrefTaskCount, 0);
        taskCount = taskCount + numTasks;
        mEditor.putLong(sPrefTaskCount, taskCount);
        mEditor.commit();
    }

    static public long getTaskCount() {
        return mPrefs.getLong(sPrefTaskCount, 0);
    }

    // Item
    static public void incrementItemCount() {
        changeItemCount(1);
    }

    static public void decrementItemCount() {
        changeItemCount(-1);
    }

    static private void changeItemCount(long numItems) {
        long itemCount = mPrefs.getLong(sPrefItemCount, 0);
        itemCount = itemCount + numItems;
        mEditor.putLong(sPrefItemCount, itemCount);
        mEditor.commit();
    }

    static public long getItemCount() {
        return mPrefs.getLong(sPrefItemCount, 0);
    }

    // Room
    static public void incrementRoomCount() {
        changeRoomCount(1);
    }

    static public void decrementRoomCount() {
        changeRoomCount(-1);
    }

    static private void changeRoomCount(long numRooms) {
        long roomCount = mPrefs.getLong(sPrefRoomCount, 0);
        roomCount = roomCount + numRooms;
        mEditor.putLong(sPrefRoomCount, roomCount);
        mEditor.commit();
    }

    static public long getRoomCount() {
        return mPrefs.getLong(sPrefRoomCount, 0);
    }

    // New reservations
    static public void incrementNewReservationCount() {
        changeNewReservationCount(1);
    }

    static public void decrementNewReservationCount() {
        changeNewReservationCount(-1);
    }

    static private void changeNewReservationCount(long numReservations) {
        long reservationCount = mPrefs.getLong(sPrefNewReservationCount, 0);
        reservationCount = reservationCount + numReservations;
        mEditor.putLong(sPrefNewReservationCount, reservationCount);
        mEditor.commit();
    }

    static public long getNewReservationsCount() {
        return mPrefs.getLong(sPrefNewReservationCount, 0);
    }

    // Checked-in Reservations
    static public void incrementCheckedInReservationCount() {
        changeCheckedInReservationCount(1);
    }

    static public void decrementCheckedInReservationCount() {
        changeCheckedInReservationCount(-1);
    }

    static private void changeCheckedInReservationCount(long numReservations) {
        long reservationCount = mPrefs.getLong(sPrefCheckedInReservationCount, 0);
        reservationCount = reservationCount + numReservations;
        mEditor.putLong(sPrefCheckedInReservationCount, reservationCount);
        mEditor.commit();
    }

    static public long getCheckedInReservationsCount() {
        return mPrefs.getLong(sPrefCheckedInReservationCount, 0);
    }

    // Historical reservations
    static public void incrementHistoricalReservationCount() { changeHistoricalReservationCount(1);
    }

    static public void decrementHistoricalReservationCount() {
        changeHistoricalReservationCount(-1);
    }

    static private void changeHistoricalReservationCount(long numReservations) {
        long reservationCount = mPrefs.getLong(sPrefHistoricalReservationCount, 0);
        reservationCount = reservationCount + numReservations;
        mEditor.putLong(sPrefHistoricalReservationCount, reservationCount);
        mEditor.commit();
    }
    static public long getHistoricalReservationsCount() {
        return mPrefs.getLong(sPrefHistoricalReservationCount, 0);
    }

    // purchase value
    static public void setPurchaseValue(long purchaseValue) {
        mEditor.putLong(sPrefPurchaseValue, purchaseValue);
        mEditor.commit();
    }

    static public boolean isAppPurchased() {
        long purchaseValue = mPrefs.getLong(sPrefPurchaseValue, 0);
        return purchaseValue == APP_PURCHASED;
    }

    // Organization name
    static public String getOrgName() {
        return mPrefs.getString(sPrefOrganizationName, "");
    }

    static public void setOrgName(String orgName) {
        mEditor.putString(sPrefOrganizationName, orgName);
    }

    static public void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_resort_manager);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

}