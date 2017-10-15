package com.navare.prashant.resortmanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.navare.prashant.resortmanager.util.ServiceCallDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An activity representing a single Room detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link RoomListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link RoomDetailFragment}.
 */
public class RoomDetailActivity extends AppCompatActivity
        implements RoomDetailFragment.Callbacks, ServiceCallDialogFragment.ServiceCallDialogListener {

    private MenuItem deleteMenuItem = null;
    private MenuItem revertMenuItem = null;
    private MenuItem saveMenuItem = null;
    private MenuItem serviceCallMenuItem = null;

    private boolean mbDeleteMenuEnable = false;
    private boolean mbRevertMenuEnable = false;
    private boolean mbSaveMenuEnable = false;
    private boolean mbServiceCallMenuEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

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
            arguments.putString(RoomDetailFragment.ARG_ROOM_ID,
                    getIntent().getStringExtra(RoomDetailFragment.ARG_ROOM_ID));
            RoomDetailFragment fragment = new RoomDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.room_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.room_detail_actions, menu);

        saveMenuItem = menu.getItem(0);
        revertMenuItem = menu.getItem(1);
        serviceCallMenuItem = menu.getItem(2);
        deleteMenuItem = menu.getItem(3);

        // Toggle the options menu buttons as per desired state
        // It is possible that the query has already finished loading before we get here
        // as it happens on a separate thread. Hence the boolean state keepers
        EnableDeleteButton(mbDeleteMenuEnable);
        EnableSaveButton(mbSaveMenuEnable);
        EnableRevertButton(mbRevertMenuEnable);
        EnableServiceCallButton(mbServiceCallMenuEnable);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mbSaveMenuEnable) {
            promptUserForSavingRoom();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_revert:
                revertUI();
                return true;
            case R.id.menu_delete:
                deleteRoom();
                return true;
            case R.id.menu_save:
                saveRoom();
                return true;
            case R.id.menu_service_call:
                showServiceCallDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showServiceCallDialog() {
        ((RoomDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.room_detail_container)).showServiceCallDialog();
    }

    private boolean saveRoom() {
        return ((RoomDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.room_detail_container)).saveRoom();
    }

    private void promptUserForSavingRoom() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Save Changes");
        alertDialog.setMessage("Would you like to save the changes to this room?");
        alertDialog.setIcon(R.drawable.ic_save_black);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                saveRoom();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        alertDialog.show();
    }

    private void deleteRoom() {

        // First, get a confirmation from the user
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Delete");

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to delete this room?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_delete_black);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                ((RoomDetailFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.room_detail_container)).deleteRoom();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void revertUI() {
        ((RoomDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.room_detail_container)).revertUI();
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
    public void EnableRevertButton(boolean bEnable) {
        mbRevertMenuEnable = bEnable;
        if (revertMenuItem != null) {
            revertMenuItem.setEnabled(bEnable);
            revertMenuItem.setVisible(bEnable);
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
    public void EnableServiceCallButton(boolean bEnable) {
        mbServiceCallMenuEnable = bEnable;
        if (serviceCallMenuItem != null) {
            serviceCallMenuItem.setEnabled(bEnable);
            serviceCallMenuItem.setVisible(bEnable);
        }
    }


    @Override
    public void RedrawOptionsMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void onRoomDeleted() {
        Toast toast = Toast.makeText(getApplicationContext(), "Room deleted.", Toast.LENGTH_LONG);
        toast.getView().setBackgroundResource(R.drawable.toast_drawable);
        toast.show();

        finish();
    }

    @Override
    public void setTitleString(String titleString) {
        setTitle(titleString);
    }

    @Override
    public void onServiceCallDialogReportClick(ServiceCallDialogFragment dialog) {
        ((RoomDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.room_detail_container)).createServiceCall(dialog.getRoomID(), dialog.getDescription(), dialog.getPriority(), dialog.getRoomName(), dialog.getRoomDescription());
    }

    @Override
    public void onSaveCompleted() {
        Toast toast = Toast.makeText(getApplicationContext(), "Room saved.", Toast.LENGTH_LONG);
        toast.getView().setBackgroundResource(R.drawable.toast_drawable);
        toast.show();

        finish();
    }

    @Override
    public void onServiceCallDialogCancelClick(ServiceCallDialogFragment dialog) {

    }
}
