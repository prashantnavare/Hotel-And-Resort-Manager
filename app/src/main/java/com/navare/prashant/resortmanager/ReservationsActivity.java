package com.navare.prashant.resortmanager;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.navare.prashant.resortmanager.Database.Reservation;

public class ReservationsActivity extends AppCompatActivity {

    private Context mContext;
    private Button mButtonPending;
    private Button mButtonCheckedIn;
    private Button mButtonHistorical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
        mButtonPending = (Button) findViewById(R.id.pendingButton);
        mButtonCheckedIn = (Button) findViewById(R.id.checkedInButton);
        mButtonHistorical = (Button) findViewById(R.id.historicalButton);
    }

    @Override
    protected  void onResume() {
        super.onResume();
        setVariousCounts();
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

    private void setVariousCounts() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        long pendingCount = preferences.getLong(ResortManagerApp.sPrefPendingReservationCount, 0);
        String pendingButtonString = "Pending (" + String.valueOf(pendingCount) + ")";
        mButtonPending.setText(pendingButtonString);

        long checkedInCount = preferences.getLong(ResortManagerApp.sPrefCheckedInReservationCount, 0);
        String checkedInButtonString = "Checked In (" + String.valueOf(checkedInCount) + ")";
        mButtonCheckedIn.setText(checkedInButtonString);

        long historicalCount = preferences.getLong(ResortManagerApp.sPrefHistoricalReservationCount, 0);
        String historicalButtonString = "Historical (" + String.valueOf(historicalCount) + ")";
        mButtonHistorical.setText(historicalButtonString);
    }

}
