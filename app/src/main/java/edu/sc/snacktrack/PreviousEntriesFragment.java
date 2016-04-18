package edu.sc.snacktrack;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

import com.parse.ParseException;
import com.parse.ParseUser;

import edu.sc.snacktrack.snacks.SnackList;
import edu.sc.snacktrack.snacks.SnackListAdapter;

public class PreviousEntriesFragment extends Fragment implements SnackList.UpdateListener{

    private static final String TAG = "PreviousEntriesFragment";

    private ListView listview;
    private SnackListAdapter adapter;

    private View progressOverlay;

    private static ParseUser lastShowedHelpFor = null;
    private static boolean showingHelp = false;

    private Activity myActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity = (Activity) context;
        myActivity = (Activity) context;
    }

    @Override
    public void onSnackListUpdateComplete(ParseException e) {
        // Start the login activity if the session token is invalid.
        if(e != null && e.getCode() == ParseException.INVALID_SESSION_TOKEN){
            if(myActivity != null){
                Intent startLoginIntent = new Intent(myActivity, LoginActivity.class);
                ParseUser.logOutInBackground();
                myActivity.finish();
                startActivity(startLoginIntent);
            }
        }

        adapter.notifyDataSetChanged();

        if(progressOverlay != null){
            progressOverlay.setVisibility(View.GONE);
        }

        // Show the help message if appropriate.
        // That is, if we haven't already showed it for the current user this session and the
        // current user has zero entries. Only show the help message if the SnackList is pointing
        // at the current user.
        if(lastShowedHelpFor != ParseUser.getCurrentUser()
                && !showingHelp && SnackList.getInstance().size() == 0
                && ParseUser.getCurrentUser().equals(SnackList.getInstance().getUser())){
            try{
                showHelpMessage();
            } catch(IllegalStateException ignored){
                // In the case that onSaveInstanceState has been already been called, we ignore
                // the exception.
            }
        }
    }

    @Override
    public void onSnackListUpdateStart() {
        Log.d(TAG, "onSnackListUpdateStart");
        if(progressOverlay != null){
            progressOverlay.setVisibility(View.VISIBLE);
        }
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                      SnackList.getInstance().loadMoreData(count);
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
//                String objectId = SnackList.getInstance().get(position).getObjectId();
//                Bundle arguments = new Bundle();
//                arguments.putInt(SnackDetailsActivity.SNACK_POSITION_KEY, position);
//                SnackDetailsActivity snackDetailsFragment = new SnackDetailsActivity();
//                snackDetailsFragment.setArguments(arguments);
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.content_frame, snackDetailsFragment)
//                        .addToBackStack(null)
//                        .commit();

                Intent detailsIntent = new Intent(getContext(), SnackDetailsActivity.class);
                detailsIntent.putExtra(SnackDetailsActivity.SNACK_POSITION_KEY, position);
                startActivity(detailsIntent);
            }
        });

        return view;
    }

    /**
     * Displays a HelpMessageDialogFragment if one is not already visible.
     */
    private void showHelpMessage(){
        Fragment help = getFragmentManager().findFragmentByTag("helpDialog");
        if(help == null){
            showingHelp = true;
            help = new HelpMessageDialogFragment();
            ((DialogFragment) help).show(getFragmentManager(), "helpDialog");
        }
    }

    /**
     * Dialog fragment for showing the user a help message when they don't have any entries.
     */
    public static class HelpMessageDialogFragment extends DialogFragment{
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.previous_entries_help_title)
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage(R.string.previous_entries_help_message)
                    .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            lastShowedHelpFor = ParseUser.getCurrentUser();
                            dismiss();
                        }
                    })
                    .create();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            showingHelp = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the adapter and this from the SnackList's update listeners.
        SnackList.getInstance().unregisterUpdateListener(adapter);
        SnackList.getInstance().unregisterUpdateListener(this);
    }
}