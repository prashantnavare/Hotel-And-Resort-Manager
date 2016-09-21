package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;
import com.navare.prashant.resortmanager.Database.Reservation;
import com.navare.prashant.resortmanager.Database.ServiceCall;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.util.CalibrationDatePickerFragment;
import com.navare.prashant.resortmanager.util.ServiceCallDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A fragment representing a single Reservation detail screen.
 * This fragment is either contained in a {@link ReservationListActivity}
 * in two-pane mode (on tablets) or a {@link ReservationDetailActivity}
 * on handsets.
 */
public class ReservationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {

    private static final int LOADER_ID_RESERVATION_DETAILS = 2;

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
    private long mPreviousType = 0;

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * The UI elements showing the details of the reservation
     */
    private TextView mTextName;
    private TextView mTextDescription;
    private TextView mTextCapacity;

    private LinearLayout mCleaningRemindersLayout;
    private LinearLayout mCleaningDetailsLayout;

    private CheckBox mCleaningCheckBox;
    private TextView mTextCleaningFrequency;
    private Button mBtnChangeCleaningDate;
    private TextView mTextCleaningInstructions;

    private ImageView mImageView;

    private String mImageFileName;
    private File mImageFile;
    private Uri mImageFileUri;
    private Bitmap mImageBitmap = null;

