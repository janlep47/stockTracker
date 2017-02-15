package com.android.stocks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.stocks.data.StocksContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by janicerichards on 6/13/16.
 */
public class AddStockFragment extends ListFragment {

    List<StockItem> stockMatches = new ArrayList<>();
    AddStockAdapter mAdapter;
    View mLoadingPanel;
    Context mContext;
    List<StockItem> mEmptyList = new ArrayList<>();
    EditText mStockEntry;
    TextView mProblemText;

    boolean mAddProblem = false;

    private static final String LOG_TAG = AddStockFragment.class.getSimpleName();
    AppCompatActivity mActivity;

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

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_add, container, false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        mActivity = activity;

        Toolbar toolbarView = (Toolbar) root.findViewById(R.id.toolbar);
        mLoadingPanel = (View) root.findViewById(R.id.loadingPanel);

        activity.supportStartPostponedEnterTransition();

        if (null != toolbarView) {
            activity.setSupportActionBar(toolbarView);

            mStockEntry = (EditText) root.findViewById(R.id.symbol_entry);
            ListView mListView = (ListView) root.findViewById(android.R.id.list);
            mProblemText = (TextView) root.findViewById(R.id.add_stock_problem);

            mContext = getContext();
            mAdapter = new AddStockAdapter(mContext,android.R.layout.simple_list_item_2,stockMatches);
            mListView.setAdapter(mAdapter);

            mStockEntry.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (mAddProblem) {
                        mProblemText.setText(R.string.empty_stocklist_server_down);
                    } else{
                        mProblemText.setText("");  // clear out any old "server down" messages
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // EMPTY out old stock matches:
                    stockMatches = new ArrayList<>();
                    mAdapter.notifyDataSetChanged();
                    String abbr = "";
                    for (int i = 0; i < s.length(); i++) {
                        abbr += s.charAt(i);
                    }
                    if (abbr.length() > 0) {
                        new SearchStockSymbolTask().execute(abbr);
                    } else {
                        mAdapter.data = mEmptyList;
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });

            activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return root;
    }


    // List item click means add that stock to the portfolio
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        StockItem item = mAdapter.getItem(position);
        new AddStockSymbolTask().execute(item.getSymbol());
    }









    // AsyncTask<Params, Progress, Result>
    // Params - what you pass to the AsyncTask
    // Progress - if you have any updates; passed to onProgressUpdate()
    // Result - the output; returned by doInBackground()
    //
    private class SearchStockSymbolTask extends AsyncTask<String, Integer, List<StockItem>> {
        private static final String URL1 = "http://d.yimg.com/aq/autoc?query=";
        private static final String URL2 = "&region=US&lang=en-US&callback=YAHOO"+
                ".util.ScriptNodeDataSource.callbacks";
        private String url;
        //private static final String URL =
        //        "http://d.yimg.com/aq/autoc?query=y&region=US&lang=en-US&callback=YAHOO"+
        //                ".util.ScriptNodeDataSource.callbacks";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingPanel.setVisibility(View.VISIBLE);
            //displayProgressBar("downloading ...");
        }

        @Override
        protected List<StockItem> doInBackground(String... params) {
            List<StockItem> possibleStocks;
            String stockSymbolStart = params[0];
            url = URL1+stockSymbolStart+URL2;
            possibleStocks = new StockSymbolsFetchr().getSymbolList(url);
            return possibleStocks;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //updateProgressBar(values[0]);
        }

        @Override
        protected void onPostExecute(List<StockItem> result) {
            stockMatches = result;
            mAdapter.data = stockMatches;
            // invalidate the list adapter:
            mAdapter.notifyDataSetChanged();
            mLoadingPanel.setVisibility(View.GONE);
            super.onPostExecute(result);
            //dismissProgressBar();
        }
    }


    // AsyncTask<Params, Progress, Result>
    // Params - what you pass to the AsyncTask
    // Progress - if you have any updates; passed to onProgressUpdate()
    // Result - the output; returned by doInBackground()
    //
    private class AddStockSymbolTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            String stockSymbol = params[0];
            // Before adding it, make sure not already in DB
            Cursor cursor = getContext().getContentResolver().query(StocksContract.StockEntry.CONTENT_URI,
                    new String[]{StocksContract.StockEntry.COLUMN_SYMBOL},
                    null, null, StocksContract.StockEntry.COLUMN_SYMBOL + " ASC");
            // Now, get the cursor data in the symbols array, for call to YahooFinance
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String symbol = cursor.getString(0);
                    // if symbol aready in db, don't add and return:
                    if (symbol.equals(stockSymbol)) return new Integer(1);
                } while (cursor.moveToNext());
            }

            Stock stock=null;
            try {
                stock = YahooFinance.get(stockSymbol);
            } catch (IOException e) {
                Log.e(LOG_TAG, "error calling YahooFinance ...");
                Integer result = new Integer(-1);
                return result;
            }
            // NOW, add it to the database:
            StockItem.addStockToDb(mContext,stock);
            // If added OK, set result to 0; otherwise, set to -1
            mAddProblem = false;
            Integer result = new Integer(0);
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingPanel.setVisibility(View.VISIBLE);
            //displayProgressBar("downloading ...");
        }


        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            // If added successfully, end this activity, and go back to the calling activity:
            if (result.intValue() == 0) mActivity.finish();
            else {
                Log.e(LOG_TAG," DIDN'T add OK!!   --- should we put up a dialog box here?...");
                mAddProblem = true;
            }
            //mLoadingPanel.setVisibility(View.GONE);
            //super.onPostExecute(result);
        }
    }

}