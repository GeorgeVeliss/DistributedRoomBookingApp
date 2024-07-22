package com.example.booking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;

public class FilteringActivity extends AppCompatActivity {

    EditText numofPeople;
    EditText minimumStars;
    EditText Area;
    EditText Dates;
    EditText maxPrice;
    Button filteringButton;
    ListView displayedRooms;

    Button clearButton;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filtering);
        String username = getIntent().getExtras().getString("username");

        try {
            String[] filters = {"0", "0", "0", "0", "0", "0"};
            search(username, filters);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        numofPeople = findViewById(R.id.editNumberOfPeople);
        minimumStars = findViewById(R.id.editMinStars);
        Area = findViewById(R.id.editArea);
        Dates = findViewById(R.id.editDates);
        maxPrice = findViewById(R.id.editMaxPrice);

        filteringButton = findViewById(R.id.filteringButton);

        clearButton = findViewById(R.id.button2);

        displayedRooms = findViewById(R.id.displayedRooms);
        filteringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String num_people = numofPeople.getText().toString();
                if (num_people.equals(""))
                    num_people = "0";
                String min_stars = minimumStars.getText().toString();
                if (min_stars.equals(""))
                    min_stars = "0";
                String area = Area.getText().toString();
                if (area.equals(""))
                    area = "0";
                String dates = Dates.getText().toString();
                if (dates.equals(""))
                    dates = "0";
                String max_price = maxPrice.getText().toString();
                if (max_price.equals(""))
                    max_price = "0";

                try {
                    String[] filters = {num_people, min_stars, "0", area, dates, max_price};
                    search(username, filters);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),FilteringActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }

    public Handler filteringHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {

            ArrayList<ArrayList<String>> filteredRooms = (ArrayList<ArrayList<String>>) message.getData().getSerializable("filteredRooms");

            ListAdapter filteredRoomsAdapter = new ListAdapter(getLayoutInflater(), filteredRooms);

            displayedRooms.setAdapter(filteredRoomsAdapter);

            displayedRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Intent intent = new Intent(getApplicationContext(), BookingActivity.class);

                    intent.putExtra("selectedRoom", filteredRooms.get(i));
                    intent.putExtra("username", getIntent().getExtras().getString("username"));

                    startActivity(intent);
                }
            });


            return false;
        }
    });


    private void search(String username, String[] filters) throws InterruptedException {
        filteringThread t = new filteringThread(filteringHandler, username, filters);
        t.start();
    }
}
