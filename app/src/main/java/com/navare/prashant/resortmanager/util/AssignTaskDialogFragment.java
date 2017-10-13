package com.navare.prashant.resortmanager.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.navare.prashant.resortmanager.R;

/**
 * Created by pnavare on 20/9/17.
 */

public class AssignTaskDialogFragment extends DialogFragment {

    private TextView mTextAssigneeName;
    private TextView mTextAssigneePhone;
    private Button mBtnOK;
    private Button mBtnCancel;
    private Context mContext;

    private String mAssigneeName = "";
    private String mAssigneePhone = "";

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    // Each method passes the DialogFragment in case the host needs to query it.
    public interface AssignTaskDialogListener {
        void onAssignTaskOKClick(AssignTaskDialogFragment dialog);
        void onCancelClick(AssignTaskDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    AssignTaskDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the InventoryDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the InventoryDialogListener so we can send events to the host
            mListener = (AssignTaskDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement AssignTaskDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_assign_task, container, false);

        mTextAssigneeName = ((TextView) rootView.findViewById(R.id.textAssigneeName));
        mTextAssigneeName.setText(mAssigneeName);

        mTextAssigneePhone = ((TextView) rootView.findViewById(R.id.textAssigneePhone));
        mTextAssigneePhone.setText(mAssigneePhone);

        mBtnOK = ((Button) rootView.findViewById(R.id.btnOK));
        mBtnOK.setOnClickListener(onAssignTask);
        mBtnCancel = ((Button) rootView.findViewById(R.id.btnCancel));
        mBtnCancel.setOnClickListener(onCancel);

        // Tweak the UI as per the type getResources().getText(R.string.main_title)
        Dialog myDialog = getDialog();
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return rootView;
    }

    View.OnClickListener onCancel=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onCancelClick(AssignTaskDialogFragment.this);
                    dismiss();
                }
            };

    View.OnClickListener onAssignTask=
            new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (mTextAssigneeName.getText().toString().isEmpty()) {
                        Toast toast = Toast.makeText(mContext, "Please enter assignee name.", Toast.LENGTH_SHORT);
                        toast.getView().setBackgroundResource(R.drawable.toast_drawable);
                        toast.show();
                        return;
                    }
                    mListener.onAssignTaskOKClick(AssignTaskDialogFragment.this);
                    dismiss();
                }
            };

    public String getAssigneeName() {
        return mTextAssigneeName.getText().toString();
    }

    public String getAssigneePhone() {
        return mTextAssigneePhone.getText().toString();
    }

    public void setAssigneeName(String assigneeName) {
        mAssigneeName = assigneeName;
    }

    public void setAssigneePhone(String assigneePhone) {
        mAssigneePhone = assigneePhone;
    }
}