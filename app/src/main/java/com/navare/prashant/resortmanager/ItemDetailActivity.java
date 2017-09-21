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

import com.navare.prashant.resortmanager.util.InventoryDialogFragment;
import com.navare.prashant.resortmanager.util.ServiceCallDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ItemDetailFragment}.
 */
public class ItemDetailActivity extends AppCompatActivity
        implements ItemDetailFragment.Callbacks, InventoryDialogFragment.InventoryDialogListener, ServiceCallDialogFragment.ServiceCallDialogListener {

    private MenuItem deleteMenuItem = null;
    private MenuItem revertMenuItem = null;
    private MenuItem saveMenuItem = null;

    private MenuItem inventoryAddMenuItem = null;
    private MenuItem inventorySubtractMenuItem = null;
    private MenuItem serviceCallMenuItem = null;

    private MenuItem cameraMenuItem = null;

    private boolean mbDeleteMenuEnable = false;
    private boolean mbRevertMenuEnable = false;
    private boolean mbSaveMenuEnable = false;

    private boolean mbInventoryAddMenuEnable = false;
    private boolean mbInventorySubtractMenuEnable = false;
    private boolean mbServiceCallMenuEnable = false;
    private boolean mbCameraMenuEnable = false;

    private Activity mThisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

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
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
        mThisActivity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_detail_actions, menu);

        saveMenuItem = menu.getItem(0);
        revertMenuItem = menu.getItem(1);
        serviceCallMenuItem = menu.getItem(2);
        inventoryAddMenuItem = menu.getItem(3);
        inventorySubtractMenuItem = menu.getItem(4);
        cameraMenuItem = menu.getItem(5);
        deleteMenuItem = menu.getItem(6);

        // Toggle the options menu buttons as per desired state
        // It is possible that the query has already finished loading before we get here
        // as it happens on a separate thread. Hence the boolean state keepers
        EnableSaveButton(mbSaveMenuEnable);
        EnableRevertButton(mbRevertMenuEnable);
        EnableDeleteButton(mbDeleteMenuEnable);

        EnableInventoryAddButton(mbInventoryAddMenuEnable);
        EnableInventorySubtractButton(mbInventorySubtractMenuEnable);
        EnableServiceCallButton(mbServiceCallMenuEnable);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            EnableCameraButton(false);
        }
        else {
            EnableCameraButton(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (mbSaveMenuEnable) {
                    promptUserForSavingItem();
                }
                else {
                    NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
                }
                return true;
            case R.id.menu_revert:
                revertUI();
                return true;
            case R.id.menu_delete:
                deleteItem();
                return true;
            case R.id.menu_save:
                saveItem();
                return true;
            case R.id.menu_inventory_add:
                showInventoryAddDialog();
                return true;
            case R.id.menu_inventory_subtract:
                showInventorySubtractDialog();
                return true;
            case R.id.menu_service_call:
                showServiceCallDialog();
                return true;
            case R.id.menu_camera:
                handleCamera();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static final int REQUEST_ID_STORAGE_PERMISSION = 13;

    private  boolean checkAndRequestStoragePermission() {
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case REQUEST_ID_STORAGE_PERMISSION: {

                Log.d("handleCamera()", "Storage Permission callback called");
                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                        Log.d("handleCamera()", "Storage permission granted");
                        ((ItemDetailFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.item_detail_container)).handleCamera();
                    }
                    else {
                        Log.d("handleCamera()", "Storage permission not granted. Ask again: ");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK(
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestStoragePermission();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // disable the photo functionality
                                                    EnableCameraButton(false);
                                                    break;
                                            }
                                        }
                                    });
                        }
                        // permission is denied (and never ask again is  checked)
                        // shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to Settings and enable Storage permission for the  Hotel/Resort Manager before taking photos of items.", Toast.LENGTH_LONG).show();
                            // disable the call assignee functionality
                            EnableCameraButton(false);
                        }
                    }
                }
            }
        }
    }

    private void showDialogOK(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage("Storage Permission is required for taking a photo of the item.")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void handleCamera() {

        if(checkAndRequestStoragePermission()) {
            ((ItemDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_detail_container)).handleCamera();
        }
    }

    private void showServiceCallDialog() {
        ((ItemDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_detail_container)).showServiceCallDialog();
    }

    private void showInventoryAddDialog() {
        ((ItemDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_detail_container)).showInventoryAddDialog();
    }

    private void showInventorySubtractDialog() {
        ((ItemDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_detail_container)).showInventorySubtractDialog();
    }

    private boolean saveItem() {
        return ((ItemDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_detail_container)).saveItem();
    }

    private void promptUserForSavingItem() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Save Changes");
        alertDialog.setMessage("Would you like to save the changes to this item?");
        alertDialog.setIcon(R.drawable.ic_menu_save);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                boolean bSuccess = saveItem();
                if (bSuccess)
                    NavUtils.navigateUpTo(mThisActivity, new Intent(mThisActivity, ItemListActivity.class));
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                NavUtils.navigateUpTo(mThisActivity, new Intent(mThisActivity, ItemListActivity.class));
            }
        });

        alertDialog.show();
    }

    private void deleteItem() {

        // First, get a confirmation from the user
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete");
        alertDialog.setMessage("Are you sure you want to delete this item?");
        alertDialog.setIcon(R.drawable.ic_delete_grey);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                ((ItemDetailFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.item_detail_container)).deleteItem();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void revertUI() {
        ((ItemDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_detail_container)).revertUI();
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
    public void EnableInventoryAddButton(boolean bEnable) {
        mbInventoryAddMenuEnable = bEnable;
        if (inventoryAddMenuItem != null) {
            inventoryAddMenuItem.setEnabled(bEnable);
            inventoryAddMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void EnableInventorySubtractButton(boolean bEnable) {
        mbInventorySubtractMenuEnable = bEnable;
        if (inventorySubtractMenuItem != null) {
            inventorySubtractMenuItem.setEnabled(bEnable);
            inventorySubtractMenuItem.setVisible(bEnable);
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

    private void EnableCameraButton(boolean bEnable) {
        mbCameraMenuEnable = bEnable;
        if (cameraMenuItem != null) {
            cameraMenuItem.setEnabled(bEnable);
            cameraMenuItem.setVisible(bEnable);
        }
    }

    @Override
    public void RedrawOptionsMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void onItemDeleted() {
        Toast toast = Toast.makeText(getApplicationContext(), "Item deleted.", Toast.LENGTH_LONG);
        toast.show();

        NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
    }

    @Override
    public void setTitleString(String titleString) {
        setTitle(titleString);
    }

    @Override
    public void onInventoryDialogPositiveClick(InventoryDialogFragment dialog) {
        String stringQuantity = dialog.getQuantity();
        if ((stringQuantity != null) && (!stringQuantity.isEmpty())) {
            long lQuantity = Long.valueOf(stringQuantity);
            if (dialog.getDialogType() == InventoryDialogFragment.InventoryDialogType.ADD) {
                ((ItemDetailFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.item_detail_container)).addToInventory(lQuantity);
            }
            else if (dialog.getDialogType() == InventoryDialogFragment.InventoryDialogType.SUBTRACT) {
                ((ItemDetailFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.item_detail_container)).subtractFromInventory(lQuantity);
            }
        }
    }

    @Override
    public void onInventoryDialogNegativeClick(InventoryDialogFragment dialog) {

    }

    @Override
    public void onServiceCallDialogReportClick(ServiceCallDialogFragment dialog) {
        ((ItemDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_detail_container)).createServiceCall(dialog.getItemID(), dialog.getDescription(), dialog.getPriority(), dialog.getItemName(), dialog.getItemLocation());
    }

    @Override
    public void onSaveCompleted() {
        Toast toast = Toast.makeText(getApplicationContext(), "Item saved.", Toast.LENGTH_LONG);
        toast.show();

        NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
    }

    @Override
    public void onServiceCallDialogCancelClick(ServiceCallDialogFragment dialog) {

    }
}
