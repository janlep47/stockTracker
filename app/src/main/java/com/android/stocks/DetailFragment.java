package com.android.stocks;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.stocks.data.StocksContract;
import com.android.stocks.data.StocksContract.StockEntry;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by janicerichards on 6/11/16.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";

    //private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mSymbol;
    private Uri mUri;
    private boolean mTransitionAnimation;

    public static final int CHART_WIDTH = 350;
    public static final int CHART_HEIGHT = 205;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            //StockEntry.TABLE_NAME + "." + StockEntry._ID,
            StockEntry.COLUMN_SYMBOL,
            StockEntry.COLUMN_NAME,
            StockEntry.COLUMN_PRICE,
            StockEntry.COLUMN_PRICE_CHANGE,
            StockEntry.COLUMN_PRICE_PCT_CHANGE,

            StockEntry.COLUMN_DIVIDEND,
            StockEntry.COLUMN_DIVIDEND_PCT,
            StockEntry.COLUMN_EPS,
            StockEntry.COLUMN_PE,
            StockEntry.COLUMN_TARGET,
            StockEntry.COLUMN_MARKET_CAP,
            StockEntry.COLUMN_DAY_HIGH,
            StockEntry.COLUMN_DAY_LOW,
            StockEntry.COLUMN_AVG_VOLUME,
            StockEntry.COLUMN_OPEN_PRICE,
            StockEntry.COLUMN_PREV_CLOSE_PRICE,
            StockEntry.COLUMN_VOLUME,
            StockEntry.COLUMN_YEAR_HIGH,
            StockEntry.COLUMN_YEAR_LOW,
            StockEntry.COLUMN_STOCK_EXCHANGE,
            StockEntry.COLUMN_BETA
    };


    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_STOCK_SYMBOL = 0;
    public static final int COL_STOCK_NAME = 1;
    public static final int COL_STOCK_PRICE = 2;
    public static final int COL_STOCK_PRICE_CHANGE = 3;
    public static final int COL_STOCK_PRICE_PCT_CHANGE = 4;

    public static final int COL_STOCK_DIVIDEND = 5;
    public static final int COL_STOCK_DIVIDEND_PCT = 6;
    public static final int COL_STOCK_EPS = 7;
    public static final int COL_STOCK_PE = 8;
    public static final int COL_STOCK_TARGET = 9;
    public static final int COL_STOCK_MARKET_CAP = 10;
    public static final int COL_STOCK_DAY_HIGH = 11;
    public static final int COL_STOCK_DAY_LOW = 12;
    public static final int COL_STOCK_AVG_VOLUME = 13;
    public static final int COL_STOCK_OPEN_PRICE = 14;
    public static final int COL_STOCK_PREV_CLOSE_PRICE = 15;
    public static final int COL_STOCK_VOLUME = 16;
    public static final int COL_STOCK_YEAR_HIGH = 17;
    public static final int COL_STOCK_YEAR_LOW = 18;
    public static final int COL_STOCK_STOCK_EXCHANGE = 19;
    public static final int COL_STOCK_BETA = 20;

    private static final int CHART_MIN_WIDTH = 1300;
    private static final int CHART_MIN_HEIGHT = 500;


    private ImageView mChartView;

    //private TextView mDateView;
    private TextView mSymbolView;
    private TextView mNameView;
    private TextView mPriceView;
    private TextView mPriceChngView;
    private TextView mPricePctChngView;

    private Button m1DButton;
    private Button m5DButton;
    private Button m3MButton;
    private Button m6MButton;
    private Button m1YButton;
    private Button m5YButton;
    private Button mMaxButton;

    private View mLoadingPanel;

    private TextView mDividendView;
    private TextView mDividendPctView;
    private TextView mEpsView;
    private TextView mPeView;
    private TextView mTargetView;
    private TextView mMktCapView;
    private TextView mHighView;
    private TextView mLowView;
    private TextView mAvgVolView;
    private TextView mOpenPriceView;
    private TextView mPrevClosePriceView;
    private TextView mVolumeView;
    private TextView mYearHighView;
    private TextView mYearLowView;
    private TextView mStockExchangeView;
    private TextView mBetaView;

    private String chartType = "1D";
    private boolean needToLoadChart = true;
    private String chartUrl;
    private static final String BASE_CHART_URL = "https://chart.finance.yahoo.com/z?s=";
    private static final String BASE_CHART_URL_END = "&q=l&l=off";


    public DetailFragment() {
        setHasOptionsMenu(true);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        if (savedInstanceState == null) needToLoadChart = true;
        else needToLoadChart = false;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mChartView = (ImageView) rootView.findViewById(R.id.detail_chart);
        mLoadingPanel = (View) rootView.findViewById(R.id.loadingPanel);
        mPriceView = (TextView) rootView.findViewById(R.id.detail_current_price);
        mPriceChngView = (TextView) rootView.findViewById(R.id.detail_price_change);

        m1DButton = (Button) rootView.findViewById(R.id.detail_graph_button_1D);
        m5DButton = (Button) rootView.findViewById(R.id.detail_graph_button_5D);
        m3MButton = (Button) rootView.findViewById(R.id.detail_graph_button_3M);
        m6MButton = (Button) rootView.findViewById(R.id.detail_graph_button_6M);
        m1YButton = (Button) rootView.findViewById(R.id.detail_graph_button_1Y);
        m5YButton = (Button) rootView.findViewById(R.id.detail_graph_button_5Y);
        mMaxButton = (Button) rootView.findViewById(R.id.detail_graph_button_MAX);

        m1DButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {loadChart(chartUrl + "&t=1d" +BASE_CHART_URL_END);}
        });
        m5DButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {loadChart(chartUrl + "&t=5d" +BASE_CHART_URL_END);    }
        });
        m3MButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {loadChart(chartUrl + "&t=3m" +BASE_CHART_URL_END);    }
        });
        m6MButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {loadChart(chartUrl + "&t=6m" +BASE_CHART_URL_END);    }
        });
        m1YButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {loadChart(chartUrl + "&t=1y" +BASE_CHART_URL_END);    }
        });
        m5YButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {loadChart(chartUrl + "&t=5y" +BASE_CHART_URL_END);    }
        });
        mMaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {loadChart(chartUrl + "&t=my" +BASE_CHART_URL_END); }
        });

        //mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mOpenPriceView = (TextView) rootView.findViewById(R.id.open_textview);
        mPrevClosePriceView = (TextView) rootView.findViewById(R.id.prev_close_textview);
        mHighView = (TextView) rootView.findViewById(R.id.high_textview);
        mLowView = (TextView) rootView.findViewById(R.id.low_textview);
        mYearHighView = (TextView) rootView.findViewById(R.id.year_high_textview);
        mYearLowView = (TextView) rootView.findViewById(R.id.year_low_textview);
        mMktCapView = (TextView) rootView.findViewById(R.id.mkt_cap_textview);
        mVolumeView = (TextView) rootView.findViewById(R.id.volume_textview);
        mTargetView = (TextView) rootView.findViewById(R.id.target_textview);
        mAvgVolView = (TextView) rootView.findViewById(R.id.avg_vol_textview);
        mPeView = (TextView) rootView.findViewById(R.id.pe_textview);
        mEpsView = (TextView) rootView.findViewById(R.id.eps_textview);
        //mBetaView = (TextView) rootView.findViewById(R.id.beta_textview);
        mDividendView = (TextView) rootView.findViewById(R.id.dividend_textview);
        //mDividendPctView = (TextView) rootView.findViewById(R.id.dividend_pct_textview);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            mSymbol = data.getString(COL_STOCK_SYMBOL);

            float currentPrice = data.getFloat(COL_STOCK_PRICE);
            String currentPriceString = Utility.formatPrice(getActivity(), currentPrice);
            mPriceView.setText(" "+currentPriceString);

            float priceChng = data.getFloat(COL_STOCK_PRICE_CHANGE);
            float priceChngPct = data.getFloat(COL_STOCK_PRICE_PCT_CHANGE);
            String priceChngInfo = Utility.formatPriceChangeInfo(getActivity(), priceChng, priceChngPct);
            mPriceChngView.setText(priceChngInfo);
            if (priceChng > 0)
                mPriceChngView.setTextColor(
                        ContextCompat.getColor(getActivity(), R.color.green));
            else
                mPriceChngView.setTextColor(
                        ContextCompat.getColor(getActivity(), R.color.red));

            // Read date from cursor and update views
            float open = data.getFloat(COL_STOCK_OPEN_PRICE);
            String openString = Utility.formatPrice(getActivity(),open);
            mOpenPriceView.setText(openString);
            mOpenPriceView.setContentDescription(getString(R.string.a11y_open, openString));

            float prevClose = data.getFloat(COL_STOCK_PREV_CLOSE_PRICE);
            String prevCloseString = Utility.formatPrice(getActivity(),prevClose);
            mPrevClosePriceView.setText(prevCloseString);
            mPrevClosePriceView.setContentDescription(getString(R.string.a11y_close, prevCloseString));

            float high = data.getFloat(COL_STOCK_DAY_HIGH);
            String highString = Utility.formatPrice(getActivity(),high);
            mHighView.setText(highString);
            mHighView.setContentDescription(getString(R.string.a11y_high, highString));

            float low = data.getFloat(COL_STOCK_DAY_LOW);
            String lowString = Utility.formatPrice(getActivity(), low);
            mLowView.setText(lowString);
            mLowView.setContentDescription(getString(R.string.a11y_low, lowString));

            float yearHigh = data.getFloat(COL_STOCK_YEAR_HIGH);
            String yearHighString = Utility.formatPrice(getActivity(), yearHigh);
            mYearHighView.setText(yearHighString);
            mYearHighView.setContentDescription(getString(R.string.a11y_year_high));

            float yearLow = data.getFloat(COL_STOCK_YEAR_LOW);
            String yearLowString = Utility.formatPrice(getActivity(), yearLow);
            mYearLowView.setText(yearLowString);
            mYearLowView.setContentDescription(getString(R.string.a11y_year_low));

            double mktCap = data.getDouble(COL_STOCK_MARKET_CAP);
            String mktCapString = Utility.formatLargeNumber(getActivity(), mktCap);
            mMktCapView .setText(mktCapString);
            mMktCapView.setContentDescription(getString(R.string.a11y_mkt_cap));

            long volume = data.getLong(COL_STOCK_VOLUME);
            String volumeString = Utility.formatLargeNumber(getActivity(), volume);
            mVolumeView.setText(String.valueOf(volumeString));
            mVolumeView.setContentDescription(getString(R.string.a11y_volume));

            float target = data.getFloat(COL_STOCK_TARGET);
            String targetString = Utility.formatPrice(getActivity(), target);
            mTargetView.setText(targetString);
            mTargetView.setContentDescription(getString(R.string.a11y_target));

            long avgVolume = data.getLong(COL_STOCK_AVG_VOLUME);
            String avgVolumeString = Utility.formatLargeNumber(getActivity(), avgVolume);
            mAvgVolView.setText(String.valueOf(avgVolumeString));
            mVolumeView.setContentDescription(getString(R.string.a11y_avg_vol));

            float pe = data.getFloat(COL_STOCK_PE);
            String peString = Utility.formatPrice(getActivity(), pe);
            mPeView.setText(peString);
            mPeView.setContentDescription(getString(R.string.a11y_pe));

            float eps = data.getFloat(COL_STOCK_EPS);
            String epsString = Utility.formatPrice(getActivity(), eps);
            mEpsView.setText(epsString);
            mEpsView.setContentDescription(getString(R.string.a11y_eps));

            //float beta = data.getFloat(COL_STOCK_BETA);
            //String betaString = Utility.formatPrice(getActivity(), beta);
            //mBetaView.setText(betaString);
            //mBetaView.setContentDescription(getString(R.string.a11y_beta));

            float dividend = data.getFloat(COL_STOCK_DIVIDEND);
            float dividendPct = data.getFloat(COL_STOCK_DIVIDEND_PCT);
            String dividendString = Utility.formatDividend(getActivity(), dividend, dividendPct);
            mDividendView.setText(dividendString);
            mDividendView.setContentDescription(getString(R.string.a11y_dividend));

            if (needToLoadChart) {
                chartUrl = BASE_CHART_URL;
                chartUrl += mSymbol.toLowerCase();
                // start out with the 1-day chart:
                loadChart(chartUrl +"&t=1d" +BASE_CHART_URL_END);
            }

        }
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if ( mTransitionAnimation ) {
            activity.supportStartPostponedEnterTransition();

            if ( null != toolbarView ) {
                activity.setSupportActionBar(toolbarView);
                activity.getSupportActionBar().setTitle(mSymbol);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private void loadChart(String url) {
        mLoadingPanel.setVisibility(View.VISIBLE);
        //350x200
        Picasso.with(getActivity())
                .load(url)
                .fit()
                .placeholder(R.drawable.blank_chart)
                //.noFade().resizeDimen(R.dimen.chart_width,R.dimen.chart_height)
                .error(R.drawable.blank_chart)
                .into(mChartView);
        mChartView.setMinimumHeight(CHART_MIN_HEIGHT);
        mChartView.setMinimumWidth(CHART_MIN_WIDTH);
        mChartView.setScaleType(ImageView.ScaleType.FIT_XY);
        mLoadingPanel.setVisibility(View.GONE);
    }

}