package com.navare.prashant.resortmanager.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navare.prashant.resortmanager.Database.Reservation;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

//extend the SimpleCursorAdapter to create a custom class where we
//can override the getView to change the row colors of the list
public class ReservationListCursorAdapter extends SimpleCursorAdapter {

    public ReservationListCursorAdapter(Context context, int layout, Cursor c,
                                        String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //get reference to the row
        View view = super.getView(position, convertView, parent);

        // If the reservation is in Waiting state and the fromDate is older than today, then mark the whole entry in red
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String reservationState = cursor.getString(cursor.getColumnIndex(Reservation.COL_FTS_RESERVATION_STATUS));
        if (reservationState.equalsIgnoreCase("Waiting") == true) {
            // Next see if fromDate is older than today by a day
            String dateString = cursor.getString(cursor.getColumnIndex(Reservation.COL_FTS_RESERVATION_DATES));
            String[] dates = dateString.split("-");
            String fromDateString = dates[0];
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
            Calendar todayDate = Calendar.getInstance();
            Calendar fromDate = Calendar.getInstance();
            try {
                fromDate.setTime(dateFormatter.parse(fromDateString));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (fromDate.before(todayDate)) {
                long numberOfDaysOld = TimeUnit.DAYS.convert((todayDate.getTimeInMillis() - fromDate.getTimeInMillis()), TimeUnit.MILLISECONDS);
                if (numberOfDaysOld >= 1) {
                    TextView textName = (TextView) view.findViewById(R.id.textReservationName);
                    TextView textDates = (TextView) view.findViewById(R.id.textReservationDates);
                    TextView textStatus = (TextView) view.findViewById(R.id.textReservationStatus);
                    textName.setTextColor(Color.RED);
                    textDates.setTextColor(Color.RED);
                    textStatus.setTextColor(Color.RED);
                }
            }
        }
        return view;
    }
}

