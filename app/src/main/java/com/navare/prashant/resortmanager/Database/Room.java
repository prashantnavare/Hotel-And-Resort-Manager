package com.navare.prashant.resortmanager.Database;

/**
 * Created by prashant on 18-Apr-15.
 */

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.HashMap;

/**
 * A class representation of a row in table "Room".
 */

public class Room {

    // Room table
    public static final String TABLE_NAME = "RoomTable";
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Add other fields here.
    // When you add a field, make sure you add corresponding entries in the FIELDS array, the CREATE_TABLE string
    // and declare a member and so on. See all ++++++++ comment lines below.
    // These fields can be anything you want.
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";

    public static final String COL_CAPACITY = "capacity";

    public static final String COL_CLEANING_REMINDERS = "cleaningReminders";
    public static final String COL_CLEANING_FREQUENCY = "cleaningFrequency";
    public static final String COL_LAST_CLEANING_DATE = "lastCleaningDate";
    public static final String COL_CLEANING_INSTRUCTIONS = "cleaningInstructions";

    public static final String COL_STATUS = "status";
    public static final String COL_RESERVATION_ID = "reservationID";

    public static final int Free = 1;
    public static final int Occupied = 2;

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // For database projection so order is consistent
    public static final String[] FIELDS = {
            BaseColumns._ID,
            COL_NAME,
            COL_DESCRIPTION,
            COL_CAPACITY,

            COL_CLEANING_REMINDERS,
            COL_CLEANING_FREQUENCY,
            COL_LAST_CLEANING_DATE,
            COL_CLEANING_INSTRUCTIONS,

            COL_STATUS,
            COL_RESERVATION_ID
    };

    public static final HashMap<String, String> mColumnMap = buildColumnMap();
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * Builds a map for all Room FTS table columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();

        map.put(BaseColumns._ID, BaseColumns._ID);
        map.put(COL_NAME, COL_NAME);
        map.put(COL_DESCRIPTION, COL_DESCRIPTION);
        map.put(COL_CAPACITY, COL_CAPACITY);

        map.put(COL_CLEANING_REMINDERS, COL_CLEANING_REMINDERS);
        map.put(COL_CLEANING_FREQUENCY, COL_CLEANING_FREQUENCY);
        map.put(COL_LAST_CLEANING_DATE, COL_LAST_CLEANING_DATE);
        map.put(COL_CLEANING_INSTRUCTIONS, COL_CLEANING_INSTRUCTIONS);

        map.put(COL_STATUS, COL_STATUS);
        map.put(COL_RESERVATION_ID, COL_RESERVATION_ID);

        return map;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /*
     * The SQL code that creates a Table for storing rooms.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + COL_NAME + " TEXT NOT NULL DEFAULT '',"
                    + COL_DESCRIPTION + " TEXT NOT NULL DEFAULT '',"
                    + COL_CAPACITY + " INTEGER,"

                    + COL_CLEANING_REMINDERS + " INTEGER,"
                    + COL_CLEANING_FREQUENCY + " INTEGER,"
                    + COL_LAST_CLEANING_DATE + " INTEGER,"
                    + COL_CLEANING_INSTRUCTIONS + " TEXT DEFAULT '',"

                    + COL_STATUS + " INTEGER,"
                    + COL_RESERVATION_ID + " INTEGER"

                    + ")";

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Fields corresponding to RoomTable columns
    public long mID = -1;
    public String mName = "";
    public String mDescription = "";

    public long mCapacity = 0;

    public long mCleaningReminders = 0;
    public long mCleaningFrequency = 0;
    public long mCleaningDate = 0;
    public String mCleaningInstructions = "";

    public long mStatus = 0;
    public long mReservationID = -1;

    /**
     * No need to do anything, fields are already set to default values above
     */
    public Room() {
    }

