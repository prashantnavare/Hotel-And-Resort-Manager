package com.navare.prashant.resortmanager.Database;

/**
 * Created by prashant on 18-Apr-15.
 */

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A class representation of a row in table "Reservation".
 */

public class Reservation {

    // Reservation table
    public static final String TABLE_NAME = "ReservationTable";
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Add other fields here.
    // When you add a field, make sure you add corresponding entries in the FIELDS array, the CREATE_TABLE string
    // and declare a member and so on. See all ++++++++ comment lines below.
    // These fields can be anything you want.
    public static final String COL_NAME = "name";
    public static final String COL_NUMADULTS = "numAdults";
    public static final String COL_NUMCHILDREN = "numChildren";
    public static final String COL_NUMDAYS = "numDays";
    public static final String COL_FROM_DATE = "fromDate";
    public static final String COL_TO_DATE = "toDate";
    public static final String COL_CURRENT_STATUS = "currentStatus";

    // Checkout related
    public static final String COL_NUM_ROOMS = "numRooms";
    public static final String COL_BILLING_TYPE = "billingType";
    public static final String COL_ROOM_CHARGE = "roomCharge";
    public static final String COL_ADULT_CHARGE = "adultCharge";
    public static final String COL_CHILD_CHARGE = "childCharge";
    public static final String COL_ADDITIONAL_CHARGES = "additionalCharges";
    public static final String COL_TAX_PERCENT = "taxPercent";
    public static final String COL_TOTAL_CHARGE = "totalCharge";

    public static final String COL_PHONE_NO = "phoneNumber";
    public static final String COL_EMAIL = "emailAddress";
    public static final String COL_ARRIVAL_DETAILS = "arrivalDetails";

    public static final int PendingStatus = 1;
    public static final int CheckedInStatus = 2;
    public static final int CheckedOutStatus = 3;

    public static final int PerRoomBilling = 1;
    public static final int PerPersonBilling = 2;

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // For database projection so order is consistent
    public static final String[] FIELDS = {
            BaseColumns._ID,
            COL_NAME,
            COL_NUMADULTS,
            COL_NUMCHILDREN,
            COL_NUMDAYS,
            COL_FROM_DATE,
            COL_TO_DATE,
            COL_CURRENT_STATUS,

            COL_NUM_ROOMS,
            COL_BILLING_TYPE,
            COL_ROOM_CHARGE,
            COL_ADULT_CHARGE,
            COL_CHILD_CHARGE,
            COL_ADDITIONAL_CHARGES,
            COL_TAX_PERCENT,
            COL_TOTAL_CHARGE,

            COL_PHONE_NO,
            COL_EMAIL,
            COL_ARRIVAL_DETAILS
    };

    public static final HashMap<String, String> mColumnMap = buildColumnMap();
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * Builds a map for all Reservation FTS table columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();

        map.put(BaseColumns._ID, BaseColumns._ID);
        map.put(COL_NAME, COL_NAME);
        map.put(COL_NUMADULTS, COL_NUMADULTS);
        map.put(COL_NUMCHILDREN, COL_NUMCHILDREN);
        map.put(COL_NUMDAYS, COL_NUMDAYS);
        map.put(COL_FROM_DATE, COL_FROM_DATE);
        map.put(COL_TO_DATE, COL_TO_DATE);
        map.put(COL_CURRENT_STATUS, COL_CURRENT_STATUS);

        map.put(COL_NUM_ROOMS, COL_NUM_ROOMS);
        map.put(COL_BILLING_TYPE, COL_BILLING_TYPE);
        map.put(COL_ROOM_CHARGE, COL_ROOM_CHARGE);
        map.put(COL_ADULT_CHARGE, COL_ADULT_CHARGE);
        map.put(COL_CHILD_CHARGE, COL_CHILD_CHARGE);
        map.put(COL_ADDITIONAL_CHARGES, COL_ADDITIONAL_CHARGES);
        map.put(COL_TAX_PERCENT, COL_TAX_PERCENT);
        map.put(COL_TOTAL_CHARGE, COL_TOTAL_CHARGE);

