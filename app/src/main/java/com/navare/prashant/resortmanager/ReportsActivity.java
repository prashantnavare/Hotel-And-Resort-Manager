package com.navare.prashant.resortmanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.navare.prashant.resortmanager.Database.Reservation;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReportsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private Button mButtonFrom;
    private Button mButtonTo;
    private Button mButtonGetReports;
    private TextView mTextTotalRevenue;
    private TextView mTextTotalOccupancy;

    private String mFromDate;
    private String mToDate;

    private LinearLayout mResultsLayout;

    private static final int LOADER_ID_HISTORICAL_RESERVATIONS = 13;

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
        mTextTotalRevenue = (TextView) findViewById(R.id.textTotalRevenue);
        mTextTotalOccupancy = (TextView) findViewById(R.id.textTotalOccupancy);

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
        if (fromDate.getTimeInMillis() >= toDate.getTimeInMillis()) {
            ResortManagerApp. showAlertDialog(mContext, "Incorrect Dates", "To Date needs to be later than From Date.");
            return;
        }

        mFromDate = String.valueOf(fromDate.getTimeInMillis());
        mToDate = String.valueOf(toDate.getTimeInMillis());
        getSupportLoaderManager().restartLoader(LOADER_ID_HISTORICAL_RESERVATIONS, null, this);

        mResultsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == LOADER_ID_HISTORICAL_RESERVATIONS) {
            String [] selectionArgs = new String[] {mFromDate, mToDate};
            return new CursorLoader(this,
                    ResortManagerContentProvider.RESERVATION_HISTORICAL_URI, Reservation.FIELDS, null, selectionArgs,
                    null);
        }
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dataCursor) {

        if (dataCursor != null) {
            int loaderID = loader.getId();
            if (loaderID == LOADER_ID_HISTORICAL_RESERVATIONS) {
                long totalCharges = 0;
                long totalAdults = 0;
                long totalChildren = 0;

                for (dataCursor.moveToFirst(); !dataCursor.isAfterLast(); dataCursor.moveToNext()) {

                    long charges = dataCursor.getLong(dataCursor.getColumnIndex(Reservation.COL_TOTAL_CHARGE));
                    long adults = dataCursor.getLong(dataCursor.getColumnIndex(Reservation.COL_NUMADULTS));
                    long children = dataCursor.getLong(dataCursor.getColumnIndex(Reservation.COL_NUMCHILDREN));

                    totalCharges += charges;
                    totalAdults += adults;
                    totalChildren += children;
                }
                mTextTotalRevenue.setText(String.valueOf(totalCharges));
                mTextTotalOccupancy.setText("Adults : " + String.valueOf(totalAdults) + "   Children : " + String.valueOf(totalChildren));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
