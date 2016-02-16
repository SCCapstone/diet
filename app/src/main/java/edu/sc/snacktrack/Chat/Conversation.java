package edu.sc.snacktrack.chat;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This ParseObject represents metadata about a conversation between two users.
 */
@ParseClassName("Conversation")
public class Conversation extends ParseObject {

    public static final String FROM_USER_KEY = "fromUser";
    public static final String TO_USER_KEY = "toUser";
    public static final String RECENT_MESSAGE_KEY = "recentMessage";

    public void setFromUser(ParseUser user){
        put(FROM_USER_KEY, user);
    }

    public ParseUser getFromUser(){
        return getParseUser(FROM_USER_KEY);
    }

    public void setToUser(ParseUser user){
        put(TO_USER_KEY, user);
    }

    public ParseUser getToUser(){
        return getParseUser(TO_USER_KEY);
    }

    public void setRecentMessage(Message message){
        put(RECENT_MESSAGE_KEY, message);
    }

    public Message getRecentMessage(){
        return (Message) getParseObject(RECENT_MESSAGE_KEY);
    }
}