        map.put(COL_PHONE_NO, COL_PHONE_NO);
        map.put(COL_EMAIL, COL_EMAIL);
        map.put(COL_ARRIVAL_DETAILS, COL_ARRIVAL_DETAILS);
        return map;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /*
     * The SQL code that creates a Table for storing reservations.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + COL_NAME + " TEXT NOT NULL DEFAULT '',"
                    + COL_NUMADULTS + " INTEGER,"
                    + COL_NUMCHILDREN + " INTEGER,"
                    + COL_NUMDAYS + " INTEGER,"
                    + COL_FROM_DATE + " INTEGER,"
                    + COL_TO_DATE + " INTEGER,"
                    + COL_CURRENT_STATUS + " INTEGER, "

                    + COL_NUM_ROOMS + " INTEGER, "
                    + COL_BILLING_TYPE + " INTEGER, "
                    + COL_ROOM_CHARGE + " INTEGER, "
                    + COL_ADULT_CHARGE + " INTEGER, "
                    + COL_CHILD_CHARGE + " INTEGER, "
                    + COL_ADDITIONAL_CHARGES + " INTEGER, "
                    + COL_TAX_PERCENT + " FLOAT, "
                    + COL_TOTAL_CHARGE + " INTEGER, "

                    + COL_PHONE_NO + " TEXT DEFAULT '' ,"
                    + COL_EMAIL + " TEXT DEFAULT '' ,"
                    + COL_ARRIVAL_DETAILS + " TEXT DEFAULT '' "
                    + ")";

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Fields corresponding to ReservationTable columns
    public long mID = -1;
    public String mName = "";
    public long mNumAdults = 0;
    public long mNumChildren = 0;
    public long mNumDays = 0;
    public long mFromDate = 0;
    public long mToDate = 0;
    public long mCurrentStatus = 0;

    public long mNumRooms = 0;
    public long mBillingType = 0;
    public long mRoomCharge = 0;
    public long mAdultCharge = 0;
    public long mChildCharge = 0;
    public long mAdditionalCharges = 0;
    public float mTaxPercent = 0;
    public long mTotalCharge = 0;

    public String mPhoneNumber = "";
    public String mEmailAddress = "";
    public String mArrivalDetails = "";

    /**
     * No need to do anything, fields are already set to default values above
     */
    public Reservation() {
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * Convert information from the ReservationTable into an Reservation object.
     */
    public void setContentFromCursor(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.mID = cursor.getLong(0);
        this.mName = cursor.getString(1);
        this.mNumAdults = cursor.getLong(2);
        this.mNumChildren = cursor.getLong(3);
        this.mNumDays = cursor.getLong(4);
        this.mFromDate = cursor.getLong(5);
        this.mToDate = cursor.getLong(6);
        this.mCurrentStatus = cursor.getLong(7);

        this.mNumRooms = cursor.getLong(8);
        this.mBillingType = cursor.getLong(9);
        this.mRoomCharge = cursor.getLong(10);
        this.mAdultCharge = cursor.getLong(11);
        this.mChildCharge = cursor.getLong(12);
        this.mAdditionalCharges = cursor.getLong(13);
        this.mTaxPercent = cursor.getLong(14);
        this.mTotalCharge = cursor.getLong(15);

        this.mPhoneNumber = cursor.getString(16);
        this.mEmailAddress = cursor.getString(17);
        this.mArrivalDetails = cursor.getString(18);
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContentValues() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_NAME, mName);
        values.put(COL_NUMADULTS, mNumAdults);
        values.put(COL_NUMCHILDREN, mNumChildren);
        values.put(COL_NUMDAYS, mNumDays);
        values.put(COL_FROM_DATE, mFromDate);
        values.put(COL_TO_DATE, mToDate);
        values.put(COL_CURRENT_STATUS, mCurrentStatus);

        values.put(COL_NUM_ROOMS, mNumRooms);
        values.put(COL_BILLING_TYPE, mBillingType);
        values.put(COL_ROOM_CHARGE, mRoomCharge);
        values.put(COL_ADULT_CHARGE, mAdultCharge);
        values.put(COL_CHILD_CHARGE, mChildCharge);
        values.put(COL_ADDITIONAL_CHARGES, mAdditionalCharges);
        values.put(COL_TAX_PERCENT, mTaxPercent);
        values.put(COL_TOTAL_CHARGE, mTotalCharge);

        values.put(COL_PHONE_NO, mPhoneNumber);
        values.put(COL_EMAIL, mEmailAddress);
        values.put(COL_ARRIVAL_DETAILS, mArrivalDetails);
        return values;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * sets the fields from a ContentValues object
     */
    public void setContentFromCV(final ContentValues values) {
        // Note that ID is NOT included here
        mName = values.getAsString(COL_NAME);
        mNumAdults = values.getAsLong(COL_NUMADULTS);
        mNumChildren = values.getAsLong(COL_NUMCHILDREN);
        mNumDays = values.getAsLong(COL_NUMDAYS);
        mFromDate = values.getAsLong(COL_FROM_DATE);
        mToDate = values.getAsLong(COL_TO_DATE);
        mCurrentStatus = values.getAsLong(COL_CURRENT_STATUS);

        mNumRooms = values.getAsLong(COL_NUM_ROOMS);
        mBillingType = values.getAsLong(COL_BILLING_TYPE);
        mRoomCharge = values.getAsLong(COL_ROOM_CHARGE);
        mAdultCharge = values.getAsLong(COL_ADULT_CHARGE);
        mChildCharge = values.getAsLong(COL_CHILD_CHARGE);
        mAdditionalCharges = values.getAsLong(COL_ADDITIONAL_CHARGES);
        mTaxPercent = values.getAsFloat(COL_TAX_PERCENT);
        mTotalCharge = values.getAsLong(COL_TOTAL_CHARGE);

        mPhoneNumber = values.getAsString(COL_PHONE_NO);
        mEmailAddress = values.getAsString(COL_EMAIL);
        mArrivalDetails = values.getAsString(COL_ARRIVAL_DETAILS);
    }

    public String getStatusString() {
        return getStatusString((int)mCurrentStatus);
    }

    public static String getStatusString(int status) {
        switch (status) {
            case PendingStatus:
                return "Pending";
            case CheckedInStatus:
                return "Checked In";
            case CheckedOutStatus:
                return "Checked Out";
        }
        return "Unknown";
    }

    public String getFTSReservationName() {
        String ftsReservationName = mName + " (Adults : " + String.valueOf(mNumAdults) + ") (Children : " + String.valueOf(mNumChildren) + ")";
        return  ftsReservationName;
    }

    public String getDatesString() {
        String datesString = "";
        if (mFromDate > 0 && mToDate > 0) {
            Calendar fromDate = Calendar.getInstance();
            Calendar toDate = Calendar.getInstance();
            fromDate.setTimeInMillis(mFromDate);
            toDate.setTimeInMillis(mToDate);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
            datesString = dateFormatter.format(fromDate.getTime()) + " - " + dateFormatter.format(toDate.getTime());
        }
        return datesString;
    }

    // Reservation FTS Table - for reservations that are in Pending or Checked in mode
    public static final String FTS_TABLE_NAME = "FTSReservationTable";
    public static final String COL_FTS_RESERVATION_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String COL_FTS_RESERVATION_DATES = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String COL_FTS_RESERVATION_STATUS = "status";
    public static final String COL_FTS_RESERVATION_REALID = "realID";
    public static final String COL_FTS_TO_DATE = "toDate";
    public static final String COL_FTS_FROM_DATE = "fromDate";

    // For database projection so order is consistent
    public static final String[] FTS_FIELDS = {
            BaseColumns._ID,
            COL_FTS_RESERVATION_NAME,
            COL_FTS_RESERVATION_DATES,
            COL_FTS_RESERVATION_STATUS,
            COL_FTS_RESERVATION_REALID,
            COL_FTS_TO_DATE,
            COL_FTS_FROM_DATE
    };

    /* Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, we will use "_id" as an alias for "rowid"
     */
    public static final String CREATE_FTS_TABLE =
            "CREATE VIRTUAL TABLE " + FTS_TABLE_NAME +
                    " USING fts3 (" +
                    COL_FTS_RESERVATION_NAME + ", " +
                    COL_FTS_RESERVATION_DATES + "," +
                    COL_FTS_RESERVATION_STATUS + "," +
                    COL_FTS_RESERVATION_REALID + "," +
                    COL_FTS_TO_DATE + "," +
                    COL_FTS_FROM_DATE +
                    ");";

    // Fields corresponding to FTSReservationTable columns
    public String mRowID = "";
    public String mFTSName = "";
    public String mFTSDates = "";
    public String mFTSStatus = "";
    public String mFTSRealID = "";
    public String mFTSToDate = "";
    public String mFTSFromDate = "";

    /**
     * Set information from the FTSReservationTable into an Reservation object.
     */
    public void setFTSContent(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.mRowID = cursor.getString(0);
        this.mFTSName = cursor.getString(1);
        this.mFTSDates = cursor.getString(2);
        this.mFTSStatus = cursor.getString(3);
        this.mFTSRealID = cursor.getString(4);
        this.mFTSToDate = cursor.getString(5);
        this.mFTSFromDate = cursor.getString(6);
    }

    public static final HashMap<String, String> mFTSColumnMap = buildFTSColumnMap();
    /**
     * Builds a map for all Reservation FTS table columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildFTSColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(COL_FTS_RESERVATION_NAME, COL_FTS_RESERVATION_NAME);
        map.put(COL_FTS_RESERVATION_DATES, COL_FTS_RESERVATION_DATES);
        map.put(COL_FTS_RESERVATION_STATUS, COL_FTS_RESERVATION_STATUS);
        map.put(COL_FTS_RESERVATION_REALID, COL_FTS_RESERVATION_REALID);
        map.put(COL_FTS_TO_DATE, COL_FTS_TO_DATE);
        map.put(COL_FTS_FROM_DATE, COL_FTS_FROM_DATE);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }

    // Completed Reservations FTS Table - for reservations that were successfully checked out.
    public static final String COMPLETED_FTS_TABLE_NAME = "CompletedFTSReservationTable";
    public static final String COMPLETED_COL_FTS_NAME = "name";
    public static final String COMPLETED_COL_FTS_PHONE_NO = "phoneNumber";
    public static final String COMPLETED_COL_FTS_EMAIL = "emailAddress";
    public static final String COMPLETED_COL_FTS_NUM_ADULTS = "numAdults";
    public static final String COMPLETED_COL_FTS_NUM_CHILDREN = "numChildren";
    public static final String COMPLETED_COL_FTS_NUM_DAYS = "numDays";
    public static final String COMPLETED_COL_FTS_DATES = "dates";
    public static final String COMPLETED_COL_FTS_NUM_ROOMS = "numRooms";
    public static final String COMPLETED_COL_FTS_ROOM_CHARGE = "roomCharge";
    public static final String COMPLETED_COL_FTS_ADULT_CHARGE = "adultCharge";
    public static final String COMPLETED_COL_FTS_CHILD_CHARGE = "childCharge";
    public static final String COMPLETED_COL_FTS_ADDITIONAL_CHARGE = "additionalCharge";
    public static final String COMPLETED_COL_FTS_TAX_PERCENT = "taxPercent";
    public static final String COMPLETED_COL_FTS_TOTAL_CHARGE = "totalCharge";
    public static final String COMPLETED_COL_FTS_TO_DATE = "toDate";

    // For database projection so order is consistent
    public static final String[] COMPLETED_FTS_FIELDS = {
            BaseColumns._ID,
            COMPLETED_COL_FTS_NAME,
            COMPLETED_COL_FTS_PHONE_NO,
            COMPLETED_COL_FTS_EMAIL,
            COMPLETED_COL_FTS_NUM_ADULTS,
            COMPLETED_COL_FTS_NUM_CHILDREN,
            COMPLETED_COL_FTS_NUM_DAYS,
            COMPLETED_COL_FTS_DATES,
            COMPLETED_COL_FTS_NUM_ROOMS,
            COMPLETED_COL_FTS_ROOM_CHARGE,
            COMPLETED_COL_FTS_ADULT_CHARGE,
            COMPLETED_COL_FTS_CHILD_CHARGE,
            COMPLETED_COL_FTS_ADDITIONAL_CHARGE,
            COMPLETED_COL_FTS_TAX_PERCENT,
            COMPLETED_COL_FTS_TOTAL_CHARGE,
            COMPLETED_COL_FTS_TO_DATE
    };

    /* Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, we will use "_id" as an alias for "rowid"
     */
    public static final String CREATE_COMPLETED_FTS_TABLE =
            "CREATE VIRTUAL TABLE " + COMPLETED_FTS_TABLE_NAME +
                    " USING fts3 (" +
                    COMPLETED_COL_FTS_NAME + "," +
                    COMPLETED_COL_FTS_PHONE_NO + "," +
                    COMPLETED_COL_FTS_EMAIL + "," +
                    COMPLETED_COL_FTS_NUM_ADULTS + "," +
                    COMPLETED_COL_FTS_NUM_CHILDREN + "," +
                    COMPLETED_COL_FTS_NUM_DAYS + "," +
                    COMPLETED_COL_FTS_DATES + "," +
                    COMPLETED_COL_FTS_NUM_ROOMS + "," +

                    COMPLETED_COL_FTS_ROOM_CHARGE + "," +
                    COMPLETED_COL_FTS_ADULT_CHARGE + "," +
                    COMPLETED_COL_FTS_CHILD_CHARGE + "," +
                    COMPLETED_COL_FTS_ADDITIONAL_CHARGE + "," +
                    COMPLETED_COL_FTS_TAX_PERCENT + "," +
                    COMPLETED_COL_FTS_TOTAL_CHARGE + "," +
                    COMPLETED_COL_FTS_TO_DATE +
                    ");";

    // Fields corresponding to FTSItemTable columns
    public String mCompletedRowID = "";
    public String mCompletedFTSName = "";
    public String mCompletedFTSPhoneNumber = "";
    public String mCompletedFTSEmailAddress = "";
    public String mCompletedFTSNumAdults = "";
    public String mCompletedFTSNumChildren = "";
    public String mCompletedFTSNumDays = "";
    public String mCompletedFTSDates = "";
    public String mCompletedFTSNumRooms = "";

    public String mCompletedFTSRoomCharge = "";
    public String mCompletedFTSAdultCharge = "";
    public String mCompletedFTSChildCharge = "";
    public String mCompletedFTSAdditionalCharge = "";
    public String mCompletedFTSTaxPercent = "";
    public String mCompletedFTSTotalCharge = "";
    public String mCompletedFTSToDate = "";

    /**
     * Set information from the CompletedFTSItemTable into a Task object.
     */
    public void setCompletedFTSContent(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.mCompletedRowID = cursor.getString(0);
        this.mCompletedFTSName = cursor.getString(1);
        this.mCompletedFTSPhoneNumber = cursor.getString(2);
        this.mCompletedFTSEmailAddress = cursor.getString(3);
        this.mCompletedFTSNumAdults = cursor.getString(4);
        this.mCompletedFTSNumChildren = cursor.getString(5);
        this.mCompletedFTSNumDays = cursor.getString(6);
        this.mCompletedFTSDates = cursor.getString(7);
        this.mCompletedFTSNumRooms = cursor.getString(8);

        this.mCompletedFTSRoomCharge = cursor.getString(9);
        this.mCompletedFTSAdultCharge = cursor.getString(10);
        this.mCompletedFTSChildCharge = cursor.getString(11);
        this.mCompletedFTSAdditionalCharge = cursor.getString(12);
        this.mCompletedFTSTaxPercent = cursor.getString(13);
        this.mCompletedFTSTotalCharge = cursor.getString(14);
        this.mCompletedFTSToDate = cursor.getString(15);
    }

    public static final HashMap<String, String> mCompletedFTSColumnMap = buildCompletedFTSColumnMap();
    /**
     * Builds a map for all Item FTS table columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildCompletedFTSColumnMap() {
        HashMap<String,String> map = new HashMap<>();
        map.put(COMPLETED_COL_FTS_NAME, COMPLETED_COL_FTS_NAME);
        map.put(COMPLETED_COL_FTS_PHONE_NO, COMPLETED_COL_FTS_PHONE_NO);
        map.put(COMPLETED_COL_FTS_EMAIL, COMPLETED_COL_FTS_EMAIL);
        map.put(COMPLETED_COL_FTS_NUM_ADULTS, COMPLETED_COL_FTS_NUM_ADULTS);
        map.put(COMPLETED_COL_FTS_NUM_CHILDREN, COMPLETED_COL_FTS_NUM_CHILDREN);
        map.put(COMPLETED_COL_FTS_NUM_DAYS, COMPLETED_COL_FTS_NUM_DAYS);
        map.put(COMPLETED_COL_FTS_DATES, COMPLETED_COL_FTS_DATES);
        map.put(COMPLETED_COL_FTS_NUM_ROOMS, COMPLETED_COL_FTS_NUM_ROOMS);

        map.put(COMPLETED_COL_FTS_ROOM_CHARGE, COMPLETED_COL_FTS_ROOM_CHARGE);
        map.put(COMPLETED_COL_FTS_ADULT_CHARGE, COMPLETED_COL_FTS_ADULT_CHARGE);
        map.put(COMPLETED_COL_FTS_CHILD_CHARGE, COMPLETED_COL_FTS_CHILD_CHARGE);
        map.put(COMPLETED_COL_FTS_ADDITIONAL_CHARGE, COMPLETED_COL_FTS_ADDITIONAL_CHARGE);
        map.put(COMPLETED_COL_FTS_TAX_PERCENT, COMPLETED_COL_FTS_TAX_PERCENT);
        map.put(COMPLETED_COL_FTS_TOTAL_CHARGE, COMPLETED_COL_FTS_TOTAL_CHARGE);
        map.put(COMPLETED_COL_FTS_TO_DATE, COMPLETED_COL_FTS_TO_DATE);

        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }
}
