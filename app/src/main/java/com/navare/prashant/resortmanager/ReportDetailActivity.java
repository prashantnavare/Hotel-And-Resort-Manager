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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.report_detail_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
