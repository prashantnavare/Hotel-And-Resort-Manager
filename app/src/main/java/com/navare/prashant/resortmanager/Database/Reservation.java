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

    public static final int WaitingStatus = 1;
    public static final int CheckedInStatus = 2;
    public static final int CheckedOutStatus = 3;

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
            COL_CURRENT_STATUS

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
                    + COL_CURRENT_STATUS + " INTEGER"
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
        this.mNumDays = cursor.getLong(3);
        this.mFromDate = cursor.getLong(4);
        this.mToDate = cursor.getLong(5);
        this.mCurrentStatus = cursor.getLong(6);
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
    }

    public String getStatusString() {
        switch ((int)mCurrentStatus) {
            case WaitingStatus:
                return "Waiting";
            case CheckedInStatus:
                return "Checked In";
            case CheckedOutStatus:
                return "Checked Out";
        }
        return "Unknown";
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

    // Reservation FTS Table
    public static final String FTS_TABLE_NAME = "FTSReservationTable";
    public static final String COL_FTS_RESERVATION_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String COL_FTS_RESERVATION_DATES = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String COL_FTS_RESERVATION_STATUS = "status";
    public static final String COL_FTS_RESERVATION_REALID = "realID";

    // For database projection so order is consistent
    public static final String[] FTS_FIELDS = {
            BaseColumns._ID,
            COL_FTS_RESERVATION_NAME,
            COL_FTS_RESERVATION_DATES,
            COL_FTS_RESERVATION_STATUS,
            COL_FTS_RESERVATION_REALID
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
                    COL_FTS_RESERVATION_REALID +
                    ");";

    // Fields corresponding to FTSReservationTable columns
    public String mRowID = "";
    public String mFTSName = "";
    public String mFTSDates = "";
    public String mFTSStatus = "";
    public String mFTSRealID = "";

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
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }

}
