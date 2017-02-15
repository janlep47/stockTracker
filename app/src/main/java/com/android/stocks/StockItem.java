package com.android.stocks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.android.stocks.data.StocksContract;

import java.math.BigDecimal;

import yahoofinance.Stock;
import yahoofinance.quotes.stock.StockDividend;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.quotes.stock.StockStats;

/**
 * Created by janicerichards on 6/13/16.
 */
public class StockItem {
    private String symbol;
    private String name;

    public String getSymbol() { return symbol; }

    public String getName() { return name; }

    protected void setSymbol(String symbolVal) { symbol = symbolVal; }

    protected void setName(String nameVal) { name = nameVal; }

    public static void deleteStockFromDb(Context context, String symbol) {
        int rowsDeleted = context.getContentResolver().delete(
                StocksContract.StockEntry.CONTENT_URI,
                StocksContract.StockEntry.COLUMN_SYMBOL + " = ?",
                new String[]{symbol});
    }

    public static void addStockToDb(Context context, Stock stock) {
        ContentValues stockValues = getStockValues(context,stock);
        Uri uri = context.getContentResolver().insert(StocksContract.StockEntry.CONTENT_URI, stockValues);
        return;
    }


    public static ContentValues getStockValues(Context context, Stock stock) {
        String symbol = stock.getSymbol();
        String longName = stock.getName();
        StockQuote quote = stock.getQuote();


        float price = quote.getPrice().floatValue();
        float priceChange = quote.getChange().floatValue();
        float pctPriceChange = quote.getChangeInPercent().floatValue();

        StockDividend stockDividend = stock.getDividend();
        float dividend, dividendPct;
        if (stockDividend != null) {
            BigDecimal annualYield = stockDividend.getAnnualYield();
            if (annualYield != null) dividend = annualYield.floatValue();
            else dividend = 0;
            //dividend = stockDividend.getAnnualYield().floatValue();
            BigDecimal annualYieldPercent = stockDividend.getAnnualYieldPercent();
            if (annualYieldPercent != null) dividendPct = annualYieldPercent.floatValue();
            else dividendPct = 0;
            //dividendPct = stockDividend.getAnnualYieldPercent().floatValue();
        } else {
            dividend = 0; dividendPct = 0;
        }
        StockStats stockStats = stock.getStats();
        float eps, pe, oneYearTarget, marketCap;
        if (stockStats != null) {
            BigDecimal stat = stockStats.getEps();
            if (stat != null) eps = stat.floatValue();
            else eps = 0;
            //eps = stockStats.getEps().floatValue();
            stat = stockStats.getPe();
            if (stat != null) pe = stat.floatValue();
            else pe = 0;
            //pe = stockStats.getPe().floatValue();
            stat = stockStats.getOneYearTargetPrice();
            if (stat != null) oneYearTarget = stat.floatValue();
            else oneYearTarget = 0;
            //oneYearTarget = stockStats.getOneYearTargetPrice().floatValue();
            stat = stockStats.getMarketCap();
            if (stat != null) marketCap = stat.floatValue();
            else marketCap = 0;
            //marketCap = stockStats.getMarketCap().floatValue();
        } else {
            eps = 0; pe = 0; oneYearTarget = 0; marketCap = 0;
        }
        float dayHigh, dayLow, openPrice, prevClosePrice, yearHigh, yearLow;
        long avgVolume, volume;
        if (quote != null) {
            BigDecimal stat = quote.getDayHigh();
            if (stat != null) dayHigh = stat.floatValue();
            else dayHigh = 0;
            //float dayHigh = quote.getDayHigh().floatValue();
            stat = quote.getDayLow();
            if (stat != null) dayLow = stat.floatValue();
            else dayLow = 0;
            //float dayLow = quote.getDayLow().floatValue();

            Long avgVolumeLong = (Long) quote.getAvgVolume();
            if (avgVolumeLong != null)
                avgVolume = avgVolumeLong.longValue();
            else
                avgVolume = 0;

            stat = quote.getOpen();
            if (stat != null) openPrice = stat.floatValue();
            else openPrice = 0;
            //float openPrice = quote.getOpen().floatValue();
            stat = quote.getPreviousClose();
            if (stat != null) prevClosePrice = stat.floatValue();
            else prevClosePrice = 0;
            //float prevClosePrice = quote.getPreviousClose().floatValue();
            
            Long volumeLong = quote.getVolume();
            if (volumeLong != null)
                volume = volumeLong.longValue();
            else
                volume = 0;

            stat = quote.getYearHigh();
            if (stat != null) yearHigh = stat.floatValue();
            else yearHigh = 0;
            //float yearHigh = quote.getYearHigh().floatValue();
            stat = quote.getYearLow();
            if (stat != null) yearLow = stat.floatValue();
            else yearLow = 0;
            //float yearLow = quote.getYearLow().floatValue();
        } else {
            dayHigh = 0; dayLow = 0; openPrice = 0; prevClosePrice = 0; yearHigh = 0; yearLow = 0;
            avgVolume = 0; volume = 0;
        }
        String stockExchange = stock.getStockExchange();
        float beta = getBeta();   // FOR NOW --- CHANGE LATER!!!


        ContentValues stockValues = new ContentValues();

        stockValues.put(StocksContract.StockEntry.COLUMN_SYMBOL, symbol);
        stockValues.put(StocksContract.StockEntry.COLUMN_NAME, longName);
        stockValues.put(StocksContract.StockEntry.COLUMN_PRICE, price);
        stockValues.put(StocksContract.StockEntry.COLUMN_PRICE_CHANGE, priceChange);
        stockValues.put(StocksContract.StockEntry.COLUMN_PRICE_PCT_CHANGE, pctPriceChange);

        stockValues.put(StocksContract.StockEntry.COLUMN_DIVIDEND, dividend);
        stockValues.put(StocksContract.StockEntry.COLUMN_DIVIDEND_PCT, dividendPct);
        stockValues.put(StocksContract.StockEntry.COLUMN_EPS, eps);
        stockValues.put(StocksContract.StockEntry.COLUMN_PE, pe);
        stockValues.put(StocksContract.StockEntry.COLUMN_TARGET, oneYearTarget);
        stockValues.put(StocksContract.StockEntry.COLUMN_MARKET_CAP, marketCap);
        stockValues.put(StocksContract.StockEntry.COLUMN_DAY_HIGH, dayHigh);
        stockValues.put(StocksContract.StockEntry.COLUMN_DAY_LOW, dayLow);
        stockValues.put(StocksContract.StockEntry.COLUMN_AVG_VOLUME, avgVolume);
        stockValues.put(StocksContract.StockEntry.COLUMN_OPEN_PRICE, openPrice);
        stockValues.put(StocksContract.StockEntry.COLUMN_PREV_CLOSE_PRICE, prevClosePrice);
        stockValues.put(StocksContract.StockEntry.COLUMN_VOLUME, volume);
        stockValues.put(StocksContract.StockEntry.COLUMN_YEAR_HIGH, yearHigh);
        stockValues.put(StocksContract.StockEntry.COLUMN_YEAR_LOW, yearLow);
        stockValues.put(StocksContract.StockEntry.COLUMN_STOCK_EXCHANGE, stockExchange);
        stockValues.put(StocksContract.StockEntry.COLUMN_BETA, beta);
        return stockValues;
    }


    public static float getBeta() {
        return (float) 1.0;  // FOR NOW !!!!!! CHANGE LATER!!!!!
    }

}
