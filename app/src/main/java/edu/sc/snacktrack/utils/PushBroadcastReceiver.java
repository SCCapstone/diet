package edu.sc.snacktrack.utils;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import edu.sc.snacktrack.SnackTrackApplication;
import edu.sc.snacktrack.main.chat.ChatActivity;
import edu.sc.snacktrack.main.chat.Conversations;
import edu.sc.snacktrack.main.chat.Message;

public class PushBroadcastReceiver extends ParsePushBroadcastReceiver{

    private static final String TAG = "PushBroadcastReceiver";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.d(TAG, "push received.");

        // If the push notification contains a chat message, add it the Conversations
        // instance
        try {
            JSONObject pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
            if(pushData.has("isChat")){
                Object isChatObj = pushData.get("isChat");
                if(isChatObj instanceof Boolean){
                    boolean isChat = (Boolean) isChatObj;
                    if(isChat){
                        Log.d(TAG, "Message received: " + pushData.getString("messageStr"));
                        Message message = Message.createWithoutData(Message.class, pushData.getString("messageId"));
                        Conversations.getInstance().addMessage(message);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        super.onPushReceive(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        // If the push notification contains a chat message, add the necessary extras to the intent
        // so MainActivity can parse them.
        try{
            JSONObject pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
            if(pushData.has("isChat")){
                Object isChatObj = pushData.get("isChat");
                if(isChatObj instanceof Boolean){
                    boolean isChat = (Boolean) isChatObj;
                    intent.putExtra("isChat", isChat);
                    intent.putExtra("fromUserId", pushData.getString("fromUserId"));
                    intent.putExtra("fromUserName", pushData.getString("fromUserName"));
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            intent.putExtra("isChat", false);
        }
        super.onPushOpen(context, intent);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // Do not display new message notification if the user is already chatting with that person.
        SnackTrackApplication application = ((SnackTrackApplication) context.getApplicationContext());
        if(application.getCurrentActivity() instanceof ChatActivity){
            ChatActivity chatActivity = (ChatActivity) application.getCurrentActivity();
            try{
                JSONObject pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
                String fromUserId = pushData.getString("fromUserId");
                if(chatActivity.getOtherUserId().equals(fromUserId)){
                    return null;
                }
            } catch(JSONException e){ }
        }

        return super.getNotification(context, intent);
    }
}
