package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.navare.prashant.resortmanager.Database.Reservation;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;

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
        void RedrawOptionsMenu();
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void setTitleString(String titleString) {
        }
        @Override
        public void RedrawOptionsMenu() {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    private TextView mTextName;
    private TextView mTextPhoneNUmber;
    private TextView mTextEmailAddress;
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
        super.onPause();
    }

    // Called when returning to the activity
    @Override
    public void onResume() {
        super.onResume();
    }

    // Called before the activity is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_report_detail, container, false);

        mTextName = ((TextView) rootView.findViewById(R.id.textName));
        mTextPhoneNUmber = ((TextView) rootView.findViewById(R.id.textPhoneNumber));
        mTextPhoneNUmber.setMovementMethod(LinkMovementMethod.getInstance());
        mTextEmailAddress = ((TextView) rootView.findViewById(R.id.textEmailAddress));
        mTextEmailAddress.setMovementMethod(LinkMovementMethod.getInstance());
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
        mTextPhoneNUmber.setText(mReservation.mCompletedFTSPhoneNumber);
        mTextEmailAddress.setText(mReservation.mCompletedFTSEmailAddress);
        mTextNumAdults.setText(mReservation.mCompletedFTSNumAdults);
        mTextNumChildren.setText(mReservation.mCompletedFTSNumChildren);
        mTextNumDays.setText(mReservation.mCompletedFTSNumDays);
        mTextDates.setText(mReservation.mCompletedFTSDates);

        mTextAllocatedRooms.setText(mReservation.mCompletedFTSNumRooms);
        mTextRoomCharge.setText(mReservation.mCompletedFTSRoomCharge);
        mTextAdultCharge.setText(mReservation.mCompletedFTSAdultCharge);
        mTextChildCharge.setText(mReservation.mCompletedFTSChildCharge);
        mTextAdditionalCharge.setText(mReservation.mCompletedFTSAdditionalCharge);
        mTextTaxPercent.setText(mReservation.mCompletedFTSTaxPercent);
        mTextTotalCharge.setText(mReservation.mCompletedFTSTotalCharge);

        mCallbacks.setTitleString("Details for " + mReservation.mCompletedFTSName) ;

        mCallbacks.RedrawOptionsMenu();
    }

    public void doEmail(String emailAddress) {

        String emailSubject = constructEmailSubject();
        String emailBody = constructEmailBody();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ emailAddress});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
        emailIntent.setType("message/rfc822");

        startActivity(Intent.createChooser(emailIntent, "Send e-mail"));
    }

    String constructEmailSubject() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String organizationName = preferences.getString(ResortManagerApp.sPrefOrganizationName, "");
        String emailSubject = "Your invoice";
        if (organizationName.isEmpty() == false) {
            emailSubject = emailSubject + " from " + organizationName;
        }
        return emailSubject;
    }

    String constructEmailBody() {
        String emailBody = "";

        emailBody = emailBody + "Name: " + mReservation.mCompletedFTSName + "\r\n";
        emailBody = emailBody + "Adults: " + mReservation.mCompletedFTSNumAdults + "\r\n";
        emailBody = emailBody + "Children: " + mReservation.mCompletedFTSNumChildren + "\r\n";
        emailBody = emailBody + "Days: " + mReservation.mCompletedFTSNumDays + "\r\n";
        emailBody = emailBody + "Dates: " + mReservation.mCompletedFTSDates + "\r\n";
        emailBody = emailBody + "Rooms: " + mReservation.mCompletedFTSNumRooms + "\r\n";
        emailBody = emailBody + "Room Charges Per Day: " + mReservation.mCompletedFTSRoomCharge + "\r\n";
        emailBody = emailBody + "Charges per Adult Per Day: " + mReservation.mCompletedFTSAdultCharge + "\r\n";
        emailBody = emailBody + "Charges per Child Per Day: " + mReservation.mCompletedFTSChildCharge + "\r\n";
        emailBody = emailBody + "Additional Charges: " + mReservation.mCompletedFTSAdditionalCharge + "\r\n";
        emailBody = emailBody + "Tax Percentage: " + mReservation.mCompletedFTSTaxPercent + "\r\n";
        emailBody = emailBody + "\r\n\r\n";
        emailBody = emailBody + "Total Charges: " + mReservation.mCompletedFTSTotalCharge + "\r\n";

        return emailBody;
    }

    public void doSMS(String mobileNumber) {
        String smsMessage = constructSMS();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + Uri.encode(mobileNumber)));
        intent.putExtra("sms_body", smsMessage);
        startActivity(intent);
    }

    String constructSMS() {
        String smsMessage = "";

        smsMessage = smsMessage + "Name: " + mReservation.mCompletedFTSName + "\r\n";
        smsMessage = smsMessage + "Adults: " + mReservation.mCompletedFTSNumAdults + "\r\n";
        if (mReservation.mCompletedFTSNumChildren.equalsIgnoreCase("0") == false)
            smsMessage = smsMessage + "Children: " + mReservation.mCompletedFTSNumChildren + "\r\n";
        smsMessage = smsMessage + "Days: " + mReservation.mCompletedFTSNumDays + "\r\n";
        smsMessage = smsMessage + "Dates: " + mReservation.mCompletedFTSDates + "\r\n";
        smsMessage = smsMessage + "Rooms: " + mReservation.mCompletedFTSNumRooms + "\r\n";
        if (mReservation.mCompletedFTSRoomCharge.equalsIgnoreCase("0") == false)
            smsMessage = smsMessage + "Room Charges Per Day: " + mReservation.mCompletedFTSRoomCharge + "\r\n";
        if (mReservation.mCompletedFTSAdultCharge.equalsIgnoreCase("0") == false)
            smsMessage = smsMessage + "Charges per Adult Per Day: " + mReservation.mCompletedFTSAdultCharge + "\r\n";
        if (mReservation.mCompletedFTSChildCharge.equalsIgnoreCase("0") == false)
            smsMessage = smsMessage + "Charges per Child Per Day: " + mReservation.mCompletedFTSChildCharge + "\r\n";
        if (mReservation.mCompletedFTSAdditionalCharge.equalsIgnoreCase("0") == false)
            smsMessage = smsMessage + "Additional Charges: " + mReservation.mCompletedFTSAdditionalCharge + "\r\n";
        if (mReservation.mCompletedFTSTaxPercent.equalsIgnoreCase("0") == false)
            smsMessage = smsMessage + "Tax Percentage: " + mReservation.mCompletedFTSTaxPercent + "\r\n";
        smsMessage = smsMessage + "\r\n";
        smsMessage = smsMessage + "Total Charges: " + mReservation.mCompletedFTSTotalCharge + "\r\n";

        return smsMessage;
    }

    public String getEmailAddress() {
        return mReservation.mCompletedFTSEmailAddress;
    }

    public String getPhoneNumber() {
        return mReservation.mCompletedFTSPhoneNumber;
    }

    public void callCustomer() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + mReservation.mCompletedFTSPhoneNumber));
        startActivity(callIntent);
    }

}
