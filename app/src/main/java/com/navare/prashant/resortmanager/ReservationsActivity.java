package com.navare.prashant.resortmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.navare.prashant.resortmanager.Database.Reservation;

public class ReservationsActivity extends AppCompatActivity {

    private Context mContext;
    private Button mButtonNew;
    private Button mButtonCheckedIn;
    private Button mButtonHistorical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
        mButtonNew = (Button) findViewById(R.id.newButton);
        mButtonCheckedIn = (Button) findViewById(R.id.checkedInButton);
        mButtonHistorical = (Button) findViewById(R.id.historicalButton);
    }

    @Override
    protected  void onResume() {
        super.onResume();
        setVariousCounts();
    }

    public void onNewClick(View view) {
        Intent intent = new Intent(this, ReservationListActivity.class);
        intent.putExtra("type", Reservation.getStatusString(Reservation.NewStatus));
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

        long newCount = preferences.getLong(ResortManagerApp.sPrefNewReservationCount, 0);
        String newButtonString = "New (" + String.valueOf(newCount) + ")";
        mButtonNew.setText(newButtonString);

        long checkedInCount = preferences.getLong(ResortManagerApp.sPrefCheckedInReservationCount, 0);
        String checkedInButtonString = "Checked In (" + String.valueOf(checkedInCount) + ")";
        mButtonCheckedIn.setText(checkedInButtonString);

        long historicalCount = preferences.getLong(ResortManagerApp.sPrefHistoricalReservationCount, 0);
        String historicalButtonString = "Historical (" + String.valueOf(historicalCount) + ")";
        mButtonHistorical.setText(historicalButtonString);
    }

}
