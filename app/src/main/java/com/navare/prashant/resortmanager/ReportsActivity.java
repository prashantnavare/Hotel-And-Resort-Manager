package com.navare.prashant.resortmanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import com.navare.prashant.resortmanager.Database.Reservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReportsActivity extends AppCompatActivity {

    private Context mContext;
    private Button mButtonFrom;
    private Button mButtonTo;
    private Button mButtonGetReports;

    private LinearLayout mResultsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
        mButtonFrom = (Button) findViewById(R.id.btnFromDate);
        mButtonTo = (Button) findViewById(R.id.btnToDate);
        mButtonGetReports = (Button) findViewById(R.id.btnGetReports);

        mResultsLayout = (LinearLayout) findViewById(R.id.resultsLayout);

        mButtonFrom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker(mButtonFrom);
            }
        });

        mButtonTo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker(mButtonTo);
            }
        });
    }

    private void showDatePicker(final Button myButton) {
        Calendar dateToShow = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener onDateChangeCallback = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                myButton.setText(dateFormatter.format(newDate.getTime()));
            }
        };

        int year = dateToShow.get(Calendar.YEAR);
        int month = dateToShow.get(Calendar.MONTH);
        int day = dateToShow.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(mContext, onDateChangeCallback, year, month, day);
        datePicker.show();
    }

    public void onGetReports(View view) {
        String uiFromDate = mButtonFrom.getText().toString();
        Calendar fromDate = Calendar.getInstance();
        if (uiFromDate.compareToIgnoreCase("Set") == 0) {
            ResortManagerApp. showAlertDialog(mContext, "Incomplete Data", "From Date needs to be set.");
            mButtonFrom.requestFocus();
            return;
        }
        else {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
            try {
                fromDate.setTime(dateFormatter.parse(uiFromDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String uiToDate = mButtonTo.getText().toString();
        Calendar toDate = Calendar.getInstance();
        if (uiToDate.compareToIgnoreCase("Set") == 0) {
            ResortManagerApp. showAlertDialog(mContext, "Incomplete Data", "To Date needs to be set.");
            mButtonTo.requestFocus();
            return;
        }
        else {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
            try {
                toDate.setTime(dateFormatter.parse(uiToDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        mResultsLayout.setVisibility(View.VISIBLE);
    }

}
