package com.navare.prashant.resortmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.navare.prashant.resortmanager.util.SimpleEula;
import com.navare.prashant.resortmanager.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends AppCompatActivity {
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // To solve the documented problem of multiple instances of Main activity (see https://code.google.com/p/android/issues/detail?id=2373)
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_main);

        new SimpleEula(this).show();

        if (ResortManagerApp.getOrgName().isEmpty()) {
            setTitle("Hotel/Resort Manager");
        }
        else {
            setTitle(ResortManagerApp.getOrgName() + " Manager");
        }

        mGridView =(GridView)findViewById(R.id.grid);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        onReservationsClick();
                        break;
                    case 1:
                        onTasksClick();
                        break;
                    case 2:
                        onRoomsClick();
                        break;
                    case 3:
                        onInventoryClick();
                        break;
                    case 4:
                        onReportsClick();
                        break;
                    case 5:
                        onBackupRestoreClick();
                        break;
                }
            }
        });

        initGridAdapater();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                onSettingsClick();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initGridAdapater() {
        int numButtons = 6;
        String[]    tileTextArray = new String[numButtons];
        int[]       tileImageArray = new int[numButtons];

        tileTextArray[0] = getString(R.string.reservations);
        tileTextArray[1]=getString(R.string.tasks) + " (" + String.valueOf(ResortManagerApp.getTaskCount()) + ")";;
        tileTextArray[2]=getString(R.string.rooms) + " (" + String.valueOf(ResortManagerApp.getRoomCount()) + ")";;
        tileTextArray[3]=getString(R.string.inventory) + " (" + String.valueOf(ResortManagerApp.getItemCount()) + ")";;
        tileTextArray[4]=getString(R.string.reports);
        tileTextArray[5]=getString(R.string.backup_restore);

        tileImageArray[0] = R.drawable.ic_reservations;
        tileImageArray[1] = R.drawable.ic_tasks;
        tileImageArray[2] = R.drawable.ic_rooms;
        tileImageArray[3] = R.drawable.ic_inventory;
        tileImageArray[4] = R.drawable.ic_reports;
        tileImageArray[5] = R.drawable.ic_backup;

        NavigationGridAdapter adapter = new NavigationGridAdapter(this, tileTextArray, tileImageArray);
        mGridView.setAdapter(adapter);
    }

    // Called when leaving the activity
    @Override
    public void onPause() {
        super.onPause();
    }

    // Called when returning to the activity
    @Override
    protected void onResume() {
        super.onResume();
        if (ResortManagerApp.getOrgName().isEmpty()) {
            setTitle("Hotel/Resort Manager");
        }
        else {
            setTitle(ResortManagerApp.getOrgName() + " Manager");
        }
        initGridAdapater();
    }

    // Called before the activity is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void onReservationsClick() {
        startActivity(new Intent(this, ReservationsActivity.class));
    }

    public void onTasksClick()
    {
        startActivity(new Intent(this, TaskListActivity.class));
    }

    public void onRoomsClick() {
        startActivity(new Intent(this, RoomListActivity.class));
    }

    public void onInventoryClick() {
        startActivity(new Intent(this, ItemListActivity.class));
    }

    public void onReportsClick() {
        startActivity(new Intent(this, ReportsActivity.class));
    }

    public void onBackupRestoreClick() {
        startActivity(new Intent(this, BackupRestoreActivity.class));
    }

    public void onSettingsClick() {
        startActivity(new Intent(this, SettingsActivity.class));
    }


    // TODO: Revisit all alert dialogs for proper messaging (e.g. Adults should not empty should read Adults field should not be empty)
}

