package edu.sc.snacktrack.client_list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

import edu.sc.snacktrack.main.existing_entries.PreviousEntriesFragment;
import edu.sc.snacktrack.R;
import edu.sc.snacktrack.snacks.SnackEntry;
import edu.sc.snacktrack.snacks.SnackList;
import edu.sc.snacktrack.utils.Utils;

/**
 * Created by spitzfor on 2/16/2016.
 */
public class DisplayClientsFragment extends Fragment implements ClientList.UpdateListener {

    private static final String TAG = "DisplayClientFragment";

    private static final String STATE_LOADING_CLIENT = "stateLoadingClient";

    private ListView listview;
    private ClientListAdapter adapter;

    private Context context;

    private View progressOverlay;

    private boolean loadingClient;

    @Override
    public void onClientListUpdateComplete() {
        adapter.notifyDataSetChanged();
        if(progressOverlay != null){
            progressOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClientListUpdateStart() {
        Log.d(TAG, "onClientListUpdateStart");
        if(progressOverlay != null){
            progressOverlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ClientListAdapter(getContext());
        setRetainInstance(true);
        ClientList.getInstance().registerUpdateListener(adapter);
        ClientList.getInstance().registerUpdateListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadingClient = false;
        if(savedInstanceState != null){
            loadingClient = savedInstanceState.getBoolean(STATE_LOADING_CLIENT, false);
        }

        progressOverlay.setVisibility(loadingClient ? View.VISIBLE : View.GONE);
        setWidgetsEnabled(!loadingClient);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LOADING_CLIENT, loadingClient);
    }

    /**
     * Disables or enables all user input widgets.
     *
     * @param enabled true to enable; false to disable
     */
    private void setWidgetsEnabled(boolean enabled){
        if(listview != null){
            listview.setEnabled(enabled);
        }
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

                progressOverlay.setVisibility(View.VISIBLE);
                setWidgetsEnabled(false);
                loadingClient = true;

                SnackList.getInstance().setUser(targetUser);
                SnackList.getInstance().refresh(new FindCallback<SnackEntry>() {
                    @Override
                    public void done(List<SnackEntry> objects, ParseException e) {
                        if(e == null){
                            try{
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.content_frame, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            } catch(Exception e2){
                                // If an exception occurs here, the user probably navigated
                                // away from this fragment.
                            } finally{
                                try{
                                    progressOverlay.setVisibility(View.GONE);
                                } catch(NullPointerException e2){
                                    // No progress overlay to hide
                                }
                            }
                        } else{
                            try{
                                progressOverlay.setVisibility(View.GONE);
                                Toast.makeText(
                                        context,
                                        Utils.getErrorMessage(e),
                                        Toast.LENGTH_LONG
                                ).show();
                            } catch(NullPointerException e2){
                                // If an exception occurs here, the user probably navigated
                                // away from this fragment.
                            }
                        }
                        loadingClient = false;
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
