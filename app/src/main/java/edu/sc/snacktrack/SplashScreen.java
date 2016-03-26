package edu.sc.snacktrack;

/**
 * Created by Josh on 12/5/2015.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(ParseUser.getCurrentUser() == null){
            startLoginActivity();
        } else{
            startMainActivity();
        }
    }

    private void startMainActivity(){
        Intent launchIntent = getIntent();
        Intent startMainIntent;

        boolean isChat = launchIntent.getBooleanExtra("isChat", false);
        String fromUserId = launchIntent.getStringExtra("fromUserId");
        String fromUserName = launchIntent.getStringExtra("fromUserName");

        startMainIntent = new Intent(SplashScreen.this, MainActivity.class);
        startMainIntent.putExtra("isChat", isChat);
        startMainIntent.putExtra("fromUserId", fromUserId);
        startMainIntent.putExtra("fromUserName", fromUserName);

        startActivity(startMainIntent);
        finish();
    }

    private void startLoginActivity(){
        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}