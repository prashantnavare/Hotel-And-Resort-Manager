package com.navare.prashant.resortmanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.navare.prashant.resortmanager.util.ContractTaskDoneDialogFragment;
import com.navare.prashant.resortmanager.util.InventoryTaskDoneDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An activity representing a single Task detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link TaskListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link TaskDetailFragment}.
 */
public class TaskDetailActivity extends AppCompatActivity
        implements  TaskDetailFragment.Callbacks,
                    ContractTaskDoneDialogFragment.ContractTaskDoneDialogListener,
                    InventoryTaskDoneDialogFragment.InventoryTaskDoneDialogListener {

    private MenuItem assignMenuItem = null;
    private MenuItem doneMenuItem = null;
    private MenuItem callMenuItem = null;
    private MenuItem saveMenuItem = null;
    private MenuItem revertMenuItem = null;

    private  boolean mbAssignMenuEnable = true;
    private boolean mbDoneMenuEnable = true;
    private boolean mbCallMenuEnable = false;
    private boolean mbSaveMenuEnable = false;
    private boolean mbRevertMenuEnable = false;

    private Activity mThisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

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
            arguments.putString(TaskDetailFragment.ARG_TASK_ID,
                    getIntent().getStringExtra(TaskDetailFragment.ARG_TASK_ID));
            TaskDetailFragment fragment = new TaskDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.task_detail_container, fragment)
                    .commit();
        }
        mThisActivity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_detail_actions, menu);

        saveMenuItem = menu.getItem(0);
        revertMenuItem = menu.getItem(1);
        assignMenuItem = menu.getItem(2);
        doneMenuItem = menu.getItem(3);
        callMenuItem = menu.getItem(4);

        // Toggle the options menu buttons as per desired state
        // It is possible that the query has already finished loading before we get here
        // as it happens on a separate thread. Hence the boolean state keepers
        EnableAssignButton(mbAssignMenuEnable);
        EnableTaskDoneButton(mbDoneMenuEnable);
        EnableCallButton(mbCallMenuEnable);
        EnableSaveButton(mbSaveMenuEnable);
        EnableRevertButton(mbRevertMenuEnable);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (mbSaveMenuEnable) {
                    promptUserForSavingTask();
                }
                else {
                    NavUtils.navigateUpTo(this, new Intent(this, TaskListActivity.class));
                }
                return true;
            case R.id.menu_assign:
                assignTask();
                return true;
            case R.id.menu_done:
                doneTask();
                return true;
            case R.id.menu_call:
                callAssignee();
                return true;
            case R.id.menu_save:
                saveTask();
                return true;
            case R.id.menu_revert:
                revertUI();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static final int REQUEST_ID_CONTACT_SMS_PERMISSIONS = 12;
    public static final int REQUEST_ID_CALL_PHONE_PERMISSION = 13;

    private void assignTask() {
        if(checkAndRequestContactAndSMSPermissions()) {
            // carry on the normal flow, as READ_CONTACTS and SEND_SMS permissions are granted.
            ((TaskDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.task_detail_container)).assignTask();
        }
    }

    private  boolean checkAndRequestContactAndSMSPermissions() {
        int readContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int sendSMSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (readContactsPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (sendSMSPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_CONTACT_SMS_PERMISSIONS);
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

            case REQUEST_ID_CONTACT_SMS_PERMISSIONS: {

                Log.d("assignTask()", "Contacts and SMS Permissions callback called");
                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

                        Log.d("assignTask()", "contacts & sms permissions granted");
                        ((TaskDetailFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.task_detail_container)).assignTask();
                        break;
                    }
                    else {
                        Log.d("assignTask()", "Some permissions are not granted. Ask again: ");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                            showDialogOK("Contacts and SMS Permissions are required for assigning a task.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestContactAndSMSPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // disable the assign task functionality
                                                    EnableAssignButton(false);
                                                    break;
                                            }
                                        }
                                    });
                            break;
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to Settings and enable Contacts and SMS permissions for the  Resort Manager before assigning tasks.", Toast.LENGTH_LONG).show();
                            // disable the assign task functionality
                            EnableAssignButton(false);
                            break;
                        }
                    }
                }
            }
            case REQUEST_ID_CALL_PHONE_PERMISSION: {

                Log.d("assignTask()", "Call Phone Permission callback called");
                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map
                perms.put(Manifest.permission.CALL_PHONE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    // Check for both permissions
                    if (perms.get(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                        Log.d("assignTask()", "call phone permission granted");
                        ((TaskDetailFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.task_detail_container)).callAssignee();
                        break;
                    }
                    else {
                        Log.d("assignTask()", "Some permissions are not granted. Ask again: ");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                            showDialogOK("Phone Permission is required for calling the assignee of a task.",
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
                            Toast.makeText(this, "Go to Settings and enable Phone permission for the  Resort Manager before calling assignees from the app.", Toast.LENGTH_LONG).show();
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

    private void doneTask() {

        ((TaskDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.task_detail_container)).showTaskDoneDialog();
    }

    private void callAssignee() {

        if(checkAndRequestCallPhonePermission()) {
            ((TaskDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.task_detail_container)).callAssignee();
        }
    }

    private void promptUserForSavingTask() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Save Changes");
        alertDialog.setMessage("Would you like to save the changes to this task?");
        alertDialog.setIcon(R.drawable.ic_menu_save);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                saveTask();
                NavUtils.navigateUpTo(mThisActivity, new Intent(mThisActivity, TaskListActivity.class));
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                NavUtils.navigateUpTo(mThisActivity, new Intent(mThisActivity, TaskListActivity.class));
            }
        });
        alertDialog.show();
    }

    private void saveTask() {
        ((TaskDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.task_detail_container)).saveTask();
    }

    private void revertUI() {
        ((TaskDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.task_detail_container)).revertUI();
    }

    @Override
    public void EnableAssignButton(boolean bEnable) {
        mbAssignMenuEnable = bEnable;
        if (assignMenuItem != null) {
            assignMenuItem.setEnabled(bEnable);
            assignMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableTaskDoneButton(boolean bEnable) {
        mbDoneMenuEnable = bEnable;
        if (doneMenuItem != null) {
            doneMenuItem.setEnabled(bEnable);
            doneMenuItem.setVisible(bEnable);
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
    public void RedrawOptionsMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void onTaskDone() {
        Toast toast = Toast.makeText(getApplicationContext(), "Task marked as done.", Toast.LENGTH_SHORT);
        toast.show();

        NavUtils.navigateUpTo(this, new Intent(this, TaskListActivity.class));
    }

    @Override
    public void setTitleString(String titleString) {
        setTitle(titleString);
    }

    @Override
    public void onContractTaskDoneClick(ContractTaskDoneDialogFragment dialog) {
        long contractValidTillDate = dialog.getContractValidTillDate();
        ((TaskDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.task_detail_container)).markContractTaskAsDone(contractValidTillDate);

    }

    @Override
    public void onContractTaskCancelClick(ContractTaskDoneDialogFragment dialog) {

    }

    @Override
    public void onInventoryTaskDoneClick(InventoryTaskDoneDialogFragment dialog) {
        long addedQuantity = dialog.getAddedQuantity();
        ((TaskDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.task_detail_container)).markInventoryTaskAsDone(addedQuantity);

    }

    @Override
    public void onInventoryTaskCancelClick(InventoryTaskDoneDialogFragment dialog) {

    }
}
