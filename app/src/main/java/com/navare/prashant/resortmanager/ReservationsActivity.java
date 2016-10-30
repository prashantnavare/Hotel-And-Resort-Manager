package com.navare.prashant.resortmanager;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.navare.prashant.resortmanager.Database.Reservation;

public class ReservationsActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
    }

    public void onPendingClick(View view) {
        Intent intent = new Intent(this, ReservationListActivity.class);
        intent.putExtra("type", Reservation.getStatusString(Reservation.PendingStatus));
        startActivity(intent);
    }

    public void onCheckedInClick(View view) {
        Intent intent = new Intent(this, ReservationListActivity.class);
        intent.putExtra("type", Reservation.getStatusString(Reservation.CheckedInStatus));
        startActivity(intent);
    }

    public void onHistoricalClick(View view) {
        startActivity(new Intent(this, ReportListActivity.class));
    }
}
