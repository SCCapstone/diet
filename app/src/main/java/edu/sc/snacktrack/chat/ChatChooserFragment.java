package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.Utils;

/**
 * Fragment for choosing from a list of previous chats or starting a new chat.
 */
public class ChatChooserFragment extends Fragment{

    private static final String TAG = "ChatChooserFragment";

    private static final int USERNAME_REQUEST_CODE = 100;

    private Activity myActivity;

    private View progressOverlay;
    private Button newChatButton;
    private ListView chatChooserListView;

    private volatile boolean startingChat = false;

    private ChatChooserAdapter chatChooserAdapter;

    private ChatStarter chatStarter;

    private Toast toast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatChooserAdapter = new ChatChooserAdapter(getContext());
//        updateConversations();

        if(!Conversations.getInstance().isUpdating() && Conversations.getInstance().needsRefresh()){
            Conversations.getInstance().refresh(new FindCallback<Message>() {
                @Override
                public void done(List<Message> messages, ParseException e) {
                    Set<Conversations.Group> groups = Conversations.getInstance().keySet();
                    for(Conversations.Group group : groups){
                        final ChatChooserItem item = new ChatChooserItem();
                        Iterator<ParseUser> it = group.iterator();
                        ParseUser user1 = it.next();
                        ParseUser user2 = it.next();
                        if(user1 == ParseUser.getCurrentUser()){
                            item.setUsername(user2.getUsername());
                        } else if(user2 == ParseUser.getCurrentUser()){
                            item.setUsername(user1.getUsername());
                        } else{
                            // This group doesn't have the current user
                            continue;
                        }
                        Conversations.getInstance().getConversation(group, new FindCallback<Message>() {
                            @Override
                            public void done(List<Message> objects, ParseException e) {
                                int recentMessageIndex = objects.size() - 1;
                                item.setRecentMessage(objects.get(recentMessageIndex).getMessage());
                                item.setCreatedTime(objects.get(recentMessageIndex).getCreatedAt().getTime());
                            }
                        });
                        chatChooserAdapter.addItem(item);
                    }
                    chatChooserAdapter.sort();
                    chatChooserAdapter.notifyDataSetChanged();
                    Log.d(TAG, "DONE " + chatChooserAdapter.getCount());
                }
            });
        } else{
            Set<Conversations.Group> groups = Conversations.getInstance().keySet();
            for(Conversations.Group group : groups){
                Log.d(TAG, "add");
                Log.d(TAG, "group has " + group.size() + " members.");
                final ChatChooserItem item = new ChatChooserItem();
                Iterator<ParseUser> it = group.iterator();
                ParseUser user1 = it.next();
                ParseUser user2 = it.next();
                if(user1 == ParseUser.getCurrentUser()){
                    item.setUsername(user2.getUsername());
                } else if(user2 == ParseUser.getCurrentUser()){
                    item.setUsername(user1.getUsername());
                }
                Conversations.getInstance().getConversation(group, new FindCallback<Message>() {
                    @Override
                    public void done(List<Message> objects, ParseException e) {
                        int recentMessageIndex = objects.size() - 1;
                        item.setRecentMessage(objects.get(recentMessageIndex).getMessage());
                        item.setCreatedTime(objects.get(recentMessageIndex).getCreatedAt().getTime());
                    }
                });
                chatChooserAdapter.addItem(item);
            }
            chatChooserAdapter.sort();
            chatChooserAdapter.notifyDataSetChanged();
            Log.d(TAG, "DONE " + chatChooserAdapter.getCount());
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity = (Activity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Chat");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel any running chat starter.
        if(chatStarter != null){
            chatStarter.cancel(true);
        }
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
            chatStarter = new ChatStarter();
            chatStarter.execute(username);
        }
    }

//    /**
//     * Finds the relevant past conversations to display in the chat chooser.
//     */
//    private void updateConversations(){
//        ArrayList<ParseQuery<Conversation>> orQueries = new ArrayList<>();
//        ParseQuery<Conversation> oredQuery;
//
//        orQueries.add(ParseQuery.getQuery(Conversation.class)
//                        .whereEqualTo(Conversation.TO_USER_KEY, ParseUser.getCurrentUser())
//        );
//        orQueries.add(ParseQuery.getQuery(Conversation.class)
//                        .whereEqualTo(Conversation.FROM_USER_KEY, ParseUser.getCurrentUser())
//        );
//        oredQuery = ParseQuery.or(orQueries);
//        oredQuery.include(Conversation.FROM_USER_KEY);
//        oredQuery.include(Conversation.TO_USER_KEY);
//        oredQuery.include(Conversation.RECENT_MESSAGE_KEY);
//        oredQuery.orderByDescending("updatedAt");
//        oredQuery.findInBackground(new FindCallback<Conversation>() {
//            @Override
//            public void done(List<Conversation> conversations, ParseException e) {
//                if (e == null) {
//                    // Pin the conversations for faster access later
//                    for (Conversation conversation : conversations) {
//                        conversation.pinInBackground();
//                    }
//
//                    for (Conversation conversation : filterConversations(conversations)) {
//                        chatChooserAdapter.addConversation(conversation);
//                    }
//                } else {
//                    updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
//                }
//            }
//        });
//    }
//
//    /**
//     * Filters a list of conversations by removing any duplicates.
//     * The conversations *must* be sorted in descending order by updatedAt, or this method will
//     * not work correctly.
//     *
//     * @param conversations The list of conversations to filter
//     * @return The filtered conversations
//     */
//    private List<Conversation> filterConversations(List<Conversation> conversations){
//        ArrayList<Conversation> filtered = new ArrayList<>();
//
//        for(Conversation conversation : conversations){
//            ParseUser otherUser1;
//            boolean add = true;
//
//            if(conversation.getToUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
//                otherUser1 = conversation.getFromUser();
//            } else{
//                otherUser1 = conversation.getToUser();
//            }
//
//            for(Conversation filteredConv : filtered){
//                ParseUser otherUser2;
//
//                if(filteredConv.getToUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
//                    otherUser2 = filteredConv.getFromUser();
//                } else{
//                    otherUser2 = filteredConv.getToUser();
//                }
//
//
//                if(otherUser1.getObjectId().equals(otherUser2.getObjectId())){
//                    add = false;
//                    break;
//                }
//            }
//
//            if(add){
//                filtered.add(conversation);
//            }
//        }
//
//        return filtered;
//    }

