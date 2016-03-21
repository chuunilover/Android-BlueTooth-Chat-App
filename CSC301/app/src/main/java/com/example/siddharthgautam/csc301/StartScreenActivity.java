package com.example.siddharthgautam.csc301;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Set;
/*
public class StartScreenActivity extends AppCompatActivity {

    private Button connectButton;
    private Button openChatsButton;
    private Button openMainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        initializeViews();
    }

    private void initializeViews(){
        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartScreenActivity.this, DisplayScreenActivity.class));
            }
        });

        openChatsButton = (Button) findViewById(R.id.openChatsButton);
        openChatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartScreenActivity.this, chatActivity.class)); // open chats
            }
        });

        openMainButton = (Button) findViewById(R.id.openMainButton);
        openChatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMain(v);
            }
        });
    }

    public void openMain(View view) {
        Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
        StartScreenActivity.this.startActivity(intent);
    }
*/

public class StartScreenActivity extends Activity {

    private int waitTime = 4000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        new android.os.Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(StartScreenActivity.this, DisplayScreenActivity.class);
                StartScreenActivity.this.startActivity(mainIntent);
                finish();
                //StartScreenActivity.this.finish();
            }
        }, waitTime);
    }
}

