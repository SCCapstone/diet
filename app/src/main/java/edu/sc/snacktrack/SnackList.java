package edu.sc.snacktrack;

import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * This singleton class holds and manages a local list snack entries.
 */
public class SnackList{

    private static final String TAG = "SnackList";

    /**
     * The current instance of the SnackList
     */
    private static SnackList instance;

    /**
     * The local list of snack entries.
     */
    private ArrayList<SnackEntry> snacks;

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

        /**
         * Called for each registered UpdateListener when an update completes.
         */
        void onSnackListUpdateComplete();

        /**
         * Called for each registered UpdateListener when an update starts. Only called for
         * typically long-running tasks, such as saving a dirty SnackEntry or refreshing the
         * SnackList.
         */
        void onSnackListUpdateStart();
    }

    /**
     * Private constructor to prevent multiple instances. Use getInstance() to get the current
     * instance of SnackList.
     */
    private SnackList(){
        snacks = new ArrayList<>();
        updateListeners = new ArrayList<>();
    }

    /**
     * Returns the current SnackList instance.
     *
     * @return The SnackList instance
     */
    public static SnackList getInstance(){
        if(instance == null){
            instance = new SnackList();
        }
        return instance;
    }

    /**
     * Gets a SnackEntry at a specified position.
     *
     * @param position The position.
     * @return The SnackEntry
     */
    public SnackEntry get(int position){
        return snacks.get(position);
    }

    /**
     * Returns the size of the SnackList.
     *
     * @return The size
     */
    public int size(){
        return snacks.size();
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
            listener.onSnackListUpdateStart();
        }
    }

    /**
     * Notifies all UpdateListeners that an update has completed.
     */
    private void notifyUpdateComplete(){
        for(UpdateListener listener : updateListeners){
            listener.onSnackListUpdateComplete();
        }
    }

    /**
     * Saves a SnackEntry to Parse and adds it to the SnackList.
     *
     * @param entry The SnackEntry to add
     * @param callback Optional. The callback to invoke after completion.
     */
    public void addSnack(final SnackEntry entry, @Nullable final SaveCallback callback){
        if(entry.isDirty()){
            notifyUpdateStart();
            entry.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        snacks.add(0, entry);
                    }
                    notifyUpdateComplete();

                    if(callback != null){
                        callback.done(e);
                    }
                }
            });
        } else if(!snacks.contains(entry)){
            snacks.add(0, entry);
            notifyUpdateComplete();
            if(callback != null){
                callback.done(null);
            }
        }
    }

    /**
     * Saves a SnackEntry and its photo to Parse and adds the SnackEntry to the SnackList.
     *
     * @param entry The SnackEntry to add
     * @param photo The SnackEntry's photo
     * @param callback Optional. The callback to invoke after completion.
     */
    public void addSnack(final SnackEntry entry, final ParseFile photo, @Nullable final SaveCallback callback){
        if(photo.isDirty()){
            notifyUpdateStart();
            photo.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        entry.setPhoto(photo);
                        addSnack(entry, callback);
                    }
                }
            });
        } else {
            entry.setPhoto(photo);
            addSnack(entry, callback);
        }
    }

    /**
     * Refreshes the SnackList. That is, queries Parse for the current user's SnackEntrys and
     * repopulates the SnackList with the result. If the query fails, the SnackList remains
     * unchanged.
     *
     * @param callback Optional. The callback to invoke after completion.
     */
    public void refresh(@Nullable final FindCallback<SnackEntry> callback){
        Log.d(TAG, "Refresh start");
        notifyUpdateStart();

        ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);
        query.orderByDescending("createdAt");
        query.whereEqualTo("owner", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<SnackEntry>() {
            @Override
            public void done(List<SnackEntry> refreshedSnacks, ParseException e) {
                if (e == null) {
                    snacks.clear();
                    snacks.addAll(refreshedSnacks);
                }

                if (callback != null) {
                    callback.done(refreshedSnacks, e);
                }
                notifyUpdateComplete();
            }
        });
    }
}