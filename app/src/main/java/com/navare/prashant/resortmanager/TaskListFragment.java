package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.util.TaskListCursorAdapter;

/**
 * A list fragment representing a list of Tasks. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TaskDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TaskListFragment extends ListFragment {

    public static final int LOADER_ID_TASK_LIST = 11;
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
        void onTaskSelected(String id);
        String getQuery();
        void setTaskCount(long taskCount);
    }

    private Context mContext;

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onTaskSelected(String id) {
        }

        @Override
        public String getQuery() {
            return null;
        }

        @Override
        public void setTaskCount(long taskCount) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] columns = new String[] {
                Task.COL_FTS_ITEM_NAME,
                Task.COL_FTS_ITEM_LOCATION,
                Task.COL_FTS_TASK_TYPE,
                Task.COL_FTS_ASSIGNED_TO,
                Task.COL_FTS_DUE_DATE,
                Task.COL_FTS_TASK_PRIORITY
        };
        int[] views = new int[] {
                R.id.textItemName,
                R.id.textItemLocation,
                R.id.textTaskType,
                R.id.textAssignedTo,
                R.id.textDueDate,
                R.id.textPriority
        };
        setListAdapter(new TaskListCursorAdapter(getActivity(),
                R.layout.task_list_row, null, columns, views, 0));

        getNewTaskList(null);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        Activity activity = getActivity();
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        Task task = new Task();
        task.setFTSContent(cursor);
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that a task has been selected.
        mCallbacks.onTaskSelected(task.mFTSRealID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    public void getNewTaskList(final String searchString){

        // Load the content
        getLoaderManager().restartLoader(LOADER_ID_TASK_LIST, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String [] selectionArgs = null;
                if (searchString != null) {
                    selectionArgs = new String[] {searchString};
                }

                return new CursorLoader(getActivity(),
                        ResortManagerContentProvider.FTS_TASK_URI, Task.FTS_FIELDS, null, selectionArgs,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
                ((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
                if (c != null) {
                    mCallbacks.setTaskCount(c.getCount());
                    if (c.getCount() > 0) {
                        Toast toast = Toast.makeText(mContext, "Tap on any task to see the task details.", Toast.LENGTH_LONG);
                        toast.getView().setBackgroundResource(R.drawable.toast_drawable);
                        toast.show();
                    }
                }
                else {
                    mCallbacks.setTaskCount(0);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
                ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
            }
        });
    }
}
