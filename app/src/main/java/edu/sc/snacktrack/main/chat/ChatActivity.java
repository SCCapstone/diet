package edu.sc.snacktrack.main.chat;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.SnackTrackApplication;

public class ChatActivity extends AppCompatActivity{

    public static final String OTHER_USER_ID_KEY = "otherUserId";
    public static final String OTHER_USER_NAME_KEY = "otherUserName";

    private SnackTrackApplication application;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        application = (SnackTrackApplication) getApplicationContext();

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if(savedInstanceState == null){
            ChatFragment chatFragment = new ChatFragment();
            String otherUserId = getIntent().getStringExtra(OTHER_USER_ID_KEY);
            String otherUserName = getIntent().getStringExtra(OTHER_USER_NAME_KEY);

            Bundle args = new Bundle();

            args.putString(ChatFragment.ARG_OTHER_USER_ID, otherUserId);
            args.putString(ChatFragment.ARG_OTHER_USER_NAME, otherUserName);

            chatFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, chatFragment)
                    .commit();
        } else{
            // Fragment restored from instance state
        }
    }

    /**
     * Gets the id of the user specified in the intent for this activity.
     *
     * @return The other user's objectId
     */
    public String getOtherUserId(){
        return getIntent().getStringExtra(OTHER_USER_ID_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        application.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearReferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearReferences();
    }

    private void clearReferences(){
        Activity currentActivity = application.getCurrentActivity();
        if(currentActivity == this){
            application.setCurrentActivity(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }
}
