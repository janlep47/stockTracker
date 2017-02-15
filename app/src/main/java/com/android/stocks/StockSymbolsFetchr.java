package com.android.stocks;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by janicerichards on 6/13/16.
 */

// Used by AddStock menu option, as each stock symbol is typed in a character at a time.
public class StockSymbolsFetchr {

    private static final String TAG = "StockSymbolsFetchr";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<StockItem> getSymbolList(String url) {
        List<StockItem> items = new ArrayList<>();

        try {
            //"YAHOO.util.ScriptNodeDataSource.callbacks("
            String result = getUrlString(url);
            String jsonString = result.substring(42);
            jsonString = jsonString.substring(0,jsonString.length()-2);  // peel off last ");"

            JSONObject obj = new JSONObject(jsonString);
            parseItems(items, obj);

        } catch (JSONException exc) {
            Log.i(TAG, "JSON exception: ", exc);
        } catch (IOException ioe) {
            Log.i(TAG, "Failed to fetch URL: ", ioe);
        }
        return items;
    }



    private void parseItems(List<StockItem> items, JSONObject jsonObj) throws IOException, JSONException {
        JSONObject one = (JSONObject) jsonObj.get("ResultSet");

        JSONArray results = (JSONArray) one.get("Result");
        JSONObject result;
        for (int i = 0; i < results.length(); i++) {
            result = results.getJSONObject(i);

            StockItem item = new StockItem();
            item.setSymbol(result.getString("symbol"));
            item.setName(result.getString("name"));

            items.add(item);
        }

    }
}
