package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.sc.snacktrack.R;

/**
 * ChatAdapter for displaying a list of chat messages.
 */
public class ChatAdapter extends BaseAdapter {

    private static final String TAG = "ChatAdapter";

    private int layoutResourceId;

    private ArrayList<ChatAdapterItem> items;

    private Context context;

    public ChatAdapter(Context context) {
        this.layoutResourceId = R.layout.message;
        this.context = context;
        this.items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ChatAdapterItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void remove(ChatAdapterItem item){
        items.remove(item);
    }

    public boolean containsByMessageId(String messageId){
        for(ChatAdapterItem item : items){
            if(item.getMessageId().equals(messageId)){
                return true;
            }
        }

        return false;
    }

    public void addMessage(final Message message){
        final ChatAdapterItem item = new ChatAdapterItem();
        message.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    item.readMessage(message);
                    items.add(item);
                    notifyDataSetChanged();
                } else {

                }
            }
        });
    }

    public void add(ChatAdapterItem item){
        items.add(item);
    }

    public void clear(){
        items.clear();
    }

    public void sortMessages(){
        Collections.sort(items, new Comparator<ChatAdapterItem>() {
            @Override
            public int compare(ChatAdapterItem lhs, ChatAdapterItem rhs) {
                Long time1 = lhs.getCreatedTime();
                Long time2 = rhs.getCreatedTime();
                return time1.compareTo(time2);
            }
        });
        notifyDataSetChanged();
    }

    public ChatAdapterItem[] getAllItems(){
        ChatAdapterItem[] messages = new ChatAdapterItem[getCount()];

        for(int i = 0; i < messages.length; ++i){
            messages[i] = this.items.get(i);
        }

        return messages;
    }

    public void addAll(ChatAdapterItem[] items){
        for(ChatAdapterItem item : items){
            this.items.add(item);
        }

    }

    /**
     * Gets the last item.
     *
     * @return The last item if the list is not empty. null otherwise
     */
    public ChatAdapterItem getLast(){
        if(items.size() > 0){
            return items.get(items.size()-1);
        } else{
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MessageHolder messageHolder;
        Resources resources = context.getResources();

        ChatAdapterItem message = items.get(position);

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


        // Adjust the text views gravity to distinguish between the current the user and the
        // user he's chatting with. The current user's items will be right aligned in the row and
        // the other user's will be left aligned in the row.
        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 50, resources.getDisplayMetrics()
        );
        if(message.getFromUsername().equals(ParseUser.getCurrentUser().getUsername())){
            messageHolder.messageTextViewWrapper.setGravity(Gravity.RIGHT);
            messageHolder.messageTextViewWrapper.setPadding(paddingPx,0,0,0);
            messageHolder.messageTextView.setBackgroundColor(resources.getColor(R.color.chat_current_user));
        } else{
            messageHolder.messageTextViewWrapper.setGravity(Gravity.LEFT);
            messageHolder.messageTextViewWrapper.setPadding(0, 0, paddingPx, 0);
            messageHolder.messageTextView.setBackgroundColor(resources.getColor(R.color.chat_other_user));

        }


        return row;
    }

    static class MessageHolder{
        TextView toTextView;
        TextView fromTextView;
        TextView messageTextView;
        LinearLayout messageTextViewWrapper;
    }
}
