package edu.sc.snacktrack;

import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.DeleteCallback;
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
     * The designated ParseUser from whom owns the foreign SnackList
     */
    private ParseUser targetUser;

    private boolean isUpdating;

    /**
     * UpdateListener interface.
     *
     * Interface definition for callbacks to be invoked when the SnackList is modified.
     */
    public interface UpdateListener{

        /**
         * Called for each registered UpdateListener when an update completes.
         */
        void onSnackListUpdateComplete(ParseException e);

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
        isUpdating = false;
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
     * Sets a new target user to be used when refreshing a SnackList
     */
    public ParseUser setUser(ParseUser user) {
        targetUser = user;
        return targetUser;
    }

    public ParseUser getUser() {
        return targetUser;
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
        isUpdating = true;
        for(UpdateListener listener : updateListeners){
            listener.onSnackListUpdateStart();
        }
    }

    /**
     * Notifies all UpdateListeners that an update has completed.
     */
    private void notifyUpdateComplete(ParseException e){
        for(UpdateListener listener : updateListeners){
            listener.onSnackListUpdateComplete(e);
        }

        isUpdating = false;
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
                    notifyUpdateComplete(e);

                    if(callback != null){
                        callback.done(e);
                    }
                }
            });
        } else if(!snacks.contains(entry)){
            snacks.add(0, entry);
            notifyUpdateComplete(null);
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
                    } else if(callback != null){
                        callback.done(e);
                    }
                }
            });
        } else {
            entry.setPhoto(photo);
            addSnack(entry, callback);
        }
    }

    /**
     * Edits specified snack entry.
     *
     * @param toEdit The snack entry to edit.
     * @param callback The callback to invoke after completion.
     */
    public void editSnack(final SnackEntry toEdit, final SaveCallback callback){
        toEdit.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                notifyUpdateComplete(e);
                callback.done(e);
            }
        });
    }

    /**
     * Edits a snack entry that includes a new image.
     *
     * @param toEdit The snack entry to edit.
     * @param newPhoto The snack entry's new image.
     * @param callback The callback to invoke after completion.
     */
    public void editSnack(final SnackEntry toEdit, final ParseFile newPhoto, final SaveCallback callback){
        newPhoto.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    toEdit.setPhoto(newPhoto);
                    toEdit.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            callback.done(e);
                            notifyUpdateComplete(e);
                        }
                    });
                } else{
                    callback.done(e);

                }
            }
        });
    }

    /**
     * Deletes a snack entry.
     *
     * @param position Position of the snack entry to delete
     * @param failsafe The failsafe's object id must match the snack entry at the specified position.
     * @param callback The callback to invoke after completion.
     */
    public void deleteSnack(final int position, final SnackEntry failsafe, final DeleteCallback callback){
        SnackEntry toDelete = get(position);
        if(!toDelete.getObjectId().equals(failsafe.getObjectId())){
            callback.done(new ParseException(ParseException.OTHER_CAUSE, "Invalid delete"));
        } else{
            get(position).deleteInBackground(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        snacks.remove(position);
                        notifyUpdateComplete(e);
                    }
                    callback.done(e);
                }
            });
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
        query.whereEqualTo("owner", targetUser);
        query.setLimit(10);
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
                notifyUpdateComplete(e);
            }
        });
    }

    public void loadMoreData(Integer currentCount){
        if(!isUpdating){
            notifyUpdateStart();
            ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);
            query.orderByDescending("createdAt");
            query.whereEqualTo("owner", targetUser);
            query.setLimit(10);
            query.setSkip(currentCount);
            query.findInBackground(new FindCallback<SnackEntry>() {
                @Override
                public void done(List<SnackEntry> moreSnacks, ParseException e) {
                    if (e == null) {
                        snacks.addAll(moreSnacks);
                    }

                    notifyUpdateComplete(e);
                }
            });
        }
    }
}