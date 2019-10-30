package com.example.momen.uber_clone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

    Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.initialize(new Parse.Configuration.Builder(this).
                applicationId(getString(R.string.back4app_app_id))
                // if defined
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        );
        ParseUser.getCurrentUser().put("RiderOrDriver","rider");

        aSwitch = (Switch) findViewById(R.id.switch1);
        if(ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        Log.d("MyApp", "Anonymous login failed.");
                    } else {
                        Log.d("MyApp", "Anonymous user logged in.");
                    }
                }
            });
        }
        else{
            if(ParseUser.getCurrentUser().get("RiderOrDriver")!=null){
                redirectUser();
            }
        }
    }
    public void redirectUser()
    {
        if(ParseUser.getCurrentUser().get("RiderOrDriver").equals("rider")){
            startActivity(new Intent(MainActivity.this,YourLocation.class));
        }
        else{
            startActivity(new Intent(MainActivity.this,ViewRequests.class));
        }
    }

    public void getStarted(View view)
    {
        String riderOrDriver = "rider";
        if(aSwitch.isChecked()){
            riderOrDriver = "driver";
        }
        ParseUser.getCurrentUser().put("RiderOrDriver",riderOrDriver);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    redirectUser();
                }
            }
        });
    }
}
