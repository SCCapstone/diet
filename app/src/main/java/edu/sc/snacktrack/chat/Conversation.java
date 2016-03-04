package edu.sc.snacktrack.chat;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * This ParseObject represents metadata about a conversation between two users.
 */
@ParseClassName("Conversation")
public class Conversation extends ParseObject {

    public static final String FROM_USER_KEY = "fromUser";
    public static final String TO_USER_KEY = "toUser";
    public static final String RECENT_MESSAGE_KEY = "recentMessage";

    /**
     * Sets the from user.
     *
     * @param user The from user
     */
    public void setFromUser(ParseUser user){
        put(FROM_USER_KEY, user);
    }

    /**
     * Gets the from user.
     *
     * @return The from user
     */
    public ParseUser getFromUser(){
        return getParseUser(FROM_USER_KEY);
    }

    /**
     * Sets the to user.
     *
     * @param user The to user
     */
    public void setToUser(ParseUser user){
        put(TO_USER_KEY, user);
    }

    /**
     * Gets the to user.
     *
     * @return The from user
     */
    public ParseUser getToUser(){
        return getParseUser(TO_USER_KEY);
    }

    /**
     * Sets the recent message ParseObject for this conversation.
     *
     * @param message The recent message
     */
    public void setRecentMessage(Message message){
        put(RECENT_MESSAGE_KEY, message);
    }

    /**
     * Gets the recent message ParseObject for this conversation.
     *
     * @return The recent message
     */
    public Message getRecentMessage(){
        return (Message) getParseObject(RECENT_MESSAGE_KEY);
    }
}
