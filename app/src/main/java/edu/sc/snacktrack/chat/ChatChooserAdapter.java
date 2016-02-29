package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import edu.sc.snacktrack.R;

public class ChatChooserAdapter extends BaseAdapter{

    private static final String TAG = "ChatChooserAdapter";

    private Context context;
    private ArrayList<ChatChooserItem> items;
    private int layoutResourceId;

    public ChatChooserAdapter(Context context){
        this.context = context;
        this.layoutResourceId = R.layout.chat_chooser_item;
        items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ChatChooserItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addConversation(final Conversation conversation){
        final ChatChooserItem item = new ChatChooserItem();
        ParseQuery<Conversation> query = null;
        final ParseUser fromUser = conversation.getFromUser();
        final ParseUser toUser = conversation.getToUser();
        final Message recentMessage = conversation.getRecentMessage();

        // Create a query if necessary
        if(conversation.isDataAvailable()){
            // Don't add this conversation if it has dangling pointers.
            if(fromUser == null || toUser == null || recentMessage == null){
                Log.d(TAG, "dangling pointer(s)");
                return;
            }

            if(!fromUser.isDataAvailable() || !toUser.isDataAvailable() || !recentMessage.isDataAvailable()){
                query = ParseQuery.getQuery(Conversation.class);
                query.whereEqualTo("objectId", conversation.getObjectId());
                query.include(Conversation.FROM_USER_KEY);
                query.include(Conversation.TO_USER_KEY);
                query.include(Conversation.RECENT_MESSAGE_KEY);
            }
        } else{
            query = ParseQuery.getQuery(Conversation.class);
            query.whereEqualTo("objectId", conversation.getObjectId());
            query.include(Conversation.FROM_USER_KEY);
            query.include(Conversation.TO_USER_KEY);
            query.include(Conversation.RECENT_MESSAGE_KEY);
        }

        // Execute the query if it exists
        if(query != null){
            query.findInBackground(new FindCallback<Conversation>() {
                @Override
                public void done(List<Conversation> objects, ParseException e) {
                    if(e == null){
                        if(objects.size() == 1){
                            Conversation fetchedConversation = objects.get(0);
                            ParseUser fetchedFromUser = fetchedConversation.getFromUser();
                            ParseUser fetchedToUser = fetchedConversation.getToUser();
                            Message fetchedMessage = fetchedConversation.getRecentMessage();

                            // Don't add this conversation if it has dangling pointers.
                            if(fetchedFromUser == null || fetchedToUser == null || fetchedMessage == null){
                                return;
                            }

                            if(ParseUser.getCurrentUser().getObjectId().equals(fetchedFromUser.getObjectId())){
                                item.setUsername(fetchedToUser.getUsername());
                            } else{
                                item.setUsername(fetchedFromUser.getUsername());
                            }
                            item.setRecentMessage(fetchedMessage.getMessage());
                            items.add(item);
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        } else{
            if(ParseUser.getCurrentUser().getObjectId().equals(fromUser.getObjectId())){
                item.setUsername(toUser.getUsername());
            } else{
                item.setUsername(fromUser.getUsername());
            }
            item.setRecentMessage(recentMessage.getMessage());
            items.add(item);
            notifyDataSetChanged();
        }

//        conversation.fetchIfNeededInBackground(new GetCallback<Conversation>() {
//            @Override
//            public void done(Conversation object1, ParseException e) {
//                if(e == null) {
//                    ParseUser user = object1.getFromUser();
//                    Message recentMessage = object1.getRecentMessage();
//
//                    if(user.isDataAvailable()){
//
//                    }
//                    item.setUsername(object1.getFromUser().getUsername());
//                    if(recentMessage != null){
//                        item.setRecentMessage(recentMessage.getMessage());
//                    }
//                    if(user != null){
//                        item.setUsername(user.getUsername());
//                    }
//                    items.add(item);
//                    notifyDataSetChanged();
//                }
//            }
//        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;

        ChatChooserItem item = items.get(position);

        if(row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new Holder();
            holder.usernameTextView = (TextView) row.findViewById(R.id.usernameTextView);
            holder.messageTextView = (TextView) row.findViewById(R.id.recentMessageTextView);

            row.setTag(holder);
        } else{
            holder = (Holder) row.getTag();
        }

        holder.usernameTextView.setText(item.getUsername());
        holder.messageTextView.setText(item.getRecentMessage());

        return row;
    }

    static class Holder{
        TextView usernameTextView;
        TextView messageTextView;
    }
}
