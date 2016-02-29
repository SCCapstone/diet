package edu.sc.snacktrack.chat;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * This class essentially wraps a message to make it parcelable.
 */
public class ChatItem implements Parcelable{

    private static final String TAG = "ChatItem";

    private String message;
    private String fromUsername;
    private String toUsername;
    private String messageId;
    private long createdTime;

    public ChatItem(String message, String fromUsername, String toUsername, String messageId, long createdTime){
        this.message = message;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.messageId = messageId;
        this.createdTime = createdTime;
    }

    public ChatItem(Parcel parcel){

        this(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readLong());
    }

    public ChatItem(Message message){
        this(message.getMessage(), message.getFromUser().getUsername(), message.getToUser().getUsername(), message.getObjectId(), message.getCreatedAt().getTime());
    }

    public ChatItem(){
        this("", "", "", "", 0);
    }

    public void readMessage(Message message){
        this.fromUsername = message.getFromUser().getUsername();
        this.toUsername = message.getToUser().getUsername();
        this.messageId = message.getObjectId();
        this.message = message.getMessage();
        this.createdTime = message.getCreatedAt().getTime();
    }

    public long getCreatedTime(){
        return createdTime;
    }

    public void setCreatedTime(long createdTime){
        this.createdTime = createdTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }


    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "Write to parcel");
        dest.writeString(message);
        dest.writeString(fromUsername);
        dest.writeString(toUsername);
        dest.writeString(messageId);
        dest.writeLong(createdTime);
    }

    public static final Parcelable.Creator<ChatItem> CREATOR = new Parcelable.Creator<ChatItem>(){
        public ChatItem createFromParcel(Parcel in){
            return new ChatItem(in);
        }

        public ChatItem[] newArray(int size){
            return new ChatItem[size];
        }
    };
}
