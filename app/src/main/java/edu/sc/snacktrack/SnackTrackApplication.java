package edu.sc.snacktrack;

import android.app.Activity;
import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

import edu.sc.snacktrack.chat.Conversation;
import edu.sc.snacktrack.chat.Message;

public class SnackTrackApplication extends Application{

    private Activity mCurrentActivity = null;

    public void onCreate(){
        super.onCreate();

        // Enable local datastore
        Parse.enableLocalDatastore(this);

        // Register ParseObject subclasses
        ParseObject.registerSubclass(SnackEntry.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Conversation.class);

        // Authenticate client with the application ID and client key.
        Parse.initialize(this);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}