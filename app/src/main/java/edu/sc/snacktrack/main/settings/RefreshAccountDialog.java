package edu.sc.snacktrack.main.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRole;
import com.parse.ParseUser;

import java.util.List;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.snacks.SnackEntry;

/**
 * Dialog fragment for "refreshing account.
 */
public class RefreshAccountDialog extends DialogFragment{

    private static TheTask refreshTask;
    private Context context;
    private ProgressBar progressBar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refresh_account_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        Button button = (Button) view.findViewById(R.id.refresh_account);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(refreshTask == null){
                    refreshTask = new TheTask();
                    refreshTask.execute(RefreshAccountDialog.this);
                    progressBar.setIndeterminate(true);
                } else if(refreshTask.getStatus() == AsyncTask.Status.FINISHED || refreshTask.isCancelled()){
                    refreshTask = new TheTask();
                    refreshTask.execute(RefreshAccountDialog.this);
                    progressBar.setIndeterminate(true);
                }
            }
        });

        if(refreshTask != null && refreshTask.getStatus() == AsyncTask.Status.RUNNING){
            refreshTask.fragment = this;
        }

        return view;
    }

    private static class TheTask extends AsyncTask<RefreshAccountDialog, Integer, ParseException> {

        private RefreshAccountDialog fragment;

        @Override
        protected ParseException doInBackground(RefreshAccountDialog... params) {
            fragment = params[0];


            ParseQuery<SnackEntry> snackQuery = new ParseQuery<>(SnackEntry.class);
            snackQuery.whereEqualTo("owner", ParseUser.getCurrentUser());

            try{
                List<SnackEntry> entries = snackQuery.find();
                int progress = 0;
                final int maxProgress = entries.size() * 1;

                // Recreate the read access role for this user.
                ParseRole role = new ParseRole("role_" + ParseUser.getCurrentUser().getObjectId());
                role.setACL(new ParseACL(ParseUser.getCurrentUser()));


                // Delete old role if it exists
                ParseQuery<ParseRole> roleQuery = ParseRole.getQuery();
                List<ParseRole> roles;
                roleQuery.whereEqualTo("name", role.getName());
                roles = roleQuery.find();
                if(roles.size() != 0){
                    roles.get(0).delete();
                }
                
                // Save new role
                role.save();

                // Create the ACL for all entries.
                ParseACL entryACL = new ParseACL(ParseUser.getCurrentUser());
                entryACL.setRoleReadAccess(role, true);

                // Set the correct ACL for all entries.
                for(int i = 0; i < entries.size(); ++i){
                    Log.d("RefreshAccountDialog", ""+i);
                    SnackEntry entry = entries.get(i);
                    entry.setACL(entryACL);
                    entry.save();
                    publishProgress(++progress, maxProgress);
                }
            } catch(ParseException e){
                return e;
            }

            // Remove the (now invalid) pointer to this user's dietitian.
            ParseUser.getCurrentUser().remove("myDietitian");
            try {
                ParseUser.getCurrentUser().save();
            } catch (ParseException e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(fragment != null && fragment.progressBar != null){
                if(values.length > 1){
                    if(values[0] < values[1]){
                        fragment.progressBar.setProgress(values[0]);
                        fragment.progressBar.setMax(values[1]);
                        fragment.progressBar.setIndeterminate(false);
                    } else{
                        fragment.progressBar.setIndeterminate(true);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(ParseException e) {
            if(e != null){
                Log.d("RefreshAccountDialog", e.getMessage());
                if(fragment != null && fragment.context != null){
                    Toast.makeText(
                            fragment.context,
                            "Could not refresh account at this time.",
                            Toast.LENGTH_LONG
                    ).show();
                }

            } else{
                if(fragment != null && fragment.context != null){
                    Toast.makeText(
                            fragment.context,
                            "Account refreshed!",
                            Toast.LENGTH_LONG
                    ).show();
                }
                if(fragment != null){
                    fragment.dismiss();
                }
            }
        }
    }
}