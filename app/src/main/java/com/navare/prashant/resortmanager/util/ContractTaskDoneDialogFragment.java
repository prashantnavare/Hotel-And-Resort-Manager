package com.navare.prashant.resortmanager.util;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.navare.prashant.resortmanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ContractTaskDoneDialogFragment extends DialogFragment {

    private Button mBtnContractValidTillDate;
    private TextView mTextCompletionComments;
    private Button mBtnTaskDone;
    private Button mBtnCancel;

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    // Each method passes the DialogFragment in case the host needs to query it.
    public interface ContractTaskDoneDialogListener {
        void onContractTaskDoneClick(ContractTaskDoneDialogFragment dialog);
        void onContractTaskCancelClick(ContractTaskDoneDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    private ContractTaskDoneDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the InventoryDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the InventoryDialogListener so we can send events to the host
            mListener = (ContractTaskDoneDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TaskDoneDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle("Contract Task Done");
        View rootView = inflater.inflate(R.layout.dialog_contract_task_done, container, false);
        mBtnContractValidTillDate = (Button) rootView.findViewById(R.id.btnContractValidTillDate);
        mBtnContractValidTillDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        mTextCompletionComments = ((TextView) rootView.findViewById(R.id.textCompletionComments));
        mBtnTaskDone = ((Button) rootView.findViewById(R.id.btnTaskDone));
        mBtnTaskDone.setOnClickListener(onTaskDone);
        // By default, disable the TaskDone button till the contract date is set.
        mBtnTaskDone.setEnabled(false);

        mBtnCancel = ((Button) rootView.findViewById(R.id.btnCancel));
        mBtnCancel.setOnClickListener(onCancel);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return rootView;
    }

    private void showDatePicker() {
        Calendar dateToShow = Calendar.getInstance();
        CalibrationDatePickerFragment datePicker = new CalibrationDatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("year", dateToShow.get(Calendar.YEAR));
        args.putInt("month", dateToShow.get(Calendar.MONTH));
        args.putInt("day", dateToShow.get(Calendar.DAY_OF_MONTH));
        datePicker.setArguments(args);
        /**
         * Set Call back to capture selected date
         */
        DatePickerDialog.OnDateSetListener onDateChangeCallback = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                mBtnContractValidTillDate.setText(dateFormatter.format(newDate.getTime()));
                mBtnTaskDone.setEnabled(true);
            }
        };
        datePicker.setCallBack(onDateChangeCallback);
        datePicker.show(((FragmentActivity)mListener).getSupportFragmentManager(), "Contract Date Picker");
    }

    private View.OnClickListener onCancel=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onContractTaskCancelClick(ContractTaskDoneDialogFragment.this);
                    dismiss();
                }
            };

    private View.OnClickListener onTaskDone=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onContractTaskDoneClick(ContractTaskDoneDialogFragment.this);
                    dismiss();
                }
            };

    public String getCompletionComments() {
        return mTextCompletionComments.getText().toString();
    }

    public long getContractValidTillDate() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
        Calendar contractDate = Calendar.getInstance();
        String uiContractDate = mBtnContractValidTillDate.getText().toString();
        if (uiContractDate.compareToIgnoreCase("Set") != 0) {
            try {
                contractDate.setTime(dateFormatter.parse(uiContractDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return contractDate.getTimeInMillis();
        }
        return 0;
    }
}