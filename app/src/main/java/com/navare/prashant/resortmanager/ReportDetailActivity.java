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
    ReportDetailFragment mMyFragment;

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
        callMenuItem = menu.getItem(1);

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
                // TODO: doCall();
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
        if(checkAndRequestSMSPermissions()) {
            SMSDialogFragment dialog = new SMSDialogFragment();
            dialog.setMobileNumber(mMyFragment.getPhoneNumber());
            dialog.show(getSupportFragmentManager(), "SMSDialogFragment");
        }
    }

    public static final int REQUEST_ID_SMS_PERMISSION = 12;

    private  boolean checkAndRequestSMSPermissions() {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState.isEmpty()) {
            outState.putBoolean("bug:fix", true);
        }
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

                    // Check for both permissions
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
                                                    checkAndRequestSMSPermissions();
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
        }
    }

    private void EnableMessageButton(boolean bEnable) {
        if (messageMenuItem != null) {
            messageMenuItem.setEnabled(bEnable);
            messageMenuItem.setVisible(bEnable);
        }

    }

    private void EnableCallButton(boolean bEnable) {
        if (callMenuItem != null) {
            callMenuItem.setEnabled(bEnable);
            callMenuItem.setVisible(bEnable);
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
