package com.android.stocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.android.stocks.sync.StockHawkSyncAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {
    public static String formatPrice(Context context, float price) {
        return String.format(context.getString(R.string.format_price), price);
    }

    //public static String formatPctPriceChange(Context context, float pctPriceChange) {
    //    return String.format(context.getString(R.string.format_pct_price_change), pctPriceChange);
    //}

    public static String formatDividend(Context context, float dividend, float dividendPct) {
        String d1 = String.format(context.getString(R.string.format_price), dividend);
        String d2 = String.format(context.getString(R.string.format_price), dividendPct);
        //return String.format(context.getString(R.string.format_dividend), dividend, dividendPct);
        return d1 + "(" + d2 + "%)";
    }

    public static String formatPriceChangeInfo(Context context, float priceChng, float priceChngPct) {
        String d1 = String.format(context.getString(R.string.format_price), priceChng);
        String d2 = String.format(context.getString(R.string.format_price), priceChngPct);
        if (priceChng > 0)
            return "     +" + d1 + "  (+" + d2 + "%)";
        else
            return "     -" + d1 + "  (-" + d2 + "%)";
    }

    public static String formatLargeNumber(Context context, double lrgNumber) {
        // billion
        if (lrgNumber > 1000000000.00) {
            double shorterNumber = lrgNumber / 1000000000.00;
            long shorterVal = Math.round(shorterNumber);
            return String.format(context.getString(R.string.format_intb_number), shorterVal);
            //return String.valueOf(shorterVal) + "B";
        } else if (lrgNumber > 1000000.00) {
            double shorterNumber = lrgNumber / 1000000.00;
            long shorterVal = Math.round(shorterNumber);
            return String.format(context.getString(R.string.format_intm_number), shorterVal);
            //return String.valueOf(shorterVal) + "M";
        } else if (lrgNumber > 1000.00) {
            double shorterNumber = lrgNumber / 1000.00;
            long shorterVal = Math.round(shorterNumber);
            return String.format(context.getString(R.string.format_intk_number), shorterVal);
            //return String.valueOf(shorterVal) + "K";
        } else {
            return String.format(context.getString(R.string.format_price), lrgNumber);
        }
    }

    public static String formatLargeNumber(Context context, long lrgNumber) {
        // billion
        if (lrgNumber > 1000000000) {
            double shorterNumber = lrgNumber / 1000000000.00;
            long shorterVal = Math.round(shorterNumber);
            return String.format(context.getString(R.string.format_intb_number), shorterVal);
            //return String.valueOf(shorterVal) + "B";
        } else if (lrgNumber > 1000000) {
            double shorterNumber = lrgNumber / 1000000.00;
            long shorterVal = Math.round(shorterNumber);
            return String.format(context.getString(R.string.format_intm_number), shorterVal);
            //return String.valueOf(shorterVal) + "M";
        } else if (lrgNumber > 1000) {
            double shorterNumber = lrgNumber / 1000.00;
            long shorterVal = Math.round(shorterNumber);
            return String.format(context.getString(R.string.format_intk_number), shorterVal);
            //return String.valueOf(shorterVal) + "K";
        } else {
            return String.valueOf(lrgNumber);
        }
    }


    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * @param c Context used to get the SharedPreferences
     * @return the status integer type
     */

    @SuppressWarnings("ResourceType")
    static public
    @StockHawkSyncAdapter.Status
    int getStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_status_key), StockHawkSyncAdapter.STATUS_UNKNOWN);
    }

    /**
     * Resets the network status.  (Sets it to StockHawkSyncAdapter.STATUS_UNKNOWN)
     *
     * @param c Context used to get the SharedPreferences
     */

    static public void resetStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_status_key), StockHawkSyncAdapter.STATUS_UNKNOWN);
        spe.apply();
    }
}