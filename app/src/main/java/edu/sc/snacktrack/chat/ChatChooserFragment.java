package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.os.Handler;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.Utils;

/**
 * Fragment for choosing from a list of previous chats or starting a new chat.
 */
public class ChatChooserFragment extends Fragment{

    private static final String TAG = "ChatChooserFragment";

    private static final int USERNAME_REQUEST_CODE = 100;

    private View progressOverlay;
    private Button newChatButton;
    private ListView chatChooserListView;

    private volatile boolean startingChat = false;

    private ChatChooserAdapter chatChooserAdapter;

    private Toast toast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatChooserAdapter = new ChatChooserAdapter(getContext());
        updateConversations();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Chat");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_chooser, container, false);
        newChatButton = (Button) view.findViewById(R.id.newChatButton);
        chatChooserListView = (ListView) view.findViewById(R.id.chatChooserListView);

        progressOverlay = view.findViewById(R.id.progressOverlay);

        chatChooserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatChooserItem item = chatChooserAdapter.getItem(position);
                startChat(item.getUsername());
            }
        });

        chatChooserListView.setAdapter(chatChooserAdapter);


        newChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewChatDialogFragment newChatDialogFragment = new NewChatDialogFragment();
                newChatDialogFragment.setTargetFragment(ChatChooserFragment.this, USERNAME_REQUEST_CODE);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                newChatDialogFragment.show(ft, "newChatDialog");
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case USERNAME_REQUEST_CODE:
                if(resultCode == Activity.RESULT_OK){
                    String username = data.getStringExtra("username");
                    Log.d(TAG, "received username " + username);
                    startChat(username);
                }
        }
    }

    /**
     * Attempts to start a chat with a specified username.
     *
     * @param username The username to chat with
     */
    private void startChat(final String username){

        if(username.equals(ParseUser.getCurrentUser().getUsername())){
            updateToast("You can't chat with yourself", Toast.LENGTH_SHORT);
            return;
        }

        if(!startingChat){
            progressOverlay.setVisibility(View.VISIBLE);
            newChatButton.setEnabled(false);
            startingChat = true;

            // If chatStarterThread is unexpectedly destroyed, this handler will keep the app from
            // crashing.
            Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler(){
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.d(TAG, "Uncaught exception: " + ex);

                    startingChat = false;
                    progressOverlay.setVisibility(View.GONE);
                    newChatButton.setEnabled(true);
                }
            };

            Thread chatStarterThread = new Thread(new Runnable() {
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startingChat = false;
                                progressOverlay.setVisibility(View.GONE);
                                newChatButton.setEnabled(true);
                            }
                        });
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle args = new Bundle();
                            FragmentManager fm = getFragmentManager();

                            // The fragment manager may be null if the fragment gets destroyed.
                            if(fm == null){
                                startingChat = false;
                                progressOverlay.setVisibility(View.GONE);
                                newChatButton.setEnabled(true);
                                return;
                            }

                            args.putString(ChatFragment.ARG_OTHER_USER_ID, conversation.getToUser().getObjectId());
                            args.putString(ChatFragment.ARG_CONVERSATION_ID, conversation.getObjectId());
                            Fragment chatFragment = new ChatFragment();
                            chatFragment.setArguments(args);
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, chatFragment)
                                    .addToBackStack(null)
                                    .commit();

                            // Give some time for the chat fragment to display before setting
                            // startingChat to false and hiding the progress overlay
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    startingChat = false;
                                    progressOverlay.setVisibility(View.GONE);
                                    newChatButton.setEnabled(true);
                                }
                            }, 1000);
                        }
                    });
                }
            });
            chatStarterThread.setUncaughtExceptionHandler(exceptionHandler);
            chatStarterThread.start();
        }
    }

    /**
     * Attempts to find a user with a specified username.
     *
     * @param username The username to search for
     * @return The user if one was found. null otherwise.
     */
    private ParseUser findUser(String username) {
        List<ParseUser> users;

        ParseQuery<ParseUser> query;

        // First try local query
        query = ParseUser.getQuery();
        query.whereEqualTo("username", username);
        query.fromLocalDatastore();
        try{
            users = query.find();
        } catch(ParseException e){
            updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
            return null;
        }

        // If local query fails, try online query
        if(users.size() != 0){
            return users.get(0);
        } else{
            query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            try{
                users = query.find();
            } catch(ParseException e){
                updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
                return null;
            }

            if(users.size() == 1){
                return users.get(0);
            } else{
                updateToast("Username not found", Toast.LENGTH_SHORT);
                return null;
            }
        }

    }

    /**
     * Attempts to find a conversation with the a specified user or creates a new one if
     * no conversation exists.
     *
     * @param otherUser The fromUser
     * @return The conversation if one was found or could be created. null otherwise.
     */
    private Conversation findConversation(ParseUser otherUser){

        List<Conversation> conversations;
        ParseQuery<Conversation> query = ParseQuery.getQuery(Conversation.class);
        query.fromLocalDatastore();
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
            updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
            return null;
        }
    }

    /**
     * Finds the relevant past conversations to display in the chat chooser.
     */
    private void updateConversations(){
        ArrayList<ParseQuery<Conversation>> orQueries = new ArrayList<>();
        ParseQuery<Conversation> oredQuery;

        orQueries.add(ParseQuery.getQuery(Conversation.class)
                        .whereEqualTo(Conversation.TO_USER_KEY, ParseUser.getCurrentUser())
        );
        orQueries.add(ParseQuery.getQuery(Conversation.class)
                        .whereEqualTo(Conversation.FROM_USER_KEY, ParseUser.getCurrentUser())
        );
        oredQuery = ParseQuery.or(orQueries);
        oredQuery.include(Conversation.FROM_USER_KEY);
        oredQuery.include(Conversation.TO_USER_KEY);
        oredQuery.include(Conversation.RECENT_MESSAGE_KEY);
        oredQuery.orderByDescending("updatedAt");
        oredQuery.findInBackground(new FindCallback<Conversation>() {
            @Override
            public void done(List<Conversation> conversations, ParseException e) {

                // Pin the conversations for faster access later
                for (Conversation conversation : conversations) {
                    conversation.pinInBackground();
                }

                for (Conversation conversation : filterConversations(conversations)) {
                    chatChooserAdapter.addConversation(conversation);
                }
//                chatChooserAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Filters a list of conversations by removing any duplicates.
     * The conversations *must* be sorted in descending order by updatedAt, or this method will
     * not work correctly.
     *
     * @param conversations The list of conversations to filter
     * @return The filtered conversations
     */
    private List<Conversation> filterConversations(List<Conversation> conversations){
        ArrayList<Conversation> filtered = new ArrayList<>();

        for(Conversation conversation : conversations){
            ParseUser otherUser1;
            boolean add = true;

            if(conversation.getToUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                otherUser1 = conversation.getFromUser();
            } else{
                otherUser1 = conversation.getToUser();
            }

            for(Conversation filteredConv : filtered){
                ParseUser otherUser2;

                if(filteredConv.getToUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                    otherUser2 = filteredConv.getFromUser();
                } else{
                    otherUser2 = filteredConv.getToUser();
                }


                if(otherUser1.getObjectId().equals(otherUser2.getObjectId())){
                    add = false;
                    break;
                }
            }

            if(add){
                filtered.add(conversation);
            }
        }

        return filtered;
    }

    /**
     * Cancels the current toast and displays a new toast.
     *
     * @param text The text to display
     * @param length The length to display the toast
     */
    private void updateToast(final String text, final int length){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }

                toast = Toast.makeText(
                        getActivity(),
                        text,
                        length
                );
                toast.show();
            }
        });
    }
}
