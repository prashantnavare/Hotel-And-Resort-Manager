package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.navare.prashant.resortmanager.InAppBilling.IabHelper;
import com.navare.prashant.resortmanager.InAppBilling.IabResult;
import com.navare.prashant.resortmanager.InAppBilling.Purchase;
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
    public static String sPrefTotalReservationCount = "TotalReservationCount";
    public static String sPrefNewReservationCount = "NewReservationCount";
    public static String sPrefCheckedInReservationCount = "CheckedInReservationCount";
    public static String sPrefHistoricalReservationCount = "HistoricalReservationCount";
    private static String sPrefPurchaseValue = "PurchaseValue";

    public static long APP_PURCHASED = 0xdeadbeef;

    private static SharedPreferences mPrefs;
    private static SharedPreferences.Editor mEditor;

    // purchase related
    static private IabHelper mHelper;

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

    // Max free rooms before requiring a purchase
    private static int mMaxFreeRooms = 10;
    static public int getMaxFreeRooms() { return mMaxFreeRooms;}

    // Max free reservations before requiring a purchase
    private static int mMaxFreeReservations = 100;
    static public int getMaxFreeReservations() { return mMaxFreeReservations;}

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

    // Total New reservations
    static public void incrementTotalReservationCount() {
        changeTotalReservationCount(1);
    }

    static private void changeTotalReservationCount(long numReservations) {
        long reservationCount = mPrefs.getLong(sPrefTotalReservationCount, 0);
        reservationCount = reservationCount + numReservations;
        mEditor.putLong(sPrefTotalReservationCount, reservationCount);
        mEditor.commit();
    }

    static public long getTotalReservationsCount() {
        return mPrefs.getLong(sPrefTotalReservationCount, 0);
    }

    // current New reservations
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
        alertDialog.setIcon(R.mipmap.ic_resort_manager);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    // purchase related
    static public void setPurchaseValue(long purchaseValue) {
        mEditor.putLong(sPrefPurchaseValue, purchaseValue);
        mEditor.commit();
    }

    static public boolean isAppPurchased() {
        long purchaseValue = mPrefs.getLong(sPrefPurchaseValue, 0);
        return purchaseValue == APP_PURCHASED;
    }

    public interface PurchaseListener {
        void onPurchaseCompleted();
    }

    private static Activity mPurchaseActivity;

    static public void promptForPurchase(final Activity callingActivity, String purchaseMessage) {

        if (callingActivity instanceof PurchaseListener) {
            mPurchaseActivity = callingActivity;
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mPurchaseActivity);
            alertDialog.setTitle("Purchase Hotel/Resort Manager");
            alertDialog.setMessage(purchaseMessage);
            alertDialog.setIcon(R.mipmap.ic_resort_manager);
            alertDialog.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {

                    initiatePurchase(callingActivity);
                }
            });

            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        }
    }

    static public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null)
            return false;

        return mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    private static final String SKU_RESORT_MANAGER = "com.navare.prashant.resortmanager";
    // (arbitrary) request code for the purchase flow
    private static final int PURCHASE_REQUEST = 10001;

    static private void initiatePurchase(final Activity callingActivity) {

        String base64EncodedPublicKey = "com.navare.prashant.resortmanager";

        mHelper = new IabHelper(mAppContext, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("initiatePurchase()", "Problem setting up In-app Billing: " + result);
                    showPurchaseErrorAlert("There was a problem with the purchase. Please try again later. Thank you.");
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) {
                    showPurchaseErrorAlert("There was a problem with the purchase. Please try again later. Thank you.");
                    return;
                }
                // Hooray, IAB is fully set up!
                Log.d("initiatePurchase()", "Launching purchase flow...");
                setWaitScreen(true);

                String payload = "";
                try {
                    mHelper.launchPurchaseFlow(callingActivity, SKU_RESORT_MANAGER, PURCHASE_REQUEST,
                            mPurchaseFinishedListener, payload);
                }
                catch (IabHelper.IabAsyncInProgressException e) {
                    showPurchaseErrorAlert("Another purchase operation may be in progress. Please try again later.");
                    setWaitScreen(false);
                }
            }
        });
    }

    static private void showPurchaseErrorAlert(String message) {
        showPurchaseAlertInternal(message, true);
    }

    static private void showPurchaseSuccessAlert(String message) {
        showPurchaseAlertInternal(message, false);
    }

    static private void showPurchaseAlertInternal(String message, boolean bError) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mPurchaseActivity);
        if (bError) {
            alertDialog.setTitle("Purchase Error");
            alertDialog.setIcon(R.drawable.ic_error);
        }
        else {
            alertDialog.setTitle("Purchase Successful");
            alertDialog.setIcon(R.drawable.ic_success);
        }
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton("OK", null);
        alertDialog.create().show();
    }

    // Callback for when a purchase is finished
    private static IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d("initiatePurchase()", "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isFailure()) {
                showPurchaseErrorAlert("There was an error while completing the purchase. Please try and again later.");
                setWaitScreen(false);
                return;
            }

            Log.d("initiatePurchase()", "Purchase successful.");

            if (purchase.getSku().equals(SKU_RESORT_MANAGER)) {
                // bought the  Resort Manager app!
                Log.d("initiatePurchase()", "Purchase is premium upgrade. Congratulating user.");
                showPurchaseSuccessAlert("Thank you for for the purchase.");
                ResortManagerApp.setPurchaseValue(ResortManagerApp.APP_PURCHASED);
                if (mPurchaseActivity != null) {
                    ((PurchaseListener) mPurchaseActivity).onPurchaseCompleted();
                }
                setWaitScreen(false);
            }
        }
    };

    static void setWaitScreen(boolean set) {
        // TODO: Implement purchase Wait screen
    }
}