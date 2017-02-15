package com.android.stocks;

/**
 * Created by janicerichards on 6/9/16.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class AddStockActivity extends AppCompatActivity {

    private final String LOG_TAG = AddStockActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
            arguments.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);

            AddStockFragment fragment = new AddStockFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_add_container, fragment)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }
    }
}
