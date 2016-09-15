package com.navare.prashant.resortmanager.Database;

/**
 * Created by prashant on 18-Apr-15.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.HashMap;

/**
 * A class representation of a row in table "Item".
 */

public class ServiceCall {

    // ServiceCall table
    public static final String TABLE_NAME = "ServiceCallTable";
    // ++++++++++++++++++++++++++++++++++++++++++++++++Add other fields here.
    // When you add a field, make sure you add corresponding entries in the FIELDS array, the CREATE_TABLE string
    // and declare a member and so on. See all ++++++++ comment lines below.
    // These fields can be anything you want.
    public static final String COL_ITEMID = "itemID";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_PRIORITY = "priority";
    public static final String COL_STATUS = "status";
    public static final String COL_OPEN_TIME_STAMP = "openTimeStamp";
    public static final String COL_CLOSED_TIME_STAMP = "closedTimeStamp";
    public static final String COL_ITEM_NAME = "itemName";
    public static final String COL_ITEM_LOCATION = "itemLocation";
    public static final String COL_ROOMID = "roomID";

    // Defines related to service call status
    public static final long OpenStatus = 1;
    public static final long ClosedStatus = 2;


    // For database projection so order is consistent
    public static final String[] FIELDS = {
            BaseColumns._ID,
            COL_ITEMID,
            COL_DESCRIPTION,
            COL_PRIORITY,
            COL_STATUS,
            COL_OPEN_TIME_STAMP,
            COL_CLOSED_TIME_STAMP,
            COL_ITEM_NAME,
            COL_ITEM_LOCATION,
            COL_ROOMID
    };

    public static final HashMap<String, String> mColumnMap = buildColumnMap();
    /**
     * Builds a map for all Item FTS table columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();

        map.put(BaseColumns._ID, BaseColumns._ID);
        map.put(COL_ITEMID, COL_ITEMID);
        map.put(COL_DESCRIPTION, COL_DESCRIPTION);
        map.put(COL_PRIORITY, COL_PRIORITY);
        map.put(COL_STATUS, COL_STATUS);
        map.put(COL_OPEN_TIME_STAMP, COL_OPEN_TIME_STAMP);
        map.put(COL_CLOSED_TIME_STAMP, COL_CLOSED_TIME_STAMP);
        map.put(COL_ITEM_NAME, COL_ITEM_NAME);
        map.put(COL_ITEM_LOCATION, COL_ITEM_LOCATION);

        map.put(COL_ROOMID, COL_ROOMID);
        return map;
    }

    /*
     * The SQL code that creates a Table for storing service calls.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + COL_ITEMID + " INTEGER,"
                    + COL_DESCRIPTION + " TEXT NOT NULL DEFAULT '',"
                    + COL_PRIORITY + " INTEGER, "
                    + COL_STATUS + " INTEGER, "
                    + COL_OPEN_TIME_STAMP + " INTEGER,"
                    + COL_CLOSED_TIME_STAMP + " INTEGER,"
                    + COL_ITEM_NAME + " TEXT NOT NULL DEFAULT '',"
                    + COL_ITEM_LOCATION + " TEXT NOT NULL DEFAULT '', "
                    + COL_ROOMID + " INTEGER "
                    + ")";

    // Fields corresponding to ItemTable columns
    public long mID = -1;
    public long mItemID = -1;
    public String mDescription = "";
    public long mPriority = 0;
    public long mStatus = 0;
    public long mOpenTimeStamp = 0;
    public long mClosedTimeStamp = 0;
    public String mItemName = "";
    public String mItemLocation = "";
    public long mRoomID = -1;

    /**
     * No need to do anything, fields are already set to default values above
     */
    public ServiceCall() {
    }

    /**
     * Convert information from the ItemTable into an Item object.
     */
    public void setContentFromCursor(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.mID = cursor.getLong(0);
        this.mItemID = cursor.getLong(1);
        this.mDescription = cursor.getString(2);
        this.mPriority = cursor.getLong(3);
        this.mStatus = cursor.getLong(4);

        this.mOpenTimeStamp = cursor.getLong(5);
        this.mClosedTimeStamp = cursor.getLong(6);
        this.mItemName = cursor.getString(7);
        this.mItemLocation = cursor.getString(8);
        this.mRoomID = cursor.getLong(9);
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContentValues() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_ITEMID, mItemID);
        values.put(COL_DESCRIPTION, mDescription);
        values.put(COL_PRIORITY, mPriority);
        values.put(COL_STATUS, mStatus);
        values.put(COL_OPEN_TIME_STAMP, mOpenTimeStamp);
        values.put(COL_CLOSED_TIME_STAMP, mClosedTimeStamp);
        values.put(COL_ITEM_NAME, mItemName);
        values.put(COL_ITEM_LOCATION, mItemLocation);
        values.put(COL_ROOMID, mRoomID);

        return values;
    }

    /**
     * sets the fields from a ContentValues object
     */
    public void setContentFromCV(final ContentValues values) {
        // Note that ID is NOT included here
        mItemID = values.getAsLong(COL_ITEMID);
        mDescription = values.getAsString(COL_DESCRIPTION);
        mPriority = values.getAsLong(COL_PRIORITY);
        mStatus = values.getAsLong(COL_STATUS);
        mOpenTimeStamp = values.getAsLong(COL_OPEN_TIME_STAMP);
        mClosedTimeStamp = values.getAsLong(COL_CLOSED_TIME_STAMP);
        mItemName = values.getAsString(COL_ITEM_NAME);
        mItemLocation = values.getAsString(COL_ITEM_LOCATION);
        mRoomID = values.getAsLong(COL_ROOMID);
    }
}
