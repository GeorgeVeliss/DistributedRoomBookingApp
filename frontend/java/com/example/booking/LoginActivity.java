package com.example.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;



public class LoginActivity extends AppCompatActivity {
    EditText Username;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        Username = (EditText) findViewById(R.id.Persons_name);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = Username.getText().toString();
                Intent i = new Intent(getApplicationContext(),FilteringActivity.class);
                i.putExtra("username", username);
                startActivity(i);
            }
        });
    }
}