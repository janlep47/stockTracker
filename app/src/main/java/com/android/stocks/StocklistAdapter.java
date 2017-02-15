/* Janice Richards
 *
 *  Project 3: Stock Hawk
 * 
 *
 */

package com.android.stocks;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bumptech.glide.Glide;
import com.android.stocks.data.StocksContract;

public class StocklistAdapter extends RecyclerView.Adapter<StocklistAdapter.StocklistAdapterViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private StocklistAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    final private ItemChoiceManager mICM;

    public static final String LOG_TAG = StocklistAdapter.class.getSimpleName();

    /**
     * Cache of the children views for a stocklist list item.
     */
    public class StocklistAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mSymbolView;
        public final TextView mLongNameView;
        public final TextView mPriceView;
        public final TextView mPriceChngView;


        public StocklistAdapterViewHolder(View view) {
            super(view);
            mSymbolView = (TextView) view.findViewById(R.id.list_item_symbol_textview);
            mLongNameView = (TextView) view.findViewById(R.id.list_item_fullname_textview);
            mPriceView = (TextView) view.findViewById(R.id.list_item_price_textview);
            mPriceChngView = (TextView) view.findViewById(R.id.list_item_price_change_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int symbolColumnIndex = mCursor.getColumnIndex(StocksContract.StockEntry.COLUMN_SYMBOL);
            mClickHandler.onClick(mCursor.getString(symbolColumnIndex), this);
            mICM.onClick(this);
        }
    }

    public static interface StocklistAdapterOnClickHandler {
        void onClick(String stockSymbol, StocklistAdapterViewHolder vh);
    }

    public StocklistAdapter(Context context, StocklistAdapterOnClickHandler dh, View emptyView, int choiceMode) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

    /*
        This takes advantage of the fact that the viewGroup passed to onCreateViewHolder is the
        RecyclerView that will be used to contain the view, so that it can get the current
        ItemSelectionManager from the view.

        One could implement this pattern without modifying RecyclerView by taking advantage
        of the view tag to store the ItemChoiceManager.
     */
    @Override
    public StocklistAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = R.layout.list_item_stocks;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new StocklistAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(StocklistAdapterViewHolder stocklistAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        // Read symbol from cursor
        String stockSymbol = mCursor.getString(StocklistFragment.COL_STOCK_SYMBOL);
        stocklistAdapterViewHolder.mSymbolView.setText(stockSymbol);
        stocklistAdapterViewHolder.mSymbolView.setContentDescription(mContext.getString(R.string.a11y_symbol, stockSymbol));

        String stockName = mCursor.getString(StocklistFragment.COL_STOCK_NAME);
        stocklistAdapterViewHolder.mLongNameView.setText(stockName);

        // Read stock price from cursor
        float price = mCursor.getFloat(StocklistFragment.COL_STOCK_PRICE);
        String priceString = Utility.formatPrice(mContext,price);
        stocklistAdapterViewHolder.mPriceView.setText(priceString);
        stocklistAdapterViewHolder.mPriceView.setContentDescription(mContext.getString(R.string.a11y_price, priceString));

        // Read stock percentage price change from cursor
        float priceChange = mCursor.getFloat(StocklistFragment.COL_STOCK_PRICE_CHANGE);
        String priceChangeString = Utility.formatPrice(mContext, priceChange);
        stocklistAdapterViewHolder.mPriceChngView.setText(priceChangeString);
        stocklistAdapterViewHolder.mPriceChngView.setContentDescription(mContext.getString(R.string.a11y_price_change, priceChangeString));

        if (priceChange > 0.0) {
            stocklistAdapterViewHolder.mPriceChngView.setBackgroundResource(R.color.green);
        } else {
            stocklistAdapterViewHolder.mPriceChngView.setBackgroundResource(R.color.red);
        }
        mICM.onBindViewHolder(stocklistAdapterViewHolder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof StocklistAdapterViewHolder ) {
            StocklistAdapterViewHolder vfh = (StocklistAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
}
