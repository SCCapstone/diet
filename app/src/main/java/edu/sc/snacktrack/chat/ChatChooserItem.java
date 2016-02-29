package edu.sc.snacktrack.chat;

public class ChatChooserItem {

    private String username;
    private String recentMessage;
    private String id;

    public ChatChooserItem(String username, String recentMessage, String id){
        this.username = username;
        this.recentMessage = recentMessage;
        this.id = id;
    }

    public ChatChooserItem(){
        this("", "", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRecentMessage() {
        return recentMessage;
    }

    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage;
    }
}
