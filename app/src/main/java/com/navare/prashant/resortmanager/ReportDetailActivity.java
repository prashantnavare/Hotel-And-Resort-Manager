package com.navare.prashant.resortmanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
 * An activity representing a list of Tasks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TaskDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TaskListFragment} and the item details
 * (if present) is a {@link TaskDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link TaskListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ReportDetailActivity extends AppCompatActivity
        implements ReportDetailFragment.Callbacks, EmailDialogFragment.EmailDialogListener, SMSDialogFragment.SMSDialogListener {

    private Activity mThisActivity;
    private MenuItem emailMenuItem = null;
    private MenuItem messageMenuItem = null;
    private MenuItem callMenuItem = null;
    private ReportDetailFragment mMyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ReportDetailFragment.ARG_COMPLETED_RESERVATION_ID,
                    getIntent().getStringExtra(ReportDetailFragment.ARG_COMPLETED_RESERVATION_ID));
            mMyFragment = new ReportDetailFragment();
            mMyFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.report_detail_container, mMyFragment)
                    .commit();
        }
        mThisActivity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.report_detail_actions, menu);

        emailMenuItem = menu.getItem(0);
        messageMenuItem = menu.getItem(1);
        callMenuItem = menu.getItem(2);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_email:
                doEmail();
                return true;
            case R.id.menu_message:
                doMessage();
                return true;
            case R.id.menu_call:
                doCall();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doEmail() {
        EmailDialogFragment dialog = new EmailDialogFragment();
        dialog.setEmailAddress(mMyFragment.getEmailAddress());
        dialog.show(getSupportFragmentManager(), "EmailDialogFragment");
    }

    private void doMessage() {
        if(checkAndRequestSMSPermission()) {
            SMSDialogFragment dialog = new SMSDialogFragment();
            dialog.setMobileNumber(mMyFragment.getPhoneNumber());
            dialog.show(getSupportFragmentManager(), "SMSDialogFragment");
        }
    }

    private void doCall() {
        if(checkAndRequestCallPhonePermission()) {
            ((ReportDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.report_detail_container)).callCustomer();
        }
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

    @Override
    public void EnableEmailButton(boolean bEnable) {
        if (emailMenuItem != null) {
            emailMenuItem.setEnabled(bEnable);
            emailMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableMessageButton(boolean bEnable) {
        if (messageMenuItem != null) {
            messageMenuItem.setEnabled(bEnable);
            messageMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableCallButton(boolean bEnable) {
        if (callMenuItem != null) {
            callMenuItem.setEnabled(bEnable);
            callMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void RedrawOptionsMenu() {
        invalidateOptionsMenu();
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    @Override
    public void setTitleString(String titleString) {
        setTitle(titleString);
    }

    @Override
    public void onEmailDialogEmailClick(EmailDialogFragment dialog) {
        ((ReportDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.report_detail_container)).doEmail(dialog.getEmailAddress());
    }

    @Override
    public void onEmailDialogCancelClick(EmailDialogFragment dialog) {

    }

    @Override
    public void onSMSDialogSMSClick(SMSDialogFragment dialog) {
        ((ReportDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.report_detail_container)).doSMS(dialog.getMobileNumber());
    }

    @Override
    public void onSMSDialogCancelClick(SMSDialogFragment dialog) {

    }
}
