package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.ListIterator;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.Utils;

/**
 * Fragment for chatting with another user.
 */
public class ChatFragment extends Fragment implements Conversations.UpdateListener{
    private static final String TAG = "ChatFragment";

    private ParseUser otherUser;
    private Conversations.Group group;

    private ListView messageListView;
    private Button sendButton;
    private EditText messageET;

    private View progressOverlay;

    private ChatAdapter chatAdapter;

    private Context context;

    private Toast toast;

    /**
     * Whether or not validating is in progress (that is, this validateUserInfo() is running).
     */
    private boolean userValidatingInProgress = false;

    /**
     * Whether or not the other user has been successfully validated.
     */
    private boolean userChecked = false;

    /**
     * Whether or not the user is valid. This flag is irrelevant if userChecked is false.
     */
    private boolean userValid = false;

    private static final String STATE_MESSAGES = "messages";

    public static final String ARG_OTHER_USER_ID = "argOtherUserId";
    public static final String ARG_OTHER_USER_NAME = "argOtherUserName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatAdapter = new ChatAdapter(getContext());

        // Check arguments
        if(getArguments().getString(ARG_OTHER_USER_ID) == null || getArguments().getString(ARG_OTHER_USER_NAME) == null){
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        otherUser = ParseUser.createWithoutData(
                ParseUser.class, getArguments().getString(ARG_OTHER_USER_ID)
        );
        group = new Conversations.Group(ParseUser.getCurrentUser(), otherUser);

        setTitleToUsername();
        validateUserInfo();
    }

    /**
     * Checks that the information for the other user is valid. Sets the following flags
     * as appropriate:
     *
     *   userChecked - whether or not the user has been successfully validated.
     *   userValid - whether or not the user is valid.
     *   userValidatingInProgress - whether or not validating is in progress (that is, this method
     *   is running).
     */
    private void validateUserInfo(){
        if(!userValidatingInProgress){
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            String otherUserId = getArguments().getString(ARG_OTHER_USER_ID);
            String otherUserName = getArguments().getString(ARG_OTHER_USER_NAME);

            userChecked = false;
            userValidatingInProgress = true;

            query.whereEqualTo("objectId", otherUserId);
            query.whereEqualTo("username", otherUserName);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if(e == null){
                        if(objects.size() == 1){
                            userValid = true;
                        } else{
                            userValid = false;
                        }
                        userChecked = true;
                    } else{
                        updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
                        userValid = false;
                        userChecked = false;
                    }

                    userValidatingInProgress = false;
                }
            });
        }
    }

    /**
     * Attempts to set the action bar's title to the other user's username.
     */
    private void setTitleToUsername(){
        getActivity().setTitle(getArguments().getString(ARG_OTHER_USER_NAME));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();

        Conversations.getInstance().registerUpdateListener(this);
        refreshAdapter();
    }

    @Override
    public void onStop(){
        super.onStop();
        Conversations.getInstance().unregisterUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        chatAdapter.clear();
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
        setListViewScrollListener();

        messageET = (EditText) view.findViewById(R.id.toSendEditText);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        setSendButtonClickListener();

        return view;
    }

    /**
     * Sets the onClickListener for the send message button.
     */
    private void setSendButtonClickListener(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Make sure the user is valid
                if (userChecked) {
                    if (!userValid) {
                        updateToast("This user is not valid.", Toast.LENGTH_SHORT);
                        return;
                    }
                } else {
                    updateToast("Please wait before doing that", Toast.LENGTH_SHORT);
                    validateUserInfo();
                    return;
                }
                final Message message = new Message();
                ParseACL acl = new ParseACL();
                final String messageStr = messageET.getText().toString();

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
                            Conversations.getInstance().addMessage(message);
                            sendPush(message);
                        }

                        messageET.setEnabled(true);
                        sendButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void setListViewScrollListener(){
        messageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final int threshold = 1;

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (messageListView.getFirstVisiblePosition() <= threshold) {
                        Conversations.getInstance().loadMore(group, new FindCallback<Message>() {
                            @Override
                            public void done(List<Message> messages, ParseException e) {
                                if(e == null){
                                    ListIterator<Message> it = messages.listIterator(0);
                                    int previousFirstIndex = messageListView.getFirstVisiblePosition();

                                    while(it.hasNext()){
                                        chatAdapter.addEnd(new ChatItem(it.next()));
                                    }
                                    chatAdapter.notifyDataSetChanged();

                                    // Restore the list view's scroll position
                                    View v = messageListView.getChildAt(0);
                                    int top = (v == null) ? 0 : (v.getTop() - messageListView.getPaddingTop());
                                    messageListView.setSelectionFromTop(previousFirstIndex + messages.size(), top);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    /**
     * Sends a push notification to the recipient of a message. The push notification has the
     * following fields (in a JSONObject):
     *
     *   isChat - always true
     *   messageId - objectId of the message
     *   messageStr - The message's string representation
     *   fromUserId - objectId of the current user
     *   fromUserName - username of the current user
     *   title - title of the push notification ("New message from [username]")
     *   alert - the notification's message (the message string)
     *
     * @param message The message to push
     */
    private void sendPush(Message message){
        ParsePush push = new ParsePush();
        ParseQuery<ParseInstallation> installationQuery = ParseInstallation.getQuery();
        JSONObject pushData = new JSONObject();

        installationQuery.whereEqualTo("user", otherUser);

        try{
            pushData.put("isChat", true);
            pushData.put("messageId", message.getObjectId());
            pushData.put("messageStr", message.getMessage());
            pushData.put("fromUserId", ParseUser.getCurrentUser().getObjectId());
            pushData.put("fromUserName", ParseUser.getCurrentUser().getUsername());
            pushData.put("title", String.format(
                    "New message from %s", ParseUser.getCurrentUser().getUsername()
            ));
            pushData.put("alert", message.getMessage());
        } catch(JSONException e){
            Log.d(TAG, e.getMessage());
            updateToast("JSON exception occurred", Toast.LENGTH_LONG);
        }

        push.setQuery(installationQuery);
        push.setData(pushData);
        push.sendInBackground();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            chatAdapter.addAll((ChatItem[]) savedInstanceState.getParcelableArray(STATE_MESSAGES));
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

    public void refreshAdapter(){
        Conversations.getInstance().getConversation(group, new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if(e == null){
                    ListIterator<Message> it = messages.listIterator(messages.size());
                    chatAdapter.clear();
                    while(it.hasPrevious()){
                        chatAdapter.add(new ChatItem(it.previous()));
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onConversationsGroupUpdate(Conversations.Group updatedGroup, Message... newMessages) {
        if(updatedGroup.equals(this.group)){
            for(int i = newMessages.length - 1; i >= 0; --i){
                chatAdapter.add(new ChatItem(newMessages[i]));
            }
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConversationsRefresh() {
        refreshAdapter();
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

        if(context != null){
            toast = Toast.makeText(
                    context,
                    text,
                    length
            );
        }

        toast.show();
    }
}
