package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class BackupRestoreActivity extends AppCompatActivity {

    private GridView mGridView;
    private AdView mAdView;
    private Activity mThisActivity;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mThisActivity = this;
        setContentView(R.layout.activity_backup_restore);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
        mGridView =(GridView)findViewById(R.id.grid);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        onBackupClick();
                        break;
                    case 1:
                        onRestoreClick();
                        break;
                }
            }
        });

        initGridAdapater();

        // Banner Ad
        mAdView = (AdView) findViewById(R.id.adView);
        if (ResortManagerApp.isAppPurchased()) {
            mAdView.setVisibility(View.GONE);
            mAdView = null;
        }
        else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    private void initGridAdapater() {
        String[]    tileTextArray = new String[2];
        int[]       tileImageArray = new int[2];

        tileTextArray[0] = getString(R.string.backup);
        tileTextArray[1]=getString(R.string.restore);

        tileImageArray[0] = R.drawable.ic_backup;
        tileImageArray[1] = R.drawable.ic_restore;

        NavigationGridAdapter adapter = new NavigationGridAdapter(this, tileTextArray, tileImageArray);
        mGridView.setAdapter(adapter);
    }

    public void onBackupClick() {
        BackupManager backupManager = new BackupManager(this);
        backupManager.dataChanged();
        Toast toast = Toast.makeText(mContext, "Backup operation has been scheduled.", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onRestoreClick() {
        BackupManager backupManager = new BackupManager(this);
        backupManager.requestRestore(
                new RestoreObserver(){
                    public void restoreFinished(int error) {
                        if (error == 0) {
                            String orgName;
                            if (ResortManagerApp.getOrgName().isEmpty()) {
                                orgName = "Hotel/Resort Manager";
                            }
                            else {
                                orgName = ResortManagerApp.getOrgName() + " Manager";
                            }
                            Toast toast = Toast.makeText(mContext, orgName + " data has been restored.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
        Toast toast = Toast.makeText(mContext, "Restore operation has been scheduled. You will be notified when the restore operation is completed.", Toast.LENGTH_SHORT);
        toast.show();
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
}
