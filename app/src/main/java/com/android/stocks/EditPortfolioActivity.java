package com.android.stocks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by janicerichards on 6/12/16.
 */
public class EditPortfolioActivity  extends AppCompatActivity {

    EditPortfolioFragment mFragment;
    private static final String LOG_TAG = EditPortfolioActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
            arguments.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);

            mFragment = new EditPortfolioFragment();
            mFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_edit_container, mFragment)
                    .commit();
            // animation mode
            supportPostponeEnterTransition();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stockedit, menu);
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
        } else if (id == R.id.action_delete) {

        }
        return super.onOptionsItemSelected(item);
    }
}
