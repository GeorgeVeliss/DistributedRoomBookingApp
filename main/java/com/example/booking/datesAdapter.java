package com.example.booking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class datesAdapter extends BaseAdapter {

    // override other abstract methods here
    private LayoutInflater inflater;

    private ArrayList<String> items;

    public datesAdapter(LayoutInflater inflater, ArrayList<String> items){
        this.inflater = inflater;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.date_item, container, false);
        }

        ((TextView) convertView.findViewById(R.id.date)).setText(items.get(position));

        return convertView;
    }
}
