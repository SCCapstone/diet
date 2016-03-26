package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
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
public class ChatChooserFragment extends Fragment implements Conversations.UpdateListener{

    private static final String TAG = "ChatChooserFragment";

    private static final int USERNAME_REQUEST_CODE = 100;

    private Activity myActivity;

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

    }

    @Override
    public void onConversationsGroupUpdate(Conversations.Group updatedGroup, Message... newMessages) {
        refreshChatChooserList();
    }

    @Override
    public void onConversationsRefresh() {
        refreshChatChooserList();
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
        Conversations.getInstance().registerUpdateListener(this);
        if(!Conversations.getInstance().isUpdating() && Conversations.getInstance().needsRefresh()){
            Conversations.getInstance().refresh(new FindCallback<Message>() {
                @Override
                public void done(List<Message> messages, ParseException e) {
                    if(e == null){
                        refreshChatChooserList();
                    } else{
                        updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
                    }
                }
            });
        } else{
            refreshChatChooserList();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Conversations.getInstance().unregisterUpdateListener(this);
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
                startChat(item.getUsername(), item.getUserId());
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
                    startChat(username, null);
                }
        }
    }

    /**
     * Refreshes the chat chooser list with the most recent messages to the current user.
     */
    private void refreshChatChooserList(){
        Set<Conversations.Group> groups = Conversations.getInstance().keySet();

        chatChooserAdapter.clear();

        for(Conversations.Group group : groups){
            final ChatChooserItem item = new ChatChooserItem();
            Iterator<ParseUser> it = group.iterator();
            ParseUser user1 = it.next();
            ParseUser user2 = it.next();
            if(user1 != null && user2 != null){
                if(user1 == ParseUser.getCurrentUser()){
                    item.setUsername(user2.getUsername());
                    item.setUserId(user2.getObjectId());
                } else if(user2 == ParseUser.getCurrentUser()){
                    item.setUsername(user1.getUsername());
                    item.setUserId(user1.getObjectId());
                } else{
                    // This group doesn't have the current user
                    continue;
                }
            } else{
                continue;
            }

            Conversations.getInstance().getConversation(group, new FindCallback<Message>() {
                @Override
                public void done(List<Message> objects, ParseException e) {
                    if(e == null){
                        if(objects.size() > 0){
                            int recentMessageIndex = objects.size() - 1;
                            item.setRecentMessage(objects.get(recentMessageIndex).getMessage());
                            item.setCreatedTime(objects.get(recentMessageIndex).getCreatedAt().getTime());
                        }
                    }
                }
            });
            chatChooserAdapter.addItem(item);
        }
        chatChooserAdapter.sort();
        chatChooserAdapter.notifyDataSetChanged();
    }

    /**
     * Attempts to start a chat with a specified username.
     *
     * @param username The username to chat with
     * @param userId (nullable) The userId of the user. If null, queries parse for the id using
     *               the specified username.
     */
    private void startChat(final String username, @Nullable String userId){

        if(startingChat){
            updateToast("Chat already starting", Toast.LENGTH_SHORT);
            return;
        }

        if(username.equals(ParseUser.getCurrentUser().getUsername())){
            updateToast("You can't chat with yourself", Toast.LENGTH_SHORT);
            return;
        }

        startingChat = true;

        final Intent chatIntent = new Intent(getActivity(), ChatActivity.class);

        chatIntent.putExtra(ChatActivity.OTHER_USER_NAME_KEY, username);

        if(userId == null){
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            userQuery.whereEqualTo("username", username);

            progressOverlay.setVisibility(View.VISIBLE);

            userQuery.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() == 1) {
                            if (getActivity() != null) {
                                ParseUser otherUser = objects.get(0);
                                chatIntent.putExtra(ChatActivity.OTHER_USER_ID_KEY, otherUser.getObjectId());
                                startActivity(chatIntent);
                            }
                        } else{
                            updateToast("User " + username + " not found", Toast.LENGTH_SHORT);
                        }
                    } else{
                        updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
                    }

                    progressOverlay.setVisibility(View.GONE);
                    startingChat = false;
                }
            });
        } else{
            chatIntent.putExtra(ChatActivity.OTHER_USER_ID_KEY, userId);
            startActivity(chatIntent);
            startingChat = false;
        }
    }

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
}
