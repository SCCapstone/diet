package edu.sc.snacktrack;


        import java.util.ArrayList;
        import java.util.List;
        import android.content.Context;
        import android.content.Intent;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;


public class CustomAdapter extends BaseAdapter {
    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ImageLoader imageLoader;
    private List<SnackEntry> mySnackList = null;
    private ArrayList<SnackEntry> arraylist;

    public CustomAdapter(Context context, List<SnackEntry> mySnackList) {
        this.context = context;
        this.mySnackList = mySnackList;
        inflater = LayoutInflater.from(context);
        this.arraylist = new ArrayList<SnackEntry>();
        this.arraylist.addAll(mySnackList);
        imageLoader = new ImageLoader(context);
    }

    public class ViewHolder {
        TextView name;
        TextView date;
        ImageView photo;
    }

    @Override
    public int getCount() {
        return mySnackList.size();
    }

    @Override
    public Object getItem(int position) {
        return mySnackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.snack_entry, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.date = (TextView) view.findViewById(R.id.date);
            // Locate the ImageView in listview_item.xml
            holder.photo = (ImageView) view.findViewById(R.id.pic);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
       // holder.name.setText(mySnackList.get(position).getName());
        holder.date.setText(mySnackList.get(position).getCreatedAt().toString().substring(0,10));
       // Set the results into ImageView
        imageLoader.DisplayImage(mySnackList.get(position).getPhoto().getUrl(),
                holder.photo);
        // Listen for ListView Item Click
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Send single item click data to SingleItemView Class
//                Intent intent = new Intent(context, SingleItemView.class);
//                // Pass all data rank
//                intent.putExtra("rank",
//                        (worldpopulationlist.get(position).getRank()));
//                // Pass all data country
//                intent.putExtra("country",
//                        (worldpopulationlist.get(position).getCountry()));
//                // Pass all data population
//                intent.putExtra("population",
//                        (worldpopulationlist.get(position).getPopulation()));
//                // Pass all data flag
//                intent.putExtra("flag",
//                        (worldpopulationlist.get(position).getFlag()));
//                // Start SingleItemView Class
//                context.startActivity(intent);
            }
        });
        return view;
    }
}