    private AdView mAdView;


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
        void EnableServiceCallButton(boolean bEnable);
        void onReservationDeleted();
        void setTitleString(String titleString);
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
        public void EnableServiceCallButton(boolean bEnable) {
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

        mTextDescription = ((TextView) rootView.findViewById(R.id.textDescription));
        mTextDescription.addTextChangedListener(this);

        mTextCapacity = (TextView) rootView.findViewById(R.id.textCapacity);
        mTextCapacity.addTextChangedListener(this);

        mCleaningRemindersLayout = (LinearLayout) rootView.findViewById(R.id.cleaningRemindersLayout);
        mCleaningDetailsLayout = (LinearLayout) rootView.findViewById(R.id.cleaningDetailsLayout);

        // Cleaning
        mCleaningCheckBox = (CheckBox) rootView.findViewById(R.id.chkCleaning);
        mCleaningCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mCleaningDetailsLayout.setVisibility(View.VISIBLE);
                } else {
                    mCleaningDetailsLayout.setVisibility(View.GONE);
                }
                enableRevertAndSaveButtons();
            }
        });
        mTextCleaningFrequency = (TextView) rootView.findViewById(R.id.textCleaningFrequency);
        mTextCleaningFrequency.addTextChangedListener(this);

        mBtnChangeCleaningDate = (Button) rootView.findViewById(R.id.btnChangeCleaningDate);
        mBtnChangeCleaningDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker(DatePickerType.CLEANING);
            }
        });

        mTextCleaningInstructions = (TextView) rootView.findViewById(R.id.textCleaningInstructions);
        mTextCleaningInstructions.addTextChangedListener(this);

        // image related
        mImageView = ((ImageView) rootView.findViewById(R.id.imageReservation));

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

    enum DatePickerType {CLEANING}

    private void showDatePicker(final DatePickerType pickerType) {
        Calendar dateToShow = Calendar.getInstance();
        if (mReservation != null) {
            switch (pickerType) {
                case CLEANING:
                    if (mReservation.mCleaningDate > 0) {
                        dateToShow.setTimeInMillis(mReservation.mCleaningDate);
                    }
                    break;
            }
        }
        CalibrationDatePickerFragment datePicker = new CalibrationDatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("year", dateToShow.get(Calendar.YEAR));
        args.putInt("month", dateToShow.get(Calendar.MONTH));
        args.putInt("day", dateToShow.get(Calendar.DAY_OF_MONTH));
        datePicker.setArguments(args);
        /**
         * Set Call back to capture selected date
         */
        OnDateSetListener onDateChangeCallback = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                switch (pickerType) {
                    case CLEANING:
                        mBtnChangeCleaningDate.setText(dateFormatter.format(newDate.getTime()));
                        break;
                }
                enableRevertAndSaveButtons();
            }
        };
        datePicker.setCallBack(onDateChangeCallback);
        datePicker.show(((FragmentActivity)mContext).getSupportFragmentManager(), "Instrument Date Picker");
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
                mCallbacks.setTitleString(mReservation.mName);

                // Toggle the action bar buttons appropriately
                mCallbacks.EnableDeleteButton(true);
                mCallbacks.EnableRevertButton(false);
                mCallbacks.EnableSaveButton(false);
                mCallbacks.RedrawOptionsMenu();
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

        mCallbacks.EnableRevertButton(false);
        mCallbacks.EnableSaveButton(false);
        mCallbacks.RedrawOptionsMenu();
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
            // a new reservation is being inserted.
            Uri uri = getActivity().getContentResolver().insert(ResortManagerContentProvider.RESERVATION_URI, mReservation.getContentValues());
            if (uri != null) {
                mReservationID = uri.getLastPathSegment();
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
            mCallbacks.EnableSaveButton(false);
            mCallbacks.EnableRevertButton(false);
            mCallbacks.RedrawOptionsMenu();
            mCallbacks.setTitleString(mReservation.mName);
        }
        return true;
    }

    private void showAlertDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle("Incomplete Data");

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_hospital_inventory);

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
        mReservation.mDescription = mTextDescription.getText().toString();

        if (mTextCapacity.getText().toString().isEmpty()) {
            showAlertDialog("Capacity cannot be empty.");
            mTextCapacity.requestFocus();
            return false;
        }
        else {
            mReservation.mCapacity = Long.valueOf(mTextCapacity.getText().toString());
        }

        // Cleaning related
        if (mCleaningCheckBox.isChecked()) {
            mReservation.mCleaningReminders = 1;
            if (mTextCleaningFrequency.getText().toString().isEmpty()) {
                showAlertDialog("Cleaning frequency cannot be empty.");
                mTextCleaningFrequency.requestFocus();
                return false;
            }
            else {
                mReservation.mCleaningFrequency = Long.valueOf(mTextCleaningFrequency.getText().toString());
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
            Calendar cleaningDate = Calendar.getInstance();
            String uiCleaningDate = mBtnChangeCleaningDate.getText().toString();
            if (uiCleaningDate.compareToIgnoreCase("Set") != 0) {
                try {
                    cleaningDate.setTime(dateFormatter.parse(uiCleaningDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mReservation.mCleaningDate = cleaningDate.getTimeInMillis();
            }

            mReservation.mCleaningInstructions = mTextCleaningInstructions.getText().toString();
        }
        else {
            mReservation.mCleaningReminders = 0;
        }
        if (mImageBitmap != null) {
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            mImageBitmap.compress(Bitmap.CompressFormat.PNG, 0, imageStream);
            mReservation.mImage = imageStream.toByteArray();
        }
        return true;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void updateUIFromReservation() {

        mTextName.setText(mReservation.mName);
        mTextDescription.setText(mReservation.mDescription);

        mTextCapacity.setText(String.valueOf(mReservation.mCapacity));

        // Set the cleaning UI elements
        if (mReservation.mCleaningReminders > 0) {
            mCleaningCheckBox.setChecked(true);
            mCleaningDetailsLayout.setVisibility(View.VISIBLE);
            if (mReservation.mCleaningFrequency > 0)
                mTextCleaningFrequency.setText(String.valueOf(mReservation.mCleaningFrequency));
            if (mReservation.mCleaningDate > 0) {
                Calendar cleaningDate = Calendar.getInstance();
                cleaningDate.setTimeInMillis(mReservation.mCleaningDate);
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                mBtnChangeCleaningDate.setText(dateFormatter.format(cleaningDate.getTime()));
            }
            else {
                mBtnChangeCleaningDate.setText("Set");
            }
            mTextCleaningInstructions.setText(mReservation.mCleaningInstructions);
        }
        else {
            mCleaningCheckBox.setChecked(false);
            mCleaningDetailsLayout.setVisibility(View.GONE);
        }

        if (mReservation.mImage == null) {
            mImageView.setImageBitmap(null);
        }
        else {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = false;
            mImageBitmap = BitmapFactory.decodeByteArray(mReservation.mImage, 0, mReservation.mImage.length, bmpFactoryOptions);
            // Display it
            mImageView.setImageBitmap(mImageBitmap);
        }
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void displayUIForNewReservation() {
        mTextName.setText("");
        mTextDescription.setText("");
        mTextCapacity.setText("");

        mCleaningCheckBox.setChecked(false);
        mCleaningDetailsLayout.setVisibility(View.GONE);

        mImageView.setImageBitmap(null);

        mCallbacks.EnableRevertButton(false);
        mCallbacks.EnableSaveButton(false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void showServiceCallDialog() {
        ServiceCallDialogFragment dialog = new ServiceCallDialogFragment();
        dialog.setReservation(mReservation);
        dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "ServiceCallDialogFragment");
    }

    public void createServiceCall(long reservationID, String description, long priority, String reservationName, String reservationDescription) {
        ServiceCall sc = new ServiceCall();
        sc.mReservationID = reservationID;
        sc.mDescription = description;
        sc.mPriority = priority;
        sc.mStatus = ServiceCall.OpenStatus;
        sc.mOpenTimeStamp = Calendar.getInstance().getTimeInMillis();
        sc.mItemName = reservationName;
        sc.mItemLocation = reservationDescription;

        // a new service call is being inserted.
        Uri uri = getActivity().getContentResolver().insert(ResortManagerContentProvider.SERVICE_CALL_URI, sc.getContentValues());
        if (uri != null) {
            Toast toast = Toast.makeText(mContext, "Problem report created.", Toast.LENGTH_SHORT);
            toast.show();

            // Also create a corresponding task
            Task task = new Task();
            task.mTaskType = Task.ServiceCall;
            task.mReservationID = reservationID;
            task.mServiceCallID = Long.valueOf(uri.getLastPathSegment());
            task.mItemName = mReservation.mName;
            task.mItemLocation = mReservation.mDescription;
            task.mStatus = Task.OpenStatus;
            task.mPriority = priority;

            Uri taskUri = getActivity().getContentResolver().insert(ResortManagerContentProvider.TASK_URI, task.getContentValues());
        }
        else {
            Toast toast = Toast.makeText(mContext, "Failed to create problem report.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public void handleCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            mImageFileName = mContext.getExternalFilesDir(null).getAbsolutePath() + "/" + String.valueOf(Calendar.getInstance().getTimeInMillis()) + ".png";
            mImageFile = new File(mImageFileName);
            mImageFileUri = Uri.fromFile(mImageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inSampleSize = 4;
            mImageBitmap = BitmapFactory.decodeFile(mImageFileName, bmpFactoryOptions);
            // Display it
            mImageView.setImageBitmap(mImageBitmap);
            mImageFile.delete();
            enableRevertAndSaveButtons();
        }
    }
}