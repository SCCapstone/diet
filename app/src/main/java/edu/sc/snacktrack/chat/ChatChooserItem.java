package edu.sc.snacktrack.chat;

/**
 * This class wraps a Conversation ParseObject to display in ChatChooserAdapter.
 */
public class ChatChooserItem implements Comparable<ChatChooserItem>{

    private String username;
    private String recentMessage;
    private String id;
    private long createdTime;

    /**
     * Creates a new ChatChooserItem with all fields specified.
     *
     * @param username The other user's username
     * @param recentMessage The recent message string
     * @param id The objectId of the Conversation ParseObject
     */
    public ChatChooserItem(String username, String recentMessage, String id, long createdTime){
        this.username = username;
        this.recentMessage = recentMessage;
        this.id = id;
        this.createdTime = createdTime;
    }

    /**
     * Creates an empty ChatChooserItem.
     */
    public ChatChooserItem(){
        this("", "", "", 0l);
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

    /**
     * Gets the created time.
     *
     * @return The created time
     */
    public long getCreatedTime(){
        return createdTime;
    }

    /**
     * Sets the created time.
     *
     * @param createdTime The created time
     */
    public void setCreatedTime(long createdTime){
        this.createdTime = createdTime;
    }

    @Override
    public int compareTo(ChatChooserItem another) {
        return Long.valueOf(this.createdTime).compareTo(another.createdTime);
    }
}