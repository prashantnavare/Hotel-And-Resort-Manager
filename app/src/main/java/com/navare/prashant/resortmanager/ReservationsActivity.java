package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.google.android.gms.ads.AdView;
import com.navare.prashant.resortmanager.Database.Reservation;

public class ReservationsActivity extends AppCompatActivity {

    private GridView mGridView;
    private AdView mAdView;
    private Activity mThisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThisActivity = this;
        setContentView(R.layout.activity_reservations);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGridView =(GridView)findViewById(R.id.grid);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        onNewClick();
                        break;
                    case 1:
                        onCheckedInClick();
                        break;
                    case 2:
                        onHistoricalClick();
                        break;
                }
            }
        });

        initGridAdapater();
    }

    private void initGridAdapater() {
        String[]    tileTextArray = new String[3];
        int[]       tileImageArray = new int[3];

        tileTextArray[0]=getString(R.string.new_string) + " (" + String.valueOf(ResortManagerApp.getNewReservationsCount()) + ")";;
        tileTextArray[1]=getString(R.string.checked_in) + " (" + String.valueOf(ResortManagerApp.getCheckedInReservationsCount()) + ")";;
        tileTextArray[2]=getString(R.string.historical) + " (" + String.valueOf(ResortManagerApp.getHistoricalReservationsCount()) + ")";;

        tileImageArray[0] = R.drawable.ic_new;
        tileImageArray[1] = R.drawable.ic_tasks;
        tileImageArray[2] = R.drawable.ic_historical;

        NavigationGridAdapter adapter = new NavigationGridAdapter(this, tileTextArray, tileImageArray);
        mGridView.setAdapter(adapter);
    }

    @Override
    protected  void onResume() {
        super.onResume();
        initGridAdapater();
    }

    public void onNewClick() {
        Intent intent = new Intent(this, ReservationListActivity.class);
        intent.putExtra("type", Reservation.getStatusString(Reservation.NewStatus));
        startActivity(intent);
    }

    public void onCheckedInClick() {
        Intent intent = new Intent(this, ReservationListActivity.class);
        intent.putExtra("type", Reservation.getStatusString(Reservation.CheckedInStatus));
        startActivity(intent);
    }

    public void onHistoricalClick() {
        startActivity(new Intent(this, ReportListActivity.class));
    }
}
