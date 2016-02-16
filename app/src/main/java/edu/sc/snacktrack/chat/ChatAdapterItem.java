package edu.sc.snacktrack.chat;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.parse.ParseObject;

import java.util.Date;

/**
 * This class essentially wraps a message to make it parcelable.
 */
public class ChatAdapterItem implements Parcelable{

    private static final String TAG = "ChatAdapterItem";

    private String message;
    private String fromUsername;
    private String toUsername;
    private String messageId;
    private long createdTime;

    public ChatAdapterItem(String message, String fromUsername, String toUsername, String messageId, long createdTime){
        this.message = message;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.messageId = messageId;
        this.createdTime = createdTime;
    }

    public ChatAdapterItem(Parcel parcel){

        this(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readLong());
    }

    public ChatAdapterItem(Message message){
        this(message.getMessage(), message.getFromUser().getUsername(), message.getToUser().getUsername(), message.getObjectId(), message.getCreatedAt().getTime());
    }

    public ChatAdapterItem(){
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

    public static final Parcelable.Creator<ChatAdapterItem> CREATOR = new Parcelable.Creator<ChatAdapterItem>(){
        public ChatAdapterItem createFromParcel(Parcel in){
            return new ChatAdapterItem(in);
        }

        public ChatAdapterItem[] newArray(int size){
            return new ChatAdapterItem[size];
        }
    };
}
