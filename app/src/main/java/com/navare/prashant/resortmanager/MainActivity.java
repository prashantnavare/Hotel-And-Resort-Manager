package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.navare.prashant.resortmanager.InAppBilling.IabHelper;
import com.navare.prashant.resortmanager.InAppBilling.IabResult;
import com.navare.prashant.resortmanager.InAppBilling.IabHelper.IabAsyncInProgressException;
import com.navare.prashant.resortmanager.InAppBilling.Purchase;
import com.navare.prashant.resortmanager.util.SimpleEula;
import com.navare.prashant.resortmanager.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
    private Button mButtonTasks;
    private Button mButtonReservations;
    private Button mButtonInventory;
    private Button mButtonRooms;
    private Button mButtonRemoveAds;
    private AdView mAdView;
    private InterstitialAd mInterstitialAdForTasks;
    private InterstitialAd mInterstitialAdForInventory;
    private InterstitialAd mInterstitialAdForReports;
    private InterstitialAd mInterstitialAdForBackupRestore;
    private IabHelper mHelper;
    private Activity mThisActivity;

    // TODO: replace this with the real SKU
    static final String SKU_INVENTORY_MANAGER = "android.test.purchased";
    // (arbitrary) request code for the purchase flow
    static final int PURCHASE_REQUEST = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThisActivity = this;
        // To solve the documented problem of multiple instances of Main activity (see https://code.google.com/p/android/issues/detail?id=2373)
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_main);

        new SimpleEula(this).show();

        // Buttons
        mButtonTasks = (Button) findViewById(R.id.tasks_button);
        mButtonReservations = (Button) findViewById(R.id.reservations_button);
        mButtonInventory = (Button) findViewById(R.id.inventory_button);
        mButtonRooms = (Button) findViewById(R.id.rooms_button);

        // Set the title to the name of the hospital
        setTitleAndVariousCount();

        // Ads related
        mButtonRemoveAds = (Button) findViewById(R.id.removeads_button);
        // Banner Ad
        mAdView = (AdView) findViewById(R.id.adView);

        doAdsInit();
    }

    private void removeAdStuff() {
        mButtonRemoveAds.setVisibility(View.GONE);
        mAdView.setVisibility(View.GONE);
    }
    private void doAdsInit() {

        if (ResortManagerApp.isAppPurchased()) {
            removeAdStuff();
            return;
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Tasks related interstitial ad
        mInterstitialAdForTasks = new InterstitialAd(this);
        mInterstitialAdForTasks.setAdUnitId(getString(R.string.interstitial_tasks_ad_unit_id));

        mInterstitialAdForTasks.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForTasks();
                onTasksClick(null);
            }
        });

        // Inventory related interstitial ad
        mInterstitialAdForInventory = new InterstitialAd(this);
        mInterstitialAdForInventory.setAdUnitId(getString(R.string.interstitial_inventory_ad_unit_id));

        mInterstitialAdForInventory.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForInventory();
                onInventoryClick(null);
            }
        });

        // Reports related interstitial ad
        mInterstitialAdForReports = new InterstitialAd(this);
        mInterstitialAdForReports.setAdUnitId(getString(R.string.interstitial_reports_ad_unit_id));

        mInterstitialAdForReports.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForReports();
                onRoomsClick(null);
            }
        });

        // BackupRestore related interstitial ad
        mInterstitialAdForBackupRestore = new InterstitialAd(this);
        mInterstitialAdForBackupRestore.setAdUnitId(getString(R.string.interstitial_backuprestore_ad_unit_id));

        mInterstitialAdForBackupRestore.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForBackupRestore();
                onBackupRestoreClick(null);
            }
        });
    }

    private void doAdsReload() {
        if (mInterstitialAdForTasks != null && !mInterstitialAdForTasks.isLoaded()) {
            requestNewInterstitialForTasks();
        }
        if (mInterstitialAdForInventory != null && !mInterstitialAdForInventory.isLoaded()) {
            requestNewInterstitialForInventory();
        }
        if (mInterstitialAdForReports != null && !mInterstitialAdForReports.isLoaded()) {
            requestNewInterstitialForReports();
        }
        if (mInterstitialAdForBackupRestore != null && !mInterstitialAdForBackupRestore.isLoaded()) {
            requestNewInterstitialForBackupRestore();
        }
    }

    // Called when leaving the activity
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    // Called when returning to the activity
    @Override
    protected void onResume() {
        super.onResume();
        setTitleAndVariousCount();
        if (mAdView != null) {
            mAdView.resume();
        }
        doAdsReload();
    }

    // Called before the activity is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
            mAdView = null;
        }
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    private void requestNewInterstitialForTasks() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForTasks.loadAd(adRequest);
    }

    private void requestNewInterstitialForInventory() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForInventory.loadAd(adRequest);
    }

    private void requestNewInterstitialForReports() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForReports.loadAd(adRequest);
    }

    private void requestNewInterstitialForBackupRestore() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForBackupRestore.loadAd(adRequest);
    }

    public void onTasksClick(View view)
    {
        if (mInterstitialAdForTasks != null && mInterstitialAdForTasks.isLoaded()) {
            mInterstitialAdForTasks.show();
        }
        else {
            startActivity(new Intent(this, TaskListActivity.class));
        }
    }

    public void onReservationsClick(View view) {
        if (mInterstitialAdForInventory != null && mInterstitialAdForInventory.isLoaded()) {
            mInterstitialAdForInventory.show();
        }
        else {
            startActivity(new Intent(this, ReservationListActivity.class));
        }
    }

    public void onInventoryClick(View view) {
        if (mInterstitialAdForInventory != null && mInterstitialAdForInventory.isLoaded()) {
            mInterstitialAdForInventory.show();
        }
        else {
            startActivity(new Intent(this, ItemListActivity.class));
        }
    }

    public void onRoomsClick(View view) {
        if (mInterstitialAdForReports != null && mInterstitialAdForReports.isLoaded()) {
            mInterstitialAdForReports.show();
        }
        else {
            startActivity(new Intent(this, RoomListActivity.class));
        }
    }

    public void onBackupRestoreClick(View view) {
        if (mInterstitialAdForBackupRestore != null && mInterstitialAdForBackupRestore.isLoaded()) {
            mInterstitialAdForBackupRestore.show();
        }
        else {
            startActivity(new Intent(this, BackupRestoreActivity.class));
        }
    }

    public void onSettingsClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onRemoveAdsClick(View view) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Remove Ads");

        // Setting Dialog Message
        alertDialog.setMessage("Would you like to purchase  Resort Manager and remove the ads?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_hospital_inventory);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                initiatePurchase();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void initiatePurchase() {

        // TODO: compute your public key and store it in base64EncodedPublicKey
        String base64EncodedPublicKey = "Foo";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("initiatePurchase()", "Problem setting up In-app Billing: " + result);
                    // TODO: Show error message to the user
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) {
                    // TODO: Show error message to the user
                    return;
                }
                // Hooray, IAB is fully set up!
                Log.d("initiatePurchase()", "Launching purchase flow...");
                setWaitScreen(true);

                String payload = "";
                try {
                    mHelper.launchPurchaseFlow(mThisActivity, SKU_INVENTORY_MANAGER, PURCHASE_REQUEST,
                            mPurchaseFinishedListener, payload);
                } catch (IabAsyncInProgressException e) {
                    showPurchaseErrorAlert("Another purchase operation may be in progress. Please try again later.");
                    setWaitScreen(false);
                }
            }
        });
    }

    private void showPurchaseErrorAlert(String message) {
        showPurchaseAlertInternal(message, true);
    }

    private void showPurchaseSuccessAlert(String message) {
        showPurchaseAlertInternal(message, false);
    }

    private void showPurchaseAlertInternal(String message, boolean bError) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
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
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
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

            if (purchase.getSku().equals(SKU_INVENTORY_MANAGER)) {
                // bought the  Resort Manager app!
                Log.d("initiatePurchase()", "Purchase is premium upgrade. Congratulating user.");
                showPurchaseSuccessAlert("Thank you for for the purchase.");
                ResortManagerApp.setPurchaseValue(ResortManagerApp.APP_PURCHASED);
                removeAdStuff();
                setWaitScreen(false);
            }
        }
    };

    void setWaitScreen(boolean set) {
        // TODO: Implement purchase Wait screen
    }

    private void setTitleAndVariousCount() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String titleString = preferences.getString(ResortManagerApp.sPrefOrganizationName, "");
        titleString = titleString + "  Resort Manager";
        setTitle(titleString);

        long taskCount = preferences.getLong(ResortManagerApp.sPrefTaskCount, 0);
        String taskButtonString = "Tasks (" + String.valueOf(taskCount) + ")";
        mButtonTasks.setText(taskButtonString);

        long reservationsCount = preferences.getLong(ResortManagerApp.sPrefReservationCount, 0);
        String reservationsButtonString = "Reservations (" + String.valueOf(reservationsCount) + ")";
        mButtonReservations.setText(reservationsButtonString);

        long itemCount = preferences.getLong(ResortManagerApp.sPrefItemCount, 0);
        String itemButtonString = "Inventory (" + String.valueOf(itemCount) + ")";
        mButtonInventory.setText(itemButtonString);

        long roomCount = preferences.getLong(ResortManagerApp.sPrefRoomCount, 0);
        String roomButtonString = "Rooms (" + String.valueOf(roomCount) + ")";
        mButtonRooms.setText(roomButtonString);
    }

    // TODO: don't save completed tasks - just delete the task
    // TODO: get rid of completed task fts code in task.java
    // TODO: Revive Reports but for billing (and not for items). Add billing to reports
    // TODO: reservation entity should have:
    //          1: way to choose rooms
    //          2: tarif options (per person or per room)
    //          3. 3 states: waiting, checked-in and checked-out
    //          4. FTS name should have the number of people in brackets (e.g. Prashant (4))
    // TODO: all activities for Reservation similar to Room
    // TODO: Apply Tariff at the time of checkout
    //          1. Can be per person or per room
    //          2. UI for specifying either
    // TODO: Replace ic_hospital_inventory with ic_resort_manager icon
    // TODO: In Reservations list, show reservations that are Waiting and FromDate is in the past as RED
    // TODO: In Task List, change item to room and Location to Description when the task is for a room
}

