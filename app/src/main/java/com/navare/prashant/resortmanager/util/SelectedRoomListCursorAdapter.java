package com.navare.prashant.resortmanager.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navare.prashant.resortmanager.Database.Room;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//extend the SimpleCursorAdapter to create a custom class where we
//can override the getView to change the row text of the list
public class SelectedRoomListCursorAdapter extends SimpleCursorAdapter {

    public SelectedRoomListCursorAdapter(Context context, int layout, Cursor c,
                                         String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //get reference to the row
        View view = super.getView(position, convertView, parent);

        Cursor cursor = getCursor();
        cursor.moveToPosition(position);

        TextView textRoomName = (TextView) view.findViewById(R.id.textRoomName);
        TextView textRoomDescription = (TextView) view.findViewById(R.id.textRoomDescription);
        Long roomCapacity = cursor.getLong(cursor.getColumnIndex(Room.COL_CAPACITY));
        String roomName = cursor.getString(cursor.getColumnIndex(Room.COL_NAME));
        String roomDescription = cursor.getString(cursor.getColumnIndex(Room.COL_DESCRIPTION));
        roomName = roomName + " (Accomodates " + String.valueOf(roomCapacity) + ")";
        textRoomName.setText(roomName);
        textRoomDescription.setText(roomDescription);
        return view;
    }
}

