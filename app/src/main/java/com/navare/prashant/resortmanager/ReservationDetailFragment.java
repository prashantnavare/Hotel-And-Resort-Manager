package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.navare.prashant.resortmanager.Database.Reservation;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;
import com.navare.prashant.resortmanager.Database.Room;
import com.navare.prashant.resortmanager.util.SelectedRoomListCursorAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * A fragment representing a single Reservation detail screen.
 * This fragment is either contained in a {@link ReservationListActivity}
 * in two-pane mode (on tablets) or a {@link ReservationDetailActivity}
 * on handsets.
 */
public class ReservationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {

    private static final int LOADER_ID_RESERVATION_DETAILS = 2;
    private static final int LOADER_ID_ROOM_DETAILS = 3;

    /**
     * The fragment argument representing the reservation ID that this fragment
     * represents.
     */
    public static final String ARG_RESERVATION_ID = "reservation_id";

    private Context mContext = null;

    /**
     * The reservation this fragment is presenting.
     */
    private String mReservationID = "-1";
    private Reservation mReservation = null;

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * The UI elements showing the details of the reservation
     */
    private ScrollView mReservationDetailsScrollView;
    private TextView mTextName;
    private TextView mTextPhoneNumber;
    private TextView mTextEmailAddress;
    private TextView mTextNumAdults;
    private TextView mTextNumChildren;
    private TextView mTextNumDays;
    private Button mBtnFromDate;
    private LinearLayout mArrivalDetailsLayout;
    private TextView mTextArrivalDetails;

    private LinearLayout mSelectedRoomsLayout;
    private TextView mSelectedRoomTextLabel;
    private ListView mSelectedRoomListView;
    private Button mBtnCheckinCancel;
    private Button mBtnCheckin;

    private LinearLayout mCheckoutLayout;
    private LinearLayout mChildChargeLayout;
    private TextView mTextAllocatedRooms;
    private TextView mTextRoomCharge;
    private TextView mTextAdultCharge;
    private TextView mTextChildCharge;
    private TextView mTextAdditionalCharge;
    private TextView mTextTaxPercent;
    private TextView mTextTotalCharge;

    private AdView mAdView;

    private SelectedRoomListCursorAdapter mRoomCursorAdapter;

