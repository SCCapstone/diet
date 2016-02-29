package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.Utils;

public class NewChatDialogFragment extends DialogFragment{

    private static final String TAG = "NewChatDialogFragment";

    private boolean startingChat = false;

    private AppCompatActivity myActivity;

    private Toast toast;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = (AppCompatActivity) activity;
    }

    private ParseUser findUser(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", username);
        try{
            List<ParseUser> users = query.find();
            if(users.size() == 1){
                return users.get(0);
            } else{
                updateToast("User not found", Toast.LENGTH_SHORT);
                return null;
            }
        } catch(ParseException e){
            updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
            return null;
        }

    }

    private Conversation findConversation(ParseUser otherUser){

        List<Conversation> conversations;
        ParseQuery<Conversation> query = ParseQuery.getQuery(Conversation.class);
        query.whereEqualTo(Conversation.FROM_USER_KEY, ParseUser.getCurrentUser());
        query.whereEqualTo(Conversation.TO_USER_KEY, otherUser);
        try {
            conversations = query.find();
            if(conversations.size() > 0){
                return conversations.get(0);
            } else{
                Conversation conversation = new Conversation();
                ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
                acl.setReadAccess(otherUser, true);
                conversation.setACL(acl);
                conversation.setFromUser(ParseUser.getCurrentUser());
                conversation.setToUser(otherUser);
                conversation.save();
                conversation.pin();
                return conversation;
            }
        } catch (ParseException e) {
            return null;
        }
    }


    private void startChat(final String username){

        if(username.equals(ParseUser.getCurrentUser().getUsername())){
            updateToast("You can't chat with yourself", Toast.LENGTH_SHORT);
            return;
        }

        if(!startingChat){
            startingChat = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ParseUser user = findUser(username);
                    final Conversation conversation;
                    if(user != null){
                        conversation = findConversation(user);
                    } else{
                        conversation = null;
                    }

                    if(conversation == null){
                        Log.d(TAG, "null conversation");
                        startingChat = false;
                        return;
                    }

                    myActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle args = new Bundle();
                            args.putString(ChatFragment.ARG_OTHER_USER_ID, conversation.getToUser().getObjectId());
                            args.putString(ChatFragment.ARG_CONVERSATION_ID, conversation.getObjectId());
                            Fragment chatFragment = new ChatFragment();
                            chatFragment.setArguments(args);
                            myActivity.getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, chatFragment)
                                    .addToBackStack(null)
                                    .commit();
                            startingChat = false;
                            dismiss();
                        }
                    });

                }
            }).start();
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_chat_dialog, container, false);
        final EditText usernameEditText = (EditText) view.findViewById(R.id.usernameEditText);
        final Button chatButton = (Button) view.findViewById(R.id.chatButton);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                startChat(username);
                Utils.closeSoftKeyboard(myActivity, chatButton);
            }
        });
        getDialog().setTitle("New chat");
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    /**
     * Cancels the current toast and displays a new toast.
     *
     * @param text The text to display
     * @param length The length to display the toast
     */
    private void updateToast(final String text, final int length){
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }

                toast = Toast.makeText(
                        myActivity,
                        text,
                        length
                );
                toast.show();
            }
        });
    }
}
