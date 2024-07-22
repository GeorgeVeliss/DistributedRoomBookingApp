package com.example.booking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BookingActivity extends AppCompatActivity {
    Button rate_button;
    Button confirm_button;
    TextView maximum_people;
    TextView rating;
    TextView price;
    TextView area;
    TextView manager;
    TextView name;
    EditText userDates;
    EditText userRating;
    ListView availableDatesList;


    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_booking);

        String username = getIntent().getExtras().getString("username");

        ArrayList<String> selectedRoom = (ArrayList<String>) getIntent().getExtras().getSerializable("selectedRoom");

        String roomName = selectedRoom.get(0);

        name = findViewById(R.id.Booking_room_name);
        rating = findViewById(R.id.Booking_rating);
        area =  findViewById(R.id.Booking_area);
        maximum_people = findViewById(R.id.Booking_guests);
        price =  findViewById(R.id.Booking_price);
        manager = findViewById(R.id.Booking_manager);

        userDates = (EditText) findViewById(R.id.Booking_dates);
        userRating = (EditText) findViewById(R.id.rating);

        image = (ImageView) findViewById(R.id.roomImage);

        availableDatesList = findViewById(R.id.displayedRooms);

        confirm_button = (Button) findViewById(R.id.confirm_button);
        rate_button = (Button) findViewById(R.id.rate_button);

        name.setText("Room Name: "+selectedRoom.get(0));
        rating.setText("Rating: "+selectedRoom.get(1));
        area.setText("Area: "+selectedRoom.get(2));
        maximum_people.setText("Maximum number of guests: "+selectedRoom.get(3));
        price.setText("Price: "+selectedRoom.get(4));
        manager.setText("Manager: "+selectedRoom.get(5));

        getImageThread get_image_thread = new getImageThread(getImageHandler, username, roomName);
        get_image_thread.start();

        getAvailableDatesThread get_available_dates_thread = new getAvailableDatesThread(getAvailableDatesHandler, username, roomName);
        get_available_dates_thread.start();
        rate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (userRating.getText().toString().isEmpty())
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please enter rating between 0-5", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    ratingThread rating_thread = new ratingThread(ratingHandler, selectedRoom.get(0), userRating.getText().toString());
                    rating_thread.start();
                }
            }
        });

        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userDates.getText().toString().isEmpty())
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please enter dates to book room", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    try {
                        book(username, roomName, userDates.getText().toString());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public Handler getAvailableDatesHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {

            ArrayList<String> availableDates = (ArrayList<String>) message.getData().getSerializable("availableDates");

            datesAdapter filteredRoomsAdapter = new datesAdapter(getLayoutInflater(), availableDates);

            availableDatesList.setAdapter(filteredRoomsAdapter);

            return false;
        }
    });

    public Handler getImageHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {

            byte[] roomImage = message.getData().getByteArray("roomImage");

            Bitmap map = BitmapFactory.decodeByteArray(roomImage, 0, roomImage.length);
            image.setImageBitmap(map);

            return false;
        }
    });

    public Handler ratingHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            Boolean success = message.getData().getBoolean("success");

            if (success) {
                Toast toast = Toast.makeText(getApplicationContext(), "Rating successful!", Toast.LENGTH_SHORT);
                toast.show();
            }

            return false;
        }
    });

    public Handler bookingHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            int result = message.getData().getInt("result");

            if (result == 2) {
                Toast toast = Toast.makeText(getApplicationContext(), "Date(s) specified not available.", Toast.LENGTH_SHORT);
                toast.show();
            }
            else if (result == 3) {
                Toast toast = Toast.makeText(getApplicationContext(), "Booking successful!", Toast.LENGTH_SHORT);
                toast.show();

                Intent i = new Intent(getApplicationContext(), FilteringActivity.class);
                i.putExtra("username", getIntent().getExtras().getString("username"));
                startActivity(i);
            }
            return false;
        }
    });

    private void book(String username, String roomName, String dates) throws InterruptedException {
        bookingThread t = new bookingThread(bookingHandler, username, roomName,dates);
        t.start();
    }
}