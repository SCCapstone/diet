package edu.sc.snacktrack;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class PreviousEntriesFragment extends Fragment implements RemoteDataTaskFragment.RDTTaskCallbacks{

    private static final String TAG = "PreviousEntriesFragment";

    private static int counter = 0;
    private final String REMOTE_DATA_TASK_FRAGMENT_TAG =
            String.format("PreviousEntriesFragment.remoteDataTaskFragment%d", counter++);


    private ListView listview;
    private CustomAdapter adapter;
    private List<SnackEntry> mySnackList = null;

    private View progressOverlay;

    private RemoteDataTaskFragment remoteDataTaskFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_previous_entries, container, false);

        // Set up the progress overlay
        progressOverlay = view.findViewById(R.id.progressOverlay);
        ((TextView) progressOverlay.findViewById(R.id.progressMessage)).setText(
                "Accessing SnackTrack Database..."
        );

        setUpRemoteDataTask();

        return view;
    }

    /**
     * Manages the initialization and restoration of the remote data task fragment.
     */
    private void setUpRemoteDataTask(){
        FragmentManager fm = getFragmentManager();
        remoteDataTaskFragment = (RemoteDataTaskFragment) fm.findFragmentByTag(REMOTE_DATA_TASK_FRAGMENT_TAG);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (remoteDataTaskFragment == null) {

            remoteDataTaskFragment = new RemoteDataTaskFragment();
            remoteDataTaskFragment.setCallbacks(this);

            fm.beginTransaction().add(remoteDataTaskFragment, REMOTE_DATA_TASK_FRAGMENT_TAG).commit();
        } else{
            Log.d(TAG, "Restart remote data task fragment");
            remoteDataTaskFragment.setCallbacks(this);
            remoteDataTaskFragment.restart();
        }

        // If remote data access is in progress, show the progress overlay
        if(remoteDataTaskFragment.isRunning()){
            progressOverlay.setVisibility(View.VISIBLE);
//            setWidgetsEnabled(false);
        }
    }

    public RemoteDataTaskFragment getRemoteDataTaskFragment(){
        return this.remoteDataTaskFragment;
    }

    @Override
    public void onRDTPreExecute() {
        progressOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRDTProgressUpdate(int percent) {

    }

    @Override
    public void onRDTCancelled() {

    }

    @Override
    public void onRDTPostExecute(List<SnackEntry> snackList) {


        mySnackList = snackList;

        // Locate the listview in listview_main.xml
        listview = (ListView) getView().findViewById(R.id.SnackList);
        // Pass the results into ListViewAdapter.java
        adapter = new CustomAdapter(getContext(), mySnackList);
        // Binds the Adapter to the ListView
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            SnackEntry entry = snackList.get(position);
//
//                            Intent intent = new Intent(MainActivity.this, SnackDetailsActivity.class);
//                            intent.putExtra(SnackDetailsActivity.DESCRIPTION_KEY, entry.getDescription());
//                            intent.putExtra(SnackDetailsActivity.MEAL_TYPE_KEY, entry.getMealType());
//                            intent.putExtra(SnackDetailsActivity.PHOTO_URL_KEY, entry.getPhoto().getUrl());
//                            startActivity(intent);
            }
        });

        progressOverlay.setVisibility(View.GONE);
    }
}
