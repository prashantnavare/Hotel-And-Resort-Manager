package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.util.ReportDetailCursorAdapter;

/**
 * A list fragment representing a list of Tasks. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TaskDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ReportDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_NAME = "item_name";

    private static final int LOADER_ID_TASK_LIST = 11;
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        String getQuery();
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public String getQuery() {
            return null;
        }
    };

    private String mItemID;
    private String mItemName;
    private ListView mReportListView;
    private ReportDetailCursorAdapter mListAdapter;
    private ReportDetailActivity mMyActivity;

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

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemID = getArguments().getString(ARG_ITEM_ID);
            mItemName = getArguments().getString(ARG_ITEM_NAME);
        }

        String[] columns = new String[] {
                Task.COMPLETED_COL_FTS_TASK_TYPE,
                Task.COMPLETED_COL_FTS_ASSIGNED_TO,
                Task.COMPLETED_COL_FTS_COMPLETION_DATE,
                Task.COMPLETED_COL_FTS_TASK_PRIORITY,
                Task.COMPLETED_COL_FTS_COMPLETION_COMMENTS
        };
        int[] views = new int[] {
                R.id.textTaskType,
                R.id.textAssignedTo,
                R.id.textCompletionDate,
                R.id.textPriority,
                R.id.textComments
        };
        mListAdapter = new ReportDetailCursorAdapter(getActivity(),
                R.layout.report_detail_row, null, columns, views, 0);

        getNewTaskList(null);
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

        mReportListView = ((ListView) rootView.findViewById(R.id.reportListView));
        mReportListView.setAdapter(mListAdapter);

        mMyActivity.setTitle("Reports for " + mItemName);

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
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
        mMyActivity = (ReportDetailActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    public void getNewTaskList(final String searchString){

        // Load the content
        getLoaderManager().restartLoader(LOADER_ID_TASK_LIST, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String [] selectionArgs;
                if (searchString != null) {
                    selectionArgs = new String[] {mItemID, searchString};
                }
                else {
                    selectionArgs = new String[] {mItemID, ""};
                }

                return new CursorLoader(getActivity(),
                        ResortManagerContentProvider.COMPLETED_FTS_TASK_URI, Task.COMPLETED_FTS_FIELDS, null, selectionArgs,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
                mListAdapter.swapCursor(c);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
                mListAdapter.swapCursor(null);
            }
        });
    }
}