    public String getStatusString() {
        switch ((int)mStatus) {
            case Free:
                return "Free";
            case Occupied:
                return "Occupied";
        }
        return "Unknown";
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * Convert information from the RoomTable into an Room object.
     */
    public void setContentFromCursor(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.mID = cursor.getLong(0);
        this.mName = cursor.getString(1);
        this.mDescription = cursor.getString(2);
        this.mCapacity = cursor.getLong(3);

        this.mCleaningReminders = cursor.getLong(4);
        this.mCleaningFrequency = cursor.getLong(5);
        this.mCleaningDate = cursor.getLong(6);
        this.mCleaningInstructions = cursor.getString(7);

        this.mStatus = cursor.getLong(8);
        this.mReservationID = cursor.getLong(9);
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
        values.put(COL_DESCRIPTION, mDescription);
        values.put(COL_CAPACITY, mCapacity);

        values.put(COL_CLEANING_REMINDERS, mCleaningReminders);
        values.put(COL_CLEANING_FREQUENCY, mCleaningFrequency);
        values.put(COL_LAST_CLEANING_DATE, mCleaningDate);
        values.put(COL_CLEANING_INSTRUCTIONS, mCleaningInstructions);

        values.put(COL_STATUS, mStatus);
        values.put(COL_RESERVATION_ID, mReservationID);

        return values;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * sets the fields from a ContentValues object
     */
    public void setContentFromCV(final ContentValues values) {
        // Note that ID is NOT included here
        mName = values.getAsString(COL_NAME);
        mDescription = values.getAsString(COL_DESCRIPTION);
        mCapacity = values.getAsLong(COL_CAPACITY);

        mCleaningReminders = values.getAsLong(COL_CLEANING_REMINDERS);
        mCleaningFrequency = values.getAsLong(COL_CLEANING_FREQUENCY);
        mCleaningDate = values.getAsLong(COL_LAST_CLEANING_DATE);
        mCleaningInstructions = values.getAsString(COL_CLEANING_INSTRUCTIONS);

        mStatus = values.getAsLong(COL_STATUS);
        mReservationID = values.getAsLong(COL_RESERVATION_ID);
    }

    // Room FTS Table
    public static final String FTS_TABLE_NAME = "FTSRoomTable";
    public static final String COL_FTS_ROOM_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String COL_FTS_ROOM_DESCRIPTION = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String COL_FTS_ROOM_REALID = "realID";

    // For database projection so order is consistent
    public static final String[] FTS_FIELDS = {
            BaseColumns._ID,
            COL_FTS_ROOM_NAME,
            COL_FTS_ROOM_DESCRIPTION,
            COL_FTS_ROOM_REALID
    };

    /* Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, we will use "_id" as an alias for "rowid"
     */
    public static final String CREATE_FTS_TABLE =
            "CREATE VIRTUAL TABLE " + FTS_TABLE_NAME +
                    " USING fts3 (" +
                    COL_FTS_ROOM_NAME + ", " +
                    COL_FTS_ROOM_DESCRIPTION + "," +
                    COL_FTS_ROOM_REALID +
                    ");";

    // Fields corresponding to FTSRoomTable columns
    public String mRowID = "";
    public String mFTSName = "";
    public String mFTSDescription = "";
    public String mFTSRealID = "";

    /**
     * Set information from the FTSRoomTable into an Room object.
     */
    public void setFTSContent(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.mRowID = cursor.getString(0);
        this.mFTSName = cursor.getString(1);
        this.mFTSDescription = cursor.getString(2);
        this.mFTSRealID = cursor.getString(3);
    }

    public static final HashMap<String, String> mFTSColumnMap = buildFTSColumnMap();
    /**
     * Builds a map for all Room FTS table columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildFTSColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(COL_FTS_ROOM_NAME, COL_FTS_ROOM_NAME);
        map.put(COL_FTS_ROOM_DESCRIPTION, COL_FTS_ROOM_DESCRIPTION);
        map.put(COL_FTS_ROOM_REALID, COL_FTS_ROOM_REALID);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }
}
