package edu.sc.snacktrack.chat;

import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This singleton class holds and manages messages for the current user.
 */
public class Conversations extends HashMap<Conversations.Group, List<Message>>{

    private static Conversations instance;

    private volatile boolean isUpdating;
    private volatile ParseUser lastUpdateUser;
    private volatile List<UpdateListener> updateListeners;

    public static final String PINNED_MESSAGES_LABEL = "pinnedMessages";

    /**
     * UpdateListener interface.
     *
     * Interface definition for callbacks to be invoked when the Conversations instance is modified.
     * UpdateListeners must be registered and unregistered as appropriate via the
     * registerUpdateListener() and unregisterUpdateListener() methods.
     */
    public interface UpdateListener{

        /**
         * Called when new messages are added for a specific group.
         *
         * @param updatedGroup The group that was updated.
         * @param newMessages The new messages for that group.
         */
        void onConversationsGroupUpdate(Group updatedGroup, Message... newMessages);

        /**
         * Called when the refresh() method completes.
         */
        void onConversationsRefresh();
    }

    /**
     * Registers an UpdateListener.
     *
     * @param listener The UpdateListener to register.
     */
    public void registerUpdateListener(UpdateListener listener){
        updateListeners.add(listener);
    }

    /**
     * Unregisters an UpdateListener.
     *
     * @param listener The UpdateListener to unregister.
     * @return true if the UpdateListener was removed. false otherwise.
     */
    public boolean unregisterUpdateListener(UpdateListener listener){
        return updateListeners.remove(listener);
    }

    /**
     * Calls onConversationsGroupUpdate() for each UpdateListener.
     *
     * @param group The updated group.
     * @param newMessages The new messages for that group.
     */
    public void notifyGroupUpdate(Group group, Message... newMessages){
        for(UpdateListener listener : updateListeners){
            listener.onConversationsGroupUpdate(group, newMessages);
        }
    }

    /**
     * Calls onConversationsRefresh() for each UpdateListener
     */
    public void notifyRefresh(){
        for(UpdateListener listener : updateListeners){
            listener.onConversationsRefresh();
        }
    }

    /**
     * Private constructor to prevent multiple instances. Use getInstance() to get the current
     * instance of Conversations.
     */
    private Conversations(){
        super();
        updateListeners = new ArrayList<>();
        isUpdating = false;
        lastUpdateUser = null;
    }

    /**
     * Gets the current Conversations instance.
     *
     * @return The current Conversations instance.
     */
    public static Conversations getInstance(){
        if(instance == null){
            instance = new Conversations();
        }

        return instance;
    }

    /**
     * Checks if this Conversations instance is updating.
     *
     * @return true if an updating is occuring. false otherwise.
     */
    public boolean isUpdating(){
        return isUpdating;
    }

    /**
     * Checks if this Conversations instance should be refreshed. That is, if it has never been
     * refreshed or the current conversations are not for the current user.
     *
     * @return true if the conversations need to be refreshed. false otherwise.
     */
    public boolean needsRefresh(){
        return lastUpdateUser == null || ParseUser.getCurrentUser() != lastUpdateUser;
    }

    /**
     * Gets the conversation (that is, list of messages) for a group of users. Note that currently
     * we only support chats between two users.
     *
     * @param group The group of users in the conversation.
     * @param callback The callback to invoke upon completion.
     */
    public void getConversation(final Group group, final FindCallback<Message> callback){
        List<Message> conv = this.get(group);
        if(conv == null){
            Iterator<ParseUser> it = group.iterator();
            ParseUser user1;
            ParseUser user2;

            if(it.hasNext()){
                user1 = it.next();
            } else{
                user1 = null;
            }
            if(it.hasNext()){
                user2 = it.next();
            } else{
                user2 = null;
            }

            fetchMessages(user1, user2, 10, 0, new FindCallback<Message>() {
                @Override
                public void done(List<Message> messages, ParseException e) {
                    if (e == null) {
                        put(group, messages);
                    }
                    callback.done(messages, e);
                }
            });
        } else{
            callback.done(conv, null);
        }
    }

    public void loadMore(final Group group, final FindCallback<Message> callback){
        final List<Message> conv = this.get(group);
        if(!isUpdating) {
            if (conv == null) {
                getConversation(group, callback);
            } else {
                Iterator<ParseUser> it = group.iterator();
                ParseUser user1;
                ParseUser user2;

                if (it.hasNext()) {
                    user1 = it.next();
                } else {
                    user1 = null;
                }
                if (it.hasNext()) {
                    user2 = it.next();
                } else {
                    user2 = null;
                }

                fetchMessages(user1, user2, 10, conv.size(), new FindCallback<Message>() {
                    @Override
                    public void done(List<Message> messages, ParseException e) {
                        if (e == null) {
//                            ListIterator<Message> it = messages.listIterator(messages.size());
//                            while (it.hasPrevious()) {
//                                conv.add(it.previous());
//                            }
                            conv.addAll(messages);
                        }
                        callback.done(messages, e);
                    }
                });
            }
        } else if(callback != null){
            callback.done(null, new ParseException(
                    ParseException.OTHER_CAUSE,
                    "Cannot load more messages - updating already in progress."
            ));
        }
    }

