package edu.sc.snacktrack;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Created by spitzfor on 2/16/2016.
 */
public class DisplayClientsFragment extends Fragment implements ClientList.UpdateListener {

    private static final String TAG = "DisplayClientFragment";

    private ListView listview;
    private ClientListAdapter adapter;

    private View progressOverlay;

    @Override
    public void onClientListUpdateComplete() {
        adapter.notifyDataSetChanged();
        progressOverlay.setVisibility(View.GONE);
    }

    @Override
    public void onClientListUpdateStart() {
        Log.d(TAG, "onClientListUpdateStart");
        progressOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new ClientListAdapter(getContext());
        ClientList.getInstance().registerUpdateListener(adapter);
        ClientList.getInstance().registerUpdateListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.action_new).setEnabled(false);
        menu.findItem(R.id.action_new).setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_display_clients, container, false);

        progressOverlay = view.findViewById(R.id.progressOverlay);
        ((TextView) progressOverlay.findViewById(R.id.progressMessage)).setText(
                "Accessing SnackTrack Database..."
        );

        listview = (ListView) view.findViewById(R.id.ClientList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String objectId = ClientList.getInstance().get(position).getObjectId();
                final Fragment fragment = new PreviousEntriesFragment();
                ParseUser targetUser = ParseUser.createWithoutData(ParseUser.class, objectId);
                targetUser.fetchInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser object, ParseException e) {
                        if (e == null) {
                            SnackList.getInstance().setUser(object);
                            SnackList.getInstance().refresh(null);
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                });

            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Unregister the adapter and this from the ClientList's update listeners.
        ClientList.getInstance().unregisterUpdateListener(adapter);
        ClientList.getInstance().unregisterUpdateListener(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity().setTitle("My Clients");
    }
}
