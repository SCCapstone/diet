package edu.sc.snacktrack.main.chat;

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

    /**
     * Gets the from user.
     *
     * @return The from user
     */
    public ParseUser getFromUser(){
        return getParseUser(FROM_KEY);
    }

    /**
     * Gets the to user.
     *
     * @return The from user
     */
    public ParseUser getToUser(){
        return getParseUser(TO_KEY);
    }

    /**
     * Gets the message string.
     *
     * @return The message string
     */
    public String getMessage(){
        return getString(MESSAGE_KEY);
    }

    /**
     * Sets the from user.
     *
     * @param user The from user
     */
    public void setFromUser(ParseUser user){
        put(FROM_KEY, user);
    }

    /**
     * Sets the to user.
     *
     * @param user The to user
     */
    public void setToUser(ParseUser user){
        put(TO_KEY, user);
    }

    /**
     * Sets the message string.
     *
     * @param message The message string
     */
    public void setMessage(String message){
        put(MESSAGE_KEY, message);
    }
}