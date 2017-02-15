/* Janice Richards
 *
 *  Project 3: Stock Hawk
 * 
 *
 */

package com.android.stocks.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the stocks database.
 */
public class StocksContract {

    // The "Content authority" is a name for the entire content provider.
    public static final String CONTENT_AUTHORITY = "com.android.stocks";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // e.g. content://com.android.stocks/stocks/ is a valid path for at the stock data.
    public static final String PATH_STOCKS = "stocks";


    /* Inner class that defines the table contents of the weather table */
    public static final class StockEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STOCKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCKS;

        public static final String TABLE_NAME = "stocks";


        //_id INTEGER PRIMARY KEY AUTOINCREMENT
        public static final String COLUMN_ID = "_id";

        // primary key:  stock symbol (e.g. AAPL, GOOG):
        public static final String COLUMN_SYMBOL = "symbol";

        // long name
        public static final String COLUMN_NAME = "name";

        // Time, stored as long in milliseconds (for the current stock price listed)
        //public static final String COLUMN_TIME = "date";

        // Price, stored as float (stock price)
        public static final String COLUMN_PRICE = "price";

        // Price change, stored as float
        public static final String COLUMN_PRICE_CHANGE = "priceChange";

        // Percentage price change, stored as float
        public static final String COLUMN_PRICE_PCT_CHANGE = "pricePctChange";

        public static final String COLUMN_DIVIDEND = "dividend";
        public static final String COLUMN_DIVIDEND_PCT = "dividendPct";
        public static final String COLUMN_EPS = "eps";
        public static final String COLUMN_PE = "pe";
        public static final String COLUMN_TARGET = "oneYearTarget";
        public static final String COLUMN_MARKET_CAP = "marketCap";
        public static final String COLUMN_DAY_HIGH = "dayHigh";
        public static final String COLUMN_DAY_LOW = "dayLow";
        public static final String COLUMN_AVG_VOLUME = "avgVolume";
        public static final String COLUMN_OPEN_PRICE = "openPrice";
        public static final String COLUMN_PREV_CLOSE_PRICE = "prevClosePrice";
        public static final String COLUMN_VOLUME = "volume";
        public static final String COLUMN_YEAR_HIGH = "yearHigh";
        public static final String COLUMN_YEAR_LOW = "yearLow";
        public static final String COLUMN_STOCK_EXCHANGE = "stockExchange";
        public static final String COLUMN_BETA = "beta";



        public static Uri buildStocksUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildStockInfoWithSymbol(String stockSymbol) {
            return CONTENT_URI.buildUpon()
                    .appendPath(stockSymbol).build();
        }

        public static String getStockSymbolFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}

