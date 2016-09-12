package com.navare.prashant.resortmanager.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.R;

//extend the SimpleCursorAdapter to create a custom class where we
//can override the getView to change the row colors of the list
public class ReportDetailCursorAdapter extends SimpleCursorAdapter {

    public ReportDetailCursorAdapter(Context context, int layout, Cursor c,
                                     String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //get reference to the row
        View view = super.getView(position, convertView, parent);

        // If the priority is Urgent, mark it red
        TextView textPriority = (TextView) view.findViewById(R.id.textPriority);
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String priority = cursor.getString(cursor.getColumnIndex(Task.COMPLETED_COL_FTS_TASK_PRIORITY));
        if (priority.equalsIgnoreCase("Urgent")) {
            textPriority.setTextColor(Color.RED);
        }
        else {
            textPriority.setTextColor(Color.BLUE);
        }
        return view;
    }
}

