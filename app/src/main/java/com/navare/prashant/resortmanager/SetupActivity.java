package com.navare.prashant.resortmanager;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
    }

    public void onSettingsClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onBackupClick(View view) {
        BackupManager backupManager = new BackupManager(this);
        backupManager.dataChanged();
        Toast toast = Toast.makeText(mContext, "Backup operation has been scheduled.", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onRestoreClick(View view) {
        BackupManager backupManager = new BackupManager(this);
        backupManager.requestRestore(
                new RestoreObserver(){
                    public void restoreFinished(int error) {
                        if (error == 0) {
                            Toast toast = Toast.makeText(mContext, "ResortManager data has been restored.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
        Toast toast = Toast.makeText(mContext, "Restore operation has been scheduled. You will be notified when the restore operation is completed.", Toast.LENGTH_SHORT);
        toast.show();

    }
}
