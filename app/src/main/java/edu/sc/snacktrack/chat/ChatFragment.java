package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.sc.snacktrack.R;

/**
 * Fragment for chatting with another user.
 */
public class ChatFragment extends Fragment{
    private static final String TAG = "ChatFragment";

    private ParseUser otherUser;
    private Conversation conversation;

    private ListView messageListView;
    private Button sendButton;
    private EditText messageET;

    private View progressOverlay;

    private ChatAdapter chatAdapter;

    private Activity myActivity;

    private Toast toast;

    private NewMessageFetcher newMessageFetcher;

    private static final String STATE_MESSAGES = "messages";

    public static final String ARG_OTHER_USER_ID = "argOtherUserId";
    public static final String ARG_CONVERSATION_ID = "conversationId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatAdapter = new ChatAdapter(getContext());

        otherUser = ParseUser.createWithoutData(
                ParseUser.class, getArguments().getString(ARG_OTHER_USER_ID)
        );
        conversation = ParseObject.createWithoutData(
                Conversation.class, getArguments().getString(ARG_CONVERSATION_ID)
        );

        setTitleToUsername();

        try {
            Log.d(TAG, "fetch from local datastore...");
            conversation.fetchFromLocalDatastore();
        } catch (ParseException e) {
            Log.d(TAG, "fetch from local datastore failed!");
            conversation.fetchInBackground();
        }