    private boolean mbCheckoutInProgress = false;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callbacks for when an reservation has been selected.
         */
        void EnableCheckinButton(boolean bEnable);
        void EnableCheckoutButton(boolean bEnable);
        void EnableCompleteCheckoutButton(boolean bEnable);
        void EnableCallButton(boolean bEnable);
        void EnableMessageButton(boolean bEnable);
        void EnableEmailButton(boolean bEnable);
        void EnableZoomInButton(boolean bEnable);
        void EnableSaveButton(boolean bEnable);
        void EnableRevertButton(boolean bEnable);
        void EnableDeleteButton(boolean bEnable);
        void RedrawOptionsMenu();
        void onReservationDeleted();
        void setTitleString(String titleString);
        void onCheckinCompleted();
        void onCheckoutCompleted();
        void onSaveCompleted();
    }
    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void EnableCheckinButton(boolean bEnable) {
        }
        @Override
        public void EnableCheckoutButton(boolean bEnable) {
        }
        @Override
        public void EnableCompleteCheckoutButton(boolean bEnable) {
        }
        @Override
        public void EnableCallButton(boolean bEnable) {
        }
        @Override
        public void EnableMessageButton(boolean bEnable) {
        }
        @Override
        public void EnableEmailButton(boolean bEnable) {
        }
        @Override
        public void EnableZoomInButton(boolean bEnable) {
        }
        @Override
        public void EnableSaveButton(boolean bEnable) {
        }
        @Override
        public void EnableRevertButton(boolean bEnable) {
        }
        @Override
        public void EnableDeleteButton(boolean bEnable) {
        }
        @Override
        public void RedrawOptionsMenu() {
        }
        @Override
        public void onReservationDeleted() {
        }
        @Override
        public void setTitleString(String titleString) {
        }
        @Override
        public void onCheckinCompleted() {
        }
        @Override
        public void onCheckoutCompleted() {
        }
        @Override
        public void onSaveCompleted() {
        }
    };

    /**
     * The fragment's current callback object, which is notified of changes to the reservation
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReservationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_RESERVATION_ID)) {
            String passedInID = getArguments().getString(ARG_RESERVATION_ID);
            if (passedInID != null)
                mReservationID = passedInID;
        }

        String[] columns = new String[] {
                Room.COL_NAME,
                Room.COL_DESCRIPTION
        };
        int[] views = new int[] {
                R.id.textRoomName,
                R.id.textRoomDescription
        };
        mRoomCursorAdapter = new SelectedRoomListCursorAdapter(mContext, R.layout.selected_room_list_row, null, columns, views, 0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement reservation detail fragment's callbacks.");
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

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reservation_detail, container, false);

        mReservationDetailsScrollView = (ScrollView) rootView.findViewById(R.id.reservationDetailsScrollView);
        mTextName = ((TextView) rootView.findViewById(R.id.textName));
        mTextName.addTextChangedListener(this);

        mTextPhoneNumber = ((TextView) rootView.findViewById(R.id.textPhoneNumber));
        mTextPhoneNumber.addTextChangedListener(this);

        mTextEmailAddress = ((TextView) rootView.findViewById(R.id.textEmailAddress));
        mTextEmailAddress.addTextChangedListener(this);

        mTextNumAdults = ((TextView) rootView.findViewById(R.id.textNumAdults));
        mTextNumAdults.addTextChangedListener(this);

        mTextNumChildren = ((TextView) rootView.findViewById(R.id.textNumChildren));
        mTextNumChildren.addTextChangedListener(this);

        mBtnFromDate = (Button) rootView.findViewById(R.id.btnFromDate);
        mBtnFromDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        mTextNumDays = (TextView) rootView.findViewById(R.id.textNumDays);
        mTextNumDays.addTextChangedListener(this);

        mArrivalDetailsLayout = (LinearLayout) rootView.findViewById(R.id.arrivalDetailsLayout);
        mTextArrivalDetails = (TextView) rootView.findViewById(R.id.textArrivalDetails);
        mTextArrivalDetails.addTextChangedListener(this);

        mSelectedRoomsLayout = (LinearLayout) rootView.findViewById(R.id.selectedRoomsLayout);
        mSelectedRoomListView = (ListView) rootView.findViewById(R.id.list);

        mSelectedRoomTextLabel = ((TextView) rootView.findViewById(R.id.selectedRoomTextLabel));
        mSelectedRoomListView.setAdapter(mRoomCursorAdapter);
        mSelectedRoomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int numItemsSelected = mSelectedRoomListView.getCheckedItemCount();
                mSelectedRoomTextLabel.setText("Please select rooms for this reservation: " + String.valueOf(numItemsSelected) + " rooms selected.");
                mCallbacks.EnableRevertButton(true);
                if (numItemsSelected > 0) {
                    mBtnCheckin.setEnabled(true);
                }
                else {
                    mBtnCheckin.setEnabled(false);
                }
            }
        });
        mBtnCheckin = ((Button) rootView.findViewById(R.id.btnCheckin));
        mBtnCheckin.setOnClickListener(onCheckinClicked);
        // By default, disable the button till at least 1 room is selected.
        mBtnCheckin.setEnabled(false);

        mBtnCheckinCancel = ((Button) rootView.findViewById(R.id.btnCheckinCancel));
        mBtnCheckinCancel.setOnClickListener(onCheckinCancelClicked);


        // Checkout related
        mCheckoutLayout = (LinearLayout) rootView.findViewById(R.id.checkoutLayout);
        mCheckoutLayout.setVisibility(View.GONE);

        mChildChargeLayout = (LinearLayout) rootView.findViewById(R.id.childChargeLayout);

        mTextAllocatedRooms = ((TextView) rootView.findViewById(R.id.textAllocatedRooms));

        mTextRoomCharge = ((TextView) rootView.findViewById(R.id.textRoomCharge));
        mTextRoomCharge.addTextChangedListener(this);

        mTextAdultCharge = ((TextView) rootView.findViewById(R.id.textAdultCharge));
        mTextAdultCharge.addTextChangedListener(this);

        mTextChildCharge = ((TextView) rootView.findViewById(R.id.textChildCharge));
        mTextChildCharge.addTextChangedListener(this);

        mTextAdditionalCharge = ((TextView) rootView.findViewById(R.id.textAdditionalCharge));
        mTextAdditionalCharge.addTextChangedListener(this);

        mTextTaxPercent = ((TextView) rootView.findViewById(R.id.textTaxPercent));
        mTextTaxPercent.addTextChangedListener(this);

        mTextTotalCharge = ((TextView) rootView.findViewById(R.id.textTotalCharge));

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
    public  void onDestroyView() {
        super.onDestroyView();
    }

    private void showDatePicker() {
        Calendar dateToShow = Calendar.getInstance();
        if (mReservation != null) {
            if (mReservation.mFromDate > 0) {
                dateToShow.setTimeInMillis(mReservation.mFromDate);
            }
        }
        OnDateSetListener onDateChangeCallback = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                mBtnFromDate.setText(dateFormatter.format(newDate.getTime()));
                enableRevertAndSaveButtons();
            }
        };

        int year = dateToShow.get(Calendar.YEAR);
        int month = dateToShow.get(Calendar.MONTH);
        int day = dateToShow.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(mContext, onDateChangeCallback, year, month, day);
        datePicker.getDatePicker().setMinDate(Calendar.getInstance().getTime().getTime());
        datePicker.show();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mReservationID.equalsIgnoreCase("-1")) {
            displayUIForNewReservation();
        }
        else {
            getLoaderManager().initLoader(LOADER_ID_RESERVATION_DETAILS, null, this);
            getLoaderManager().restartLoader(LOADER_ID_ROOM_DETAILS, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == LOADER_ID_RESERVATION_DETAILS) {
            Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_URI,
                    mReservationID);

            return new CursorLoader(getActivity(),
                    reservationURI, Reservation.FIELDS, null, null,
                    null);
        }
        else if (id == LOADER_ID_ROOM_DETAILS) {

            String [] selectionArgs = null;
            if (mReservationID.equalsIgnoreCase("-1") == false) {
                selectionArgs = new String[] {mReservationID};
            }
            return new CursorLoader(getActivity(),
                    ResortManagerContentProvider.RESERVATION_ROOMS_URI, Room.FIELDS, null, selectionArgs,
                    null);
        }
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dataCursor) {

        if (dataCursor != null) {
            int loaderID = loader.getId();
            if (loaderID == LOADER_ID_RESERVATION_DETAILS) {
                if (mReservation == null)
                    mReservation = new Reservation();

                mReservation.setContentFromCursor(dataCursor);
                updateUIFromReservation();
            }
            else if (loaderID == LOADER_ID_ROOM_DETAILS) {
                mRoomCursorAdapter.swapCursor(dataCursor);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    private void updateSelectedRoomsUI() {
        Cursor cursor = mRoomCursorAdapter.getCursor();
        for (int i = 0; i < mSelectedRoomListView.getCount(); i++) {
            cursor.moveToPosition(i);
            if (cursor.getLong(cursor.getColumnIndex(Room.COL_STATUS)) == Room.Occupied) {
                mSelectedRoomListView.setItemChecked(i, true);
            }
            else {
                mSelectedRoomListView.setItemChecked(i, false);
            }
        }
        int numRoomsSelected = mSelectedRoomListView.getCheckedItemCount();
        mSelectedRoomTextLabel.setText("Please select rooms for this reservation: " + String.valueOf(numRoomsSelected) + " rooms selected.");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mbCheckoutInProgress == false)
            enableRevertAndSaveButtons();
        else
            calculateTotalCharge();
    }

    private void enableRevertAndSaveButtons() {
        mCallbacks.EnableRevertButton(true);
        mCallbacks.EnableSaveButton(true);
        mCallbacks.RedrawOptionsMenu();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void revertUI() {
        if (mReservation == null) {
            displayUIForNewReservation();
        }
        else {
            updateUIFromReservation();
            updateSelectedRoomsUI();
        }
    }

    public void deleteReservation() {
        Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_URI,
                mReservationID);
        int result = getActivity().getContentResolver().delete(reservationURI, null, null);
        if (result > 0) {
            if (mReservation.mCurrentStatus == Reservation.PendingStatus) {
                ResortManagerApp.decrementPendingReservationCount();
            }
            else if (mReservation.mCurrentStatus == Reservation.CheckedInStatus) {
                ResortManagerApp.decrementCheckedInReservationCount();
            }
            releaseSelectedRooms();
            mCallbacks.onReservationDeleted();
        }
    }

    public boolean saveReservation() {
        boolean bAllDataOK = updateReservationFromUI();
        if (!bAllDataOK)
            return false;

        boolean bSuccess = false;
        if (mReservationID.equalsIgnoreCase("-1")) {
            // a new reservation is being inserted. A new reservation always starts life in Pending status
            mReservation.mCurrentStatus = Reservation.PendingStatus;
            Uri uri = getActivity().getContentResolver().insert(ResortManagerContentProvider.RESERVATION_URI, mReservation.getContentValues());
            if (uri != null) {
                mReservationID = uri.getLastPathSegment();
                mReservation.mID = Long.valueOf(mReservationID);
                bSuccess = true;
                ResortManagerApp.incrementPendingReservationCount();
            }
        }
        else {
            Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_URI,
                    mReservationID);
            int result = getActivity().getContentResolver().update(reservationURI, mReservation.getContentValues(), null, null);
            if (result > 0)
                bSuccess = true;
        }
        if (bSuccess) {
            mCallbacks.onSaveCompleted();
        }
        return true;
    }

    public void doCheckin() {
        // First swap out the reserveationDetailsScrollView with the roomLayout
        mReservationDetailsScrollView.setVisibility(View.GONE);
        mSelectedRoomsLayout.setVisibility(View.VISIBLE);
        mBtnCheckin.setText("Checkin");
        mBtnCheckin.setOnClickListener(onCheckinClicked);
        mBtnCheckin.setEnabled(false);
        updateSelectedRoomsUI();
    }

    public void doZoomIn() {
        // First swap out the reserveationDetailsScrollView with the roomLayout
        mReservationDetailsScrollView.setVisibility(View.GONE);
        mSelectedRoomsLayout.setVisibility(View.VISIBLE);
        mBtnCheckin.setText("Save");
        mBtnCheckin.setOnClickListener(onSaveRoomsClicked);
        mBtnCheckin.setEnabled(false);
        updateSelectedRoomsUI();
    }

    private View.OnClickListener onCheckinClicked=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    doTheRealCheckin();
                }
            };

    private View.OnClickListener onCheckinCancelClicked=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mReservationDetailsScrollView.setVisibility(View.VISIBLE);
                    mSelectedRoomsLayout.setVisibility(View.GONE);
                }
            };

    private View.OnClickListener onSaveRoomsClicked=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    doSaveSelectedRooms();
                }
            };


    private void doSaveSelectedRooms() {
        mReservation.mNumRooms = mSelectedRoomListView.getCheckedItemCount();
        Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_URI,
                mReservationID);
        int result = getActivity().getContentResolver().update(reservationURI, mReservation.getContentValues(), null, null);
        if (result > 0) {
            // Get all the selected rooms and update their status
            updateSelectedRooms();
            mReservationDetailsScrollView.setVisibility(View.VISIBLE);
            mSelectedRoomsLayout.setVisibility(View.GONE);
        }

    }

    private void doTheRealCheckin() {
        // The fromDate becomes today
        mReservation.mFromDate = Calendar.getInstance().getTimeInMillis();
        mReservation.mCurrentStatus = Reservation.CheckedInStatus;
        Calendar toDate = Calendar.getInstance();
        toDate.setTimeInMillis(mReservation.mFromDate + TimeUnit.MILLISECONDS.convert(mReservation.mNumDays, TimeUnit.DAYS));
        mReservation.mToDate = toDate.getTimeInMillis();
        mReservation.mNumRooms = mSelectedRoomListView.getCheckedItemCount();
        Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_URI,
                mReservationID);
        int result = getActivity().getContentResolver().update(reservationURI, mReservation.getContentValues(), null, null);
        if (result > 0) {
            ResortManagerApp.decrementPendingReservationCount();
            ResortManagerApp.incrementCheckedInReservationCount();
            // Get all the selected rooms and update their status
            updateSelectedRooms();
            mCallbacks.onCheckinCompleted();
        }

    }

    public void doCheckout() {
        // 1. Hide the selected rooms layout
        // 2. Show the checkout layout
        // 3. What to do after checkout?
        mSelectedRoomsLayout.setVisibility(View.GONE);
        mCheckoutLayout.setVisibility(View.VISIBLE);
        mTextAllocatedRooms.setText(String.valueOf(mReservation.mNumRooms));
        if (mReservation.mNumChildren <= 0) {
            mChildChargeLayout.setVisibility(View.GONE);
        }
        else {
            mChildChargeLayout.setVisibility(View.VISIBLE);
        }

        mbCheckoutInProgress = true;

        // Show the completeCheckout button and disable the checkout button
        mCallbacks.EnableCompleteCheckoutButton(true);
        mCallbacks.EnableCheckoutButton(false);
    }

    private void calculateTotalCharge() {
        long totalCharge = 0;

        Calendar todayDate = Calendar.getInstance();
        float numMilliseconds = todayDate.getTimeInMillis() - mReservation.mFromDate;
        float numMillisecondsInADay = (24 * 60 * 60 * 1000);
        float numFloatDays = numMilliseconds / numMillisecondsInADay;
        long numDays = Math.round(numFloatDays);
        if (numDays == 0)
            numDays = 1;

        if (mTextRoomCharge.getText().toString().isEmpty() == false) {
            long roomCharge = Long.valueOf(mTextRoomCharge.getText().toString());
            totalCharge += (roomCharge * mReservation.mNumRooms * numDays);
            mReservation.mRoomCharge = roomCharge;
        }
        if (mTextAdultCharge.getText().toString().isEmpty() == false) {
            long adultCharge = Long.valueOf(mTextAdultCharge.getText().toString());
            totalCharge += (adultCharge * mReservation.mNumAdults * numDays);
            mReservation.mAdultCharge = adultCharge;
        }
        if (mTextChildCharge.getText().toString().isEmpty() == false) {
            long childCharge = Long.valueOf(mTextChildCharge.getText().toString());
            totalCharge += (childCharge * mReservation.mNumChildren * numDays);
            mReservation.mChildCharge = childCharge;
        }
        if (mTextAdditionalCharge.getText().toString().isEmpty() == false) {
            long additionalCharge = Long.valueOf(mTextAdditionalCharge.getText().toString());
            totalCharge += additionalCharge;
            mReservation.mAdditionalCharges = additionalCharge;
        }
        if (mTextTaxPercent.getText().toString().isEmpty() == false) {
            float taxPercent = Float.valueOf(mTextTaxPercent.getText().toString());
            totalCharge += (totalCharge * taxPercent)/100;
            mReservation.mTaxPercent = taxPercent;
        }
        mTextTotalCharge.setText(String.valueOf(totalCharge));
        mReservation.mTotalCharge = totalCharge;
    }

    public void doCompleteCheckout() {
        if (mbCheckoutInProgress == false)
            return;

        // Make sure at least one of room or charges per adult is filled in.
        if (mTextAdultCharge.getText().toString().isEmpty() && mTextRoomCharge.getText().toString().isEmpty()) {
            ResortManagerApp.showAlertDialog(mContext, "Incomplete Data", "Either Room or Charges per Adult must be specified.");
            mTextRoomCharge.requestFocus();
            return;
        }

        mReservation.mCurrentStatus = Reservation.CheckedOutStatus;
        mReservation.mToDate = Calendar.getInstance().getTimeInMillis();
        Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_URI,
                mReservationID);
        int result = getActivity().getContentResolver().update(reservationURI, mReservation.getContentValues(), null, null);
        if (result > 0) {
            ResortManagerApp.decrementCheckedInReservationCount();
            ResortManagerApp.incrementHistoricalReservationCount();
            // Release all the selected rooms
            releaseSelectedRooms();
            mCallbacks.onCheckoutCompleted();
        }
    }

    private void releaseSelectedRooms() {
        int size = mSelectedRoomListView.getCount();
        for (int i = 0; i < size; i++) {
            updateRoom(i, false);
        }
    }

    private void updateSelectedRooms() {
        int size = mSelectedRoomListView.getCount();
        for (int i = 0; i < size; i++) {
            boolean bChecked = mSelectedRoomListView.isItemChecked(i);
            updateRoom(i, bChecked);
        }
    }

    private void updateRoom(int position, boolean bSelected) {
        Cursor cursor = mRoomCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        Room room = new Room();
        room.setContentFromCursor(cursor);
        if (bSelected) {
            // update the room only if it was NOT occupied before
            if (room.mStatus != Room.Occupied) {
                room.mReservationID = mReservation.mID;
                room.mStatus = Room.Occupied;
                Uri roomURI = Uri.withAppendedPath(ResortManagerContentProvider.ROOM_URI, String.valueOf(room.mID));
                int result = getActivity().getContentResolver().update(roomURI, room.getContentValues(), null, null);
            }
        }
        else {
            // update the room only if it was selected before.
            if ((room.mStatus == Room.Occupied) && (room.mReservationID == mReservation.mID)) {
                room.mStatus = Room.Free;
                room.mReservationID = -1;
                Uri roomURI = Uri.withAppendedPath(ResortManagerContentProvider.ROOM_URI, String.valueOf(room.mID));
                int result = getActivity().getContentResolver().update(roomURI, room.getContentValues(), null, null);
            }
        }
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private boolean updateReservationFromUI() {
        if (mReservation == null)
            mReservation = new Reservation();

        String errorDialogTitle = "Incomplete Data";
        if (mTextName.getText().toString().isEmpty()) {
            ResortManagerApp.showAlertDialog(mContext, errorDialogTitle, "Reservation name cannot be empty.");
            mTextName.requestFocus();
            return false;
        }
        else {
            mReservation.mName = mTextName.getText().toString();
        }

        if (mTextPhoneNumber.getText().toString().isEmpty() == false) {
            mReservation.mPhoneNumber = mTextPhoneNumber.getText().toString();
        }

        if (mTextEmailAddress.getText().toString().isEmpty() == false) {
            mReservation.mEmailAddress = mTextEmailAddress.getText().toString();
        }

        if (mTextNumAdults.getText().toString().isEmpty()) {
            ResortManagerApp.showAlertDialog(mContext, errorDialogTitle, "Adults cannot be empty.");
            mTextNumAdults.requestFocus();
            return false;
        }
        else {
            mReservation.mNumAdults = Long.valueOf(mTextNumAdults.getText().toString());
        }

        if (mTextNumChildren.getText().toString().isEmpty() == false) {
            mReservation.mNumChildren = Long.valueOf(mTextNumChildren.getText().toString());
        }
        String uiFromDate = mBtnFromDate.getText().toString();
        if (uiFromDate.compareToIgnoreCase("Set") == 0) {
            ResortManagerApp.showAlertDialog(mContext, errorDialogTitle, "From Date needs to be set.");
            mBtnFromDate.requestFocus();
            return false;
        }
        else {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
            Calendar fromDate = Calendar.getInstance();
            try {
                fromDate.setTime(dateFormatter.parse(uiFromDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mReservation.mFromDate = fromDate.getTimeInMillis();
        }

        if (mTextNumDays.getText().toString().isEmpty()) {
            ResortManagerApp.showAlertDialog(mContext, errorDialogTitle, "Days cannot be empty.");
            mTextNumDays.requestFocus();
            return false;
        }
        else {
            mReservation.mNumDays = Long.valueOf(mTextNumDays.getText().toString());
            Calendar toDate = Calendar.getInstance();
            toDate.setTimeInMillis(mReservation.mFromDate + TimeUnit.MILLISECONDS.convert(mReservation.mNumDays, TimeUnit.DAYS));
            mReservation.mToDate = toDate.getTimeInMillis();
        }
        if (mTextArrivalDetails.getText().toString().isEmpty() == false) {
            mReservation.mArrivalDetails = mTextArrivalDetails.getText().toString();
        }

        mReservation.mNumRooms = mSelectedRoomListView.getCheckedItemCount();
        return true;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void updateUIFromReservation() {

        mTextName.setText(mReservation.mName);
        mTextPhoneNumber.setText(mReservation.mPhoneNumber);
        mTextEmailAddress.setText(mReservation.mEmailAddress);
        mTextNumAdults.setText(String.valueOf(mReservation.mNumAdults));
        mTextNumChildren.setText(String.valueOf(mReservation.mNumChildren));
        mTextNumDays.setText(String.valueOf(mReservation.mNumDays));
        if (mReservation.mFromDate > 0) {
            Calendar fromDate = Calendar.getInstance();
            fromDate.setTimeInMillis(mReservation.mFromDate);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
            mBtnFromDate.setText(dateFormatter.format(fromDate.getTime()));
        }
        else {
            mBtnFromDate.setText("Set");
        }
        if (mReservation.mCurrentStatus == Reservation.PendingStatus) {
            mArrivalDetailsLayout.setVisibility(View.VISIBLE);
            mTextArrivalDetails.setText(String.valueOf(mReservation.mArrivalDetails));
        }
        else if (mReservation.mCurrentStatus == Reservation.CheckedInStatus) {
            mArrivalDetailsLayout.setVisibility(View.GONE);
        }

        // Toggle the action bar buttons appropriately
        mCallbacks.EnableDeleteButton(true);
        mCallbacks.EnableRevertButton(false);
        mCallbacks.EnableSaveButton(false);
        if (mReservation.mCurrentStatus == Reservation.PendingStatus) {
            mCallbacks.EnableCheckinButton(true);
            mCallbacks.EnableCheckoutButton(false);
            mCallbacks.EnableZoomInButton(false);
        }
        else if (mReservation.mCurrentStatus == Reservation.CheckedInStatus) {
            mCallbacks.EnableCheckinButton(false);
            mCallbacks.EnableCheckoutButton(true);
            mCallbacks.EnableZoomInButton(true);
        }
        if (mReservation.mPhoneNumber.isEmpty() == false) {
            mCallbacks.EnableCallButton(true);
            mCallbacks.EnableMessageButton(true);
        }
        else {
            mCallbacks.EnableCallButton(false);
            mCallbacks.EnableMessageButton(false);
        }
        if (mReservation.mEmailAddress.isEmpty() == false) {
            mCallbacks.EnableEmailButton(true);
        }
        else {
            mCallbacks.EnableEmailButton(false);
        }
        mCallbacks.RedrawOptionsMenu();
        mCallbacks.setTitleString(mReservation.mName + " - " + mReservation.getStatusString()) ;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void displayUIForNewReservation() {
        // For new reservations, show the reservation details scrollview, & hide the room selection layout and checkout layout
        mReservationDetailsScrollView.setVisibility(View.VISIBLE);
        mSelectedRoomsLayout.setVisibility(View.GONE);
        mCheckoutLayout.setVisibility(View.GONE);

        mTextName.setText("");
        mTextPhoneNumber.setText("");
        mTextEmailAddress.setText("");
        mTextNumAdults.setText("");
        mTextNumChildren.setText("");
        mTextNumDays.setText("");
        mBtnFromDate.setText("Set");
        mTextArrivalDetails.setText("");

        mCallbacks.EnableRevertButton(false);
        mCallbacks.EnableSaveButton(false);
        mCallbacks.EnableDeleteButton(false);

        // New reservation so disable checkin and checkout buttons
        mCallbacks.EnableCheckinButton(false);
        mCallbacks.EnableCheckoutButton(false);

        // New reservation so disable call, message and e-mail buttons
        mCallbacks.EnableCallButton(false);
        mCallbacks.EnableMessageButton(false);
        mCallbacks.EnableEmailButton(false);

        mCallbacks.setTitleString("New Reservation");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public String getEmailAddress() {
        return mReservation.mEmailAddress;
    }

    public String getPhoneNumber() {
        return mReservation.mPhoneNumber;
    }

    public void callCustomer() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + mReservation.mPhoneNumber));
        startActivity(callIntent);
    }

    public void doEmail(String emailAddress) {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ emailAddress});
        emailIntent.setType("message/rfc822");

        startActivity(Intent.createChooser(emailIntent, "Send e-mail"));
    }

    public void doSMS(String mobileNumber) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", mobileNumber);
        startActivity(smsIntent);
    }
}
