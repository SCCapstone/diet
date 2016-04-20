package edu.sc.snacktrack.main.chat;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;

import edu.sc.snacktrack.R;

/**
 * Adapter for displaying a list of chat messages.
 */
public class ChatAdapter extends BaseAdapter {

    private static final String TAG = "ChatAdapter";

    private int layoutResourceId;

    private ArrayList<ChatItem> items;

    private Context context;

    public ChatAdapter(Context context) {
        this.layoutResourceId = R.layout.chat_item;
        this.context = context;
        this.items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ChatItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

//    /**
//     * Add a Message to this adapter. If the Message is not fetched, it will be fetched in the
//     * background and the ChatAdapter will update if the fetch is successful.
//     *
//     * @param message The Message to add.
//     */
//    public void addMessage(final Message message){
//        final ChatItem item = new ChatItem();
//        message.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
//            @Override
//            public void done(ParseObject object, ParseException e) {
//                if (e == null) {
//                    item.readMessage(message);
//                    items.add(item);
//                    notifyDataSetChanged();
//                } else {
//
//                }
//            }
//        });
//    }

    /**
     * Adds a ChatItem to the beginning of this adapter.
     *
     * @param item the ChatItem to add
     */
    public void add(ChatItem item){
        items.add(item);
    }

    /**
     * Adds a ChatItem to the end of this adapter.
     *
     * @param item the item it add
     */
    public void addEnd(ChatItem item){
        items.add(0, item);
    }

    /**
     * Clears this adapter. That is, removes all items from the adapter.
     */
    public void clear(){
        items.clear();
    }

    /**
     * Gets all ChatItems currently in this adapter.
     *
     * @return array of ChatItems
     */
    public ChatItem[] getAllItems(){
        ChatItem[] messages = new ChatItem[getCount()];

        for(int i = 0; i < messages.length; ++i){
            messages[i] = this.items.get(i);
        }

        return messages;
    }

    /**
     * Adds an array of ChatItems to this adapter
     *
     * @param items array of ChatItems
     */
    public void addAll(ChatItem[] items){
        for(ChatItem item : items){
            this.items.add(item);
        }

    }

    /**
     * Gets the last item from the other user.
     *
     * @return The last item from the other user if there is one. null otherwise.
     */
    public ChatItem getLastFromOther(){
        for(int i = items.size() - 1; i >= 0; --i){
            ChatItem currentItem = items.get(i);
            if(currentItem.getToUsername().equals(ParseUser.getCurrentUser().getUsername())){
                return currentItem;
            }
        }

        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MessageHolder messageHolder;
        Resources resources = context.getResources();

        ChatItem message = items.get(position);

        if(row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            messageHolder = new MessageHolder();
            messageHolder.fromTextView = (TextView) row.findViewById(R.id.fromTextView);
            messageHolder.toTextView = (TextView) row.findViewById(R.id.toTextView);
            messageHolder.messageTextView = (TextView) row.findViewById(R.id.messageTextView);
            messageHolder.messageTextViewWrapper = (LinearLayout) row.findViewById(R.id.messageTextViewWrapper);

            row.setTag(messageHolder);
        } else{
            messageHolder = (MessageHolder) row.getTag();
        }

        messageHolder.messageTextView.setText(message.getMessage());
        messageHolder.fromTextView.setText(message.getFromUsername());
        messageHolder.toTextView.setText(message.getToUsername());


        // Adjust the text view's gravity, padding, and color to distinguish between the current
        // the user and the user he's chatting with. The current user's items will be right aligned
        // in the row and the other user's will be left aligned in the row.
        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 50, resources.getDisplayMetrics()
        );
        if(message.getFromUsername().equals(ParseUser.getCurrentUser().getUsername())){
            messageHolder.messageTextViewWrapper.setGravity(Gravity.RIGHT);
            messageHolder.messageTextViewWrapper.setPadding(paddingPx, 0, 0, 0);
            messageHolder.messageTextView.setBackgroundColor(resources.getColor(R.color.chat_current_user));
            messageHolder.messageTextView.setTextColor(resources.getColor(R.color.chat_current_user_text));
        } else{
            messageHolder.messageTextViewWrapper.setGravity(Gravity.LEFT);
            messageHolder.messageTextViewWrapper.setPadding(0, 0, paddingPx, 0);
            messageHolder.messageTextView.setBackgroundColor(resources.getColor(R.color.chat_other_user));
            messageHolder.messageTextView.setTextColor(resources.getColor(R.color.chat_other_user_text));
        }

        return row;
    }

    /**
     * Class for the ViewHolder pattern. Holds views for a single ChatItem.
     */
    static class MessageHolder{
        TextView toTextView;
        TextView fromTextView;
        TextView messageTextView;
        LinearLayout messageTextViewWrapper;
    }
}