    /**
     * Attempts to add a message to this Conversations, fetching the message if needed.
     * @param message The message to add
     */
    public void addMessage(final Message message){
        message.fetchIfNeededInBackground(new GetCallback<Message>() {
            @Override
            public void done(final Message fetchedMessage, ParseException e) {
                if(e == null){
                    final Group group = new Group(message.getFromUser(), message.getToUser());
                    getConversation(group, new FindCallback<Message>() {
                        @Override
                        public void done(List<Message> messages, ParseException e) {
                            if(e == null){
                                if(!hasMessage(fetchedMessage)){
                                    messages.add(0, fetchedMessage);
                                    notifyGroupUpdate(group, fetchedMessage);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Checks if this Conversations has a specified message.
     *
     * @param message The message to check.
     * @return true message was found. false otherwise.
     */
    public boolean hasMessage(Message message){
        if(message.isDataAvailable()){
            Group group = new Group(message.getFromUser(), message.getToUser());
            for(Message message1 : get(group)){
                if(message1.getObjectId().equals(message.getObjectId())){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Fetches a full list of messages between two users.
     *
     * @param user1 The first user
     * @param user2 The second user
     * @param callback The callback to invoke upon completion.
     */
    private void fetchMessages(ParseUser user1, ParseUser user2, int limit, int skip, final FindCallback<Message> callback) {
        List<ParseQuery<Message>> orQueries = new ArrayList<>();
        ParseQuery<Message> oredQuery;

        isUpdating = true;

        Log.d("Conversations", "Skip is " + skip);

        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.FROM_KEY, user1)
                        .whereEqualTo(Message.TO_KEY, user2)
        );
        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.TO_KEY, user1)
                        .whereEqualTo(Message.FROM_KEY, user2)
        );
        oredQuery = ParseQuery.or(orQueries);
        oredQuery.include(Message.FROM_KEY).include(Message.TO_KEY);
        oredQuery.orderByDescending("createdAt");
        oredQuery.setLimit(limit);
        oredQuery.setSkip(skip);
        oredQuery.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e == null) {
                    if (messages.size() > 0) {
                        ParseUser fromUser = messages.get(0).getFromUser();
                        ParseUser toUser = messages.get(0).getToUser();
                        // Null users not allowed
                        if (fromUser == null || toUser == null) {
                            messages.clear();
                        }
                    }
                    callback.done(messages, e);
                    isUpdating = false;
                }
            }
        });
    }

    /**
     * Refreshes the conversations for the current user. Up to 100 messages are fetched.
     *
     * @param callback The callback to invoke upon completion.
     */
    public void refresh(@Nullable final FindCallback<Message> callback){
        List<ParseQuery<Message>> orQueries = new ArrayList<>();
        ParseQuery<Message> oredQuery;

        isUpdating = true;

        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.FROM_KEY, ParseUser.getCurrentUser())
        );
        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.TO_KEY, ParseUser.getCurrentUser())
        );

        oredQuery = ParseQuery.or(orQueries);
        oredQuery.include(Message.FROM_KEY).include(Message.TO_KEY);
        oredQuery.orderByDescending("createdAt");
        oredQuery.setLimit(100);
        oredQuery.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if(e == null){
                    Conversations.this.clear();

                    ParseObject.pinAllInBackground(PINNED_MESSAGES_LABEL, messages);

                    for(Message message : messages){
                        Group group = new Group(message.getFromUser(), message.getToUser());

                        // If the size is less than 2, the from user and to user are equal, which
                        // is not allowed.
                        if(group.size() < 2){
                            message.unpinInBackground(PINNED_MESSAGES_LABEL);
                            continue;
                        }

                        // Null members are not allowed
                        if(group.hasNullMember()){
                            message.unpinInBackground(PINNED_MESSAGES_LABEL);
                            continue;
                        }

                        List<Message> conversation = get(group);
                        if(conversation == null){
                            conversation = new ArrayList<>();
                            put(group, conversation);
                        }
                        conversation.add(message);
                    }

                    lastUpdateUser = ParseUser.getCurrentUser();
                    notifyRefresh();
                 }

                if(callback != null){
                    callback.done(messages, e);
                }

                isUpdating = false;
            }
        });
    }

    /**
     * This class represents a group of users in a conversation. Note that current we
     * only support chatting between two users.
     */
    public static class Group extends HashSet<ParseUser>{
        public Group(ParseUser... users){
            super(Arrays.asList(users));
        }

        /**
         * Checks if this group has a null member.
         *
         * @return true if it does. false otherwise.
         */
        public boolean hasNullMember(){
            Iterator<ParseUser> it = iterator();

            while(it.hasNext()){
                if(it.next() == null){
                    return true;
                }
            }

            return false;
        }
    }
}
