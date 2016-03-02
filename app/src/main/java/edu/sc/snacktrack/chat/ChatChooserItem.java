package edu.sc.snacktrack.chat;

/**
 * This class wraps a Conversation ParseObject to display in ChatChooserAdapter.
 */
public class ChatChooserItem {

    private String username;
    private String recentMessage;
    private String id;

    /**
     * Creates a new ChatChooserItem with all fields specified.
     *
     * @param username The other user's username
     * @param recentMessage The recent message string
     * @param id The objectId of the Conversation ParseObject
     */
    public ChatChooserItem(String username, String recentMessage, String id){
        this.username = username;
        this.recentMessage = recentMessage;
        this.id = id;
    }

    /**
     * Creates an empty ChatChooserItem.
     */
    public ChatChooserItem(){
        this("", "", "");
    }

    /**
     * Gets the objectId of the conversation.
     *
     * @return The objectId
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the objectId of the conversation.
     *
     * @param id The objectId
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the other user's username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the other user's username.
     *
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the recent message string.
     *
     * @return The recent message string
     */
    public String getRecentMessage() {
        return recentMessage;
    }

    /**
     * Sets the recent message string.
     *
     * @param recentMessage The recent message string
     */
    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage;
    }
}