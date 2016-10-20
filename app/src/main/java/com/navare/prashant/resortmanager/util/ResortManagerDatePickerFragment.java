package com.navare.prashant.resortmanager.util;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by prashant on 21-May-15.
 */

public class ResortManagerDatePickerFragment extends DialogFragment {

    private OnDateSetListener mOnDateSetCallback;
    private int mYear, mMonth, mDay;

    public ResortManagerDatePickerFragment() {
    }

    public void setCallBack(OnDateSetListener ondate) {
        mOnDateSetCallback = ondate;
    }


    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mYear = args.getInt("year");
        mMonth = args.getInt("month");
        mDay = args.getInt("day");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), mOnDateSetCallback, mYear, mMonth, mDay);
    }
}