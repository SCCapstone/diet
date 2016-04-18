package edu.sc.snacktrack.client_list;

/**
 * Created by spitzfor on 2/16/2016.
 */

import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * This singleton class holds and manages a local dietitian's list of clients.
 */
public class ClientList {

    private static final String TAG = "ClientList";

    /**
     * The current instance of the ClientList
     */
    private static ClientList instance;

    /**
     * The local list of ParseUsers.
     */
    private ArrayList<ParseUser> clients;

    /**
     * The list of update listeners.
     */
    private ArrayList<UpdateListener> updateListeners;

    /**
     * The designated ParseUser from whom owns the foreign ClientList
     */
    private ParseUser targetUser;

    /**
     * UpdateListener interface.
     *
     * Interface definition for callbacks to be invoked when the SnackList is modified.
     */
    public interface UpdateListener{

        /**
         * Called for each registered UpdateListener when an update completes.
         */
        void onClientListUpdateComplete();

        /**
         * *************************************************************************
         */
        void onClientListUpdateStart();
    }

    /**
     * Private constructor to prevent multiple instances. Use getInstance() to get the current
     * instance of ClientList.
     */
    private ClientList(){
        clients = new ArrayList<>();
        updateListeners = new ArrayList<>();
    }

    /**
     * Returns the current ClientList instance.
     *
     * @return The ClientList instance
     */
    public static ClientList getInstance(){
        if(instance == null){
            instance = new ClientList();
        }
        return instance;
    }

    /**
     * Gets a ParseUser (client) at a specified position.
     *
     * @param position The position.
     * @return The ParseUser (client)
     */
    public ParseUser get(int position){
        return clients.get(position);
    }

    /**
     * Returns the size of the ClientList.
     *
     * @return The size
     */
    public int size(){
        return clients.size();
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
            listener.onClientListUpdateStart();
        }
    }

    /**
     * Notifies all UpdateListeners that an update has completed.
     */
    private void notifyUpdateComplete(){
        for(UpdateListener listener : updateListeners){
            listener.onClientListUpdateComplete();
        }
    }

    /**
     * Refreshes the ClientList. That is, queries Parse for the current user's ParseUsers (clients) and
     * repopulates the ClientList with the result. If the query fails, the ClientList remains
     * unchanged.
     *
     * @param callback Optional. The callback to invoke after completion.
     */
    public void refresh(@Nullable final FindCallback<ParseUser> callback){
        Log.d(TAG, "Refresh start");
        notifyUpdateStart();

        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("myDietitian", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> refreshedClients, ParseException e) {
                if (e == null) {
                    clients.clear();
                    clients.addAll(refreshedClients);
                }

                if (callback != null) {
                    callback.done(refreshedClients, e);
                }
                notifyUpdateComplete();
            }
        });
    }
}
