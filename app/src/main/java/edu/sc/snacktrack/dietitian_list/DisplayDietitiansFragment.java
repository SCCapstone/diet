package edu.sc.snacktrack.dietitian_list;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.settings.SettingsFragment;
import edu.sc.snacktrack.snacks.SnackEntry;

/**
 * Created by spitzfor on 3/22/2016.
 */
public class DisplayDietitiansFragment extends Fragment implements DietitianList.UpdateListener {

    private static final String TAG = "DisplayDietitianFrag";

    private ListView listview;
    private DietitianListAdapter adapter;

    private View progressOverlay;
    public Context cont;
    public ParseUser userSel;
    public String objectId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cont = context;
    }

    @Override
    public void onDietitianListUpdateComplete() {
        adapter.notifyDataSetChanged();
        progressOverlay.setVisibility(View.GONE);
    }

    @Override
    public void onDietitianListUpdateStart() {
        Log.d(TAG, "onDietitianListUpdateStart");
        progressOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new DietitianListAdapter(getContext());
        DietitianList.getInstance().registerUpdateListener(adapter);
        DietitianList.getInstance().registerUpdateListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        getActivity().setTitle("Pick Your Dietitian");
        View view = inflater.inflate(R.layout.fragment_display_dietitians, container, false);

        progressOverlay = view.findViewById(R.id.progressOverlay);
        ((TextView) progressOverlay.findViewById(R.id.progressMessage)).setText(
                "Accessing SnackTrack Database..."
        );

        listview = (ListView) view.findViewById(R.id.DietitianList);
        listview.setAdapter(adapter);

        Log.i("Testing",Integer.toString(DietitianList.getInstance().size()));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                userSel = DietitianList.getInstance().get(position);
                objectId = DietitianList.getInstance().get(position).getObjectId();

                AlertDialog.Builder aBuilder = new AlertDialog.Builder(view.getContext());
                aBuilder.setTitle("Confirmation");
                aBuilder.setIcon(R.mipmap.ic_launcher);
                aBuilder.setMessage("Are you sure you want to add \"" + userSel.getUsername() + "\" and let them see your entries?");
                aBuilder.setCancelable(false);
                aBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dietitianSearch(objectId);
                        SettingsFragment settingsFragment = new SettingsFragment();
                        getFragmentManager().beginTransaction().replace(R.id.content_frame, settingsFragment).addToBackStack(null).commit();
                        dialog.dismiss();
                    }
                });

                aBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog aDiag = aBuilder.create();
                aDiag.show();
            }
        });

        return view;
    }

    private void dietitianSearch(final String userId) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("objectId", userId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if (!objects.isEmpty()) {
                        ParseUser result = objects.get(0);
                        ParseUser.getCurrentUser().put("myDietitian", result);
                        giveAccess(result);
                    } else
                        Toast.makeText(cont, "User not found", Toast.LENGTH_LONG).show();
                } else
                    Log.e("SEARCH ERROR", "ERROR IN SEARCHING");
            }
        });
    }

    private void giveAccess(final ParseUser targetUser) {

        final ParseACL entryACL = new ParseACL();

        ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);
        query.whereEqualTo("owner", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<SnackEntry>() {

            @Override
            public void done(List<SnackEntry> refreshedSnacks, ParseException e) {
                if (e == null) {
                    entryACL.setReadAccess(targetUser, true);
                    entryACL.setWriteAccess(targetUser, false);
                    entryACL.setReadAccess(ParseUser.getCurrentUser(), true);
                    entryACL.setWriteAccess(ParseUser.getCurrentUser(), true);

                    for (ParseObject entry : refreshedSnacks) {
                        entry.setACL(entryACL);
                        entry.saveInBackground();
                    }

                    Toast.makeText(cont, "Successfully granted " + targetUser.getUsername().toString() + " access!", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(cont, "Something went wrong when fetching SnackList...", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Unregister the adapter and this from the ClientList's update listeners.
        DietitianList.getInstance().unregisterUpdateListener(adapter);
        DietitianList.getInstance().unregisterUpdateListener(this);
    }
}
