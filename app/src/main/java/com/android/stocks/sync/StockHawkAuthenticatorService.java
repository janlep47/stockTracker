package com.android.stocks.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by janicerichards on 6/7/16, with help from UDACIY code.
 */
public class StockHawkAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private StockHawkAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new StockHawkAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
