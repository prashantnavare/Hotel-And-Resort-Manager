package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.navare.prashant.resortmanager.Database.Reservation;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;
import com.navare.prashant.resortmanager.Database.Room;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.util.ReportDetailCursorAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A list fragment representing a list of Tasks. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TaskDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ReportDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID_COMPLETED_RESERVATION_DETAILS = 11;
    public static final String ARG_COMPLETED_RESERVATION_ID = "completed_reservation_id";
    private Context mContext = null;

    private String mReservationID = "-1";
    private Reservation mReservation = null;


    public interface Callbacks {
        void setTitleString(String titleString);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void setTitleString(String titleString) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    private TextView mTextName;
    private TextView mTextContactInfo;
    private TextView mTextNumAdults;
    private TextView mTextNumChildren;
    private TextView mTextNumDays;
    private TextView mTextDates;

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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReportDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_COMPLETED_RESERVATION_ID)) {
            mReservationID = getArguments().getString(ARG_COMPLETED_RESERVATION_ID);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof ReportDetailFragment.Callbacks)) {
            throw new IllegalStateException("Activity must implement reservation detail fragment's callbacks.");
        }

        mCallbacks = (ReportDetailFragment.Callbacks) activity;
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
        View rootView = inflater.inflate(R.layout.fragment_report_detail, container, false);

        mTextName = ((TextView) rootView.findViewById(R.id.textName));
        mTextContactInfo = ((TextView) rootView.findViewById(R.id.textContactInfo));
        mTextNumAdults = ((TextView) rootView.findViewById(R.id.textNumAdults));
        mTextNumChildren = ((TextView) rootView.findViewById(R.id.textNumChildren));
        mTextNumDays = (TextView) rootView.findViewById(R.id.textNumDays);
        mTextDates = (TextView) rootView.findViewById(R.id.textDates);
        // Checkout related
        mTextAllocatedRooms = ((TextView) rootView.findViewById(R.id.textAllocatedRooms));
        mTextRoomCharge = ((TextView) rootView.findViewById(R.id.textRoomCharge));
        mTextAdultCharge = ((TextView) rootView.findViewById(R.id.textAdultCharge));
        mTextChildCharge = ((TextView) rootView.findViewById(R.id.textChildCharge));
        mTextAdditionalCharge = ((TextView) rootView.findViewById(R.id.textAdditionalCharge));
        mTextTaxPercent = ((TextView) rootView.findViewById(R.id.textTaxPercent));
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID_COMPLETED_RESERVATION_DETAILS, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == LOADER_ID_COMPLETED_RESERVATION_DETAILS) {
            Uri reservationURI = Uri.withAppendedPath(ResortManagerContentProvider.FTS_COMPLETED_RESERVATION_URI,
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
            if (loaderID == LOADER_ID_COMPLETED_RESERVATION_DETAILS) {
                if (mReservation == null)
                    mReservation = new Reservation();

                mReservation.setCompletedFTSContent(dataCursor);
                updateUIFromReservation();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void updateUIFromReservation() {

        mTextName.setText(mReservation.mCompletedFTSName);
        mTextContactInfo.setText(mReservation.mCompletedFTSContactInfo);
        mTextNumAdults.setText(mReservation.mCompletedFTSNumAdults);
        mTextNumChildren.setText(mReservation.mCompletedFTSNumChildren);
        mTextNumDays.setText(mReservation.mCompletedFTSNumDays);
        mTextDates.setText(mReservation.mCompletedFTSDates);
        mCallbacks.setTitleString("Details for " + mReservation.mCompletedFTSName) ;

        mTextAllocatedRooms.setText(mReservation.mCompletedFTSNumRooms);
        mTextRoomCharge.setText(mReservation.mCompletedFTSRoomCharge);
        mTextAdultCharge.setText(mReservation.mCompletedFTSAdultCharge);
        mTextChildCharge.setText(mReservation.mCompletedFTSChildCharge);
        mTextAdditionalCharge.setText(mReservation.mCompletedFTSAdditionalCharge);
        mTextTaxPercent.setText(mReservation.mCompletedFTSTaxPercent);
        mTextTotalCharge.setText(mReservation.mCompletedFTSTotalCharge);
    }

}