        // If we're just starting this fragment, display any pinned messages while we fetch
        // new ones.
        if(savedInstanceState == null){
            displayPinnedMessages();
        }
    }

    /**
     * Attempts to set the action bar's title to the other user's username.
     */
    private void setTitleToUsername(){
        String username = otherUser.getUsername();
        if(username != null){
            getActivity().setTitle(username);
        } else{
            otherUser.fetchInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser fetchedUser, ParseException e) {
                    if (e == null) {
                        if (fetchedUser != null && fetchedUser.getUsername() != null) {
                            getActivity().setTitle(fetchedUser.getUsername());
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.myActivity = (Activity) context;
    }

    @Override
    public void onStart() {
        super.onStart();

        newMessageFetcher = new NewMessageFetcher();
        newMessageFetcher.execute();
    }

    @Override
    public void onStop(){
        super.onStop();

        if(newMessageFetcher != null){
            newMessageFetcher.cancel(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArray(STATE_MESSAGES, chatAdapter.getAllItems());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        messageListView = (ListView) view.findViewById(R.id.messageListView);
        messageListView.setAdapter(chatAdapter);
//        setListLongClickListener();

        messageET = (EditText) view.findViewById(R.id.toSendEditText);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Message message = new Message();
                ParseACL acl = new ParseACL();
                String messageStr = messageET.getText().toString();

                message.setMessage(messageStr);
                message.setFromUser(ParseUser.getCurrentUser());
                message.setToUser(otherUser);

                acl.setWriteAccess(ParseUser.getCurrentUser(), true);
                acl.setReadAccess(ParseUser.getCurrentUser(), true);
                acl.setReadAccess(otherUser, true);

                message.setACL(acl);

                messageET.setEnabled(false);
                sendButton.setEnabled(false);

                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            messageET.setText("");
                            chatAdapter.addMessage(message);
                            chatAdapter.notifyDataSetChanged();
                            conversation.setRecentMessage(message);
                            conversation.saveEventually();
                        }

                        messageET.setEnabled(true);
                        sendButton.setEnabled(true);
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            chatAdapter.addAll((ChatItem[]) savedInstanceState.getParcelableArray(STATE_MESSAGES));
        } else{
            updateAllMessages();
        }
    }

//    /**
//     * Sets the long click listener for messageListView. Currently, long clicking an item
//     * displays a dialog for deleting a message.
//     */
//    private void setListLongClickListener(){
//        messageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//                final ChatItem item = chatAdapter.getItem(position);
//                final Message message = ParseObject.createWithoutData(Message.class, item.getMessageId());
//
//                new AlertDialog.Builder(getContext())
//                        .setTitle("Delete this message?")
//                        .setMessage(String.format("%s", item.getMessage()))
//                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                message.deleteInBackground(new DeleteCallback() {
//                                    @Override
//                                    public void done(ParseException e) {
//                                        if (e == null) {
//                                            chatAdapter.remove(item);
//                                            chatAdapter.notifyDataSetChanged();
//                                        } else {
//                                            updateToast(
//                                                    "" + e.getMessage(),
//                                                    Toast.LENGTH_SHORT
//                                            );
//                                        }
//                                    }
//                                });
//                            }
//                        })
//                        .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        })
//                        .setCancelable(true)
//                        .show();
//                return true;
//            }
//        });
//    }

    /**
     * Fetches a full list of messages between the two users and repopulates chatAdapter with
     * those messages (clearing the old messages).
     */
    private void updateAllMessages(){
        List<ParseQuery<Message>> orQueries = new ArrayList<>();
        ParseQuery<Message> oredQuery;

        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.FROM_KEY, ParseUser.getCurrentUser())
                        .whereEqualTo(Message.TO_KEY, otherUser)
        );
        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.TO_KEY, ParseUser.getCurrentUser())
                        .whereEqualTo(Message.FROM_KEY, otherUser)
        );
        oredQuery = ParseQuery.or(orQueries);
        oredQuery.orderByAscending("createdAt");
        oredQuery.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> objects, ParseException e) {
                if (e == null) {
                    chatAdapter.clear();
                    for (Message message : objects) {
                        message.pinInBackground();
                        chatAdapter.add(new ChatItem(message));
                    }

                    chatAdapter.notifyDataSetChanged();
                } else {

                }
            }
        });
    }

    private void displayPinnedMessages(){
        List<ParseQuery<Message>> orQueries = new ArrayList<>();
        ParseQuery<Message> oredQuery;
        List<Message> pinnedMessages;

        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.FROM_KEY, ParseUser.getCurrentUser())
                        .whereEqualTo(Message.TO_KEY, otherUser)
        );
        orQueries.add(ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.TO_KEY, ParseUser.getCurrentUser())
                        .whereEqualTo(Message.FROM_KEY, otherUser)
        );
        oredQuery = ParseQuery.or(orQueries);
        oredQuery.orderByAscending("createdAt");
        oredQuery.fromLocalDatastore();
        try {
            pinnedMessages = oredQuery.find();
        } catch(ParseException e){
            // Give up and do not display anything
            pinnedMessages = null;
        }

        if(pinnedMessages != null){
            chatAdapter.clear();
            for(Message message : pinnedMessages){
                chatAdapter.add(new ChatItem((message)));
            }
            chatAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Cancels the current toast and displays a new toast.
     *
     * @param text The text to display
     * @param length The length to display the toast
     */
    private void updateToast(String text, int length){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(
                getContext(),
                text,
                length
        );
        toast.show();
    }

    /**
     * AsyncTask for periodically checking for new messages
     */
    private class NewMessageFetcher extends AsyncTask<Integer, Void, Void> {

        private static final int DEFAULT_INTERVAL_MILIS = 5000; // 5 seconds

        @Override
        protected Void doInBackground(Integer... params) {
            int intervalMilis;
            if(params.length > 0){
                intervalMilis = params[0];
            } else{
                intervalMilis = DEFAULT_INTERVAL_MILIS;
            }
            while(true){
                try{
                    Thread.sleep(intervalMilis);
                } catch(InterruptedException e){
                    updateToast("New Message Fetcher died", Toast.LENGTH_LONG);
                    Log.d(TAG, "New message fetcher died");
                    e.printStackTrace();
                    break;
                }

                fetchNewMessages();
            }

            return null;
        }

        /**
         * Fetches messages with a createdAt time greater than the most recent message in chatAdapter.
         */
        private void fetchNewMessages(){
            List<ParseQuery<Message>> orQueries = new ArrayList<>();
            ParseQuery<Message> oredQuery;
            ChatItem lastItem = chatAdapter.getLastFromOther();
            List<Message> newMessages = null;

            Date lastDate = new Date(0);
            if(lastItem != null){
                lastDate.setTime(lastItem.getCreatedTime());
            }

//            orQueries.add(ParseQuery.getQuery(Message.class)
//                            .whereEqualTo(Message.FROM_KEY, ParseUser.getCurrentUser())
//                            .whereEqualTo(Message.TO_KEY, otherUser)
//            );
            orQueries.add(ParseQuery.getQuery(Message.class)
                            .whereEqualTo(Message.TO_KEY, ParseUser.getCurrentUser())
                            .whereEqualTo(Message.FROM_KEY, otherUser)
            );
            oredQuery = ParseQuery.or(orQueries);
            oredQuery.whereGreaterThan("createdAt", lastDate);
            oredQuery.orderByAscending("createdAt");
            try {
                newMessages = oredQuery.find();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(newMessages != null){
                if(newMessages.size() > 0){
                    for(Message message : newMessages){
                        chatAdapter.add(new ChatItem(message));
                    }

                    if(myActivity!= null){
                        myActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatAdapter.notifyDataSetChanged();
                            }
                        });
                    } else{
                        Log.d(TAG, "myActivity is null");
                    }
                }
            }
        }
    }
}
