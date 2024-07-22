package com.example.booking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class ListAdapter extends BaseAdapter {

    // override other abstract methods here
    private LayoutInflater inflater;

    private ArrayList<ArrayList<String>> items;

    public ListAdapter(LayoutInflater inflater, ArrayList<ArrayList<String>> items){
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
            convertView = inflater.inflate(R.layout.list_item, container, false);
        }

        ((TextView) convertView.findViewById(R.id.roomName)).setText("Room name: "+items.get(position).get(0));
        ((TextView) convertView.findViewById(R.id.roomRating)).setText("Rating: "+items.get(position).get(1));
        ((TextView) convertView.findViewById(R.id.roomArea)).setText("Area: "+items.get(position).get(2));
        ((TextView) convertView.findViewById(R.id.roomPeople)).setText("Maximum number of guests: "+items.get(position).get(3));
        ((TextView) convertView.findViewById(R.id.roomPrice)).setText("Price: "+items.get(position).get(4));
        ((TextView) convertView.findViewById(R.id.roomManager)).setText("Manager: "+items.get(position).get(5));

        return convertView;
    }
}