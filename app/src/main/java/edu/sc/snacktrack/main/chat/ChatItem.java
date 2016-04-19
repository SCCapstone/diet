package edu.sc.snacktrack.main.chat;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * This class wraps a Message ParseObject to display in ChatAdapter. This class is parcelable
 * so it can be saved onSaveInstanceState().
 */
public class ChatItem implements Parcelable{

    private static final String TAG = "ChatItem";

    private String message;
    private String fromUsername;
    private String toUsername;
    private String messageId;
    private long createdTime;

    /**
     * Creates a ChatItem with all fields specified.
     *
     * @param message The message string
     * @param fromUsername The from user's username
     * @param toUsername The to user's username
     * @param messageId The objectId of the Message ParseObject
     * @param createdTime The createdAt time for the Message ParseObject
     */
    public ChatItem(String message, String fromUsername, String toUsername, String messageId, long createdTime){
        this.message = message;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.messageId = messageId;
        this.createdTime = createdTime;
    }

    /**
     * Creates a ChatItem from a parcel.
     *
     * @param parcel The parcel
     */
    public ChatItem(Parcel parcel){

        this(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readLong());
    }

    /**
     * Creates a ChatItem from a Message ParseObject. The Message must be fully fetched or bad
     * things will happen.
     *
     * @param message The fetched Message
     */
    public ChatItem(Message message){
        this(message.getMessage(), message.getFromUser().getUsername(), message.getToUser().getUsername(), message.getObjectId(), message.getCreatedAt().getTime());
    }

    /**
     * Creates an empty ChatItem.
     */
    public ChatItem(){
        this("", "", "", "", 0);
    }

    /**
     * Reads a Message ParseObject into this ChatItem. The message must be fully fetched or
     * bad things will happen.
     *
     * @param message The fetched message
     */
    public void readMessage(Message message){
        this.fromUsername = message.getFromUser().getUsername();
        this.toUsername = message.getToUser().getUsername();
        this.messageId = message.getObjectId();
        this.message = message.getMessage();
        this.createdTime = message.getCreatedAt().getTime();
    }

    /**
     * Gets the createdAt time of the message.
     *
     * @return The createdAt time
     */
    public long getCreatedTime(){
        return createdTime;
    }

    /**
     * Gets the objectId of the message.
     *
     * @return The objectId
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets the from user's username.
     *
     * @return The from user's username
     */
    public String getFromUsername() {
        return fromUsername;
    }

    /**
     * Gets the to user's username.
     *
     * @return The to user's username
     */
    public String getToUsername() {
        return toUsername;
    }

    /**
     * Gets the message string.
     *
     * @return The message string
     */
    public String getMessage() {
        return message;
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

    /**
     * Required CREATOR static field for parcelable objects.
     */
    public static final Parcelable.Creator<ChatItem> CREATOR = new Parcelable.Creator<ChatItem>(){

        @Override
        public ChatItem createFromParcel(Parcel in){
            return new ChatItem(in);
        }

        @Override
        public ChatItem[] newArray(int size){
            return new ChatItem[size];
        }
    };
}
