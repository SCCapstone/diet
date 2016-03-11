package edu.sc.snacktrack;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import edu.sc.snacktrack.chat.Conversations;
import edu.sc.snacktrack.chat.Message;

public class PushBroadcastReceiver extends ParsePushBroadcastReceiver{

    private static final String TAG = "PushBroadcastReceiver";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.d(TAG, "push received.");
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
}
