package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.navare.prashant.resortmanager.InAppBilling.IabHelper;
import com.navare.prashant.resortmanager.InAppBilling.IabHelper.IabAsyncInProgressException;
import com.navare.prashant.resortmanager.InAppBilling.IabResult;
import com.navare.prashant.resortmanager.InAppBilling.Purchase;
import com.navare.prashant.resortmanager.util.SimpleEula;
import com.navare.prashant.resortmanager.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends AppCompatActivity {
    private GridView mGridView;
    private AdView mAdView;

    private InterstitialAd mInterstitialAdForReservations;
    private InterstitialAd mInterstitialAdForTasks;
    private InterstitialAd mInterstitialAdForReports;
    private InterstitialAd mInterstitialAdForRooms;
    private InterstitialAd mInterstitialAdForInventory;
    private InterstitialAd mInterstitialAdForBackupRestore;
    private IabHelper mHelper;
    private Activity mThisActivity;

    static final String SKU_RESORT_MANAGER = "com.navare.prashant.resortmanager";
    // (arbitrary) request code for the purchase flow
    static final int PURCHASE_REQUEST = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: remove this before final build
        ResortManagerApp.setPurchaseValue(ResortManagerApp.APP_PURCHASED);

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

        if (ResortManagerApp.getOrgName().isEmpty()) {
            setTitle("Hotel/Resort Manager");
        }
        else {
            setTitle(ResortManagerApp.getOrgName() + " Manager");
        }

        mGridView =(GridView)findViewById(R.id.grid);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        onReservationsClick();
                        break;
                    case 1:
                        onTasksClick();
                        break;
                    case 2:
                        onRoomsClick();
                        break;
                    case 3:
                        onInventoryClick();
                        break;
                    case 4:
                        onReportsClick();
                        break;
                    case 5:
                        onBackupRestoreClick();
                        break;
                    case 6:
                        onRemoveAdsClick();
                        break;
                }
            }
        });

        initGridAdapater();

        doAdsInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                onSettingsClick();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initGridAdapater() {
        int numButtons = 0;
        if (ResortManagerApp.isAppPurchased()) {
            numButtons = 6;
        }
        else {
            numButtons = 7;
        }
        String[]    tileTextArray = new String[numButtons];
        int[]       tileImageArray = new int[numButtons];

        tileTextArray[0] = getString(R.string.reservations);
        tileTextArray[1]=getString(R.string.tasks) + " (" + String.valueOf(ResortManagerApp.getTaskCount()) + ")";;
        tileTextArray[2]=getString(R.string.rooms) + " (" + String.valueOf(ResortManagerApp.getRoomCount()) + ")";;
        tileTextArray[3]=getString(R.string.inventory) + " (" + String.valueOf(ResortManagerApp.getItemCount()) + ")";;
        tileTextArray[4]=getString(R.string.reports);
        tileTextArray[5]=getString(R.string.backup_restore);

        tileImageArray[0] = R.drawable.ic_reservations;
        tileImageArray[1] = R.drawable.ic_tasks;
        tileImageArray[2] = R.drawable.ic_rooms;
        tileImageArray[3] = R.drawable.ic_inventory;
        tileImageArray[4] = R.drawable.ic_reports;
        tileImageArray[5] = R.drawable.ic_backup;

        if (numButtons == 7) {
            tileTextArray[6] = getString(R.string.remove_ads);
            tileImageArray[6] = R.drawable.ic_remove_ads;
        }

        NavigationGridAdapter adapter = new NavigationGridAdapter(this, tileTextArray, tileImageArray);
        mGridView.setAdapter(adapter);
    }

    private void removeAdStuff() {
        mAdView.setVisibility(View.GONE);
    }

    private void doAdsInit() {

        mAdView = (AdView) findViewById(R.id.adView);

        if (ResortManagerApp.isAppPurchased()) {
            removeAdStuff();
            return;
        }

        // TODO: Replace the ad unit ID with the real one
        MobileAds.initialize(this, "ca-app-pub-3940256099942544/6300978111");

        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Reservations related interstitial ad
        mInterstitialAdForReservations = new InterstitialAd(this);
        mInterstitialAdForReservations.setAdUnitId(getString(R.string.interstitial_tasks_ad_unit_id));

        mInterstitialAdForReservations.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForReservations();
                onReservationsClick();
            }
        });

        // Tasks related interstitial ad
        mInterstitialAdForTasks = new InterstitialAd(this);
        mInterstitialAdForTasks.setAdUnitId(getString(R.string.interstitial_tasks_ad_unit_id));

        mInterstitialAdForTasks.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForTasks();
                onTasksClick();
            }
        });

        // Rooms related interstitial ad
        mInterstitialAdForRooms = new InterstitialAd(this);
        mInterstitialAdForRooms.setAdUnitId(getString(R.string.interstitial_reports_ad_unit_id));

        mInterstitialAdForRooms.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForRooms();
                onRoomsClick();
            }
        });

        // Inventory related interstitial ad
        mInterstitialAdForInventory = new InterstitialAd(this);
        mInterstitialAdForInventory.setAdUnitId(getString(R.string.interstitial_inventory_ad_unit_id));

        mInterstitialAdForInventory.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForInventory();
                onInventoryClick();
            }
        });

        // Reports related interstitial ad
        mInterstitialAdForReports = new InterstitialAd(this);
        mInterstitialAdForReports.setAdUnitId(getString(R.string.interstitial_tasks_ad_unit_id));

        mInterstitialAdForReports.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForReports();
                onReportsClick();
            }
        });

        // Backup/Restore related interstitial ad
        mInterstitialAdForBackupRestore = new InterstitialAd(this);
        mInterstitialAdForBackupRestore.setAdUnitId(getString(R.string.interstitial_backuprestore_ad_unit_id));

        mInterstitialAdForBackupRestore.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitialForSetup();
                onBackupRestoreClick();
            }
        });
    }

    private void doAdsReload() {
        if (mInterstitialAdForReservations != null && !mInterstitialAdForReservations.isLoaded()) {
            requestNewInterstitialForReservations();
        }
        if (mInterstitialAdForTasks != null && !mInterstitialAdForTasks.isLoaded()) {
            requestNewInterstitialForTasks();
        }
        if (mInterstitialAdForReports != null && !mInterstitialAdForReports.isLoaded()) {
            requestNewInterstitialForReports();
        }
        if (mInterstitialAdForRooms != null && !mInterstitialAdForRooms.isLoaded()) {
            requestNewInterstitialForRooms();
        }
        if (mInterstitialAdForInventory != null && !mInterstitialAdForInventory.isLoaded()) {
            requestNewInterstitialForInventory();
        }
        if (mInterstitialAdForBackupRestore != null && !mInterstitialAdForBackupRestore.isLoaded()) {
            requestNewInterstitialForSetup();
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
        if (ResortManagerApp.getOrgName().isEmpty()) {
            setTitle("Hotel/Resort Manager");
        }
        else {
            setTitle(ResortManagerApp.getOrgName() + " Manager");
        }
        initGridAdapater();

        if (ResortManagerApp.isAppPurchased()) {
            removeAdStuff();
        }
        else {
            if (mAdView != null) {
                mAdView.resume();
            }
            doAdsReload();
        }
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

    private void requestNewInterstitialForReservations() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForReservations.loadAd(adRequest);
    }

    private void requestNewInterstitialForTasks() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForTasks.loadAd(adRequest);
    }

    private void requestNewInterstitialForReports() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForReports.loadAd(adRequest);
    }

    private void requestNewInterstitialForRooms() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForRooms.loadAd(adRequest);
    }

    private void requestNewInterstitialForInventory() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForInventory.loadAd(adRequest);
    }

    private void requestNewInterstitialForSetup() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdForBackupRestore.loadAd(adRequest);
    }

    public void onReservationsClick() {
        if (mInterstitialAdForReservations != null && mInterstitialAdForReservations.isLoaded()) {
            mInterstitialAdForReservations.show();
        }
        else {
            startActivity(new Intent(this, ReservationsActivity.class));
        }
    }

    public void onTasksClick()
    {
        if (mInterstitialAdForTasks != null && mInterstitialAdForTasks.isLoaded()) {
            mInterstitialAdForTasks.show();
        }
        else {
            startActivity(new Intent(this, TaskListActivity.class));
        }
    }

    public void onRoomsClick() {
        if (mInterstitialAdForRooms != null && mInterstitialAdForRooms.isLoaded()) {
            mInterstitialAdForRooms.show();
        }
        else {
            startActivity(new Intent(this, RoomListActivity.class));
        }
    }

    public void onInventoryClick() {
        if (mInterstitialAdForInventory != null && mInterstitialAdForInventory.isLoaded()) {
            mInterstitialAdForInventory.show();
        }
        else {
            startActivity(new Intent(this, ItemListActivity.class));
        }
    }

    public void onReportsClick() {
        if (mInterstitialAdForReports != null && mInterstitialAdForReports.isLoaded()) {
            mInterstitialAdForReports.show();
        }
        else {
            startActivity(new Intent(this, ReportsActivity.class));
        }
    }

    public void onBackupRestoreClick() {
        if (mInterstitialAdForBackupRestore != null && mInterstitialAdForBackupRestore.isLoaded()) {
            mInterstitialAdForBackupRestore.show();
        }
        else {
            startActivity(new Intent(this, BackupRestoreActivity.class));
        }
    }

    public void onSettingsClick() {
        startActivity(new Intent(this, SettingsActivity.class));
    }


    public void onRemoveAdsClick() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Remove Ads");
        alertDialog.setMessage("Would you like to purchase  Hotel/Resort Manager and remove the ads?");
        alertDialog.setIcon(R.mipmap.ic_resort_manager);
        alertDialog.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                initiatePurchase();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void initiatePurchase() {

        String base64EncodedPublicKey = "com.navare.prashant.resortmanager";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
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
                    mHelper.launchPurchaseFlow(mThisActivity, SKU_RESORT_MANAGER, PURCHASE_REQUEST,
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

            if (purchase.getSku().equals(SKU_RESORT_MANAGER)) {
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

    // TODO: Revisit all alert dialogs for proper messaging (e.g. Adults should not empty should read Adults field should not be empty)
}

