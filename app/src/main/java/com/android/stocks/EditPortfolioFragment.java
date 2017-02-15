package com.android.stocks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.stocks.data.StocksContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by janicerichards on 6/12/16.
 */
public class EditPortfolioFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();


    // This is the Adapter being used to display the list's data
    private SimpleCursorAdapter mAdapter;
    private ListView mListView;
    private Toolbar mToolbarView;
    private AppCompatActivity mActivity;
    private Context mContext;
    private boolean mDeleting = false;

    private List<String> listSelectedStocks = new ArrayList<>();

    private static final String[] DETAIL_COLUMNS = {
            StocksContract.StockEntry.COLUMN_SYMBOL,
            StocksContract.StockEntry.COLUMN_NAME
    };


    // These are the Stock symbol/name rows that we will retrieve
    static final String[] PROJECTION = new String[] {
            StocksContract.StockEntry.COLUMN_ID,
            StocksContract.StockEntry.COLUMN_SYMBOL,
            StocksContract.StockEntry.COLUMN_NAME};

    // This is the select criteria
    static final String SELECTION = null;

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_STOCK_ID = 0;
    public static final int COL_STOCK_SYMBOL = 1;
    public static final int COL_STOCK_NAME = 2;


    public EditPortfolioFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_edit,container,false);

        // For the cursor adapter, specify which columns go into which views
        //String[] fromColumns = {StocksContract.StockEntry.COLUMN_SYMBOL,
         //       StocksContract.StockEntry.COLUMN_NAME};
        int[] toViews = {android.R.id.text1,
                android.R.id.text2}; // The TextViews in simple_list_item_2
        mListView = (ListView) root.findViewById(android.R.id.list);

        mAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_2,null,
                DETAIL_COLUMNS, toViews, 0);
        setListAdapter(mAdapter);

        mListView.setAdapter(mAdapter);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);

        mActivity = (AppCompatActivity)getActivity();
        mToolbarView = (Toolbar) root.findViewById(R.id.toolbar);

        mActivity.supportStartPostponedEnterTransition();

        mContext = getContext();
        Menu menu = mToolbarView.getMenu();
        if ( null != menu ) menu.clear();
        mToolbarView.inflateMenu(R.menu.stockedit);

        if ( null != mToolbarView ) {
            mActivity.setSupportActionBar(mToolbarView);

            mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return root;
    }


    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(), StocksContract.StockEntry.CONTENT_URI,
                PROJECTION, SELECTION, null, null);
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }


    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }


    private boolean inList(String stock) {
        for (int i=0; i<listSelectedStocks.size(); i++) {
            if (listSelectedStocks.get(i).equals(stock)) {
                // selection of this stock was toggled, so no longer selected.
                listSelectedStocks.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mDeleting) {
            menu.getItem(0).setVisible(false);  menu.getItem(0).setEnabled(false);
            menu.getItem(1).setVisible(true);   menu.getItem(1).setEnabled(true);
        } else {
            menu.getItem(1).setVisible(false);  menu.getItem(1).setEnabled(false);
            menu.getItem(0).setVisible(true);   menu.getItem(0).setEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // trash button hit:
            case R.id.action_delete:
                // For each stock symbol in listSelectedStocks, delete it from the DB
                for (int i=0; i<listSelectedStocks.size(); i++) {
                    String stockToDelete = listSelectedStocks.get(i);
                    new DeleteStockSymbolTask().execute(stockToDelete);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor data = mAdapter.getCursor();
        data.moveToPosition(position);
        String selectedStock = data.getString(COL_STOCK_SYMBOL);

        if (!inList(selectedStock)) {
            v.setBackgroundResource(android.R.color.darker_gray);
            listSelectedStocks.add(selectedStock);
        } else {
            v.setBackgroundResource(android.R.color.background_light);
        }

        if (listSelectedStocks.size() == 1) {
            mDeleting = true;
            getActivity().invalidateOptionsMenu();
            mActivity.getSupportActionBar().setTitle(R.string.delete_stock_msg);
        } else if (listSelectedStocks.size() == 0) {
            mDeleting = false;
            getActivity().invalidateOptionsMenu();
            mActivity.getSupportActionBar().setTitle(R.string.title_activity_edit_portfolio);
        }
        super.onListItemClick(l,v,position,id);
    }

    public void dataUpdated() {
        mAdapter.notifyDataSetChanged();
    }




    // AsyncTask<Params, Progress, Result>
    // Params - what you pass to the AsyncTask
    // Progress - if you have any updates; passed to onProgressUpdate()
    // Result - the output; returned by doInBackground()
    //
    private class DeleteStockSymbolTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            String stockSymbol = params[0];
            // NOW, delete it from the database:
            StockItem.deleteStockFromDb(mContext,stockSymbol);
            // If deleted OK, set result to 0; otherwise, set to -1
            Integer result = new Integer(0);
            return result;
        }


        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            // If added successfully, end this activity, and go back to the calling activity:
            if (result.intValue() == 0) mActivity.finish();
            else {
                Log.e(LOG_TAG," DIDN'T delete OK!! - could add error message ...");
            }
        }
    }


}
