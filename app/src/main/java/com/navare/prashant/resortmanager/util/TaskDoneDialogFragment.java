package com.navare.prashant.resortmanager.util;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.navare.prashant.resortmanager.R;

public class TaskDoneDialogFragment extends DialogFragment {

    private TextView mTextCompletionComments;
    private Button mBtnTaskDone;
    private Button mBtnCancel;

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    // Each method passes the DialogFragment in case the host needs to query it.
    public interface TaskDoneDialogListener {
        void onTaskDoneClick(TaskDoneDialogFragment dialog);
        void onCancelClick(TaskDoneDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    TaskDoneDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the InventoryDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the InventoryDialogListener so we can send events to the host
            mListener = (TaskDoneDialogListener) activity;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_task_done, container, false);
        mTextCompletionComments = ((TextView) rootView.findViewById(R.id.textCompletionComments));
        mBtnTaskDone = ((Button) rootView.findViewById(R.id.btnTaskDone));
        mBtnTaskDone.setOnClickListener(onTaskDone);
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
                    mListener.onCancelClick(TaskDoneDialogFragment.this);
                    dismiss();
                }
            };

    View.OnClickListener onTaskDone=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onTaskDoneClick(TaskDoneDialogFragment.this);
                    dismiss();
                }
            };

    public String getCompletionComments() {
        return mTextCompletionComments.getText().toString();
    }
}