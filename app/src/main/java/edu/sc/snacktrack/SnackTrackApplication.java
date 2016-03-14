package edu.sc.snacktrack;

import android.app.Activity;
import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

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

        // Authenticate client with the application ID and client key.
        Parse.initialize(this);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    /**
     * Gets the current foreground activity specified by setCurrentActivity().
     *
     * @return The current foreground activity
     */
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }

    /**
     * Sets a reference to the current foreground activity.
     *
     * @param currentActivity The current foreground activity
     */
    public void setCurrentActivity(Activity currentActivity){
        this.mCurrentActivity = currentActivity;
    }
}