/* Janice Richards
 *
 *  Project 3: Stock Hawk
 * 
 *
 */

package com.android.stocks;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.stocks.sync.StockHawkSyncAdapter;


public class MainActivity extends AppCompatActivity implements StocklistFragment.Callback  {
    private StocklistFragment mFragment;

        private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mFragment = ((StocklistFragment)getSupportFragmentManager()
                 .findFragmentById(R.id.fragment_stocklist));

        StockHawkSyncAdapter.initializeSyncAdapter(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stocklist, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            startActivity(new Intent(this, AddStockActivity.class));
            mFragment.dataUpdated();
            return true;
        } else if (id == R.id.action_edit) {
            startActivity(new Intent(this, EditPortfolioActivity.class));
            return true;
        } else if (id == R.id.about)     {
            AboutDialogFragment dialog = new AboutDialogFragment();
            dialog.show(getSupportFragmentManager(),"About:");
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onItemSelected(Uri contentUri, StocklistAdapter.StocklistAdapterViewHolder vh) {
        Intent intent = new Intent(this, DetailActivity.class)
                .setData(contentUri);

        ActivityCompat.startActivity(this, intent, null);

    }


}
