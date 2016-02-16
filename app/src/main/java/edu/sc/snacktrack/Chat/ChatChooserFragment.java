package edu.sc.snacktrack.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.Utils;

public class ChatChooserFragment extends Fragment{

    private static final String TAG = "ChatChooserFragment";

    private volatile boolean startingChat = false;

    private static final String STATE_STARTING_CHAT = "startingChat";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_chooser, container, false);
        final Button button = (Button) view.findViewById(R.id.button);
        final EditText usernameEditText = (EditText) view.findViewById(R.id.usernameEditText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                Utils.closeSoftKeyboard(getContext(), button);
                startChat(username);
            }
        });

        if(savedInstanceState != null){
            startingChat = savedInstanceState.getBoolean(STATE_STARTING_CHAT);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_STARTING_CHAT, startingChat);
    }

    private void startChat(final String username){

        if(!startingChat){
            startingChat = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ParseUser user = findUser(username);
                    final Conversation conversation;
                    if(user != null){
                        conversation = findConversation(user);
                        if(conversation != null){
                            try {
                                conversation.pin();
                            } catch (ParseException e) {
                                // will return null
                            }
                        }
                    } else{
                        conversation = null;
                    }

                    if(conversation == null){
                        Log.d(TAG, "null conversation");
                        startingChat = false;
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle args = new Bundle();
                            args.putString(ChatFragment.ARG_OTHER_USER_ID, conversation.getToUser().getObjectId());
                            args.putString(ChatFragment.ARG_CONVERSATION_ID, conversation.getObjectId());
                            Fragment chatFragment = new ChatFragment();
                            chatFragment.setArguments(args);
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, chatFragment)
                                    .addToBackStack(null)
                                    .commit();
                            startingChat = false;
                        }
                    });

                }
            }).start();
        }
    }

    private ParseUser findUser(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", username);
        try{
            List<ParseUser> users = query.find();
            if(users.size() == 1){
                return users.get(0);
            } else{
                return null;
            }
        } catch(ParseException e){
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
                conversation.setACL(new ParseACL(ParseUser.getCurrentUser()));
                conversation.setFromUser(ParseUser.getCurrentUser());
                conversation.setToUser(otherUser);
                conversation.save();
                return conversation;
            }
        } catch (ParseException e) {
            return null;
        }
    }
}
