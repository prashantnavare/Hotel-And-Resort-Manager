package com.navare.prashant.resortmanager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


/**
 * An activity representing a list of Reservations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of reservations, which when touched,
 * lead to a {@link ReservationDetailActivity} representing
 * reservation details. On tablets, the activity presents the list of reservations and
 * reservation details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of reservations is a
 * {@link ReservationListFragment} and the reservation details
 * (if present) is a {@link ReservationDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ReservationListFragment.Callbacks} interface
 * to listen for reservation selections.
 */
public class ReservationListActivity extends AppCompatActivity
        implements ReservationListFragment.Callbacks, SearchView.OnQueryTextListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private String mQuery = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.reservation_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list reservations should be given the
            // 'activated' state when touched.
            ((ReservationListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.reservation_list))
                    .setActivateOnItemClick(true);
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            ((ReservationListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.reservation_list)).getNewReservationList(mQuery);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reservation_list_actions, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Not Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(this);
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
            case R.id.menu_search:
                return true;
            case R.id.menu_add:
                onReservationSelected(null);
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Callback method from {@link ReservationListFragment.Callbacks}
     * indicating that the reservation with the given ID was selected.
     */
    @Override
    public void onReservationSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ReservationDetailFragment.ARG_RESERVATION_ID, id);
            ReservationDetailFragment fragment = new ReservationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.reservation_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected reservation ID.
            Intent detailIntent = new Intent(this, ReservationDetailActivity.class);
            detailIntent.putExtra(ReservationDetailFragment.ARG_RESERVATION_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public String getQuery() {
        return mQuery;
    }

    @Override
    public void setReservationCount(long reservationCount) {
        setTitle("Reservations (" + String.valueOf(reservationCount) + ")");
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        // handles a search query
        mQuery = !TextUtils.isEmpty(s) ? s : null;
        ((ReservationListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reservation_list)).getNewReservationList(mQuery);
        return true;
    }
}
