package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.navare.prashant.resortmanager.Database.Item;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;
import com.navare.prashant.resortmanager.Database.Room;
import com.navare.prashant.resortmanager.Database.ServiceCall;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.util.ContractTaskDoneDialogFragment;
import com.navare.prashant.resortmanager.util.InventoryTaskDoneDialogFragment;
import com.navare.prashant.resortmanager.util.ResortManagerDatePickerFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A fragment representing a single Task detail screen.
 * This fragment is either contained in a {@link TaskListActivity}
 * in two-pane mode (on tablets) or a {@link TaskDetailActivity}
 * on handsets.
 */
public class TaskDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ID_TASK_DETAILS = 12;
    public static final int LOADER_ID_ITEM_DETAILS = 13;
    public static final int LOADER_ID_SERVICE_CALL_DETAILS = 14;
    public static final int LOADER_ID_ROOM_DETAILS = 15;

    public static final String ARG_TASK_ID = "task_id";

    private Context mContext = null;

    private String mTaskID;
    private Task mTask = null;
    private Item mItem = null;
    private Room mRoom = null;
    private ServiceCall mServiceCall = null;

    private TextView mTextItemType;
    private TextView mTextItemName;
    private TextView mTextLocation;
    private TextView mTextItemLocation;
    private Button mBtnChangeDueDate;
    private TextView mTextAssignedTo;
    private TextView mTextTaskType;
    private TextView mTextInstructionsLabel;
    private TextView mTextInstructions;

    private LinearLayout mContractExpiryDateLayout;
    private TextView mTextContractExpiryDate;
    private LinearLayout mRequiredQuantityLayout;
    private TextView mTextRequiredQuantity;

    private RadioButton mUrgentButton;
    private RadioButton mNormalRadioButton;
    int mPreviousPriority = 0;

    private AdView mAdView;

    String mCurrentAssignee = "";
    String mNewAssignee = "";

    public interface Callbacks {
        /**
         * Callbacks for when a task has been selected.
         */
        void EnableAssignButton(boolean bEnable);
        void EnableTaskDoneButton(boolean bEnable);
        void EnableCallButton(boolean bEnable);
        void EnableSaveButton(boolean bEnable);
        void EnableRevertButton(boolean bEnable);
        void RedrawOptionsMenu();
        void onTaskDone();
        void setTitleString(String titleString);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public void EnableAssignButton(boolean bEnable) {
        }

        @Override
        public void EnableTaskDoneButton(boolean bEnable) {
        }

        @Override
        public void EnableCallButton(boolean bEnable) {
        }

        @Override
        public void EnableRevertButton(boolean bEnable) {
        }

        @Override
        public void EnableSaveButton(boolean bEnable) {
        }

        @Override
        public void RedrawOptionsMenu() {
        }

        @Override
        public void onTaskDone() {
        }

        @Override
        public void setTitleString(String titleString) {
        }

    };

    /**
     * The fragment's current callback object, which is notified of changes to the item
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_TASK_ID)) {
            mTaskID = getArguments().getString(ARG_TASK_ID);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement task detail fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    // Called when returning to the activity
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    // Called before the activity is destroyed
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);

        mTextItemType = ((TextView) rootView.findViewById(R.id.textItemType));
        mTextItemName = ((TextView) rootView.findViewById(R.id.textItemName));
        mTextLocation = ((TextView) rootView.findViewById(R.id.textLocation));
        mTextItemLocation = ((TextView) rootView.findViewById(R.id.textItemLocation));

        mBtnChangeDueDate = (Button) rootView.findViewById(R.id.btnChangeDueDate);
        mBtnChangeDueDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        mTextAssignedTo = ((TextView) rootView.findViewById(R.id.textAssignedTo));

        mTextTaskType = ((TextView) rootView.findViewById(R.id.textTaskType));

        mTextInstructionsLabel = ((TextView) rootView.findViewById(R.id.textInstructionsLabel));
        mTextInstructions = ((TextView) rootView.findViewById(R.id.textInstructions));

        mUrgentButton = (RadioButton) rootView.findViewById(R.id.urgentRadioButton);
        mUrgentButton.setChecked(false);
        mUrgentButton.setOnClickListener(new RadioGroup.OnClickListener() {
            public void onClick(View v){
                if (mPreviousPriority != Task.UrgentPriority) {
                    mPreviousPriority = Task.UrgentPriority;
                    mCallbacks.EnableRevertButton(true);
                    mCallbacks.EnableSaveButton(true);
                    mCallbacks.RedrawOptionsMenu();
                }
            }
        });
        mNormalRadioButton = (RadioButton) rootView.findViewById(R.id.normalRadioButton);
        mUrgentButton.setChecked(false);
        mNormalRadioButton.setOnClickListener(new RadioGroup.OnClickListener() {
            public void onClick(View v){
                if (mPreviousPriority != Task.NormalPriority) {
                    mPreviousPriority = Task.NormalPriority;
                    mCallbacks.EnableRevertButton(true);
                    mCallbacks.EnableSaveButton(true);
                    mCallbacks.RedrawOptionsMenu();
                }
            }
        });

        mContractExpiryDateLayout = (LinearLayout) rootView.findViewById(R.id.contractExpiryDateLayout);
        mTextContractExpiryDate = ((TextView) rootView.findViewById(R.id.textContractExpiryDate));
        mRequiredQuantityLayout = (LinearLayout) rootView.findViewById(R.id.requiredQuantityLayout);
        mTextRequiredQuantity = ((TextView) rootView.findViewById(R.id.textRequiredQuantity));

        // Banner Ad
        mAdView = (AdView) rootView.findViewById(R.id.adView);
        if (ResortManagerApp.isAppPurchased()) {
            mAdView.setVisibility(View.GONE);
            mAdView = null;
        }
        else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if ((mTaskID != null) && (!mTaskID.isEmpty())) {
            getLoaderManager().initLoader(LOADER_ID_TASK_DETAILS, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == LOADER_ID_TASK_DETAILS) {
            Uri taskURI = Uri.withAppendedPath(ResortManagerContentProvider.TASK_URI,
                    mTaskID);

            return new CursorLoader(getActivity(),
                    taskURI, Task.FIELDS, null, null,
                    null);
        }
        else if (id == LOADER_ID_ITEM_DETAILS) {
            Uri itemURI = Uri.withAppendedPath(ResortManagerContentProvider.ITEM_URI,
                    String.valueOf(mTask.mItemID));

            return new CursorLoader(getActivity(),
                    itemURI, Item.FIELDS, null, null,
                    null);
        }
        else if (id == LOADER_ID_ROOM_DETAILS) {
            Uri roomURI = Uri.withAppendedPath(ResortManagerContentProvider.ROOM_URI,
                    String.valueOf(mTask.mRoomID));

            return new CursorLoader(getActivity(),
                    roomURI, Room.FIELDS, null, null,
                    null);
        }
        else if (id == LOADER_ID_SERVICE_CALL_DETAILS) {
            Uri serviceCallURI = Uri.withAppendedPath(ResortManagerContentProvider.SERVICE_CALL_URI,
                    String.valueOf(mTask.mServiceCallID));

            return new CursorLoader(getActivity(),
                    serviceCallURI, ServiceCall.FIELDS, null, null,
                    null);
        }
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dataCursor) {

        if (dataCursor != null) {
            int loaderID = loader.getId();
            if (loaderID == LOADER_ID_TASK_DETAILS) {
                if (mTask == null)
                    mTask = new Task();

                mTask.setContentFromCursor(dataCursor);
                updateUIFromTask();

                if (mTask.mTaskType == Task.ServiceCall) {
                    // Get the service call details.
                    getLoaderManager().initLoader(LOADER_ID_SERVICE_CALL_DETAILS, null, this);
                }
                else if (mTask.mTaskType == Task.Cleaning) {
                    // Get the room details.
                    getLoaderManager().initLoader(LOADER_ID_ROOM_DETAILS, null, this);
                }
                else {
                    // Get the item details
                    getLoaderManager().initLoader(LOADER_ID_ITEM_DETAILS, null, this);
                }
            }
            else if (loaderID == LOADER_ID_ITEM_DETAILS) {
                if (mItem == null)
                    mItem = new Item();
                mItem.setContentFromCursor(dataCursor);
                updateUIFromTask();
            }
            else if (loaderID == LOADER_ID_ROOM_DETAILS) {
                if (mRoom == null)
                    mRoom = new Room();
                mRoom.setContentFromCursor(dataCursor);
                updateUIFromTask();
            }
            else if (loaderID == LOADER_ID_SERVICE_CALL_DETAILS) {
                if (mServiceCall == null)
                    mServiceCall = new ServiceCall();
                mServiceCall.setContentFromCursor(dataCursor);
                updateUIFromTask();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void enableRevertAndSaveButtons() {
        mCallbacks.EnableRevertButton(true);
        mCallbacks.EnableSaveButton(true);
        mCallbacks.RedrawOptionsMenu();
    }

    public void revertUI() {
        updateUIFromTask();
    }

    public void markTaskAsDone() {
        mTask.mCompletedTimeStamp = Calendar.getInstance().getTimeInMillis();
        mTask.mStatus = Task.CompletedStatus;
        Uri taskURI = Uri.withAppendedPath(ResortManagerContentProvider.TASK_URI,
                mTaskID);
        int result = getActivity().getContentResolver().update(taskURI, mTask.getContentValues(), null, null);
        boolean bUpdateItem = false;
        boolean bUpdateRoom = false;

        // if this is a cleaning task, then set the cleaning date in the room.
        if (mTask.mTaskType == Task.Cleaning) {
            bUpdateRoom = true;
            mRoom.mCleaningDate = Calendar.getInstance().getTimeInMillis();
        }
        // if this is a maintenance task, then set the maintenance date in the item.
        if (mTask.mTaskType == Task.Maintenance) {
            bUpdateItem = true;
            mItem.mMaintenanceDate = Calendar.getInstance().getTimeInMillis();
        }
        if (bUpdateRoom) {
            Uri roomURI = Uri.withAppendedPath(ResortManagerContentProvider.ROOM_URI,
                    String.valueOf(mRoom.mID));
            result = getActivity().getContentResolver().update(roomURI, mRoom.getContentValues(), null, null);
        }
        if (bUpdateItem) {
            Uri itemURI = Uri.withAppendedPath(ResortManagerContentProvider.ITEM_URI,
                    String.valueOf(mItem.mID));
            result = getActivity().getContentResolver().update(itemURI, mItem.getContentValues(), null, null);
        }

        // if this is a service call task, then mark the service call as closed.
        if (mTask.mTaskType == Task.ServiceCall) {
            mServiceCall.mStatus = ServiceCall.ClosedStatus;
            mServiceCall.mClosedTimeStamp = Calendar.getInstance().getTimeInMillis();
            Uri serviceCallURI = Uri.withAppendedPath(ResortManagerContentProvider.SERVICE_CALL_URI,
                    String.valueOf(mServiceCall.mID));
            result = getActivity().getContentResolver().update(serviceCallURI, mServiceCall.getContentValues(), null, null);
        }
        if (result > 0)
            mCallbacks.onTaskDone();
    }

    public void markContractTaskAsDone(long contractValidTillDate) {
        mTask.mCompletedTimeStamp = Calendar.getInstance().getTimeInMillis();
        mTask.mStatus = Task.CompletedStatus;
        Uri taskURI = Uri.withAppendedPath(ResortManagerContentProvider.TASK_URI,
                mTaskID);
        int result = getActivity().getContentResolver().update(taskURI, mTask.getContentValues(), null, null);

        // Next update the item with the new contractValidTillDate
        mItem.mContractValidTillDate = contractValidTillDate;
        Uri itemURI = Uri.withAppendedPath(ResortManagerContentProvider.ITEM_URI,
                String.valueOf(mItem.mID));
        result = getActivity().getContentResolver().update(itemURI, mItem.getContentValues(), null, null);
        if (result > 0)
            mCallbacks.onTaskDone();
    }

    public void markInventoryTaskAsDone(long addedQuantity) {
        mTask.mCompletedTimeStamp = Calendar.getInstance().getTimeInMillis();
        mTask.mStatus = Task.CompletedStatus;
        Uri taskURI = Uri.withAppendedPath(ResortManagerContentProvider.TASK_URI,
                mTaskID);
        int result = getActivity().getContentResolver().update(taskURI, mTask.getContentValues(), null, null);

        // Next update the item with the new added quantity
        mItem.mCurrentQuantity = mItem.mCurrentQuantity + addedQuantity;
        Uri itemURI = Uri.withAppendedPath(ResortManagerContentProvider.ITEM_URI,
                String.valueOf(mItem.mID));
        result = getActivity().getContentResolver().update(itemURI, mItem.getContentValues(), null, null);
        if (result > 0)
            mCallbacks.onTaskDone();
    }

    public void saveTask() {
        updateTaskFromUI();
        Uri taskURI = Uri.withAppendedPath(ResortManagerContentProvider.TASK_URI,
                mTaskID);
        int result = getActivity().getContentResolver().update(taskURI, mTask.getContentValues(), null, null);
        if (result > 0) {

            // If the task was unassigned to a person, send that person an SMS.
            // If the task was assigned to a new person, send that person an SMS.
            sendTaskSMSs();
            updateUIFromTask();
        }
    }

    private void sendTaskSMSs() {
        if (!mNewAssignee.isEmpty()) {
            // If the current and new assignees are the same, don't do anytrhing.
            if (mNewAssignee.equalsIgnoreCase(mCurrentAssignee))
                return;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String titleString = preferences.getString(ResortManagerApp.sPrefOrganizationName, "");
            titleString = titleString + "  Resort Manager: ";
            // First send SMS to the new assignee
            String smsAssignMessage = titleString + "You have been assigned a " + mTask.getTaskTypeString() + " task for " + mTask.mItemName;
            if (!mTask.mItemLocation.isEmpty()) {
                smsAssignMessage = smsAssignMessage + " located at " + mTask.mItemLocation;
            }
            String assigneePhoneNumber = getPhoneNumber(mNewAssignee);
            if (!assigneePhoneNumber.isEmpty()) {
                sendAssigneeSMS(assigneePhoneNumber, smsAssignMessage);
            }
        }
    }

    private void sendAssigneeSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";

        PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, new Intent(SENT), 0);
        final SmsManager sms = SmsManager.getDefault();

        // when the SMS has been sent, send the deAssignee an SMS about unassignment
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        if (!mCurrentAssignee.isEmpty()) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            String titleString = preferences.getString(ResortManagerApp.sPrefOrganizationName, "");
                            titleString = titleString + "  Resort Manager: ";
                            String smsUnAssignMessage = titleString + "You have been unassigned from a " + mTask.getTaskTypeString() + " task for " + mTask.mItemName;
                            if (!mTask.mItemLocation.isEmpty()) {
                                smsUnAssignMessage = smsUnAssignMessage + " located at " + mTask.mItemLocation;
                            }
                            String unAssigneePhoneNumber = getPhoneNumber(mCurrentAssignee);
                            if (!unAssigneePhoneNumber.isEmpty()) {
                                sms.sendTextMessage(unAssigneePhoneNumber, null, smsUnAssignMessage, null, null);
                            }
                        }
                        break;
                }
            }
        }, new IntentFilter(SENT));

        sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
    }

    private String getPhoneNumber(String name) {
        String phoneNumber = "";
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            phoneNumber = c.getString(0);
        }
        c.close();
        return phoneNumber;
    }

    public final int PICK_CONTACT = 2015;

    public void assignTask() {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(i, PICK_CONTACT);
    }

    public void callAssignee() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + mTask.mAssignedToContactNumber));
        startActivity(callIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = mContext.getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int columnDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String assigneeName = cursor.getString(columnDisplayName);
            cursor.close();

            mCurrentAssignee = mTask.mAssignedTo;
            mNewAssignee = assigneeName;
            mTextAssignedTo.setText(assigneeName);
            enableRevertAndSaveButtons();
        }
    }


    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void updateTaskFromUI() {
        if (mTask == null)
            mTask = new Task();

        mTask.mAssignedTo = mTextAssignedTo.getText().toString();
        if (!mTask.mAssignedTo.isEmpty())
            mTask.mAssignedToContactNumber = getPhoneNumber(mTask.mAssignedTo);
        if (mNormalRadioButton.isChecked()) {
            mTask.mPriority = Task.NormalPriority;
        }
        else if (mUrgentButton.isChecked()){
            mTask.mPriority = Task.UrgentPriority;
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
        Calendar dueDate = Calendar.getInstance();
        String uiDueDate = mBtnChangeDueDate.getText().toString();
        if (uiDueDate.compareToIgnoreCase("Set") != 0) {
            try {
                dueDate.setTime(dateFormatter.parse(uiDueDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mTask.mDueDate = dueDate.getTimeInMillis();
        }
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void updateUIFromTask() {
        if (mTask.mItemID >= 0) {
            // This task for an item
            if (mTask.mTaskType == Task.Inventory)
                mTextItemType.setText("Consumable");
            else
                mTextItemType.setText("Equipment");
        }
        else if (mTask.mRoomID >= 0) {
            // This is a room
            mTextItemType.setText("Room");
            mTextLocation.setText("Description");
        }

        mTextItemName.setText(mTask.mItemName);
        mTextItemLocation.setText(mTask.mItemLocation);

        if (mTask.mDueDate > 0) {
            Date dueDate = new Date();
            dueDate.setTime(mTask.mDueDate);
            SimpleDateFormat dueDateFormat = new SimpleDateFormat("dd MMM, yyyy");
            String dueDateString = dueDateFormat.format(dueDate);
            mBtnChangeDueDate.setText(dueDateString);
        }
        else {
            mBtnChangeDueDate.setText("Set");
        }

        mTextAssignedTo.setText(mTask.mAssignedTo);

        mTextTaskType.setText(mTask.getTaskTypeString());

        // The following table rows are visible only if task is contract or inventory
        mContractExpiryDateLayout.setVisibility(View.GONE);
        mRequiredQuantityLayout.setVisibility(View.GONE);
        if (mTask.mTaskType == Task.Inventory) {
            if (mItem != null) {
                mRequiredQuantityLayout.setVisibility(View.VISIBLE);
                long requiredQuantity = mItem.mMinRequiredQuantity - mItem.mCurrentQuantity;
                mTextRequiredQuantity.setText(String.valueOf(requiredQuantity));
            }
        }
        if (mTask.mTaskType == Task.Contract) {
            if (mItem != null) {
                mContractExpiryDateLayout.setVisibility(View.VISIBLE);
                if (mItem.mContractValidTillDate > 0) {
                    Calendar contractDate = Calendar.getInstance();
                    contractDate.setTimeInMillis(mItem.mContractValidTillDate);
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                    mTextContractExpiryDate.setText(dateFormatter.format(contractDate.getTime()));
                }
                else {
                    mTextContractExpiryDate.setText("No date set");
                }
            }
        }

        if (mItem != null) {
            if (mTask.mTaskType == Task.Contract)
                mTextInstructions.setText(mItem.mContractInstructions);
            else if (mTask.mTaskType == Task.Maintenance)
                mTextInstructions.setText(mItem.mMaintenanceInstructions);
            else if (mTask.mTaskType == Task.Inventory)
                mTextInstructions.setText(mItem.mReorderInstructions);
        }
        if (mRoom != null) {
            mTextInstructions.setText(mRoom.mCleaningInstructions);
        }
        if (mServiceCall != null) {
            mTextInstructionsLabel.setText(getResources().getText(R.string.description));
            mTextInstructions.setText(mServiceCall.mDescription);
        }

        if (mTask.mPriority == Task.NormalPriority) {
            mPreviousPriority = Task.NormalPriority;
            mNormalRadioButton.setChecked(true);
        }
        else if (mTask.mPriority == Task.UrgentPriority) {
            mPreviousPriority = Task.UrgentPriority;
            mUrgentButton.setChecked(true);
        }

        // Toggle the action bar buttons appropriately
        mCallbacks.EnableAssignButton(true);

        if (!mTask.mAssignedTo.isEmpty()) {
            if (!mTask.mAssignedToContactNumber.isEmpty()) {
                mCallbacks.EnableCallButton(true);
            }
        }
        mCallbacks.EnableTaskDoneButton(true);
        mCallbacks.EnableSaveButton(false);
        mCallbacks.EnableRevertButton(false);
        mCallbacks.RedrawOptionsMenu();

        String titleString = mTask.getTaskTypeString() + " (" + mTask.mItemName + ")";
        mCallbacks.setTitleString(titleString);
    }

    public void showTaskDoneDialog() {
        if (mTask.mTaskType == Task.Contract) {
            ContractTaskDoneDialogFragment dialog = new ContractTaskDoneDialogFragment();
            dialog.show(((FragmentActivity)mContext).getSupportFragmentManager(), "ContractTaskDoneDialogFragment");
        }
        else if (mTask.mTaskType == Task.Inventory) {
            InventoryTaskDoneDialogFragment dialog = new InventoryTaskDoneDialogFragment();
            dialog.show(((FragmentActivity)mContext).getSupportFragmentManager(), "InventoryTaskDoneDialogFragment");
        }
        else {
            markTaskAsDone();
        }
    }

    private void showDatePicker() {
        Calendar dateToShow = Calendar.getInstance();
        if (mTask.mDueDate > 0)
            dateToShow.setTimeInMillis(mTask.mDueDate);
        ResortManagerDatePickerFragment datePicker = new ResortManagerDatePickerFragment();
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
                mBtnChangeDueDate.setText(dateFormatter.format(newDate.getTime()));
                enableRevertAndSaveButtons();
            }
        };
        datePicker.setCallBack(onDateChangeCallback);
        datePicker.show(((FragmentActivity)mContext).getSupportFragmentManager(), "Task Due Date Picker");
    }

}
