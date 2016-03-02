package edu.sc.snacktrack;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import edu.sc.snacktrack.chat.Conversation;
import edu.sc.snacktrack.chat.Message;

public class SnackTrackApplication extends Application{

    private static final String PARSE_APPLICATION_ID = "46YXlwzvjKZaNIfSE0h1uLdhMg7Zf6mWDtvF4CiY";
    private static final String PARSE_CLIENT_KEY = "XrCl4tpwXecaYajHRF7KY6A0JoCfwvTBy93r1xTF";

    public void onCreate(){
        super.onCreate();

        // Enable local datastore
        Parse.enableLocalDatastore(this);

        // Register ParseObject subclasses
        ParseObject.registerSubclass(SnackEntry.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Conversation.class);

        // Authenticate client with the application ID and client key.
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);
    }
}