package com.navare.prashant.resortmanager.Database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ResortManagerContentProvider extends ContentProvider {

    private static String SCHEME = "content://";
    private static String PROVIDER_NAME = "com.navare.prashant.ResortManager.provider";

    // FTS Items related
    private static final String FTS_ITEMS_SUB_SCHEME = "/fts_items";
    private static final String FTS_ITEM_URL = SCHEME + PROVIDER_NAME + FTS_ITEMS_SUB_SCHEME;
    public static final Uri FTS_ITEM_URI = Uri.parse(FTS_ITEM_URL);
    // UriMatcher stuff
    private static final int SEARCH_FTS_ITEMS = 1;
    private static final int SEARCH_SUGGEST_ITEMS = 2;

    // Actual ItemTable related
    // get_item related
    private static final String ITEM_SUB_SCHEME = "/item";
    private static final String ITEM_URL = SCHEME + PROVIDER_NAME + ITEM_SUB_SCHEME;
    public static final Uri ITEM_URI = Uri.parse(ITEM_URL);
    // UriMatcher stuff
    private static final int ITEMS = 3;
    private static final int ITEM_ID = 4;

    // ServiceCall Table related
    private static final String SERVICE_CALL_SUB_SCHEME = "/service_call";
    private static final String SERVICE_CALL_URL = SCHEME + PROVIDER_NAME + SERVICE_CALL_SUB_SCHEME;
    public static final Uri SERVICE_CALL_URI = Uri.parse(SERVICE_CALL_URL);
    // UriMatcher stuff
    private static final int SERVICE_CALLS = 5;
    private static final int SERVICE_CALL_ID = 6;

    // FTS Tasks related
    private static final String FTS_TASKS_SUB_SCHEME = "/fts_tasks";
    private static final String FTS_TASK_URL = SCHEME + PROVIDER_NAME + FTS_TASKS_SUB_SCHEME;
    public static final Uri FTS_TASK_URI = Uri.parse(FTS_TASK_URL);
    // UriMatcher stuff
    private static final int SEARCH_FTS_TASKS = 7;
    private static final int SEARCH_SUGGEST_TASKS = 8;

    // Actual TaskTable related
    // get_task related
    private static final String TASK_SUB_SCHEME = "/task";
    private static final String TASK_URL = SCHEME + PROVIDER_NAME + TASK_SUB_SCHEME;
    public static final Uri TASK_URI = Uri.parse(TASK_URL);
    // UriMatcher stuff
    private static final int TASKS = 9;
    private static final int TASK_ID = 10;

    // computeNewTasks related
    private static final String COMPUTE_NEW_TASKS_SUB_SCHEME = "/computeNewTasks";
    private static final String COMPUTE_NEW_TASKS_URL = SCHEME + PROVIDER_NAME + TASK_SUB_SCHEME;
    public static final Uri COMPUTE_NEW_TASKS_URI = Uri.parse(COMPUTE_NEW_TASKS_URL);
    // UriMatcher stuff
    private static final int COMPUTE_NEW_TASKS = 11;

    // FTS Rooms related
    private static final String FTS_ROOMS_SUB_SCHEME = "/fts_rooms";
    private static final String FTS_ROOM_URL = SCHEME + PROVIDER_NAME + FTS_ROOMS_SUB_SCHEME;
    public static final Uri FTS_ROOM_URI = Uri.parse(FTS_ROOM_URL);
    // UriMatcher stuff
    private static final int SEARCH_FTS_ROOMS = 13;
    private static final int SEARCH_SUGGEST_ROOMS = 14;

    // Actual RoomTable related
    // get_room related
    private static final String ROOM_SUB_SCHEME = "/room";
    private static final String ROOM_URL = SCHEME + PROVIDER_NAME + ROOM_SUB_SCHEME;
    public static final Uri ROOM_URI = Uri.parse(ROOM_URL);
    // UriMatcher stuff
    private static final int ROOMS = 15;
    private static final int ROOM_ID = 16;

    // FTS Reservations related
    private static final String FTS_RESERVATIONS_SUB_SCHEME = "/fts_reservations";
    private static final String FTS_RESERVATION_URL = SCHEME + PROVIDER_NAME + FTS_RESERVATIONS_SUB_SCHEME;
    public static final Uri FTS_RESERVATION_URI = Uri.parse(FTS_RESERVATION_URL);
    // UriMatcher stuff
    private static final int SEARCH_FTS_RESERVATIONS = 17;
    private static final int SEARCH_SUGGEST_RESERVATIONS = 18;

    // Actual ReservationTable related
    // get_reservation related
    private static final String RESERVATION_SUB_SCHEME = "/reservation";
    private static final String RESERVATION_URL = SCHEME + PROVIDER_NAME + RESERVATION_SUB_SCHEME;
    public static final Uri RESERVATION_URI = Uri.parse(RESERVATION_URL);
    private static final String RESERVATION_ROOMS_SUB_SCHEME = "/reservationRooms";
    private static final String RESERVATION_ROOMS_URL = SCHEME + PROVIDER_NAME + RESERVATION_ROOMS_SUB_SCHEME;
    public static final Uri RESERVATION_ROOMS_URI = Uri.parse(RESERVATION_ROOMS_URL);
    // UriMatcher stuff
    private static final int RESERVATIONS = 19;
    private static final int RESERVATION_ID = 20;
    private static final int RESERVATION_ROOMS_ID = 21;

    // Completed FTS Reservations related
    private static final String FTS_COMPLETED_RESERVATIONS_SUB_SCHEME = "/completed_fts_reservations";
    private static final String FTS_COMPLETED_RESERVATION_URL = SCHEME + PROVIDER_NAME + FTS_COMPLETED_RESERVATIONS_SUB_SCHEME;
    public static final Uri FTS_COMPLETED_RESERVATION_URI = Uri.parse(FTS_COMPLETED_RESERVATION_URL);
    // UriMatcher stuff
    private static final int SEARCH_FTS_COMPLETED_RESERVATIONS = 22;
    private static final int COMPLETED_RESERVATION_ID = 23;

    private static final UriMatcher mURIMatcher = buildUriMatcher();

    /**
     * Builds up a UriMatcher for various queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);

        // to get FTS items...
        matcher.addURI(PROVIDER_NAME, FTS_ITEMS_SUB_SCHEME, SEARCH_FTS_ITEMS);

        // for item
        matcher.addURI(PROVIDER_NAME, ITEM_SUB_SCHEME , ITEMS);
        matcher.addURI(PROVIDER_NAME, ITEM_SUB_SCHEME + "/#", ITEM_ID);

        // for serviceCall
        matcher.addURI(PROVIDER_NAME, SERVICE_CALL_SUB_SCHEME , SERVICE_CALLS);
        matcher.addURI(PROVIDER_NAME, SERVICE_CALL_SUB_SCHEME + "/#", SERVICE_CALL_ID);

        // to get FTS tasks...
        matcher.addURI(PROVIDER_NAME, FTS_TASKS_SUB_SCHEME, SEARCH_FTS_TASKS);

        // for task
        matcher.addURI(PROVIDER_NAME, TASK_SUB_SCHEME , TASKS);
        matcher.addURI(PROVIDER_NAME, TASK_SUB_SCHEME + "/#", TASK_ID);

        // for compute new tasks
        matcher.addURI(PROVIDER_NAME, COMPUTE_NEW_TASKS_SUB_SCHEME , COMPUTE_NEW_TASKS);

        // to get FTS rooms...
        matcher.addURI(PROVIDER_NAME, FTS_ROOMS_SUB_SCHEME, SEARCH_FTS_ROOMS);

        // for room
        matcher.addURI(PROVIDER_NAME, ROOM_SUB_SCHEME , ROOMS);
        matcher.addURI(PROVIDER_NAME, ROOM_SUB_SCHEME + "/#", ROOM_ID);

        // to get FTS reservations...
        matcher.addURI(PROVIDER_NAME, FTS_RESERVATIONS_SUB_SCHEME, SEARCH_FTS_RESERVATIONS);

        // for reservation
        matcher.addURI(PROVIDER_NAME, RESERVATION_SUB_SCHEME , RESERVATIONS);
        matcher.addURI(PROVIDER_NAME, RESERVATION_SUB_SCHEME + "/#", RESERVATION_ID);

        // for reservation rooms
        matcher.addURI(PROVIDER_NAME, RESERVATION_ROOMS_SUB_SCHEME, RESERVATION_ROOMS_ID);

        // to get FTS completed reservations...
        matcher.addURI(PROVIDER_NAME, FTS_COMPLETED_RESERVATIONS_SUB_SCHEME, SEARCH_FTS_COMPLETED_RESERVATIONS);
        matcher.addURI(PROVIDER_NAME, FTS_COMPLETED_RESERVATIONS_SUB_SCHEME + "/#", COMPLETED_RESERVATION_ID);

        // to get suggestions...
        matcher.addURI(PROVIDER_NAME, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST_ITEMS);
        matcher.addURI(PROVIDER_NAME, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST_ITEMS);

        return matcher;
    }

    // MIME types used for searching items or looking up a single item
    private static final String ITEMS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.navare.prashant.ResortManager.Item";
    private static final String ITEM_DEFINITION_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.navare.prashant.ResortManager.Item";

    // MIME types used for searching rooms or looking up a single room
    private static final String ROOMS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.navare.prashant.ResortManager.Room";
    private static final String ROOM_DEFINITION_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.navare.prashant.ResortManager.Room";

    // MIME types used for searching rooms or looking up a single reservation
    private static final String RESERVATIONS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.navare.prashant.ResortManager.Reservation";
    private static final String RESERVATION_DEFINITION_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.navare.prashant.ResortManager.Reservation";

    private ResortManagerDatabase mResortDB;

    public ResortManagerContentProvider() {
    }

    @Override
    public boolean onCreate() {
        mResortDB = new ResortManagerDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor resultCursor = null;
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (mURIMatcher.match(uri)) {

            case SEARCH_FTS_ITEMS:
                if (selectionArgs == null) {
                    resultCursor = getAllFTSItems();
                }
                else {
                    resultCursor =  searchFTSItems(selectionArgs[0]);
                }
                break;
            case ITEM_ID:
                resultCursor =  getItem(uri);
                break;
            case SEARCH_SUGGEST_ITEMS:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri);
                }
                resultCursor = getSuggestionsFTSForItems(selectionArgs[0]);
                break;

            case SEARCH_FTS_TASKS:
                if (selectionArgs == null) {
                    resultCursor = getAllFTSTasks();
                }
                else {
                    resultCursor =  searchFTSTasks(selectionArgs[0]);
                }
                break;
            case TASK_ID:
                resultCursor =  getTask(uri);
                break;

            case SERVICE_CALL_ID:
                resultCursor =  getServiceCall(uri);
                break;

            case SEARCH_FTS_ROOMS:
                if (selectionArgs == null) {
                    resultCursor = getAllFTSRooms();
                }
                else {
                    resultCursor =  searchFTSRooms(selectionArgs[0]);
                }
                break;
            case ROOM_ID:
                resultCursor =  getRoom(uri);
                break;

            case SEARCH_FTS_RESERVATIONS:
                resultCursor = getFTSReservations(projection, selection, selectionArgs, sortOrder);
                break;

            case RESERVATION_ID:
                resultCursor =  getReservation(uri);
                break;

            case RESERVATION_ROOMS_ID:
                if (selectionArgs == null) {
                    resultCursor = getReservationRooms(null);
                }
                else {
                    resultCursor =  getReservationRooms(selectionArgs[0]);
                }
                break;

            case SEARCH_FTS_COMPLETED_RESERVATIONS:
                if (selectionArgs == null) {
                    resultCursor = getAllFTSCompletedReservations();
                }
                else {
                    resultCursor =  searchFTSCompletedReservations(selectionArgs[0]);
                }
                break;

            case COMPLETED_RESERVATION_ID:
                resultCursor =  getCompletedReservation(uri);
                break;

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        if (resultCursor != null)
            resultCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return resultCursor;
    }

    private Cursor getAllFTSItems() {
        return mResortDB.getAllFTSItems(Item.FTS_FIELDS);
    }

    private Cursor searchFTSItems(String query) {
        query = query.toLowerCase();
        return mResortDB.getFTSItemMatches(query, Item.FTS_FIELDS);
    }

    private Cursor getItem(Uri uri) {
        String rowId = uri.getLastPathSegment();
        return mResortDB.getItem(rowId, Item.FIELDS);
    }

    private Cursor getSuggestionsFTSForItems(String query) {
        query = query.toLowerCase();
        return mResortDB.getFTSItemMatches(query, Item.FTS_FIELDS);
    }

    private Cursor getAllFTSRooms() {
        return mResortDB.getAllFTSRooms(Room.FTS_FIELDS);
    }

    private Cursor searchFTSRooms(String query) {
        query = query.toLowerCase();
        return mResortDB.getFTSRoomMatches(query, Room.FTS_FIELDS);
    }

    private Cursor getRoom(Uri uri) {
        String rowId = uri.getLastPathSegment();
        return mResortDB.getRoom(rowId, Room.FIELDS);
    }

    private Cursor getFTSReservations(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mResortDB.getFTSReservations(projection, selection, selectionArgs, sortOrder);
    }

    private Cursor getAllFTSCompletedReservations() {
        return mResortDB.getAllFTSCompletedReservations(Reservation.COMPLETED_FTS_FIELDS);
    }

    private Cursor searchFTSCompletedReservations(String query) {
        query = query.toLowerCase();
        return mResortDB.getFTSCompletedReservationMatches(query, Reservation.COMPLETED_FTS_FIELDS);
    }

    private Cursor getReservation(Uri uri) {
        String rowId = uri.getLastPathSegment();
        return mResortDB.getReservation(rowId, Reservation.FIELDS);
    }

    private Cursor getCompletedReservation(Uri uri) {
        String rowId = uri.getLastPathSegment();
        return mResortDB.getCompletedReservation(rowId, Reservation.COMPLETED_FTS_FIELDS);
    }

    private Cursor getReservationRooms(String reservationID) {
        return mResortDB.getReservationRooms(reservationID, Room.FIELDS);
    }

    private Cursor getAllFTSTasks() {
        return mResortDB.getAllFTSTasks(Task.FTS_FIELDS);
    }

    private Cursor searchFTSTasks(String query) {
        query = query.toLowerCase();
        return mResortDB.getFTSTaskMatches(query, Task.FTS_FIELDS);
    }

    private Cursor getTask(Uri uri) {
        String rowId = uri.getLastPathSegment();
        return mResortDB.getTask(rowId, Task.FIELDS);
    }

    private Cursor getServiceCall(Uri uri) {
        String rowId = uri.getLastPathSegment();
        return mResortDB.getServiceCall(rowId, ServiceCall.FIELDS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (mURIMatcher.match(uri)) {
            case ITEM_ID:
                return deleteItem(uri);
            case ROOM_ID:
                return deleteRoom(uri);
            case RESERVATION_ID:
                return deleteReservation(uri);
            case TASK_ID:
                return deleteTask(uri);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private int deleteItem(Uri uri) {
        String rowId = uri.getLastPathSegment();
        int rowsDeleted = mResortDB.deleteItem(rowId);
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_ITEM_URI, null);
        }
        return rowsDeleted;
    }

    private int deleteRoom(Uri uri) {
        String rowId = uri.getLastPathSegment();
        int rowsDeleted = mResortDB.deleteRoom(rowId);
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_ROOM_URI, null);
        }
        return rowsDeleted;
    }

    private int deleteReservation(Uri uri) {
        String rowId = uri.getLastPathSegment();
        int rowsDeleted = mResortDB.deleteReservation(rowId);
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_RESERVATION_URI, null);
        }
        return rowsDeleted;
    }

    private int deleteTask(Uri uri) {
        String rowId = uri.getLastPathSegment();
        int rowsDeleted = mResortDB.deleteTask(rowId);
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_TASK_URI, null);
        }
        return rowsDeleted;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        switch (mURIMatcher.match(uri)) {
            case ITEMS:
                return insertItem(values);
            case ROOMS:
                return insertRoom(values);
            case RESERVATIONS:
                return insertReservation(values);
            case SERVICE_CALLS:
                return insertServiceCall(values);
            case TASKS:
                return insertTask(values);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private Uri insertItem(ContentValues values) {

         // Add a new item
        long rowID = mResortDB.insertItem(values);
        // If record is added successfully
        if (rowID > 0)
        {
            Uri newItemUri = ContentUris.withAppendedId(ITEM_URI, rowID);
            getContext().getContentResolver().notifyChange(FTS_ITEM_URI, null);
            return newItemUri;
        }
        return null;
    }

    private Uri insertRoom(ContentValues values) {

        // Add a new room
        long rowID = mResortDB.insertRoom(values);
        // If record is added successfully
        if (rowID > 0)
        {
            Uri newRoomUri = ContentUris.withAppendedId(ROOM_URI, rowID);
            getContext().getContentResolver().notifyChange(FTS_ROOM_URI, null);
            return newRoomUri;
        }
        return null;
    }

    private Uri insertReservation(ContentValues values) {

        // Add a new room
        long rowID = mResortDB.insertReservation(values);
        // If record is added successfully
        if (rowID > 0)
        {
            Uri newReservationUri = ContentUris.withAppendedId(RESERVATION_URI, rowID);
            getContext().getContentResolver().notifyChange(FTS_RESERVATION_URI, null);
            return newReservationUri;
        }
        return null;
    }

    private Uri insertServiceCall(ContentValues values) {

        // Add a new service call
        long rowID = mResortDB.insertServiceCall(values);
        // If record is added successfully
        if (rowID > 0)
        {
            return ContentUris.withAppendedId(SERVICE_CALL_URI, rowID);
        }
        return null;
    }

    private Uri insertTask(ContentValues values) {

        // Add a new task
        long rowID = mResortDB.insertTask(values);
        // If record is added successfully
        if (rowID > 0)
        {
            Uri newTaskUri = ContentUris.withAppendedId(TASK_URI, rowID);
            getContext().getContentResolver().notifyChange(FTS_TASK_URI, null);
            return newTaskUri;
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        switch (mURIMatcher.match(uri)) {
            case ITEM_ID:
                return updateItem(uri, values, selection, selectionArgs);
            case ROOM_ID:
                return updateRoom(uri, values, selection, selectionArgs);
            case RESERVATION_ID:
                return updateReservation(uri, values, selection, selectionArgs);
            case TASK_ID:
                return updateTask(uri, values, selection, selectionArgs);
            case SERVICE_CALL_ID:
                return updateServiceCall(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String itemId = uri.getLastPathSegment();
        int rowsUpdated = mResortDB.updateItem(itemId, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_ITEM_URI, null);
        }
        return rowsUpdated;
    }

    private int updateRoom(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String roomId = uri.getLastPathSegment();
        int rowsUpdated = mResortDB.updateRoom(roomId, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_ROOM_URI, null);
        }
        return rowsUpdated;
    }

    private int updateReservation(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String reservationId = uri.getLastPathSegment();
        int rowsUpdated = mResortDB.updateReservation(reservationId, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_RESERVATION_URI, null);
        }
        return rowsUpdated;
    }

    private int updateTask(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String taskId = uri.getLastPathSegment();
        int rowsUpdated = mResortDB.updateTask(taskId, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_TASK_URI, null);
        }
        return rowsUpdated;
    }

    private int updateServiceCall(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String serviceCallId = uri.getLastPathSegment();
        int rowsUpdated = mResortDB.updateServiceCall(serviceCallId, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(FTS_TASK_URI, null);
        }
        return rowsUpdated;
    }

    @Override
    public String getType(Uri uri) {
        switch (mURIMatcher.match(uri)) {
            case SEARCH_FTS_ITEMS:
                return ITEMS_MIME_TYPE;
            case ITEMS:
                return ITEM_DEFINITION_MIME_TYPE;

            case SEARCH_FTS_ROOMS:
                return ROOMS_MIME_TYPE;
            case ROOMS:
                return ROOM_DEFINITION_MIME_TYPE;

            case SEARCH_FTS_RESERVATIONS:
                return RESERVATIONS_MIME_TYPE;
            case RESERVATIONS:
                return RESERVATION_DEFINITION_MIME_TYPE;

            // PNTODO: Should this be different?
            case SEARCH_SUGGEST_ITEMS:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if(method.equals("computeNewTasks")) {
            computeNewTasks();
        }
        return null;
    }

    private void computeNewTasks() {
        Log.d("HIContentProvider", "starting computeNewTasks queries...");
        mResortDB.computeNewTasks();
        getContext().getContentResolver().notifyChange(FTS_TASK_URI, null);
    }
}
