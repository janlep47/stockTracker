package com.android.stocks.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by janicerichards on 6/7/16, with help from UDACITY code.
 */
public class StockHawkSyncService  extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static StockHawkSyncAdapter sStockHawkSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sStockHawkSyncAdapter == null) {
                sStockHawkSyncAdapter = new StockHawkSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sStockHawkSyncAdapter.getSyncAdapterBinder();
    }
}