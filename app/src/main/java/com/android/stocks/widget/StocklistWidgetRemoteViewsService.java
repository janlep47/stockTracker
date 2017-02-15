package com.android.stocks.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.stocks.R;
import com.android.stocks.Utility;
import com.android.stocks.data.StocksContract;

/**
 * Created by janicerichards on 6/14/16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StocklistWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = StocklistWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] STOCKLIST_COLUMNS = {
            StocksContract.StockEntry.TABLE_NAME + "." + StocksContract.StockEntry._ID,
            StocksContract.StockEntry.COLUMN_SYMBOL,
            StocksContract.StockEntry.COLUMN_NAME,
            StocksContract.StockEntry.COLUMN_PRICE,
            StocksContract.StockEntry.COLUMN_PRICE_CHANGE
    };
    // these indices must match above
    static final int INDEX_STOCK_ID = 0;
    static final int INDEX_STOCK_SYMBOL = 1;
    static final int INDEX_STOCK_NAME = 2;
    static final int INDEX_STOCK_PRICE = 3;
    static final int INDEX_STOCK_PRICE_CHANGE = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(StocksContract.StockEntry.CONTENT_URI,
                        STOCKLIST_COLUMNS,
                        null,
                        null,
                        StocksContract.StockEntry.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);
                String symbol = data.getString(INDEX_STOCK_SYMBOL);
                String name = data.getString(INDEX_STOCK_NAME);
                float price = data.getFloat(INDEX_STOCK_PRICE);
                String priceString = Utility.formatPrice(StocklistWidgetRemoteViewsService.this, price);
                float priceChng = data.getFloat(INDEX_STOCK_PRICE_CHANGE);
                String priceChngString = Utility.formatPrice(StocklistWidgetRemoteViewsService.this, priceChng);

                views.setTextViewText(R.id.widget_symbol, symbol);
                //views.setTextViewText(R.id.widget_name, name);
                views.setTextViewText(R.id.widget_price, priceString);
                //Log.e(LOG_TAG, " R.id.widget_price_change = " + R.id.widget_price_change +
                //        "  priceChngString = " + priceChngString);
                if (priceChng >= 0)
                    views.setTextColor(R.id.widget_price_change,
                            ContextCompat.getColor(getApplicationContext(),R.color.green));
                else
                    views.setTextColor(R.id.widget_price_change,
                            ContextCompat.getColor(getApplicationContext(), R.color.red));
                views.setTextViewText(R.id.widget_price_change, priceChngString);
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                //    setRemoteContentDescription(views, description);
                //}


                final Intent fillInIntent = new Intent();
                Uri stockUri = StocksContract.StockEntry.buildStockInfoWithSymbol(symbol);
                fillInIntent.setData(stockUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                //views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_STOCK_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
