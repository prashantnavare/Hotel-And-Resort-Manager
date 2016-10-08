package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;
import com.navare.prashant.resortmanager.Database.Reservation;
import com.navare.prashant.resortmanager.Database.Room;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.util.SelectedRoomListCursorAdapter;
import com.navare.prashant.resortmanager.util.TaskListCursorAdapter;

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
    private String mReservationID;
    private Reservation mReservation = null;

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * The UI elements showing the details of the reservation
     */
    private TextView mTextName;
    private TextView mTextNumPeople;
    private TextView mTextNumDays;
    private Button mBtnFromDate;

    private LinearLayout mSelectedRoomsLayout;
    private ListView mSelectedRoomListView;

    private AdView mAdView;

    private boolean mbCheckinInProgress = false;
    private SelectedRoomListCursorAdapter roomCursorAdapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callbacks for when an reservation has been selected.
         */
        void EnableDeleteButton(boolean bEnable);
        void EnableRevertButton(boolean bEnable);
        void EnableSaveButton(boolean bEnable);
        void RedrawOptionsMenu();
        void EnableCheckinButton(boolean bEnable);
        void EnableCheckoutButton(boolean bEnable);
        void onReservationDeleted();
        void setTitleString(String titleString);
        void onCheckinCompleted();
    }
    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void EnableDeleteButton(boolean bEnable) {
        }
        @Override
        public void EnableRevertButton(boolean bEnable) {
        }
        @Override
        public void EnableSaveButton(boolean bEnable) {
        }
        @Override
        public void EnableCheckinButton(boolean bEnable) {
        }
        @Override
        public void EnableCheckoutButton(boolean bEnable) {
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
            mReservationID = getArguments().getString(ARG_RESERVATION_ID);
        }
        String[] columns = new String[] {
                Room.COL_NAME,
                Room.COL_DESCRIPTION
        };
        int[] views = new int[] {
                R.id.textRoomName,
                R.id.textRoomDescription
        };
        roomCursorAdapter = new SelectedRoomListCursorAdapter(mContext, R.layout.selected_room_list_row, null, columns, views, 0);
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

        mTextName = ((TextView) rootView.findViewById(R.id.textName));
        mTextName.addTextChangedListener(this);

        mTextNumPeople = ((TextView) rootView.findViewById(R.id.textNumPeople));
        mTextNumPeople.addTextChangedListener(this);

        mTextNumDays = (TextView) rootView.findViewById(R.id.textNumDays);
        mTextNumDays.addTextChangedListener(this);

        mBtnFromDate = (Button) rootView.findViewById(R.id.btnFromDate);
        mBtnFromDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        mSelectedRoomsLayout = (LinearLayout) rootView.findViewById(R.id.selectedRoomsLayout);
        mSelectedRoomListView = (ListView) rootView.findViewById(R.id.list);

        mSelectedRoomListView.setAdapter(roomCursorAdapter);

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
        if ((mReservationID != null) && (!mReservationID.isEmpty())) {
            getLoaderManager().initLoader(LOADER_ID_RESERVATION_DETAILS, null, this);
        }
        else {
            displayUIForNewReservation();
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
            Uri roomURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_ROOMS_URI,
                    mReservationID);

            return new CursorLoader(getActivity(),
                    roomURI, Room.FIELDS, null, null,
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
                roomCursorAdapter.swapCursor(dataCursor);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        enableRevertAndSaveButtons();
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
        }
    }

    public void deleteReservation() {
        Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_URI,
                mReservationID);
        int result = getActivity().getContentResolver().delete(reservationURI, null, null);
        if (result > 0) {
            mCallbacks.onReservationDeleted();
        }
    }

    public boolean saveReservation() {
        boolean bAllDataOK = updateReservationFromUI();
        if (!bAllDataOK)
            return false;

        boolean bSuccess = false;
        if ((mReservationID == null) || (mReservationID.isEmpty())) {
            // a new reservation is being inserted. A new reservation always starts life in Waiting status
            mReservation.mCurrentStatus = Reservation.WaitingStatus;
            Uri uri = getActivity().getContentResolver().insert(ResortManagerContentProvider.RESERVATION_URI, mReservation.getContentValues());
            if (uri != null) {
                mReservationID = uri.getLastPathSegment();
                mReservation.mID = Long.valueOf(mReservationID);
                bSuccess = true;
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
            updateUIFromReservation();
        }
        return true;
    }

    public void doCheckin() {
        if (mbCheckinInProgress == false) {
            // First, tell the user to select rooms for the checkin
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            alertDialog.setTitle("Checkin");
            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.ic_menu_checkin);

            // Setting Dialog Message
            String dialogMessage = "Please select the rooms for this reservation and click on Checkin arrow again.";
            alertDialog.setMessage(dialogMessage);

            // Setting Positive "Yes" Button
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {

                    mbCheckinInProgress = true;
                    mSelectedRoomsLayout.setVisibility(View.VISIBLE);
                    getSelectedRooms();
                }
            });

            alertDialog.show();
        }
        else {
            // TODO: verify that some rooms have been selected and doTheRealCheckin().
        }
    }

    public void getSelectedRooms() {
        getLoaderManager().initLoader(LOADER_ID_ROOM_DETAILS, null, this);
    }

    private void doTheRealCheckin() {
        // TODO: Implement this
        // The fromDate becomes today
        mReservation.mFromDate = Calendar.getInstance().getTimeInMillis();
        mReservation.mCurrentStatus = Reservation.CheckedInStatus;
        Calendar toDate = Calendar.getInstance();
        toDate.setTimeInMillis(mReservation.mFromDate + TimeUnit.MILLISECONDS.convert(mReservation.mNumDays, TimeUnit.DAYS));
        mReservation.mToDate = toDate.getTimeInMillis();
        Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.RESERVATION_URI,
                mReservationID);
        int result = getActivity().getContentResolver().update(reservationURI, mReservation.getContentValues(), null, null);
        if (result > 0) {
            mCallbacks.onCheckinCompleted();
        }

    }

    private void showAlertDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle("Incomplete Data");

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_resort_manager);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private boolean updateReservationFromUI() {
        if (mReservation == null)
            mReservation = new Reservation();

        if (mTextName.getText().toString().isEmpty()) {
            showAlertDialog("Reservation name cannot be empty.");
            mTextName.requestFocus();
            return false;
        }
        else {
            mReservation.mName = mTextName.getText().toString();
        }

        if (mTextNumPeople.getText().toString().isEmpty()) {
            showAlertDialog("People cannot be empty.");
            mTextNumPeople.requestFocus();
            return false;
        }
        else {
            mReservation.mNumPeople = Long.valueOf(mTextNumPeople.getText().toString());
        }

        String uiFromDate = mBtnFromDate.getText().toString();
        if (uiFromDate.compareToIgnoreCase("Set") == 0) {
            showAlertDialog("From Date needs to be set.");
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
            showAlertDialog("Days cannot be empty.");
            mTextNumDays.requestFocus();
            return false;
        }
        else {
            mReservation.mNumDays = Long.valueOf(mTextNumDays.getText().toString());
            Calendar toDate = Calendar.getInstance();
            toDate.setTimeInMillis(mReservation.mFromDate + TimeUnit.MILLISECONDS.convert(mReservation.mNumDays, TimeUnit.DAYS));
            mReservation.mToDate = toDate.getTimeInMillis();
        }
        return true;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void updateUIFromReservation() {

        mTextName.setText(mReservation.mName);
        mTextNumPeople.setText(String.valueOf(mReservation.mNumPeople));
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
        // Toggle the action bar buttons appropriately
        mCallbacks.EnableDeleteButton(true);
        mCallbacks.EnableRevertButton(false);
        mCallbacks.EnableSaveButton(false);
        if (mReservation.mCurrentStatus == Reservation.WaitingStatus) {
            mCallbacks.EnableCheckinButton(true);
            mCallbacks.EnableCheckoutButton(false);
            mSelectedRoomsLayout.setVisibility(View.GONE);
        }
        else if (mReservation.mCurrentStatus == Reservation.CheckedInStatus) {
            mCallbacks.EnableCheckinButton(false);
            mCallbacks.EnableCheckoutButton(true);
        }
        mCallbacks.RedrawOptionsMenu();
        mCallbacks.setTitleString(mReservation.mName + " - " + mReservation.getStatusString()) ;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void displayUIForNewReservation() {
        mTextName.setText("");
        mTextNumPeople.setText("");
        mTextNumDays.setText("");
        mBtnFromDate.setText("Set");

        mSelectedRoomsLayout.setVisibility(View.GONE);

        mCallbacks.EnableRevertButton(false);
        mCallbacks.EnableSaveButton(false);
        mCallbacks.EnableDeleteButton(false);

        // New reservation so disable checkin and checkout buttons
        mCallbacks.EnableCheckinButton(false);
        mCallbacks.EnableCheckoutButton(false);

        mCallbacks.setTitleString("New Reservation");
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
