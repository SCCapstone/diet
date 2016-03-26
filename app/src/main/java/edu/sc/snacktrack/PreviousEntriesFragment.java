package edu.sc.snacktrack;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;

public class PreviousEntriesFragment extends Fragment implements SnackList.UpdateListener{

    private static final String TAG = "PreviousEntriesFragment";

    private ListView listview;
    private SnackListAdapter adapter;

    private View progressOverlay;

    @Override
    public void onSnackListUpdateComplete() {
        adapter.notifyDataSetChanged();
        progressOverlay.setVisibility(View.GONE);
    }

    @Override
    public void onSnackListUpdateStart() {
        Log.d(TAG, "onSnackListUpdateStart");
        progressOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        adapter = new SnackListAdapter(getContext());
        SnackList.getInstance().registerUpdateListener(adapter);
        SnackList.getInstance().registerUpdateListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_previous_entries, menu);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set title
        if(SnackList.getInstance().getUser() != ParseUser.getCurrentUser()){
            getActivity().setTitle(SnackList.getInstance().getUser().getUsername() + "'s Snacks");
        }else {
            getActivity().setTitle("My Snacks");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_previous_entries, container, false);

        // Set up the progress overlay
        progressOverlay = view.findViewById(R.id.progressOverlay);
        ((TextView) progressOverlay.findViewById(R.id.progressMessage)).setText(
                "Accessing SnackTrack Database..."
        );

        listview = (ListView) view.findViewById(R.id.SnackList);
        listview.setAdapter(adapter);
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = listview.getCount();

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (listview.getLastVisiblePosition() >= count
                            - threshold) {
                      SnackList.getInstance().LoadMoreData(count);
                       // new LoadMoreData;
                    }
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {


            }
        });
        // Whenever an entry is clicked, show the details of that entry.
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String objectId = SnackList.getInstance().get(position).getObjectId();
                Bundle arguments = new Bundle();
                arguments.putString(SnackDetailsFragment.OBJECT_ID_KEY, objectId);
                SnackDetailsFragment snackDetailsFragment = new SnackDetailsFragment();
                snackDetailsFragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, snackDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the adapter and this from the SnackList's update listeners.
        SnackList.getInstance().unregisterUpdateListener(adapter);
        SnackList.getInstance().unregisterUpdateListener(this);
    }
}