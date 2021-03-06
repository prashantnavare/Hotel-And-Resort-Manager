package com.navare.prashant.resortmanager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


/**
 * An activity representing a list of Rooms. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of rooms, which when touched,
 * lead to a {@link RoomDetailActivity} representing
 * room details. On tablets, the activity presents the list of rooms and
 * room details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of rooms is a
 * {@link RoomListFragment} and the room details
 * (if present) is a {@link RoomDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link RoomListFragment.Callbacks} interface
 * to listen for room selections.
 */
public class RoomListActivity extends AppCompatActivity
        implements RoomListFragment.Callbacks, SearchView.OnQueryTextListener, ResortManagerApp.PurchaseListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private String mQuery = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.room_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list rooms should be given the
            // 'activated' state when touched.
            ((RoomListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.room_list))
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
            ((RoomListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.room_list)).getNewRoomList(mQuery);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.room_list_actions, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Not Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search rooms...");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_search:
                return true;
            case R.id.menu_add:
                handleAddRoom();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleAddRoom() {
        if (ResortManagerApp.isAppPurchased()) {
            // Create new room
            onRoomSelected(null);
        }
        else {
            if (ResortManagerApp.getRoomCount() < ResortManagerApp.getMaxFreeRooms()) {
                // Create new room
                onRoomSelected(null);
            }
            else {
                ResortManagerApp.promptForPurchase(this, "Please purchase  Hotel/Resort Manager to create additional rooms.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass on the activity result to ResortManagerApp to see if it handles it
        if (!ResortManagerApp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPurchaseCompleted() {
        // Create new room
        onRoomSelected(null);
    }

    /**
     * Callback method from {@link RoomListFragment.Callbacks}
     * indicating that the room with the given ID was selected.
     */
    @Override
    public void onRoomSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(RoomDetailFragment.ARG_ROOM_ID, id);
            RoomDetailFragment fragment = new RoomDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.room_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected room ID.
            Intent detailIntent = new Intent(this, RoomDetailActivity.class);
            detailIntent.putExtra(RoomDetailFragment.ARG_ROOM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public String getQuery() {
        return mQuery;
    }

    @Override
    public void setRoomCount(long roomCount) {
        setTitle("Rooms (" + String.valueOf(roomCount) + ")");
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        // handles a search query
        mQuery = !TextUtils.isEmpty(s) ? s : null;
        ((RoomListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.room_list)).getNewRoomList(mQuery);
        return true;
    }

}
