/* Janice Richards
 *
 *  Project 3: Stock Hawk
 * 
 *
 */

package com.android.stocks.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class StocksDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movies.db";

    public StocksDbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_STOCKS_TABLE =
                "CREATE TABLE "+ StocksContract.StockEntry.TABLE_NAME +
                        "( " + StocksContract.StockEntry.COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StocksContract.StockEntry.COLUMN_SYMBOL + " TEXT KEY, " +
                        StocksContract.StockEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StocksContract.StockEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                        StocksContract.StockEntry.COLUMN_PRICE_CHANGE + " REAL NOT NULL, " +
                        StocksContract.StockEntry.COLUMN_PRICE_PCT_CHANGE + " REAL NOT NULL, " +

                        StocksContract.StockEntry.COLUMN_DIVIDEND + " REAL, " +
                        StocksContract.StockEntry.COLUMN_DIVIDEND_PCT + " REAL, " +
                        StocksContract.StockEntry.COLUMN_EPS + " REAL, " +
                        StocksContract.StockEntry.COLUMN_PE + " REAL, " +
                        StocksContract.StockEntry.COLUMN_TARGET + " REAL, " +
                        StocksContract.StockEntry.COLUMN_MARKET_CAP + " REAL, " +
                        StocksContract.StockEntry.COLUMN_DAY_HIGH + " REAL, " +
                        StocksContract.StockEntry.COLUMN_DAY_LOW + " REAL, " +
                        StocksContract.StockEntry.COLUMN_AVG_VOLUME + " INTEGER, " +
                        StocksContract.StockEntry.COLUMN_OPEN_PRICE + " REAL, " +
                        StocksContract.StockEntry.COLUMN_PREV_CLOSE_PRICE + " REAL, " +
                        StocksContract.StockEntry.COLUMN_VOLUME + " INTEGER, " +
                        StocksContract.StockEntry.COLUMN_YEAR_HIGH + " REAL, " +
                        StocksContract.StockEntry.COLUMN_YEAR_LOW + " REAL, " +
                        StocksContract.StockEntry.COLUMN_STOCK_EXCHANGE + " TEXT, " +
                        StocksContract.StockEntry.COLUMN_BETA + " REAL);";


        db.execSQL(SQL_CREATE_STOCKS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        // just delete all tables if structure changes:
        db.execSQL("DROP TABLE IF EXISTS " + StocksContract.StockEntry.TABLE_NAME);
        onCreate(db);
        return;
    }
}
