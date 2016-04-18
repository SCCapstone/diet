package edu.sc.snacktrack.dietitian_list;

import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spitzfor on 3/22/2016.
 */

/**
 * This singleton class holds and manages a list of dietitians.
 */
public class DietitianList {

    private static final String TAG = "DietitianList";

    /**
     * The current instance of the DietitianList
     */
    private static DietitianList instance;

    /**
     * The local list of ParseUsers.
     */
    private ArrayList<ParseUser> dietitians;

    /**
     * The list of update listeners.
     */
    private ArrayList<UpdateListener> updateListeners;

    /**
     * UpdateListener interface.
     *
     * Interface definition for callbacks to be invoked when the SnackList is modified.
     */
    public interface UpdateListener{
        void onDietitianListUpdateComplete();

        void onDietitianListUpdateStart();
    }

    /**
     * Private constructor to prevent multiple instances. Use getInstance() to get the current
     * instance of DietitianList.
     */
    private DietitianList(){
        dietitians = new ArrayList<>();
        updateListeners = new ArrayList<>();
    }

    /**
     * Returns the current DietitianList instance.
     *
     * @return The DietitianList instance
     */
    public static DietitianList getInstance(){
        if(instance == null){
            instance = new DietitianList();
        }
        return instance;
    }

    /**
     * Gets a ParseUser (dietitian) at a specified position.
     *
     * @param position The position.
     * @return The ParseUser (dietitian)
     */
    public ParseUser get(int position){
        return dietitians.get(position);
    }

    /**
     * Returns the size of the DietitianList.
     *
     * @return The size
     */
    public int size(){
        return dietitians.size();
    }

    /**
     * Registers an update listener.
     *
     * @param listener The listener to register
     */
    public void registerUpdateListener(UpdateListener listener){
        if(!updateListeners.contains(listener)){
            updateListeners.add(listener);
            Log.d(TAG, "registered " + updateListeners.size());
        }
    }

    /**
     * Unregisters an update listener.
     *
     * @param listener The listener to remove
     * @return true if the listener was removed
     *         false if the listener was not found
     */
    public boolean unregisterUpdateListener(UpdateListener listener){
        return updateListeners.remove(listener);
    }

    /**
     * Notifies all UpdateListeners that a long-running update has started.
     */
    private void notifyUpdateStart(){
        for(UpdateListener listener : updateListeners){
            listener.onDietitianListUpdateComplete();
        }
    }

    /**
     * Notifies all UpdateListeners that an update has completed.
     */
    private void notifyUpdateComplete(){
        for(UpdateListener listener : updateListeners){
            listener.onDietitianListUpdateComplete();
        }
    }

    /**
     * Refreshes the DietitianList. That is, queries Parse for all ParseUsers whose isDietitian field is true and
     * repopulates the DietitianList with the result. If the query fails, the DietitianList remains
     * unchanged.
     *
     * @param callback Optional. The callback to invoke after completion.
     */
    public void refresh(@Nullable final FindCallback<ParseUser> callback){
        Log.d(TAG, "Refresh start");
        notifyUpdateStart();

        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("isDietitian", true);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> refreshedDietitians, ParseException e) {
                if (e == null) {
                    dietitians.clear();
                    dietitians.addAll(refreshedDietitians);
                }

                if (callback != null) {
                    callback.done(refreshedDietitians, e);
                }
                notifyUpdateComplete();
            }
        });
    }
}
