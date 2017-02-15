/* Janice Richards
 *
 *  Project 3: Stock Hawk
 * 
 *
 */

package com.android.stocks.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class StocksProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private StocksDbHelper mOpenHelper;

    static final int STOCKS = 100;              // PATH  stock path (DIR)
    static final int STOCKS_WITH_SYMBOL = 101;  // PATH/*  stock path followed by a String (ITEM)

    public static final String LOG_TAG = StocksProvider.class.getSimpleName();


    //stocks.symbol = ?
    private static final String sStockBySymbolSelection =
            StocksContract.StockEntry.TABLE_NAME+
                    "." + StocksContract.StockEntry.COLUMN_SYMBOL + " = ? ";

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StocksContract.CONTENT_AUTHORITY;

        // Create a corresponding code.
        matcher.addURI(authority, StocksContract.PATH_STOCKS, STOCKS);
        matcher.addURI(authority, StocksContract.PATH_STOCKS + "/*", STOCKS_WITH_SYMBOL);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new StocksDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case STOCKS:
                return StocksContract.StockEntry.CONTENT_TYPE;        // DIR
            case STOCKS_WITH_SYMBOL:
                return StocksContract.StockEntry.CONTENT_ITEM_TYPE;   // ITEM
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Cursor getStockSymbols(Uri uri) {
        Cursor cursor = mOpenHelper.getReadableDatabase().query(
                StocksContract.StockEntry.TABLE_NAME,
                new String[] {StocksContract.StockEntry.COLUMN_SYMBOL},
                null,null,
                null, null, "ASC");
        return cursor;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "stocks"
            case STOCKS:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                    StocksContract.StockEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, sortOrder);
                break;
            }
            // "stocks/*"
            case STOCKS_WITH_SYMBOL:
            {
                retCursor = getStockByStockSymbol(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    private Cursor getStockByStockSymbol(Uri uri, String[] projection, String sortOrder) {
        String stockSymbol = StocksContract.StockEntry.getStockSymbolFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(StocksContract.StockEntry.TABLE_NAME,
                projection, sStockBySymbolSelection, new String[] {stockSymbol}, null, null, sortOrder);
    }
        

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case STOCKS: {
                long _id = db.insert(StocksContract.StockEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) returnUri = StocksContract.StockEntry.buildStocksUri(_id);
                else throw new android.database.SQLException("Failed to insert row into (stocks)" + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // (don't use returnUri in call below, or else won't notify the Cursor(s) of the change.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case STOCKS:
                rowsDeleted = db.delete(
                        StocksContract.StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) getContext().getContentResolver().notifyChange(uri, null);
        Log.e(LOG_TAG,String.valueOf(rowsDeleted) + " DB ROWS DELETED ...");
        return rowsDeleted;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case STOCKS:
                rowsUpdated = db.update(StocksContract.StockEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCKS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(StocksContract.StockEntry.TABLE_NAME, null, value);
                        if (_id != -1) returnCount++;  
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                throw new UnsupportedOperationException(" (bulkInsert) unknown uri: "+uri);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
