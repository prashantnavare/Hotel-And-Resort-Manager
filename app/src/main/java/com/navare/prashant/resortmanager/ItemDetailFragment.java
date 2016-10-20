package com.navare.prashant.resortmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.navare.prashant.resortmanager.Database.ResortManagerContentProvider;
import com.navare.prashant.resortmanager.Database.Item;
import com.navare.prashant.resortmanager.Database.ServiceCall;
import com.navare.prashant.resortmanager.Database.Task;
import com.navare.prashant.resortmanager.util.ResortManagerDatePickerFragment;
import com.navare.prashant.resortmanager.util.InventoryDialogFragment;
import com.navare.prashant.resortmanager.util.ServiceCallDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {

    private static final int LOADER_ID_ITEM_DETAILS = 2;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private Context mContext = null;

    /**
     * The item this fragment is presenting.
     */
    private String mItemID;
    private Item mItem = null;
    private long mPreviousType = 0;

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * The UI elements showing the details of the item
     */
    private TextView mTextName;
    private TextView mTextLocation;
    private TextView mTextDescription;
    private RadioButton mInstrumentRadioButton;
    private RadioButton mConsumableRadioButton;

    private LinearLayout mMaintenanceRemindersLayout;
    private LinearLayout mMaintenanceDetailsLayout;
    private LinearLayout mContractRemindersLayout;
    private LinearLayout mContractDetailsLayout;

    private LinearLayout mConsumableLayout;
    private LinearLayout mInventoryDetailsLayout;

    private CheckBox mMaintenanceCheckBox;
    private TextView mTextMaintenanceFrequency;
    private Button mBtnChangeMaintenanceDate;
    private TextView mTextMaintenanceInstructions;

    private CheckBox mContractCheckBox;
    private Button mBtnContractValidTillDate;
    private TextView mTextContractInstructions;

    private CheckBox mInventoryCheckBox;
    private TextView mTextMinRequiredQuantity;
    private TextView mTextCurrentQuantity;
    private TextView mTextReorderInstructions;

    private ImageView mImageView;

    private String mImageFileName;
    private File mImageFile;
    private Uri mImageFileUri;
    private Bitmap mImageBitmap = null;

    private AdView mAdView;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callbacks for when an item has been selected.
         */
        void EnableDeleteButton(boolean bEnable);
        void EnableRevertButton(boolean bEnable);
        void EnableSaveButton(boolean bEnable);
        void EnableInventoryAddButton(boolean bEnable);
        void EnableInventorySubtractButton(boolean bEnable);
        void RedrawOptionsMenu();
        void EnableServiceCallButton(boolean bEnable);
        void onItemDeleted();
        void setTitleString(String titleString);
    }
    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void EnableDeleteButton(boolean bEnable) {
        }
        @Override
        public void EnableRevertButton(boolean bEnable) {
        }
        @Override
        public void EnableSaveButton(boolean bEnable) {
        }
        @Override
        public void EnableInventoryAddButton(boolean bEnable) {
        }
        @Override
        public void EnableInventorySubtractButton(boolean bEnable) {
        }
        @Override
        public void EnableServiceCallButton(boolean bEnable) {
        }
        @Override
        public void RedrawOptionsMenu() {
        }
        @Override
        public void onItemDeleted() {
        }

        @Override
        public void setTitleString(String titleString) {
        }
    };

    /**
     * The fragment's current callback object, which is notified of changes to the item
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemID = getArguments().getString(ARG_ITEM_ID);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement item detail fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
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


    private void showInstrumentLayout() {

        mMaintenanceRemindersLayout.setVisibility(View.VISIBLE);
        if (mMaintenanceCheckBox.isChecked())
            mMaintenanceDetailsLayout.setVisibility(View.VISIBLE);
        else
            mMaintenanceDetailsLayout.setVisibility(View.GONE);

        mContractRemindersLayout.setVisibility(View.VISIBLE);
        if (mContractCheckBox.isChecked())
            mContractDetailsLayout.setVisibility(View.VISIBLE);
        else
            mContractDetailsLayout.setVisibility(View.GONE);

        mConsumableLayout.setVisibility(View.GONE);
    }

    private void showConsumableLayout() {
        mMaintenanceRemindersLayout.setVisibility(View.GONE);
        mMaintenanceDetailsLayout.setVisibility(View.GONE);
        mContractRemindersLayout.setVisibility(View.GONE);
        mContractDetailsLayout.setVisibility(View.GONE);

        mConsumableLayout.setVisibility(View.VISIBLE);
        if (mInventoryCheckBox.isChecked())
            mInventoryDetailsLayout.setVisibility(View.VISIBLE);
        else
            mInventoryDetailsLayout.setVisibility(View.GONE);

    }
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        mTextName = ((TextView) rootView.findViewById(R.id.textName));
        mTextName.addTextChangedListener(this);

        mTextLocation = ((TextView) rootView.findViewById(R.id.textLocation));
        mTextLocation.addTextChangedListener(this);

        mTextDescription = ((TextView) rootView.findViewById(R.id.textDescription));
        mTextDescription.addTextChangedListener(this);

        mInstrumentRadioButton = (RadioButton) rootView.findViewById(R.id.instrumentRadioButton);
        mInstrumentRadioButton.setChecked(true);
        mInstrumentRadioButton.setOnClickListener(new RadioGroup.OnClickListener() {
            public void onClick(View v){
                if (mPreviousType != Item.InstrumentType) {
                    showInstrumentLayout();
                    mCallbacks.EnableRevertButton(true);
                    mCallbacks.EnableSaveButton(true);
                    mCallbacks.RedrawOptionsMenu();
                }
            }
        });
        mConsumableRadioButton = (RadioButton) rootView.findViewById(R.id.consumableRadioButton);
        mConsumableRadioButton.setOnClickListener(new RadioGroup.OnClickListener() {
            public void onClick(View v){
                if (mPreviousType != Item.ConsumableType) {
                    showConsumableLayout();
                    mCallbacks.EnableRevertButton(true);
                    mCallbacks.EnableSaveButton(true);
                    mCallbacks.RedrawOptionsMenu();
                }
            }
        });

        // Instrument related
        mMaintenanceRemindersLayout = (LinearLayout) rootView.findViewById(R.id.maintenanceRemindersLayout);
        mMaintenanceDetailsLayout = (LinearLayout) rootView.findViewById(R.id.maintenanceDetailsLayout);
        mContractRemindersLayout = (LinearLayout) rootView.findViewById(R.id.contractRemindersLayout);
        mContractDetailsLayout = (LinearLayout) rootView.findViewById(R.id.contractDetailsLayout);

        // Maintenance
        mMaintenanceCheckBox = (CheckBox) rootView.findViewById(R.id.chkMaintenance);
        mMaintenanceCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mMaintenanceDetailsLayout.setVisibility(View.VISIBLE);
                } else {
                    mMaintenanceDetailsLayout.setVisibility(View.GONE);
                }
                enableRevertAndSaveButtons();
            }
        });

        mTextMaintenanceFrequency = (TextView) rootView.findViewById(R.id.textMaintenanceFrequency);
        mTextMaintenanceFrequency.addTextChangedListener(this);

        mBtnChangeMaintenanceDate = (Button) rootView.findViewById(R.id.btnChangeMaintenanceDate);
        mBtnChangeMaintenanceDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker(DatePickerType.MAINTENANCE);
            }
        });

        mTextMaintenanceInstructions = (TextView) rootView.findViewById(R.id.textMaintenanceInstructions);
        mTextMaintenanceInstructions.addTextChangedListener(this);

        // Contract
        mContractCheckBox = (CheckBox) rootView.findViewById(R.id.chkContract);
        mContractCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mContractDetailsLayout.setVisibility(View.VISIBLE);
                } else {
                    mContractDetailsLayout.setVisibility(View.GONE);
                }
                enableRevertAndSaveButtons();
            }
        });

        mBtnContractValidTillDate = (Button) rootView.findViewById(R.id.btnContractValidTillDate);
        mBtnContractValidTillDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker(DatePickerType.CONTRACT);
            }
        });

        mTextContractInstructions = (TextView) rootView.findViewById(R.id.textContractInstructions);
        mTextContractInstructions.addTextChangedListener(this);

        // Consumable related
        mConsumableLayout = (LinearLayout) rootView.findViewById(R.id.consumableLayout);
        mInventoryDetailsLayout = (LinearLayout) rootView.findViewById(R.id.inventoryDetailsLayout);

        // Inventory related
        mTextCurrentQuantity = (TextView) rootView.findViewById(R.id.textCurrentQuantity);
        mTextCurrentQuantity.addTextChangedListener(this);

        mInventoryCheckBox = (CheckBox) rootView.findViewById(R.id.chkInventory);
        mInventoryCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mInventoryDetailsLayout.setVisibility(View.VISIBLE);
                } else {
                    mInventoryDetailsLayout.setVisibility(View.GONE);
                }
                enableRevertAndSaveButtons();
            }
        });

        mTextMinRequiredQuantity = (TextView) rootView.findViewById(R.id.textMinRequiredQuantity);
        mTextMinRequiredQuantity.addTextChangedListener(this);

        mTextReorderInstructions = (TextView) rootView.findViewById(R.id.textReorderInstructions);
        mTextReorderInstructions.addTextChangedListener(this);

        // image related
        mImageView = ((ImageView) rootView.findViewById(R.id.imageItem));

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
    public  void onDestroyView() {
        super.onDestroyView();
    }

    enum DatePickerType {MAINTENANCE, CONTRACT}

    private void showDatePicker(final DatePickerType pickerType) {
        Calendar dateToShow = Calendar.getInstance();
        if (mItem != null) {
            switch (pickerType) {
                case MAINTENANCE:
                    if (mItem.mMaintenanceDate > 0) {
                        dateToShow.setTimeInMillis(mItem.mMaintenanceDate);
                    }
                    break;
                case CONTRACT:
                    if (mItem.mContractValidTillDate > 0) {
                        dateToShow.setTimeInMillis(mItem.mContractValidTillDate);
                    }
                    break;
            }
        }
        ResortManagerDatePickerFragment datePicker = new ResortManagerDatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("year", dateToShow.get(Calendar.YEAR));
        args.putInt("month", dateToShow.get(Calendar.MONTH));
        args.putInt("day", dateToShow.get(Calendar.DAY_OF_MONTH));
        datePicker.setArguments(args);
        /**
         * Set Call back to capture selected date
         */
        OnDateSetListener onDateChangeCallback = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                switch (pickerType) {
                    case MAINTENANCE:
                        mBtnChangeMaintenanceDate.setText(dateFormatter.format(newDate.getTime()));
                        break;
                    case CONTRACT:
                        mBtnContractValidTillDate.setText(dateFormatter.format(newDate.getTime()));
                        break;
                }
                enableRevertAndSaveButtons();
            }
        };
        datePicker.setCallBack(onDateChangeCallback);
        datePicker.show(((FragmentActivity)mContext).getSupportFragmentManager(), "Instrument Date Picker");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if ((mItemID != null) && (!mItemID.isEmpty())) {
            getLoaderManager().initLoader(LOADER_ID_ITEM_DETAILS, null, this);
        }
        else {
            displayUIForNewItem();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == LOADER_ID_ITEM_DETAILS) {
            Uri itemURI = Uri.withAppendedPath(ResortManagerContentProvider.ITEM_URI,
                    mItemID);

            return new CursorLoader(getActivity(),
                    itemURI, Item.FIELDS, null, null,
                    null);
        }
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dataCursor) {

        if (dataCursor != null) {
            int loaderID = loader.getId();
            if (loaderID == LOADER_ID_ITEM_DETAILS) {
                if (mItem == null)
                    mItem = new Item();

                mItem.setContentFromCursor(dataCursor);
                updateUIFromItem();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        enableRevertAndSaveButtons();
    }

    private void enableRevertAndSaveButtons() {
        mCallbacks.EnableRevertButton(true);
        mCallbacks.EnableSaveButton(true);
        mCallbacks.RedrawOptionsMenu();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void revertUI() {
        if (mItem == null) {
            displayUIForNewItem();
        }
        else {
            updateUIFromItem();
        }
    }

    public void deleteItem() {
        Uri itemURI = Uri.withAppendedPath(ResortManagerContentProvider.ITEM_URI,
                mItemID);
        int result = getActivity().getContentResolver().delete(itemURI, null, null);
        if (result > 0) {
            mCallbacks.onItemDeleted();
        }
    }

    public boolean saveItem() {
        boolean bAllDataOK = updateItemFromUI();
        if (!bAllDataOK)
            return false;

        boolean bSuccess = false;
        if ((mItemID == null) || (mItemID.isEmpty())) {
            // a new item is being inserted.
            Uri uri = getActivity().getContentResolver().insert(ResortManagerContentProvider.ITEM_URI, mItem.getContentValues());
            if (uri != null) {
                mItemID = uri.getLastPathSegment();
                mItem.mID = Long.valueOf(mItemID);
                bSuccess = true;
            }
        }
        else {
            Uri itemURI = Uri.withAppendedPath(ResortManagerContentProvider.ITEM_URI,
                    mItemID);
            int result = getActivity().getContentResolver().update(itemURI, mItem.getContentValues(), null, null);
            if (result > 0)
                bSuccess = true;
        }
        if (bSuccess) {
            updateUIFromItem();
        }
        return true;
    }

    private void showAlertDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle("Incomplete Data");

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_resort_manager);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private boolean updateItemFromUI() {
        if (mItem == null)
            mItem = new Item();

        if (mTextName.getText().toString().isEmpty()) {
            showAlertDialog("Item name cannot be empty.");
            mTextName.requestFocus();
            return false;
        }
        else {
            mItem.mName = mTextName.getText().toString();
        }
        mItem.mLocation = mTextLocation.getText().toString();
        mItem.mDescription = mTextDescription.getText().toString();
        if (mInstrumentRadioButton.isChecked()) {
            // Instrument
            mItem.mType = Item.InstrumentType;

            // Maintenance related
            if (mMaintenanceCheckBox.isChecked()) {
                mItem.mMaintenanceReminders = 1;
                if (mTextMaintenanceFrequency.getText().toString().isEmpty()) {
                    showAlertDialog("Maintenance frequency cannot be empty.");
                    mTextMaintenanceFrequency.requestFocus();
                    return false;
                }
                else {
                    mItem.mMaintenanceFrequency = Long.valueOf(mTextMaintenanceFrequency.getText().toString());
                }

                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                Calendar maintenanceDate = Calendar.getInstance();
                String uiMaintenanceDate = mBtnChangeMaintenanceDate.getText().toString();
                if (uiMaintenanceDate.compareToIgnoreCase("Set") != 0) {
                    try {
                        maintenanceDate.setTime(dateFormatter.parse(uiMaintenanceDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    mItem.mMaintenanceDate = maintenanceDate.getTimeInMillis();
                }

                mItem.mMaintenanceInstructions = mTextMaintenanceInstructions.getText().toString();
            }
            else {
                mItem.mMaintenanceReminders = 0;
            }

            // Contract related
            if (mContractCheckBox.isChecked()) {
                mItem.mContractReminders = 1;
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                Calendar contractDate = Calendar.getInstance();
                String uiContractDate = mBtnContractValidTillDate.getText().toString();
                if (uiContractDate.compareToIgnoreCase("Set") != 0) {
                    try {
                        contractDate.setTime(dateFormatter.parse(uiContractDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    mItem.mContractValidTillDate = contractDate.getTimeInMillis();
                }

                mItem.mContractInstructions = mTextContractInstructions.getText().toString();
            }
            else {
                mItem.mContractReminders = 0;
            }
        }
        else if (mConsumableRadioButton.isChecked()){
            // Consumable
            mItem.mType = Item.ConsumableType;
            // Inventory related
            if (!mTextCurrentQuantity.getText().toString().isEmpty())
                mItem.mCurrentQuantity = Long.valueOf(mTextCurrentQuantity.getText().toString());
            if (mInventoryCheckBox.isChecked()) {
                mItem.mInventoryReminders = 1;
                if (mTextMinRequiredQuantity.getText().toString().isEmpty()) {
                    showAlertDialog("Minimum quantity cannot be empty.");
                    mTextMinRequiredQuantity.requestFocus();
                    return false;
                }
                else {
                    mItem.mMinRequiredQuantity = Long.valueOf(mTextMinRequiredQuantity.getText().toString());
                    mItem.mReorderInstructions = mTextReorderInstructions.getText().toString();
                }
            }
            else {
                mItem.mInventoryReminders = 0;
            }
        }
        if (mImageBitmap != null) {
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            mImageBitmap.compress(Bitmap.CompressFormat.PNG, 0, imageStream);
            mItem.mImage = imageStream.toByteArray();
        }
        return true;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void updateUIFromItem() {
        mTextName.setText(mItem.mName);
        mTextLocation.setText(mItem.mLocation);
        mTextDescription.setText(mItem.mDescription);
        if (mItem.mType == Item.InstrumentType) {

            // Enable the instrument layout
            showInstrumentLayout();

            mPreviousType = Item.InstrumentType;
            mInstrumentRadioButton.setChecked(true);

            // Turn on the Instrument action bar menu items
            mCallbacks.EnableServiceCallButton(true);

            // Turn off the Consumable action bar menu items
            mCallbacks.EnableInventoryAddButton(false);
            mCallbacks.EnableInventorySubtractButton(false);

            // Set the maintenance UI elements
            if (mItem.mMaintenanceReminders > 0) {
                mMaintenanceCheckBox.setChecked(true);
                mMaintenanceDetailsLayout.setVisibility(View.VISIBLE);
                if (mItem.mMaintenanceFrequency > 0)
                    mTextMaintenanceFrequency.setText(String.valueOf(mItem.mMaintenanceFrequency));
                if (mItem.mMaintenanceDate > 0) {
                    Calendar maintenanceDate = Calendar.getInstance();
                    maintenanceDate.setTimeInMillis(mItem.mMaintenanceDate);
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                    mBtnChangeMaintenanceDate.setText(dateFormatter.format(maintenanceDate.getTime()));
                }
                else {
                    mBtnChangeMaintenanceDate.setText("Set");
                }
                mTextMaintenanceInstructions.setText(mItem.mMaintenanceInstructions);
            }
            else {
                mMaintenanceCheckBox.setChecked(false);
                mMaintenanceDetailsLayout.setVisibility(View.GONE);
            }

            // Set the contract UI elements
            if (mItem.mContractReminders > 0) {
                mContractCheckBox.setChecked(true);
                mContractDetailsLayout.setVisibility(View.VISIBLE);
                if (mItem.mContractValidTillDate > 0) {
                    Calendar contractDate = Calendar.getInstance();
                    contractDate.setTimeInMillis(mItem.mContractValidTillDate);
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
                    mBtnContractValidTillDate.setText(dateFormatter.format(contractDate.getTime()));
                }
                else {
                    mBtnContractValidTillDate.setText("Set");
                }
                mTextContractInstructions.setText(mItem.mContractInstructions);
            }
            else {
                mContractCheckBox.setChecked(false);
                mContractDetailsLayout.setVisibility(View.GONE);
            }
        }
        else if (mItem.mType == Item.ConsumableType) {

            // Turn off the Instrument specific views
            showConsumableLayout();

            mPreviousType = Item.ConsumableType;
            mConsumableRadioButton.setChecked(true);

            // Turn off the Instrument action bar menu items
            mCallbacks.EnableServiceCallButton(false);

            // Turn on the Consumable action bar menu items
            mCallbacks.EnableInventoryAddButton(true);
            mCallbacks.EnableInventorySubtractButton(true);

            if (mItem.mCurrentQuantity > 0)
                mTextCurrentQuantity.setText(String.valueOf(mItem.mCurrentQuantity));

            // Set the Inventory UI elements
            if (mItem.mInventoryReminders > 0) {
                mInventoryCheckBox.setChecked(true);
                mInventoryDetailsLayout.setVisibility(View.VISIBLE);
                if (mItem.mMinRequiredQuantity > 0)
                    mTextMinRequiredQuantity.setText(String.valueOf(mItem.mMinRequiredQuantity));
                mTextReorderInstructions.setText(mItem.mReorderInstructions);
            }
            else {
                mInventoryCheckBox.setChecked(false);
                mInventoryDetailsLayout.setVisibility(View.GONE);
            }
        }

        if (mItem.mImage == null) {
            mImageView.setImageBitmap(null);
        }
        else {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = false;
            mImageBitmap = BitmapFactory.decodeByteArray(mItem.mImage, 0, mItem.mImage.length, bmpFactoryOptions);
            // Display it
            mImageView.setImageBitmap(mImageBitmap);
        }

        // Toggle remaining action bar buttons appropriately
        mCallbacks.EnableDeleteButton(true);
        mCallbacks.EnableRevertButton(false);
        mCallbacks.EnableSaveButton(false);
        mCallbacks.RedrawOptionsMenu();

        mCallbacks.setTitleString(mItem.mName);
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void displayUIForNewItem() {
        mTextName.setText("");
        mTextLocation.setText("");
        mTextDescription.setText("");
        mInstrumentRadioButton.setChecked(true);
        showInstrumentLayout();

        mMaintenanceCheckBox.setChecked(false);
        mMaintenanceDetailsLayout.setVisibility(View.GONE);

        mContractCheckBox.setChecked(false);
        mContractDetailsLayout.setVisibility(View.GONE);

        mImageView.setImageBitmap(null);

        mCallbacks.EnableRevertButton(false);
        mCallbacks.EnableSaveButton(false);
        mCallbacks.EnableDeleteButton(false);
        mCallbacks.EnableServiceCallButton(false);
        mCallbacks.EnableInventoryAddButton(false);
        mCallbacks.EnableInventorySubtractButton(false);

        mCallbacks.setTitleString("New Item");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    public void showInventoryAddDialog() {
        InventoryDialogFragment dialog = new InventoryDialogFragment();
        dialog.setDialogType(InventoryDialogFragment.InventoryDialogType.ADD);
        dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "InventoryDialogFragment");
    }

    public void showInventorySubtractDialog() {
        InventoryDialogFragment dialog = new InventoryDialogFragment();
        dialog.setDialogType(InventoryDialogFragment.InventoryDialogType.SUBTRACT);
        dialog.show(((FragmentActivity)mContext).getSupportFragmentManager(), "InventoryDialogFragment");
    }

    public void showServiceCallDialog() {
        ServiceCallDialogFragment dialog = new ServiceCallDialogFragment();
        dialog.setItem(mItem);
        dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "ServiceCallDialogFragment");
    }

    public void addToInventory(long quantity) {
        long newCurrrentQuantity =  mItem.mCurrentQuantity + quantity;
        mTextCurrentQuantity.setText(String.valueOf(newCurrrentQuantity));
        mCallbacks.EnableRevertButton(true);
        mCallbacks.EnableSaveButton(true);
        mCallbacks.RedrawOptionsMenu();
    }

    public void subtractFromInventory(long quantity) {
        long newCurrrentQuantity =  mItem.mCurrentQuantity - quantity;
        mTextCurrentQuantity.setText(String.valueOf(newCurrrentQuantity));
        mCallbacks.EnableRevertButton(true);
        mCallbacks.EnableSaveButton(true);
        mCallbacks.RedrawOptionsMenu();
    }

    public void createServiceCall(long itemID, String description, long priority, String itemName, String itemLocation) {
        ServiceCall sc = new ServiceCall();
        sc.mItemID = itemID;
        sc.mDescription = description;
        sc.mPriority = priority;
        sc.mStatus = ServiceCall.OpenStatus;
        sc.mOpenTimeStamp = Calendar.getInstance().getTimeInMillis();
        sc.mItemName = itemName;
        sc.mItemLocation = itemLocation;

        // a new service call is being inserted.
        Uri uri = getActivity().getContentResolver().insert(ResortManagerContentProvider.SERVICE_CALL_URI, sc.getContentValues());
        if (uri != null) {
            Toast toast = Toast.makeText(mContext, "Problem report created.", Toast.LENGTH_SHORT);
            toast.show();

            // Also create a corresponding task
            Task task = new Task();
            task.mTaskType = Task.ServiceCall;
            task.mItemID = itemID;
            task.mServiceCallID = Long.valueOf(uri.getLastPathSegment());
            task.mItemName = mItem.mName;
            task.mItemLocation = mItem.mLocation;
            task.mStatus = Task.OpenStatus;
            task.mPriority = priority;

            Uri taskUri = getActivity().getContentResolver().insert(ResortManagerContentProvider.TASK_URI, task.getContentValues());
        }
        else {
            Toast toast = Toast.makeText(mContext, "Failed to create problem report.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public void handleCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            mImageFileName = mContext.getExternalFilesDir(null).getAbsolutePath() + "/" + String.valueOf(Calendar.getInstance().getTimeInMillis()) + ".png";
            mImageFile = new File(mImageFileName);
            mImageFileUri = Uri.fromFile(mImageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inSampleSize = 4;
            mImageBitmap = BitmapFactory.decodeFile(mImageFileName, bmpFactoryOptions);
            // Display it
            mImageView.setImageBitmap(mImageBitmap);
            mImageFile.delete();
            enableRevertAndSaveButtons();
        }
    }
}
