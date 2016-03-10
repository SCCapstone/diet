package edu.sc.snacktrack;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

import edu.sc.snacktrack.chat.Conversation;
import edu.sc.snacktrack.chat.Message;

public class SnackTrackApplication extends Application{

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
}