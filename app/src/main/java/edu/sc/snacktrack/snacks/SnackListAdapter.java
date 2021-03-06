package edu.sc.snacktrack.snacks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.sc.snacktrack.utils.ImageLoader;
import edu.sc.snacktrack.R;


public class SnackListAdapter extends BaseAdapter implements SnackList.UpdateListener{

    private static final String TAG = "SnackListAdapter";

    // Declare Variables
    private Context context;
    private LayoutInflater inflater;

    public SnackListAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        Log.d(TAG, "New custom adapter");
    }

    @Override
    public void onSnackListUpdateComplete(ParseException e) {
        notifyDataSetChanged();
    }

    @Override
    public void onSnackListUpdateStart() {
        // Do nothing
    }

    public class ViewHolder {
        TextView textOverlay;
        TextView date;
        ImageView photo;
    }

    @Override
    public int getCount() {
        return SnackList.getInstance().size();
    }

    @Override
    public Object getItem(int position) {
        return SnackList.getInstance().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        Log.d(TAG, "Get view " + position);
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.snack_entry, null);
            // Locate the TextViews in listview_item.xml
          //  holder.name = (TextView) view.findViewById(R.id.name);
            holder.date = (TextView) view.findViewById(R.id.date);
            // Locate the ImageView in listview_item.xml
            holder.photo = (ImageView) view.findViewById(R.id.pic);
            holder.textOverlay = (TextView) view.findViewById(R.id.picTextOverlay);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
       // holder.name.setText(mySnackList.get(position).getName());
        //holder.date.setText(SnackList.getInstance().get(position).getCreatedAt().toString().substring(0, 10));

        // Entry's date formatted to show wall clock time when submitted
        Date initialDate = SnackList.getInstance().get(position).getCreatedAt();
            Format formatter = new SimpleDateFormat("EEE MMM dd, h:mm a");
            String formatDate = formatter.format(initialDate);

        holder.date.setText(formatDate);

        // Display the image if there is one. Otherwise, say there's no image.
        ParseFile photo = SnackList.getInstance().get(position).getPhoto();
        if(photo != null){
            holder.photo.setVisibility(View.VISIBLE);
            holder.textOverlay.setVisibility(View.GONE);
            ImageLoader.getInstance(context).displayImage(photo.getUrl(), holder.photo);
            holder.textOverlay.setText("");
        } else{
            holder.photo.setVisibility(View.GONE);
            holder.textOverlay.setVisibility(View.VISIBLE);
            holder.photo.setImageDrawable(null);
            holder.textOverlay.setText("text only entry");
        }

        return view;
    }
}
