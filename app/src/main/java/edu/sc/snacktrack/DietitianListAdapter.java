package edu.sc.snacktrack;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by spitzfor on 3/22/2016.
 */
public class DietitianListAdapter extends BaseAdapter implements DietitianList.UpdateListener {

    private static final String TAG = "DietitianListAdapter";

    private Context cont;
    private LayoutInflater inflater;

    public DietitianListAdapter(Context context) {
        this.cont = context;
        inflater = LayoutInflater.from(context);
        Log.d(TAG, "New custom adapter");
    }

    @Override
    public void onDietitianListUpdateComplete() { notifyDataSetChanged(); }

    @Override
    public void onDietitianListUpdateStart() {
        //Do nothing
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() { return DietitianList.getInstance().size(); }

    @Override
    public Object getItem(int position) {
        return DietitianList.getInstance().get(position);
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
            view = inflater.inflate(R.layout.dietitian_info, null);
            holder.name = (TextView) view.findViewById(R.id.dietitian_name);
            view.setTag(holder);
        }

        else
            holder = (ViewHolder) view.getTag();

        holder.name.setText(DietitianList.getInstance().get(position).getUsername());

        return view;
    }
}
