package com.navare.prashant.resortmanager.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.navare.prashant.resortmanager.ResortManagerApp;
import com.navare.prashant.resortmanager.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ResortManagerDatabase extends SQLiteOpenHelper {
    private static final String TAG = "ResortManagerDatabase";
    public static final String DATABASE_NAME = "ResortManager";
    private static final int DATABASE_VERSION = 1;

    private final Context mHelperContext;

    ResortManagerDatabase(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mHelperContext = context;
    }

    // ++++++++++++++++++++++++++ When new table is added +++++++++++++++++++++++++++++
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Item.CREATE_TABLE);
        db.execSQL(Item.CREATE_FTS_TABLE);
        db.execSQL(ServiceCall.CREATE_TABLE);
        db.execSQL(Task.CREATE_TABLE);
        db.execSQL(Task.CREATE_FTS_TABLE);
        db.execSQL(Task.CREATE_COMPLETED_FTS_TABLE);
        db.execSQL(Room.CREATE_TABLE);
        db.execSQL(Room.CREATE_FTS_TABLE);
        db.execSQL(Reservation.CREATE_TABLE);
        db.execSQL(Reservation.CREATE_FTS_TABLE);
    }

    /**
     * Starts a thread to load the database table with pre-defined inventory
     */
    private void loadInventory() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadItems();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * Starts a thread to load the database table with pre-defined tasks
     */
    private void loadTasksTable() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadTasks();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void loadItems() throws IOException {
        Log.d(TAG, "Loading items...");
        final Resources resources = mHelperContext.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.items);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] strings = TextUtils.split(line, "-");
                if (strings.length < 2) continue;
                Item item = new Item();
                item.mName = strings[0].trim();
                item.mDescription = strings[1].trim();
                String type = strings[2].trim();
                if (type.compareToIgnoreCase("Instrument")== 0)
                    item.mType = Item.InstrumentType;
                else if (type.compareToIgnoreCase("Consumable")==0)
                    item.mType = Item.ConsumableType;
                long newID = addItem(item);
                if (newID == -1) {
                    Log.e(TAG, "unable to add item: " + strings[0].trim());
                }
            }
        } finally {
            reader.close();
        }
        Log.d(TAG, "DONE loading items.");
    }

    private void loadTasks() throws IOException {
        Log.d(TAG, "Loading tasks...");
        final Resources resources = mHelperContext.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.tasks);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] strings = TextUtils.split(line, "-");
                if (strings.length < 7)
                    continue;
                Task task = new Task();
                task.mTaskType = Long.valueOf(strings[0].trim());
                task.mItemID = Long.valueOf(strings[1].trim());
                task.mItemName = strings[2].trim();
                task.mStatus = Long.valueOf(strings[3].trim());
                task.mAssignedTo = strings[4].trim();
                task.mAssignedToContactNumber = strings[5].trim();
                task.mDueDate = Long.valueOf(strings[6].trim());
                task.mPriority = Long.valueOf(strings[7].trim());
                long newID = addTask(task);
                if (newID == -1) {
                    Log.e(TAG, "unable to add task: " + strings[0].trim());
                }
            }
        } finally {
            reader.close();
        }
        Log.d(TAG, "DONE loading tasks.");
    }

    /**
     * Add an item.
     * @return rowId or -1 if failed
     */
    private long addItem(Item item) {
        final SQLiteDatabase db = this.getWritableDatabase();
        long realID = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            realID = db.insert(Item.TABLE_NAME, null, item.getContentValues());
        }
        if (realID > -1) {
            // Also add an entry to the Item FTS table
            ContentValues ftsValues = new ContentValues();
            ftsValues.put(Item.COL_FTS_ITEM_NAME, item.mName);
            ftsValues.put(Item.COL_FTS_ITEM_LOCATION, item.mLocation);
            ftsValues.put(Item.COL_FTS_ITEM_REALID, Long.toString(realID));

            long ftsID = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsID =  db.insert(Item.FTS_TABLE_NAME, null, ftsValues);
            }
            if (ftsID == -1) {
                deleteItem(String.valueOf(realID));
                return ftsID;
            }
            ResortManagerApp.incrementItemCount();
        }
        return realID;
    }

    public int deleteItem(String itemID) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            result = db.delete(Item.TABLE_NAME, BaseColumns._ID + " IS ?", new String[]{itemID});
        }

        if (result > 0) {
            int ftsResult = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsResult = db.delete(Item.FTS_TABLE_NAME, Item.COL_FTS_ITEM_REALID + " MATCH ? ", new String[]{itemID});
            }
            notifyProviderOnItemChange();

            // Lastly, delete all tasks and service calls associated with this item
            deleteAllTasksForItem(itemID);
            deleteAllServiceCallsForItem(itemID);
            ResortManagerApp.decrementItemCount();
            return ftsResult;
        }
        return result;
    }

    private void notifyProviderOnItemChange() {
        mHelperContext.getContentResolver().notifyChange(ResortManagerContentProvider.FTS_ITEM_URI, null, false);
    }

    /**
     * Returns a Cursor positioned at the item specified by id
     *
     * @param rowID id of item to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching item, or null if not found.
     */
    public Cursor getItem(String rowID, String[] columns) {
        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = new String[] {rowID};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE _id = <rowID>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Item.TABLE_NAME);
        builder.setProjectionMap(Item.mColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Returns a Cursor over all FTS items
     *
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getAllFTSItems(String[] columns) {

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Item.FTS_TABLE_NAME);
        builder.setProjectionMap(Item.mFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, null, null, null, null, Item.COL_FTS_ITEM_NAME);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    /**
     * Returns a Cursor over all FTS items that match the given searchString
     *
     * @param searchString The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getFTSItemMatches(String searchString, String[] columns) {
        //String selection = Item.COL_FTS_ITEM_NAME + " MATCH ?";
        String selection = Item.FTS_TABLE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {searchString + "*"};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <COL_FTS_ITEM_NAME> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the item_name column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the InventoryProvider when queries are made.
         * - This can be revised to also search the item description text with FTS3 by changing
         *   the selection clause to use FTS_ITEM_TABLE instead of COL_FTS_ITEM_NAME (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Item.FTS_TABLE_NAME);
        builder.setProjectionMap(Item.mFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, Item.COL_FTS_ITEM_NAME);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public long insertItem(ContentValues values) {
        Item item = new Item();
        item.setContentFromCV(values);
        return addItem(item);
    }

    public int updateItem(String itemId, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int rowsUpdated = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            rowsUpdated = db.update(Item.TABLE_NAME, values, BaseColumns._ID + "=" + itemId, null);
        }
        if (rowsUpdated > 0) {
            ContentValues ftsValues = new ContentValues();
            ftsValues.put(Item.COL_FTS_ITEM_NAME, values.getAsString(Item.COL_NAME));
            ftsValues.put(Item.COL_FTS_ITEM_LOCATION, values.getAsString(Item.COL_LOCATION));

            long ftsRowsUpdated = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsRowsUpdated =  db.update(Item.FTS_TABLE_NAME, ftsValues, Item.COL_FTS_ITEM_REALID + " MATCH " + itemId, null);
            }
        }
        notifyProviderOnItemChange();
        return rowsUpdated;
    }
    private long addRoom(Room room) {
        final SQLiteDatabase db = this.getWritableDatabase();
        long realID = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            realID = db.insert(Room.TABLE_NAME, null, room.getContentValues());
        }
        if (realID > -1) {
            // Also add an entry to the Room FTS table
            ContentValues ftsValues = new ContentValues();
            ftsValues.put(Room.COL_FTS_ROOM_NAME, room.mName);
            ftsValues.put(Room.COL_FTS_ROOM_DESCRIPTION, room.mDescription);
            ftsValues.put(Room.COL_FTS_ROOM_REALID, Long.toString(realID));

            long ftsID = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsID =  db.insert(Room.FTS_TABLE_NAME, null, ftsValues);
            }
            if (ftsID == -1) {
                deleteRoom(String.valueOf(realID));
                return ftsID;
            }
            ResortManagerApp.incrementRoomCount();
        }
        return realID;
    }

    public int deleteRoom(String roomID) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            result = db.delete(Room.TABLE_NAME, BaseColumns._ID + " IS ?", new String[]{roomID});
        }

        if (result > 0) {
            int ftsResult = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsResult = db.delete(Room.FTS_TABLE_NAME, Room.COL_FTS_ROOM_REALID + " MATCH ? ", new String[]{roomID});
            }
            notifyProviderOnRoomChange();

            // Lastly, delete all tasks and service calls associated with this room
            deleteAllTasksForRoom(roomID);
            deleteAllServiceCallsForRoom(roomID);
            ResortManagerApp.decrementRoomCount();
            return ftsResult;
        }
        return result;
    }

    private void notifyProviderOnRoomChange() {
        mHelperContext.getContentResolver().notifyChange(ResortManagerContentProvider.FTS_ROOM_URI, null, false);
    }

    /**
     * Returns a Cursor positioned at the item specified by id
     *
     * @param rowID id of item to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching item, or null if not found.
     */
    public Cursor getRoom(String rowID, String[] columns) {
        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = new String[] {rowID};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE _id = <rowID>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Room.TABLE_NAME);
        builder.setProjectionMap(Room.mColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Returns a Cursor over all FTS items
     *
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getAllFTSRooms(String[] columns) {

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Room.FTS_TABLE_NAME);
        builder.setProjectionMap(Room.mFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, null, null, null, null, Room.COL_FTS_ROOM_NAME);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    /**
     * Returns a Cursor over all FTS items that match the given searchString
     *
     * @param searchString The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getFTSRoomMatches(String searchString, String[] columns) {
        String selection = Room.FTS_TABLE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {searchString + "*"};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <COL_FTS_ITEM_NAME> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the item_name column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the InventoryProvider when queries are made.
         * - This can be revised to also search the item description text with FTS3 by changing
         *   the selection clause to use FTS_ITEM_TABLE instead of COL_FTS_ITEM_NAME (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Room.FTS_TABLE_NAME);
        builder.setProjectionMap(Room.mFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, Room.COL_FTS_ROOM_NAME);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public long insertRoom(ContentValues values) {
        Room room = new Room();
        room.setContentFromCV(values);
        return addRoom(room);
    }

    public int updateRoom(String roomId, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int rowsUpdated = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            rowsUpdated = db.update(Room.TABLE_NAME, values, BaseColumns._ID + "=" + roomId, null);
        }
        if (rowsUpdated > 0) {
            ContentValues ftsValues = new ContentValues();
            ftsValues.put(Room.COL_FTS_ROOM_NAME, values.getAsString(Room.COL_NAME));
            ftsValues.put(Room.COL_FTS_ROOM_DESCRIPTION, values.getAsString(Room.COL_DESCRIPTION));

            long ftsRowsUpdated = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsRowsUpdated =  db.update(Room.FTS_TABLE_NAME, ftsValues, Room.COL_FTS_ROOM_REALID + " MATCH " + roomId, null);
            }
        }
        notifyProviderOnRoomChange();
        return rowsUpdated;
    }

    private long addReservation(Reservation reservation) {
        final SQLiteDatabase db = this.getWritableDatabase();
        long realID = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            realID = db.insert(Reservation.TABLE_NAME, null, reservation.getContentValues());
        }
        if (realID > -1) {
            // Also add an entry to the Reservation FTS table
            ContentValues ftsValues = new ContentValues();
            ftsValues.put(Reservation.COL_FTS_RESERVATION_NAME, reservation.mName);
            ftsValues.put(Reservation.COL_FTS_RESERVATION_DATES, reservation.getDatesString());
            ftsValues.put(Reservation.COL_FTS_RESERVATION_REALID, Long.toString(realID));
            ftsValues.put(Reservation.COL_FTS_RESERVATION_STATUS, reservation.getStatusString());

            long ftsID = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsID =  db.insert(Reservation.FTS_TABLE_NAME, null, ftsValues);
            }
            if (ftsID == -1) {
                deleteReservation(String.valueOf(realID));
                return ftsID;
            }
            ResortManagerApp.incrementReservationCount();
        }
        return realID;
    }

    public int deleteReservation(String reservationID) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            result = db.delete(Reservation.TABLE_NAME, BaseColumns._ID + " IS ?", new String[]{reservationID});
        }

        if (result > 0) {
            int ftsResult = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsResult = db.delete(Reservation.FTS_TABLE_NAME, Reservation.COL_FTS_RESERVATION_REALID + " MATCH ? ", new String[]{reservationID});
            }
            notifyProviderOnReservationChange();

            ResortManagerApp.decrementReservationCount();
            return ftsResult;
        }
        return result;
    }

    private void notifyProviderOnReservationChange() {
        mHelperContext.getContentResolver().notifyChange(ResortManagerContentProvider.FTS_RESERVATION_URI, null, false);
    }

    /**
     * Returns a Cursor positioned at the item specified by id
     *
     * @param rowID id of item to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching item, or null if not found.
     */
    public Cursor getReservation(String rowID, String[] columns) {
        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = new String[] {rowID};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE _id = <rowID>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Reservation.TABLE_NAME);
        builder.setProjectionMap(Reservation.mColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public Cursor getReservationRooms(String reservationID, String[] columns) {
        String selection;
        String [] selectionArgs;
        if (reservationID != null) {
            selection = Room.COL_RESERVATION_ID + " = ? OR " + Room.COL_STATUS + " != " + String.valueOf(Room.Occupied);
            selectionArgs = new String[] {reservationID};
        }
        else {
            selection = Room.COL_STATUS + " != " + String.valueOf(Room.Occupied);
            selectionArgs = null;
        }

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Room.TABLE_NAME);
        builder.setProjectionMap(Room.mColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Returns a Cursor over all FTS Reservation
     *
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getAllFTSReservations(String[] columns) {

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Reservation.FTS_TABLE_NAME);
        builder.setProjectionMap(Reservation.mFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, null, null, null, null, Reservation.COL_FTS_RESERVATION_NAME);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    /**
     * Returns a Cursor over all FTS items that match the given searchString
     *
     * @param searchString The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getFTSReservationMatches(String searchString, String[] columns) {
        String selection = Reservation.FTS_TABLE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {searchString + "*"};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <COL_FTS_ITEM_NAME> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the item_name column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the InventoryProvider when queries are made.
         * - This can be revised to also search the item description text with FTS3 by changing
         *   the selection clause to use FTS_ITEM_TABLE instead of COL_FTS_ITEM_NAME (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Reservation.FTS_TABLE_NAME);
        builder.setProjectionMap(Reservation.mFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, Reservation.COL_FTS_RESERVATION_NAME);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public long insertReservation(ContentValues values) {
        Reservation reservation = new Reservation();
        reservation.setContentFromCV(values);
        return addReservation(reservation);
    }

    public int updateReservation(String reservationId, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int rowsUpdated = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            rowsUpdated = db.update(Reservation.TABLE_NAME, values, BaseColumns._ID + "=" + reservationId, null);
        }
        if (rowsUpdated > 0) {
            Reservation reservation = new Reservation();
            reservation.setContentFromCV(values);
            ContentValues ftsValues = new ContentValues();
            ftsValues.put(Reservation.COL_FTS_RESERVATION_NAME, reservation.mName);
            ftsValues.put(Reservation.COL_FTS_RESERVATION_DATES, reservation.getDatesString());
            ftsValues.put(Reservation.COL_FTS_RESERVATION_STATUS, reservation.getStatusString());

            long ftsRowsUpdated = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsRowsUpdated =  db.update(Reservation.FTS_TABLE_NAME, ftsValues, Reservation.COL_FTS_RESERVATION_REALID + " MATCH " + reservationId, null);
            }
        }
        notifyProviderOnRoomChange();
        return rowsUpdated;
    }

    public long insertTask(ContentValues values) {
        Task task = new Task();
        task.setContentFromCV(values);
        return addTask(task);
    }

    /**
     * Add a task.
     * @return rowId or -1 if failed
     */
    private long addTask(Task task) {
        final SQLiteDatabase db = this.getWritableDatabase();
        long realID = -1;
        synchronized (ResortManagerApp.sDatabaseLock) {
            realID = db.insert(Task.TABLE_NAME, null, task.getContentValues());
        }
        if (realID > -1) {
            // Also add an entry to the Task FTS table
            ContentValues ftsValues = new ContentValues();
            ftsValues.put(Task.COL_FTS_ITEM_NAME, task.mItemName);
            ftsValues.put(Task.COL_FTS_ITEM_LOCATION, task.mItemLocation);
            ftsValues.put(Task.COL_FTS_TASK_TYPE, task.getTaskTypeString());
            ftsValues.put(Task.COL_FTS_ASSIGNED_TO, task.mAssignedTo);

            if (task.mDueDate > 0) {
                Date dueDate = new Date();
                dueDate.setTime(task.mDueDate);

                SimpleDateFormat dueDateFormat = new SimpleDateFormat("dd MMM, yyyy");
                String dueDateString = dueDateFormat.format(dueDate);
                ftsValues.put(Task.COL_FTS_DUE_DATE, dueDateString);
            }
            ftsValues.put(Task.COL_FTS_TASK_REALID, Long.toString(realID));
            ftsValues.put(Task.COL_FTS_TASK_PRIORITY, task.getTaskPriority());
            if (task.mRoomID >= 0)
                ftsValues.put(Task.COL_FTS_ITEM_TYPE, "Room");
            else
                ftsValues.put(Task.COL_FTS_ITEM_TYPE, "Item");


            long ftsID = -1;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsID =  db.insert(Task.FTS_TABLE_NAME, null, ftsValues);
                if (ftsID == -1) {
                    deleteTask(String.valueOf(realID));
                    return ftsID;
                }
                ResortManagerApp.incrementTaskCount();
            }
            notifyProviderOnTaskChange();
        }
        return realID;
    }

    public int deleteTask(String taskID) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            result = db.delete(Task.TABLE_NAME, BaseColumns._ID + " IS ?", new String[]{taskID});
        }

        if (result > 0) {
            int ftsResult = 0;
            synchronized (ResortManagerApp.sDatabaseLock) {
                ftsResult = db.delete(Task.FTS_TABLE_NAME,
                        Task.COL_FTS_TASK_REALID + " MATCH ? ", new String[]{taskID});
                if (ftsResult > 0) {
                    ResortManagerApp.decrementTaskCount();
                    notifyProviderOnTaskChange();
                }
            }
            return ftsResult;
        }
        return result;
    }

    private void deleteAllTasksForItem(String itemID)
    {
        synchronized (ResortManagerApp.sDatabaseLock) {
            Cursor taskCursor = null;
            SQLiteQueryBuilder taskbuilder = new SQLiteQueryBuilder();
            String taskSelection = Task.COL_ITEM_ID + " = ?";
            String[] taskSelectionArgs = new String[] {itemID};

            taskbuilder.setTables(Task.TABLE_NAME);
            taskCursor = taskbuilder.query(this.getReadableDatabase(),
                    Task.FIELDS, taskSelection, taskSelectionArgs, null, null, null);

            if (taskCursor != null) {
                Task currentTask = new Task();
                for (taskCursor.moveToFirst(); !taskCursor.isAfterLast(); taskCursor.moveToNext()) {
                    currentTask.setContentFromCursor(taskCursor);
                    deleteTask(String.valueOf(currentTask.mID));
                }
            }
        }
    }

    private void deleteAllTasksForRoom(String roomID)
    {
        synchronized (ResortManagerApp.sDatabaseLock) {
            Cursor taskCursor = null;
            SQLiteQueryBuilder taskbuilder = new SQLiteQueryBuilder();

            String taskSelection = Task.COL_ROOM_ID + " = ?";
            String[] taskSelectionArgs = new String[] {roomID};

            taskbuilder.setTables(Task.TABLE_NAME);
            taskCursor = taskbuilder.query(this.getReadableDatabase(),
                    Task.FIELDS, taskSelection, taskSelectionArgs, null, null, null);

            if (taskCursor != null) {
                Task currentTask = new Task();
                for (taskCursor.moveToFirst(); !taskCursor.isAfterLast(); taskCursor.moveToNext()) {
                    currentTask.setContentFromCursor(taskCursor);
                    deleteTask(String.valueOf(currentTask.mID));
                }
            }
        }
    }

    /**
     * Returns a Cursor positioned at the task specified by id
     *
     * @param rowID id of task to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching item, or null if not found.
     */
    public Cursor getTask(String rowID, String[] columns) {
        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = new String[] {rowID};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE _id = <rowID>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Task.TABLE_NAME);
        builder.setProjectionMap(Task.mColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Returns a Cursor over all FTS tasks
     *
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getAllFTSTasks(String[] columns) {

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Task.FTS_TABLE_NAME);
        builder.setProjectionMap(Task.mFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, null, null, null, null, Item.COL_FTS_ITEM_NAME);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    /**
     * Returns a Cursor over all FTS tasks that match the given searchString
     *
     * @param searchString The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getFTSTaskMatches(String searchString, String[] columns) {
        String selection = Task.FTS_TABLE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {searchString + "*"};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <COL_FTS_ITEM_NAME> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the item_name column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the InventoryProvider when queries are made.
         * - This can be revised to also search the item description text with FTS3 by changing
         *   the selection clause to use FTS_ITEM_TABLE instead of COL_FTS_ITEM_NAME (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Task.FTS_TABLE_NAME);
        builder.setProjectionMap(Task.mFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(),
                    columns, selection, selectionArgs, null, null, Task.COL_FTS_ITEM_NAME);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Returns a Cursor over all Completed FTS tasks that match the given itemID and searchString
     *
     * @param searchString The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all items that match, or null if none found.
     */
    public Cursor getCompletedFTSTaskMatches(String itemID, String searchString, String[] columns) {

        String selectionArgsString;
        String selection = Task.COMPLETED_FTS_TABLE_NAME + " MATCH ?";
        if (!searchString.isEmpty()) {
            selectionArgsString = Task.SPECIAL_ITEM_ID_STRING_FOR_COMPLETED_TASKS + itemID + "  " + searchString + "*";
        }
        else {
            selectionArgsString = Task.SPECIAL_ITEM_ID_STRING_FOR_COMPLETED_TASKS + itemID ;
        }
        String[] selectionArgs = new String[] {selectionArgsString};

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Task.COMPLETED_FTS_TABLE_NAME);
        builder.setProjectionMap(Task.mCompletedFTSColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(),
                    columns, selection, selectionArgs, null, null, Task.COMPLETED_COL_FTS_COMPLETION_TIMESTAMP + " DESC ");
        }

        int numRows = cursor.getCount();
        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public int updateTask(String taskId, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int rowsUpdated = 0;
        Task task = new Task();
        task.setContentFromCV(values);
        synchronized (ResortManagerApp.sDatabaseLock) {
            if (task.mStatus == Task.CompletedStatus) {
                addCompletedFTSTask(task);
                deleteTask(taskId);
            }
            else {
                rowsUpdated = db.update(Task.TABLE_NAME, values, BaseColumns._ID + "=" + taskId, null);
                ContentValues ftsValues = new ContentValues();
                ftsValues.put(Task.COL_FTS_ASSIGNED_TO, values.getAsString(Task.COL_ASSIGNED_TO));
                ftsValues.put(Task.COL_FTS_TASK_PRIORITY, task.getTaskPriority());
                ftsValues.put(Task.COL_FTS_DUE_DATE, task.getTaskDueDateString());
                long ftsRowsUpdated =  db.update(Task.FTS_TABLE_NAME, ftsValues, Task.COL_FTS_TASK_REALID + " MATCH " + taskId, null);
            }
        }
        notifyProviderOnTaskChange();
        return rowsUpdated;
    }

    private void addCompletedFTSTask(Task task) {
        ContentValues completedFTSValues = new ContentValues();
        completedFTSValues.put(Task.COMPLETED_COL_FTS_TASK_TYPE, task.getTaskTypeString());
        completedFTSValues.put(Task.COMPLETED_COL_FTS_ASSIGNED_TO, task.mAssignedTo);

        Date completedDate = new Date();
        SimpleDateFormat completedDateFormat = new SimpleDateFormat("dd MMM, yyyy");
        String dueDateString = completedDateFormat.format(completedDate);
        completedFTSValues.put(Task.COMPLETED_COL_FTS_COMPLETION_DATE, dueDateString);

        completedFTSValues.put(Task.COMPLETED_COL_FTS_ITEM_ID, Task.SPECIAL_ITEM_ID_STRING_FOR_COMPLETED_TASKS + String.valueOf(task.mItemID));
        completedFTSValues.put(Task.COMPLETED_COL_FTS_TASK_PRIORITY, task.getTaskPriority());
        completedFTSValues.put(Task.COMPLETED_COL_FTS_COMPLETION_COMMENTS, task.mCompletionComments);

        long completedDateTimeStamp = Calendar.getInstance().getTimeInMillis();
        completedFTSValues.put(Task.COMPLETED_COL_FTS_COMPLETION_TIMESTAMP, String.valueOf(completedDateTimeStamp));

        getWritableDatabase().insert(Task.COMPLETED_FTS_TABLE_NAME, null, completedFTSValues);
    }

    private void notifyProviderOnTaskChange() {
        mHelperContext.getContentResolver().notifyChange(
                ResortManagerContentProvider.FTS_TASK_URI, null, false);
    }

    public long insertServiceCall(ContentValues values) {
        final SQLiteDatabase db = this.getWritableDatabase();
        long realID = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            realID = db.insert(ServiceCall.TABLE_NAME, null, values);
        }
        return realID;
    }

    public Cursor getServiceCall(String rowID, String[] columns) {
        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = new String[] {rowID};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE _id = <rowID>
         */
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(ServiceCall.TABLE_NAME);
        builder.setProjectionMap(ServiceCall.mColumnMap);

        Cursor cursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            cursor = builder.query(this.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);
        }

        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public int updateServiceCall(String serviceCallId, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int rowsUpdated = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            rowsUpdated = db.update(ServiceCall.TABLE_NAME, values, BaseColumns._ID + "=" + serviceCallId, null);
        }
        notifyProviderOnTaskChange();
        return rowsUpdated;
    }

    private void deleteAllServiceCallsForItem(String itemID) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            result = db.delete(ServiceCall.TABLE_NAME, ServiceCall.COL_ITEMID + " IS ?", new String[]{itemID});
        }
    }

    private void deleteAllServiceCallsForRoom(String roomID) {
        final SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        synchronized (ResortManagerApp.sDatabaseLock) {
            result = db.delete(ServiceCall.TABLE_NAME, ServiceCall.COL_ROOMID + " IS ?", new String[]{roomID});
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: implement upgradeDB
    }

    public void computeNewTasks() {

        Map<Pair<Long, Long>, Task> taskMap = new HashMap<Pair<Long, Long>, Task>();

        // First get all the items to see which ones have pending tasks.
        SQLiteQueryBuilder itemBuilder = new SQLiteQueryBuilder();
        itemBuilder.setTables(Item.TABLE_NAME);
        Cursor itemCursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            itemCursor = itemBuilder.query(this.getReadableDatabase(), Item.FIELDS, null, null, null, null, null);
        }
        if (itemCursor == null)
            return;

        Item item = new Item();
        for (itemCursor.moveToFirst(); !itemCursor.isAfterLast(); itemCursor.moveToNext()) {
            item.setContentFromCursor(itemCursor);
            if (item.mType == Item.InstrumentType) {
                if (item.mContractReminders > 0) {
                    boolean bCreateTask = false;
                    long dueDate = 0;
                    long priority = Task.NormalPriority;
                    if (item.mContractValidTillDate > 0) {
                        Calendar todayDate = Calendar.getInstance();
                        if (todayDate.getTimeInMillis() > item.mContractValidTillDate ) {
                            // Contract has already expired.
                            bCreateTask = true;
                            dueDate = item.mContractValidTillDate;
                            priority = Task.UrgentPriority;
                        }
                        else {
                            Calendar contractValidTillDate = Calendar.getInstance();
                            contractValidTillDate.setTimeInMillis(item.mContractValidTillDate);
                            // Contract reminders should be given 1 month before the actual contract end, unless the user has set the conract reminder interval.
                            long numberOfDaysbeforeExpiry = TimeUnit.DAYS.convert((contractValidTillDate.getTimeInMillis() - todayDate.getTimeInMillis()), TimeUnit.MILLISECONDS);
                            if (numberOfDaysbeforeExpiry <= 30) {
                                bCreateTask = true;
                                dueDate = item.mContractValidTillDate;
                                // If there are less than 7 days left, then bump up the priority to urgent
                                if (numberOfDaysbeforeExpiry <= 7) {
                                    priority = Task.UrgentPriority;
                                }
                            }
                        }
                    }
                    else {
                        // Contract valid date has not been set. The due date for the task should be today.
                        bCreateTask = true;
                        dueDate = Calendar.getInstance().getTimeInMillis();
                    }
                    if (bCreateTask) {
                        Task newTask = createItemTempTask(item, Task.Contract, dueDate, priority);
                        taskMap.put(Pair.create(item.mID, Long.valueOf(Task.Contract)), newTask);
                    }
                }
                if (item.mMaintenanceReminders > 0) {
                    if (item.mMaintenanceFrequency > 0) {
                        boolean bCreateTask = false;
                        long dueDate = 0;
                        long priority = Task.NormalPriority;
                        // Check the last Maintenance date.
                        if (item.mMaintenanceDate > 0) {
                            // If it is set, then see when the next Maintenance is due.
                            Calendar todayDate = Calendar.getInstance();
                            Calendar nextMaintenanceDate = Calendar.getInstance();
                            // If the item's maintenance date is already set in the future, use it.
                            if (todayDate.getTimeInMillis() < item.mMaintenanceDate) {
                                nextMaintenanceDate.setTimeInMillis(item.mMaintenanceDate);
                            }
                            else {
                                // Calculate the next maintenance date based on the maintenance frequency
                                nextMaintenanceDate.setTimeInMillis(item.mMaintenanceDate + TimeUnit.MILLISECONDS.convert(item.mMaintenanceFrequency, TimeUnit.DAYS));
                            }
                            if (todayDate.compareTo(nextMaintenanceDate) >= 0) {
                                // Maintenance is overdue
                                bCreateTask = true;
                                dueDate = nextMaintenanceDate.getTimeInMillis();
                                priority = Task.UrgentPriority;
                            }
                            else {
                                // Maintenance reminders are generated 1 week before the actual due date.
                                long numberOfDaysTillNextMaintenance = TimeUnit.DAYS.convert((nextMaintenanceDate.getTimeInMillis() - todayDate.getTimeInMillis()), TimeUnit.MILLISECONDS);
                                if (numberOfDaysTillNextMaintenance < 7) {
                                    bCreateTask = true;
                                    dueDate = nextMaintenanceDate.getTimeInMillis();
                                }
                            }
                        }
                        else {
                            // If it is not set, this instrument needs Maintenance with a due date of today.
                            bCreateTask = true;
                            dueDate = Calendar.getInstance().getTimeInMillis();
                        }
                        if (bCreateTask) {
                            Task newTask = createItemTempTask(item, Task.Maintenance, dueDate, priority);
                            taskMap.put(Pair.create(item.mID, Long.valueOf(Task.Maintenance)), newTask);
                        }
                    }
                }
            }
            else if (item.mType == Item.ConsumableType) {
                if (item.mInventoryReminders > 0) {
                    if (item.mCurrentQuantity < item.mMinRequiredQuantity) {
                        // There is no due date for Inventory tasks
                        Task newTask = createItemTempTask(item, Task.Inventory, 0, Task.NormalPriority);
                        taskMap.put(Pair.create(item.mID, Long.valueOf(Task.Inventory)), newTask);
                    }
                }
            }
        }

        // Next, get all the rooms to see which ones have pending cleaning tasks.
        SQLiteQueryBuilder roomBuilder = new SQLiteQueryBuilder();
        roomBuilder.setTables(Room.TABLE_NAME);
        Cursor roomCursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            roomCursor = roomBuilder.query(this.getReadableDatabase(), Room.FIELDS, null, null, null, null, null);
        }
        if (roomCursor == null)
            return;

        Room room = new Room();
        for (roomCursor.moveToFirst(); !roomCursor.isAfterLast(); roomCursor.moveToNext()) {
            room.setContentFromCursor(roomCursor);
            if (room.mCleaningReminders > 0) {
                if (room.mCleaningFrequency > 0) {
                    boolean bCreateTask = false;
                    long dueDate = 0;
                    long priority = Task.NormalPriority;
                    // Check the last cleaning date.
                    if (room.mCleaningDate > 0) {
                        // If it is set, then see when the next cleaning is due.
                        Calendar todayDate = Calendar.getInstance();
                        Calendar nextCleaningDate = Calendar.getInstance();
                        // If the room's cleaning date is already set in the future, use it.
                        if (todayDate.getTimeInMillis() < room.mCleaningDate) {
                            nextCleaningDate.setTimeInMillis(room.mCleaningDate);
                        }
                        else {
                            // Calculate the next cleaning date based on the calibration frequency
                            nextCleaningDate.setTimeInMillis(room.mCleaningDate + TimeUnit.MILLISECONDS.convert(room.mCleaningFrequency, TimeUnit.DAYS));
                        }
                        if (todayDate.compareTo(nextCleaningDate) >= 0) {
                            // Cleaning is overdue
                            bCreateTask = true;
                            dueDate = nextCleaningDate.getTimeInMillis();
                            priority = Task.UrgentPriority;
                        }
                        else {
                            // Cleaning reminders are generated 1 week before the actual due date.
                            long numberOfDaysTillNextCleaning = TimeUnit.DAYS.convert((nextCleaningDate.getTimeInMillis() - todayDate.getTimeInMillis()), TimeUnit.MILLISECONDS);
                            if (numberOfDaysTillNextCleaning < 7) {
                                bCreateTask = true;
                                dueDate = nextCleaningDate.getTimeInMillis();
                            }
                        }
                    }
                    else {
                        // If it is not set, this instrument needs calibration with a due date of today.
                        bCreateTask = true;
                        dueDate = Calendar.getInstance().getTimeInMillis();
                    }
                    if (bCreateTask) {
                        Task newTask = createRoomTempTask(room, Task.Cleaning, dueDate, priority);
                        taskMap.put(Pair.create(room.mID, Long.valueOf(Task.Cleaning)), newTask);
                    }
                }
            }
        }

        // Next get all the current open service calls
        Map<Long, ServiceCall> serviceCallMap = new HashMap<Long, ServiceCall>();

        String serviceCallSelection = ServiceCall.COL_STATUS + " = ?";
        String[] serviceCallSelectionArgs = new String[] {String.valueOf(ServiceCall.OpenStatus)};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <ServiceCallTable> WHERE status = 1
         */
        SQLiteQueryBuilder serviceCallbuilder = new SQLiteQueryBuilder();
        serviceCallbuilder.setTables(ServiceCall.TABLE_NAME);

        Cursor serviceCallCursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            serviceCallCursor = serviceCallbuilder.query(this.getReadableDatabase(),
                    ServiceCall.FIELDS, serviceCallSelection, serviceCallSelectionArgs, null, null, null);
        }

        if (serviceCallCursor != null) {
            for (serviceCallCursor.moveToFirst(); !serviceCallCursor.isAfterLast(); serviceCallCursor.moveToNext()) {
                ServiceCall serviceCall = new ServiceCall();
                serviceCall.setContentFromCursor(serviceCallCursor);
                serviceCallMap.put(serviceCall.mID, serviceCall);
            }
        }

        // Lastly, iterate through all the current open tasks in the database and eliminate all the temp tasks or service calls that we
        // have collected above if the corresponding task already exists in the database.
        String taskSelection = Task.COL_STATUS + " = ?";
        String[] taskSelectionArgs = new String[] {String.valueOf(Task.OpenStatus)};

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <TaskTable> WHERE status = 1
         */
        SQLiteQueryBuilder taskbuilder = new SQLiteQueryBuilder();
        taskbuilder.setTables(Task.TABLE_NAME);

        Cursor taskCursor = null;
        synchronized (ResortManagerApp.sDatabaseLock) {
            taskCursor = taskbuilder.query(this.getReadableDatabase(),
                    Task.FIELDS, taskSelection, taskSelectionArgs, null, null, null);
        }

        if (taskCursor != null) {
            Task currentOpenTask = new Task();
            for (taskCursor.moveToFirst(); !taskCursor.isAfterLast(); taskCursor.moveToNext()) {
                currentOpenTask.setContentFromCursor(taskCursor);
                if (currentOpenTask.mTaskType == Task.ServiceCall) {
                    serviceCallMap.remove(currentOpenTask.mServiceCallID);
                }
                else if (currentOpenTask.mTaskType == Task.Cleaning) {
                    taskMap.remove(Pair.create(currentOpenTask.mRoomID, currentOpenTask.mTaskType));
                }
                else {
                    taskMap.remove(Pair.create(currentOpenTask.mItemID, currentOpenTask.mTaskType));
                }
            }
        }

        // At this point, what is left in the taskMap and the serviceCallMap are all new tasks that need to be added to the db.
        for (Task task : taskMap.values()) {
            addTask(task);
        }
        for (ServiceCall serviceCall : serviceCallMap.values()) {
            Task serviceCallTask = createServiceCallTempTask(serviceCall);
            addTask(serviceCallTask);
        }
    }

    private Task createItemTempTask(Item item, int taskType, long dueDate, long priority) {
        Task task = new Task();
        task.mTaskType = taskType;
        task.mItemID = item.mID;
        task.mItemName = item.mName;
        task.mItemLocation = item.mLocation;
        task.mDueDate = dueDate;
        task.mStatus = Task.OpenStatus;
        task.mPriority = priority;
        return task;
    }

    private Task createRoomTempTask(Room room, int taskType, long dueDate, long priority) {
        Task task = new Task();
        task.mTaskType = taskType;
        task.mRoomID = room.mID;
        task.mItemName = room.mName;
        task.mItemLocation = room.mDescription;
        task.mDueDate = dueDate;
        task.mStatus = Task.OpenStatus;
        task.mPriority = priority;
        return task;
    }

    private Task createServiceCallTempTask(ServiceCall serviceCall) {
        Task task = new Task();
        task.mTaskType = Task.ServiceCall;
        task.mItemID = serviceCall.mItemID;
        task.mServiceCallID = serviceCall.mID;
        task.mItemName = serviceCall.mItemName;
        task.mItemLocation = serviceCall.mItemLocation;
        // No due date for service call tasks.
        task.mDueDate = 0;
        task.mStatus = Task.OpenStatus;
        task.mPriority = serviceCall.mPriority;
        return task;
    }
}
