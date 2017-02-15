/* Janice Richards
 *
 *  Project 3: Stock Hawk
 * 
 *
 */
package com.android.stocks.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import yahoofinance.YahooFinance;
import yahoofinance.Stock;

import com.android.stocks.R;
import com.android.stocks.StockItem;
import com.android.stocks.Utility;
import com.android.stocks.data.StocksContract;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;


import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Vector;


public class StockHawkSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String LOG_TAG = StockHawkSyncAdapter.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED =
            "com.android.stocks.ACTION_DATA_UPDATED";
    // Interval at which to sync with the stock information, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    //public static final int SYNC_INTERVAL = 60 * 180;    // every 3 HR.  ... FOR NOW!
    public static final int SYNC_INTERVAL = 60;    // every 1 MIN.  !!!

    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private String[] symbols;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_OK, STATUS_SERVER_DOWN, STATUS_SERVER_INVALID,  STATUS_UNKNOWN, STATUS_INVALID})
    public @interface Status {}

    public static final int STATUS_OK = 0;
    public static final int STATUS_SERVER_DOWN = 1;
    public static final int STATUS_SERVER_INVALID = 2;
    public static final int STATUS_UNKNOWN = 3;
    public static final int STATUS_INVALID = 4;


    public StockHawkSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //Log.e(LOG_TAG, "      ********    Starting sync         ***** ");
        Cursor cursor = getContext().getContentResolver().query(StocksContract.StockEntry.CONTENT_URI,
                new String[]{StocksContract.StockEntry.COLUMN_SYMBOL},
                null, null, StocksContract.StockEntry.COLUMN_SYMBOL + " ASC");
        // Now, get the cursor data in the symbols array, for call to YahooFinance
        if (cursor != null && cursor.moveToFirst()) {
            symbols = new String[cursor.getCount()];
            for (int i=0; i<symbols.length; i++) {
                String symbol = cursor.getString(0);
                symbols[i] = symbol;
                cursor.moveToNext();
            }
        }
        cursor.close();
        if (symbols == null) {updateWidgets(); return;}
        if (symbols.length == 0) {updateWidgets(); return;}

        try {
            Map<String, Stock> stocks = YahooFinance.get(symbols); // single request
            updateStockData(stocks);
        } catch (IOException e) {
            Log.e(LOG_TAG, "CALL TO YahooFinance DID NOT WORK !!!");
            setStatus(getContext(), STATUS_SERVER_DOWN);
            return;
        }
        setStatus(getContext(), STATUS_OK);
        return;
    }

    private void updateStockData(Map<String, Stock> stocks){

        // Insert the new stock information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(symbols.length);

        for (int i = 0; i < symbols.length; i++) {
            Stock stock = stocks.get(symbols[i]);

            ContentValues stockValues = StockItem.getStockValues(getContext(),stock);
            cVVector.add(stockValues);

        }
        // add to database
        if ( cVVector.size() > 0 ) {
            // first, delete ALL old data so we don't build up an endless history
            getContext().getContentResolver().delete(StocksContract.StockEntry.CONTENT_URI, null, null);

            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(StocksContract.StockEntry.CONTENT_URI, cvArray);

            updateWidgets();
        }
        //Log.e(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");  // REMOVE LATER!
        setStatus(getContext(), STATUS_OK);
        return;
    }


    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
            //
            // Add the account and account type, no password or user data
            // If successful, return the Account object, otherwise report an error.
            //
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            //
            // If you don't set android:syncable="true" in
            // in your <provider> element in the manifest,
            // then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
            // here.
            //
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }



    private static void onAccountCreated(Account newAccount, Context context) {
        //
        // Since we've created an account
        //
        StockHawkSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        //
        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        //
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        //
        // Finally, let's do a sync to get things started
        //
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    //
    // Sets the location status into shared preference.  This function should not be called from
    // the UI thread because it uses commit to write to the shared preferences.
    // @param c Context to get the PreferenceManager from.
    // @param locationStatus The IntDef value to set
    //

    static private void setStatus(Context c, @Status int status){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_status_key), status);
        spe.commit();
    }

}
