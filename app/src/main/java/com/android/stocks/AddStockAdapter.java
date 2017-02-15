package com.android.stocks;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by janicerichards on 6/13/16.
 */
public class AddStockAdapter extends ArrayAdapter<StockItem> {

    Context context;
    int layoutResourceId;
    List<StockItem> data;

    public AddStockAdapter(Context context, int layoutResourceId, List<StockItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        StockHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new StockHolder();
            holder.txt1 = (TextView)row.findViewById(android.R.id.text1);
            holder.txt2 = (TextView)row.findViewById(android.R.id.text2);

            row.setTag(holder);
        } else {
            holder = (StockHolder)row.getTag();
        }

        StockItem stock = data.get(position);
        holder.txt1.setText(stock.getSymbol());
        holder.txt2.setText(stock.getName());

        return row;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public StockItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getPosition(StockItem item) {
        return super.getPosition(item);
    }

    /*
    @Override
    public boolean hasStableIds() {
        return true;
    }
*/

    static class StockHolder {
        TextView txt1, txt2;
    }
}

