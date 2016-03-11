package edu.sc.snacktrack.chat;

import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class Conversations extends HashMap<Conversations.Group, List<Message>>{

    private static Conversations instance;

    private volatile boolean isUpdating;
    private volatile ParseUser lastUpdateUser;
    private volatile List<UpdateListener> updateListeners;

    public interface UpdateListener{
        void onConversationsGroupUpdate(Group updatedGroup, Message... newMessages);
        void onConversationsRefresh();
    }

    public boolean registerUpdateListener(UpdateListener listener){
        return updateListeners.add(listener);
    }

    public boolean unregisterUpdateListener(UpdateListener listener){
        return updateListeners.remove(listener);
    }

    public void notifyGroupUpdate(Group group, Message... newMessages){
        for(UpdateListener listener : updateListeners){
            listener.onConversationsGroupUpdate(group, newMessages);
        }
    }

    public void notifyRefresh(){
        for(UpdateListener listener : updateListeners){
            listener.onConversationsRefresh();
        }
    }

    private Conversations(){
        super();
        updateListeners = new ArrayList<>();
        isUpdating = false;
        lastUpdateUser = null;
    }

    public static Conversations getInstance(){
        if(instance == null){
            instance = new Conversations();
        }

        return instance;
    }

    public boolean isUpdating(){
        return isUpdating;
    }

    public boolean needsRefresh(){
        return lastUpdateUser == null || ParseUser.getCurrentUser() != lastUpdateUser;
    }

    public void getConversation(final Group group, final FindCallback<Message> callback){
        List<Message> conv = instance.get(group);
        if(conv == null){
            Iterator<ParseUser> it = group.iterator();
            ParseUser user1 = it.next();
            ParseUser user2 = it.next();
            getMessages(user1, user2, new FindCallback<Message>() {
                @Override
                public void done(List<Message> messages, ParseException e) {
                    put(group, messages);
                    callback.done(messages, e);
                }
            });
        } else{
            callback.done(conv, null);
        }
    }

    public void addMessage(final Message message){
        message.fetchIfNeededInBackground(new GetCallback<Message>() {
            @Override
            public void done(final Message fetchedMessage, ParseException e) {
                if(e == null){
                    final Group group = new Group(message.getFromUser(), message.getToUser());
                    getConversation(group, new FindCallback<Message>() {
                        @Override
                        public void done(List<Message> messages, ParseException e) {
                            if(!hasMessage(fetchedMessage)){
                                messages.add(fetchedMessage);
                                notifyGroupUpdate(group, fetchedMessage);
                            }
                        }
                    });
                }
            }
        });
    }

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
     */
    private static void getMessages(ParseUser user1, ParseUser user2, FindCallback<Message> callback){
        List<ParseQuery<Message>> orQueries = new ArrayList<>();
        ParseQuery<Message> oredQuery;

        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.FROM_KEY, user1)
                        .whereEqualTo(Message.TO_KEY, user2)
        );
        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.TO_KEY, user1)
                        .whereEqualTo(Message.FROM_KEY, user2)
        );
        oredQuery = ParseQuery.or(orQueries);
        oredQuery.orderByAscending("createdAt");
        oredQuery.findInBackground(callback);
    }

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
        oredQuery.orderByAscending("updatedAt");
        oredQuery.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if(e == null){
                    getInstance().clear();

                    for(Message message : messages){
                        Group group = new Group(message.getFromUser(), message.getToUser());

                        // If the size is less than 2, the from user and 2 user are equal, which
                        // is not allowed.
                        if(group.size() < 2){
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
                    Log.d("Conversations", "DONE " + messages.size() + " " + Conversations.this.size());
                }

                isUpdating = false;
            }
        });
    }

    public static class Group extends HashSet<ParseUser>{
        public Group(Collection<ParseUser> members){
            super(members);
        }

        public Group(ParseUser... users){
            super();
            for(ParseUser user : users){
                add(user);
            }
        }

        public Group(){
            super();
        }

        public boolean addMember(ParseUser member){
            return add(member);
        }
    }
}
