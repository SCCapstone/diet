package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.sc.snacktrack.R;

/**
 * Adapter for displaying a list of existing chats.
 */
public class ChatChooserAdapter extends BaseAdapter{

    private static final String TAG = "ChatChooserAdapter";

    private Context context;
    private ArrayList<ChatChooserItem> items;
    private int layoutResourceId;

    /**
     * Creates a new ChatChooserAdapter with a specified context.
     *
     * @param context The context
     */
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

    public void addItem(ChatChooserItem item){
        items.add(item);
    }

    public void addAllItems(List<ChatChooserItem> items){
        for(ChatChooserItem item : items){
            addItem(item);
        }
    }

    public void sort(){
        Collections.sort(items, Collections.reverseOrder());
    }

    public void clear(){
        items.clear();
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

    /**
     * Class for the ViewHolder pattern. Holds views for a single ChatChooserItem.
     */
    static class Holder{
        TextView usernameTextView;
        TextView messageTextView;
    }
}
