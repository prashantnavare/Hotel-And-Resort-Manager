package com.navare.prashant.resortmanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.navare.prashant.resortmanager.util.EmailDialogFragment;
import com.navare.prashant.resortmanager.util.SMSDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An activity representing a single Reservation detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ReservationListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ReservationDetailFragment}.
 */
public class ReservationDetailActivity extends AppCompatActivity
        implements ReservationDetailFragment.Callbacks, EmailDialogFragment.EmailDialogListener, SMSDialogFragment.SMSDialogListener  {

    private MenuItem checkinMenuItem = null;
    private MenuItem checkoutMenuItem = null;
    private MenuItem completeCheckoutMenuItem = null;
    private MenuItem callMenuItem = null;
    private MenuItem messageMenuItem = null;
    private MenuItem emailMenuItem = null;
    private MenuItem zoomInMenuItem = null;
    private MenuItem saveMenuItem = null;
    private MenuItem revertMenuItem = null;
    private MenuItem deleteMenuItem = null;

    private boolean mbCheckinMenuEnable = false;
    private boolean mbCheckoutMenuEnable = false;
    private boolean mbCompleteCheckoutMenuEnable = false;
    private boolean mbCallMenuEnable = false;
    private boolean mbMessageMenuEnable = false;
    private boolean mbEmailMenuEnable = false;
    private boolean mbZoomInMenuEnable = false;
    private boolean mbSaveMenuEnable = false;
    private boolean mbRevertMenuEnable = false;
    private boolean mbDeleteMenuEnable = false;

    private ReservationDetailFragment mMyFragment;
    private Activity mThisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_detail);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ReservationDetailFragment.ARG_RESERVATION_ID,
                    getIntent().getStringExtra(ReservationDetailFragment.ARG_RESERVATION_ID));
            mMyFragment = new ReservationDetailFragment();
            mMyFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.reservation_detail_container, mMyFragment)
                    .commit();
        }
        mThisActivity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reservation_detail_actions, menu);

        checkinMenuItem = menu.getItem(0);
        checkoutMenuItem = menu.getItem(1);
        completeCheckoutMenuItem = menu.getItem(2);
        callMenuItem = menu.getItem(3);
        messageMenuItem = menu.getItem(4);
        emailMenuItem = menu.getItem(5);
        zoomInMenuItem = menu.getItem(6);
        saveMenuItem = menu.getItem(7);
        revertMenuItem = menu.getItem(8);
        deleteMenuItem = menu.getItem(9);

        // Toggle the options menu buttons as per desired state
        // It is possible that the query has already finished loading before we get here
        // as it happens on a separate thread. Hence the boolean state keepers
        EnableCheckinButton(mbCheckinMenuEnable);
        EnableCheckoutButton(mbCheckoutMenuEnable);
        EnableCompleteCheckoutButton(mbCompleteCheckoutMenuEnable);
        EnableCallButton(mbCallMenuEnable);
        EnableMessageButton(mbMessageMenuEnable);
        EnableEmailButton(mbEmailMenuEnable);
        EnableZoomInButton(mbZoomInMenuEnable);
        EnableSaveButton(mbSaveMenuEnable);
        EnableRevertButton(mbRevertMenuEnable);
        EnableDeleteButton(mbDeleteMenuEnable);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (mbSaveMenuEnable) {
                    promptUserForSavingReservation();
                }
                else {
                    NavUtils.navigateUpTo(this, new Intent(this, ReservationListActivity.class));
                }
                return true;
            case R.id.menu_checkin:
                doCheckin();
                return true;
            case R.id.menu_checkout:
                doCheckout();
                return true;
            case R.id.menu_completecheckout:
                doCompleteCheckout();
                return true;
            case R.id.menu_call:
                doCall();
                return true;
            case R.id.menu_message:
                doMessage();
                return true;
            case R.id.menu_email:
                doEmail();
                return true;
            case R.id.menu_zoomin:
                doZoomIn();
                return true;
            case R.id.menu_save:
                saveReservation();
                return true;
            case R.id.menu_revert:
                revertUI();
                return true;
            case R.id.menu_delete:
                deleteReservation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ mMyFragment.getEmailAddress()});
        emailIntent.setType("message/rfc822");

        startActivity(Intent.createChooser(emailIntent, "Send e-mail"));
    }

    private void doMessage() {
        if(checkAndRequestSMSPermission()) {
            invokeSMSManager();
        }
    }

    private void invokeSMSManager() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + Uri.encode(mMyFragment.getPhoneNumber())));
        startActivity(intent);
    }

    private void doCall() {
        if(checkAndRequestCallPhonePermission()) {
            invokeCallingApp();
        }
    }

    private void invokeCallingApp() {
        ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).callCustomer();
    }

    public static final int REQUEST_ID_SMS_PERMISSION = 12;
    public static final int REQUEST_ID_CALL_PHONE_PERMISSION = 13;

    private  boolean checkAndRequestSMSPermission() {
        int sendSMSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (sendSMSPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_SMS_PERMISSION);
            return false;
        }
        return true;
    }

    private  boolean checkAndRequestCallPhonePermission() {
        int callPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (callPhonePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_CALL_PHONE_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case REQUEST_ID_SMS_PERMISSION: {

                Log.d("doMessage()", "SMS Permission callback called");
                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map with SMS permission
                perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    // Check for SMS permission
                    if (perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

                        Log.d("doMessage()", "sms permission granted");
                        EnableMessageButton(true);
                        invokeSMSManager();
                        break;
                    }
                    else {
                        Log.d("doMessage()", "SMS permission are not granted. Ask again: ");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                            showDialogOK("SMS Permission is required for sending SMS.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestSMSPermission();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // disable the SMS functionality
                                                    EnableMessageButton(false);
                                                    break;
                                            }
                                        }
                                    });
                            break;
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to Settings and enable SMS permissions for the  Resort Manager before sending SMSs.", Toast.LENGTH_LONG).show();
                            // disable the assign task functionality
                            EnableMessageButton(false);
                            break;
                        }
                    }
                }
            }
            case REQUEST_ID_CALL_PHONE_PERMISSION: {

                Log.d("doCall()", "Call Phone Permission callback called");
                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map
                perms.put(Manifest.permission.CALL_PHONE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    // Check for CALL permission
                    if (perms.get(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                        Log.d("doCall()", "call phone permission granted");
                        EnableCallButton(true);
                        invokeCallingApp();
                        break;
                    }
                    else {
                        Log.d("doCall()", "Some permissions are not granted. Ask again: ");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                            showDialogOK("Phone Permission is required for calling.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestCallPhonePermission();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // disable the call assignee functionality
                                                    EnableCallButton(false);
                                                    break;
                                            }
                                        }
                                    });
                            break;
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to Settings and enable Phone permission for the  Resort Manager before calling from the app.", Toast.LENGTH_LONG).show();
                            // disable the call assignee functionality
                            EnableCallButton(false);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    private boolean saveReservation() {
        return ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).saveReservation();
    }

    private void promptUserForSavingReservation() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Save Changes");

        // Setting Dialog Message
        alertDialog.setMessage("Would you like to save the changes to this reservation?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_menu_save);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                boolean bSuccess = saveReservation();
                if (bSuccess)
                    NavUtils.navigateUpTo(mThisActivity, new Intent(mThisActivity, ReservationListActivity.class));
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                NavUtils.navigateUpTo(mThisActivity, new Intent(mThisActivity, ReservationListActivity.class));
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void deleteReservation() {

        // First, get a confirmation from the user
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete");
        alertDialog.setMessage("Are you sure you want to delete this reservation?");
        alertDialog.setIcon(R.drawable.ic_menu_delete);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                ((ReservationDetailFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.reservation_detail_container)).deleteReservation();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void doCheckin() {
        ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).doCheckin();
    }

    private void doZoomIn() {
        ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).doZoomIn();
    }

    private void doCheckout() {
        ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).doCheckout();
    }

    private void doCompleteCheckout() {
        ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).doCompleteCheckout();
    }

    private void revertUI() {
        ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).revertUI();
    }

    @Override
    public void EnableCheckinButton(boolean bEnable) {
        mbCheckinMenuEnable = bEnable;
        if (checkinMenuItem != null) {
            checkinMenuItem.setEnabled(bEnable);
            checkinMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableCheckoutButton(boolean bEnable) {
        mbCheckoutMenuEnable = bEnable;
        if (checkoutMenuItem != null) {
            checkoutMenuItem.setEnabled(bEnable);
            checkoutMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableCompleteCheckoutButton(boolean bEnable) {
        mbCompleteCheckoutMenuEnable = bEnable;
        if (completeCheckoutMenuItem != null) {
            completeCheckoutMenuItem.setEnabled(bEnable);
            completeCheckoutMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableCallButton(boolean bEnable) {
        mbCallMenuEnable = bEnable;
        if (callMenuItem != null) {
            callMenuItem.setEnabled(bEnable);
            callMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableMessageButton(boolean bEnable) {
        mbMessageMenuEnable = bEnable;
        if (messageMenuItem != null) {
            messageMenuItem.setEnabled(bEnable);
            messageMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableEmailButton(boolean bEnable) {
        mbEmailMenuEnable = bEnable;
        if (emailMenuItem != null) {
            emailMenuItem.setEnabled(bEnable);
            emailMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableZoomInButton(boolean bEnable) {
        mbZoomInMenuEnable = bEnable;
        if (zoomInMenuItem != null) {
            zoomInMenuItem.setEnabled(bEnable);
            zoomInMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableSaveButton(boolean bEnable) {
        mbSaveMenuEnable = bEnable;
        if (saveMenuItem != null) {
            saveMenuItem.setEnabled(bEnable);
            saveMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableRevertButton(boolean bEnable) {
        mbRevertMenuEnable = bEnable;
        if (revertMenuItem != null) {
            revertMenuItem.setEnabled(bEnable);
            revertMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableDeleteButton(boolean bEnable) {
        mbDeleteMenuEnable = bEnable;
        if (deleteMenuItem != null) {
            deleteMenuItem.setEnabled(bEnable);
            deleteMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void RedrawOptionsMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void onReservationDeleted() {
        Toast toast = Toast.makeText(getApplicationContext(), "Reservation deleted.", Toast.LENGTH_SHORT);
        toast.show();

        NavUtils.navigateUpTo(this, new Intent(this, ReservationListActivity.class));
    }

    @Override
    public void onCheckinCompleted() {
        Toast toast = Toast.makeText(getApplicationContext(), "Checkin completed.", Toast.LENGTH_SHORT);
        toast.show();

        NavUtils.navigateUpTo(this, new Intent(this, ReservationListActivity.class));
    }

    @Override
    public void onCheckoutCompleted() {
        Toast toast = Toast.makeText(getApplicationContext(), "Checkout completed.", Toast.LENGTH_SHORT);
        toast.show();

        NavUtils.navigateUpTo(this, new Intent(this, ReservationListActivity.class));
    }

    @Override
    public void onSaveCompleted() {
        Toast toast = Toast.makeText(getApplicationContext(), "Reservation saved.", Toast.LENGTH_SHORT);
        toast.show();

        NavUtils.navigateUpTo(this, new Intent(this, ReservationListActivity.class));
    }

    @Override
    public void setTitleString(String titleString) {
        setTitle(titleString);
    }

    @Override
    public void onEmailDialogEmailClick(EmailDialogFragment dialog) {
        ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).doEmail(dialog.getEmailAddress());
    }

    @Override
    public void onEmailDialogCancelClick(EmailDialogFragment dialog) {

    }

    @Override
    public void onSMSDialogSMSClick(SMSDialogFragment dialog) {
        ((ReservationDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_detail_container)).doSMS(dialog.getMobileNumber());
    }

    @Override
    public void onSMSDialogCancelClick(SMSDialogFragment dialog) {

    }
}