    /**
     * Cancels the current toast and displays a new toast.
     *
     * @param text The text to display
     * @param length The length to display the toast
     */
    private void updateToast(final String text, final int length){
        if(myActivity != null){
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
    }

    /**
     * Async task for starting a new chat
     */
    private class ChatStarter extends AsyncTask<String, Void, Void>{

        private String usernameStr;

        private ParseUser user;
        private Conversation conversation;
        private ParseException exception;

        ParseQuery<?> currentQuery;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            startingChat = true;
            progressOverlay.setVisibility(View.VISIBLE);
            newChatButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(String... username) {

            this.usernameStr = username[0];

            try{
                this.user = findUser(usernameStr);
            } catch(ParseException e){
                this.user = null;
                this.exception = e;
                return null;
            }

            if(user != null){
                try{
                    this.conversation = findConversation(user);
                } catch(ParseException e){
                    this.conversation = null;
                    this.exception = e;
                    return null;
                }
            } else{
                this.conversation = null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            if(exception == null){
                if(conversation != null){
                    startChatFragment(conversation);
                } else if(user == null){
                    updateToast(String.format("User %s not found", usernameStr), Toast.LENGTH_SHORT);
                }
            } else{
                updateToast(Utils.getErrorMessage(exception), Toast.LENGTH_LONG);
            }

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

        private void startChatFragment(Conversation conversation){
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
        }

        /**
         * Attempts to find a user with a specified username.
         *
         * @param username The username to search for
         * @return The user if one was found. null otherwise.
         */
        private ParseUser findUser(String username) throws ParseException{
            List<ParseUser> users;

            ParseQuery<ParseUser> query;

            // First try local query
            query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            query.fromLocalDatastore();

            this.currentQuery = query;

            users = query.find();

            // If local query fails, try online query
            if(users.size() != 0){
                return users.get(0);
            } else{
                query = ParseUser.getQuery();
                query.whereEqualTo("username", username);

                users = query.find();

                if(users.size() == 1){
                    return users.get(0);
                } else{
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
        private Conversation findConversation(ParseUser otherUser) throws ParseException{

            List<Conversation> conversations;
            ParseQuery<Conversation> query = ParseQuery.getQuery(Conversation.class);
            query.fromLocalDatastore();
            query.whereEqualTo(Conversation.FROM_USER_KEY, ParseUser.getCurrentUser());
            query.whereEqualTo(Conversation.TO_USER_KEY, otherUser);

            this.currentQuery = query;

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

        }
    }
}
