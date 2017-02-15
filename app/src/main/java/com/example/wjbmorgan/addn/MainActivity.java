package com.example.wjbmorgan.addn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/*
This class is the main activity for user to enter an activation code to
connect to the database.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    // Called when the user clicks the start button.
    // TODO Due to time constraint, the authentication process need to be added in the future
    public void startNew(View view) {
        Intent intent = new Intent(this, MessageActivity.class);
        startActivity(intent);
    }

}
