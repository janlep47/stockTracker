/* Janice Richards
 *
 *  Project 3: Stock Hawk
 * 
 *
 */

package com.android.stocks;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;

import com.android.stocks.data.StocksContract;
import com.android.stocks.sync.StockHawkSyncAdapter;

public class StocklistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String LOG_TAG = StocklistFragment.class.getSimpleName();
    private StocklistAdapter mStocklistAdapter;
    private RecyclerView mRecyclerView;
    private boolean mAutoSelectView;
    private int mChoiceMode;
    private boolean mHoldForTransition;


    private String mInitialSelectedSymbol = "";

    private static final String SELECTED_KEY = "selected_position";

    private static final int STOCKLIST_LOADER = 0;
    // For the list view we're showing only a small subset of the stored data.
    private static final String[] STOCKLIST_COLUMNS = {
            StocksContract.StockEntry.COLUMN_SYMBOL,
            StocksContract.StockEntry.COLUMN_NAME,
            StocksContract.StockEntry.COLUMN_PRICE,
            StocksContract.StockEntry.COLUMN_PRICE_CHANGE};

    // These indices are tied to above.
    static final int COL_STOCK_SYMBOL = 0;
    static final int COL_STOCK_NAME = 1;
    static final int COL_STOCK_PRICE = 2;
    static final int COL_STOCK_PRICE_CHANGE = 3;


    public interface Callback {
        // for when a list item has been selected.
        public void onItemSelected(Uri dateUri, StocklistAdapter.StocklistAdapterViewHolder vh);
    }

    public StocklistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StocklistFragment,
                0, 0);
        mChoiceMode = a.getInt(R.styleable.StocklistFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.StocklistFragment_autoSelectView, false);
        mHoldForTransition = a.getBoolean(R.styleable.StocklistFragment_sharedElementTransitions, false);
        a.recycle();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_stocklist);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_stocklist_empty);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // The StocklistAdapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        mStocklistAdapter = new StocklistAdapter(getActivity(), new StocklistAdapter.StocklistAdapterOnClickHandler() {
            @Override
            //public void onClick(Long date, StocklistAdapter.StocklistAdapterViewHolder vh) {
            //    String locationSetting = Utility.getPreferredLocation(getActivity());
            public void onClick(String stockSymbol, StocklistAdapter.StocklistAdapterViewHolder vh) {
                //String locationSetting = Utility.getPreferredLocation(getActivity());
                ((Callback) getActivity())
                        .onItemSelected(StocksContract.StockEntry.buildStockInfoWithSymbol(
                                            //locationSetting, date),
                                            stockSymbol),
                                vh
                        );
            }
        }, emptyView, mChoiceMode);

        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mStocklistAdapter);


        // For when device is rotated, but not really applicable here, since device is locked in
        // portrait mode.
        if (savedInstanceState != null) {
            mStocklistAdapter.onRestoreInstanceState(savedInstanceState);
        } else {
            StockHawkSyncAdapter.syncImmediately(getContext());
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        if ( mHoldForTransition ) {
            getActivity().supportPostponeEnterTransition();
        }
        getLoaderManager().initLoader(STOCKLIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    //void onStocklistChanged() {getLoaderManager().restartLoader(STOCKLIST_LOADER, null, this);}


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        mStocklistAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Sort order:  Ascending, by stock symbol.
        String sortOrder = StocksContract.StockEntry.COLUMN_SYMBOL + " ASC";

        return new CursorLoader(getActivity(),
                StocksContract.StockEntry.CONTENT_URI,
                STOCKLIST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mStocklistAdapter.swapCursor(data);
        updateEmptyView();
        if ( data.getCount() == 0 ) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                // Since we know we're going to get items, we keep the listener around until
                // we see Children.
                if (mRecyclerView.getChildCount() > 0) {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int position = mStocklistAdapter.getSelectedItemPosition();
                    //if (position == RecyclerView.NO_POSITION &&
                    //        -1 != mInitialSelectedDate) {
                    if (position == RecyclerView.NO_POSITION && mInitialSelectedSymbol != "") {
                        Cursor data = mStocklistAdapter.getCursor();
                        int count = data.getCount();
                        //int dateColumn = data.getColumnIndex(StocksContract.StockEntry.COLUMN_DATE);
                        int symbolColumn = data.getColumnIndex(StocksContract.StockEntry.COLUMN_SYMBOL);
                        for (int i = 0; i < count; i++) {
                            data.moveToPosition(i);
                            //if ( data.getLong(dateColumn) == mInitialSelectedDate ) {
                            if (data.getString(symbolColumn) == mInitialSelectedSymbol) {
                                position = i;
                                break;
                            }
                        }
                    }
                    if (position == RecyclerView.NO_POSITION) position = 0;
                    // If we don't need to restart the loader, and there's a desired position to restore
                    // to, do so now.
                    mRecyclerView.smoothScrollToPosition(position);
                    RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(position);
                    if (null != vh && mAutoSelectView) {
                        mStocklistAdapter.selectView(vh);
                    }
                    if (mHoldForTransition) {
                        getActivity().supportStartPostponedEnterTransition();
                    }


                    AppCompatActivity activity = (AppCompatActivity) getActivity();
                    Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

                    // We need to start the enter transition after the data has loaded
                    //if ( mTransitionAnimation ) {
                    activity.supportStartPostponedEnterTransition();

                    if (null != toolbarView) {
                        activity.setSupportActionBar(toolbarView);
                        //getActivity().getSupportActionBar().setTitle(mSymbol);
                        toolbarView.setTitle(R.string.title_activity_list);

                        //activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                        activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
                        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                    //}

                    return true;
                }
                return false;
                }
            });
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mStocklistAdapter.swapCursor(null);
    }

    public void setInitialSelectedSymbol(String initialSelectedSymbol) {
        mInitialSelectedSymbol = initialSelectedSymbol;
    }

    // Update the empty-list view if empty portfolio or server down
    private void updateEmptyView() {
        if ( mStocklistAdapter.getItemCount() == 0 ) {
            TextView tv = (TextView) getView().findViewById(R.id.recyclerview_stocklist_empty);
            if ( null != tv ) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.empty_stocklist;
                @StockHawkSyncAdapter.Status int status = Utility.getStatus(getActivity());
                switch (status) {
                    case StockHawkSyncAdapter.STATUS_SERVER_DOWN:
                        message = R.string.empty_stocklist_server_down;
                        break;
                    default:
                        if (!Utility.isNetworkAvailable(getActivity())) {
                            message = R.string.empty_stocklist_no_network;
                        }
                }
                tv.setText(message);
            }
        }
    }

    public void dataUpdated() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_status_key))) {
            Utility.resetStatus(getContext());
            updateEmptyView();
        }
    }
}
