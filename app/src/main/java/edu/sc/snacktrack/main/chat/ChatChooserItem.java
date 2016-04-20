package edu.sc.snacktrack.main.chat;

/**
 * This class wraps a Conversation ParseObject to display in ChatChooserAdapter.
 */
public class ChatChooserItem implements Comparable<ChatChooserItem>{

    private String username;
    private String recentMessage;
    private String userId;
    private long createdTime;

    /**
     * Creates a new ChatChooserItem with all fields specified.
     *
     * @param username The other user's username
     * @param recentMessage The recent message string
     * @param userId The objectId of the other user
     */
    public ChatChooserItem(String username, String recentMessage, String userId, long createdTime){
        this.username = username;
        this.recentMessage = recentMessage;
        this.userId = userId;
        this.createdTime = createdTime;
    }

    /**
     * Creates an empty ChatChooserItem.
     */
    public ChatChooserItem(){
        this("", "", "", 0l);
    }

    /**
     * Gets the objectId of the other user.
     *
     * @return The objectId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the objectId of the other user.
     *
     * @param userId The objectId
     */
    public void setUserId(String userId) {
        this.userId = userId;
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