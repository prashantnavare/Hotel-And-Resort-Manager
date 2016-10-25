package com.navare.prashant.resortmanager.util;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.navare.prashant.resortmanager.R;

/**
 * Created by prashant on 24-May-15.
 */
public class SMSDialogFragment extends DialogFragment {

    private TextView mTextMobileNumber;
    private Button mBtnSMS;
    private Button mBtnCancel;

    private String mPhoneNumber = "";

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    // Each method passes the DialogFragment in case the host needs to query it.
    public interface SMSDialogListener {
        void onSMSDialogSMSClick(SMSDialogFragment dialog);
        void onSMSDialogCancelClick(SMSDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    SMSDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the InventoryDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ServiceCallDialogListener so we can send events to the host
            mListener = (SMSDialogListener) activity;
        }
        catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SMSDialogListener");
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

        getDialog().setTitle("Send SMS");
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        View rootView = inflater.inflate(R.layout.dialog_sms, container, false);

        mTextMobileNumber = ((TextView) rootView.findViewById(R.id.textMobileNumber));
        mTextMobileNumber.setText(mPhoneNumber);
        mTextMobileNumber.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (mTextMobileNumber.getText().toString().isEmpty())
                    mBtnSMS.setEnabled(false);
                else
                    mBtnSMS.setEnabled(true);
            }
        });

        mBtnSMS = ((Button) rootView.findViewById(R.id.btnSMS));
        mBtnSMS.setOnClickListener(onSMS);
        // By default, disable the Report button till Description is non empty.
        if (mPhoneNumber.isEmpty())
            mBtnSMS.setEnabled(false);
        else
            mBtnSMS.setEnabled(true);

        mBtnCancel = ((Button) rootView.findViewById(R.id.btnCancel));
        mBtnCancel.setOnClickListener(onCancel);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_menu_message);
    }

    View.OnClickListener onCancel=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onSMSDialogCancelClick(SMSDialogFragment.this);
                    dismiss();
                }
            };

    View.OnClickListener onSMS=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onSMSDialogSMSClick(SMSDialogFragment.this);
                    dismiss();
                }
            };

    public String getMobileNumber() {
        return mTextMobileNumber.getText().toString();
    }
    public void setMobileNumber(String phoneNumber) { mPhoneNumber = phoneNumber; }
}