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
import android.widget.CheckBox;
import android.widget.TextView;

import com.navare.prashant.resortmanager.Database.Item;
import com.navare.prashant.resortmanager.Database.Room;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.R;

/**
 * Created by prashant on 24-May-15.
 */
public class EmailDialogFragment extends DialogFragment {

    private TextView mTextEmail;
    private Button mBtnEmail;
    private Button mBtnCancel;

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    // Each method passes the DialogFragment in case the host needs to query it.
    public interface EmailDialogListener {
        void onEmailDialogEmailClick(EmailDialogFragment dialog);
        void onEmailDialogCancelClick(EmailDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    EmailDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the InventoryDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ServiceCallDialogListener so we can send events to the host
            mListener = (EmailDialogListener) activity;
        }
        catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement EmailDialogListener");
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

        getDialog().setTitle("Send E-mail");
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        View rootView = inflater.inflate(R.layout.dialog_email, container, false);

        mTextEmail = ((TextView) rootView.findViewById(R.id.textEmail));
        mTextEmail.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (mTextEmail.getText().toString().isEmpty())
                    mBtnEmail.setEnabled(false);
                else
                    mBtnEmail.setEnabled(true);
            }
        });

        mBtnEmail = ((Button) rootView.findViewById(R.id.btnEmail));
        mBtnEmail.setOnClickListener(onEmail);
        // By default, disable the Report button till Description is non empty.
        mBtnEmail.setEnabled(false);

        mBtnCancel = ((Button) rootView.findViewById(R.id.btnCancel));
        mBtnCancel.setOnClickListener(onCancel);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_menu_mail);
    }

    View.OnClickListener onCancel=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onEmailDialogCancelClick(EmailDialogFragment.this);
                    dismiss();
                }
            };

    View.OnClickListener onEmail=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onEmailDialogEmailClick(EmailDialogFragment.this);
                    dismiss();
                }
            };

    public String getEmailAddress() {
        return mTextEmail.getText().toString();
    }
}