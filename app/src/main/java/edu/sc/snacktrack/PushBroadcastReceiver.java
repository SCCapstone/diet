package edu.sc.snacktrack;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParseBroadcastReceiver;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import edu.sc.snacktrack.chat.ChatActivity;
import edu.sc.snacktrack.chat.ChatAdapter;
import edu.sc.snacktrack.chat.Conversations;
import edu.sc.snacktrack.chat.Message;

public class PushBroadcastReceiver extends ParsePushBroadcastReceiver{

    private static final String TAG = "PushBroadcastReceiver";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.d(TAG, "push received.");

        Context appContext = (SnackTrackApplication) context.getApplicationContext();

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
        } catch(JSONException e){
            Log.d(TAG, e.getMessage());
            intent.putExtra("isChat", false);
        }
        super.onPushOpen(context, intent);
    }

    private boolean isChat(Intent intent){
        boolean isChat = false;
        try {
            JSONObject pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
            if(pushData.has("isChat")){
                Object isChatObj = pushData.get("isChat");
                if(isChatObj instanceof Boolean){
                    isChat = (Boolean) isChatObj;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            isChat = false;
        }

        return isChat;
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // Do not display new message notification if the user is already chatting with that person.
        SnackTrackApplication application = ((SnackTrackApplication) context.getApplicationContext());
        if(isChat(intent) && application.getCurrentActivity() instanceof ChatActivity){
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
