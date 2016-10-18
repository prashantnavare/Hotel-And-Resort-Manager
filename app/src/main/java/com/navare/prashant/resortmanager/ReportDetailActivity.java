package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.navare.prashant.resortmanager.util.EmailDialogFragment;
import com.navare.prashant.resortmanager.util.ServiceCallDialogFragment;


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
        implements ReportDetailFragment.Callbacks, EmailDialogFragment.EmailDialogListener {

    private Activity mThisActivity;
    private MenuItem emailMenuItem = null;
    private MenuItem messageMenuItem = null;

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
            ReportDetailFragment fragment = new ReportDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.report_detail_container, fragment)
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doEmail() {
        EmailDialogFragment dialog = new EmailDialogFragment();
        dialog.show(getSupportFragmentManager(), "ServiceCallDialogFragment");
        /*
        ((ReportDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.report_detail_container)).doEmail();
                */
    }

    private void doMessage() {
        /*
        ((ReportDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.report_detail_container)).doMessage();
                */
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
}
