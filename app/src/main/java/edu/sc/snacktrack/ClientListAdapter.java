package edu.sc.snacktrack;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by spitzfor on 2/16/2016.
 */
public class ClientListAdapter extends BaseAdapter implements ClientList.UpdateListener {

    private static final String TAG = "ClientListAdapter";

    private Context cont;
    private LayoutInflater inflater;

    public ClientListAdapter(Context context) {
        this.cont = context;
        inflater = LayoutInflater.from(context);

        Log.d(TAG, "New custom adapter");
    }

    @Override
    public void onClientListUpdateComplete() {
        notifyDataSetChanged();
    }

    @Override
    public void onClientListUpdateStart() {
        //Do nothing
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return ClientList.getInstance().size();
    }

    @Override
    public Object getItem(int position) {
        return ClientList.getInstance().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        Log.d(TAG, "Get view " + position);
        final ViewHolder holder;

        if(view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.client_info, null);
            holder.name = (TextView) view.findViewById(R.id.client_name);
            view.setTag(holder);
        }

        else
        {
            holder = (ViewHolder) view.getTag();
        }

        holder.name.setText(ClientList.getInstance().get(position).getUsername());

        return view;
    }
}
