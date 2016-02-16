package edu.sc.snacktrack.chat;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * This ParseObject represents a single message between two users.
 */
@ParseClassName("Message")
public class Message extends ParseObject{

    public static final String FROM_KEY = "from";
    public static final String TO_KEY = "to";
    public static final String MESSAGE_KEY = "message";

    public ParseUser getFromUser(){
        return getParseUser(FROM_KEY);
    }

    public ParseUser getToUser(){
        return getParseUser(TO_KEY);
    }

    public String getMessage(){
        return getString(MESSAGE_KEY);
    }

    public void setFromUser(ParseUser user){
        put(FROM_KEY, user);
    }

    public void setToUser(ParseUser user){
        put(TO_KEY, user);
    }

    public void setMessage(String message){
        put(MESSAGE_KEY, message);
    }
}