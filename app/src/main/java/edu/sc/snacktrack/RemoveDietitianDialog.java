package edu.sc.snacktrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRole;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by spitzfor on 3/26/2016.
 */
public class RemoveDietitianDialog extends DialogFragment {

    private Button yButton;
    private Button nButton;
    public Context cont;

    public RemoveDietitianDialog() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cont = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remove_dietitian, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        final ParseUser myDietitian = ParseUser.getCurrentUser().getParseUser("myDietitian");

        yButton = (Button) view.findViewById(R.id.submit_button);
        nButton = (Button) view.findViewById(R.id.cancel_button);

        yButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDietitian(myDietitian);
                dismiss();
                getFragmentManager().popBackStackImmediate();
            }
        });

        nButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void removeDietitian(final ParseUser targetUser) {
        ParseQuery<ParseRole> query = ParseRole.getQuery();
        query.whereEqualTo("name", "role_" + ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseRole>() {
            @Override
            public void done(List<ParseRole> objects, ParseException e) {
                if(e == null){
                    ParseRole role;
                    if(objects.size() == 0){
                        role = new ParseRole(ParseUser.getCurrentUser().getObjectId());
                    } else{
                        role = objects.get(0);
                    }
                    role.getUsers().remove(targetUser);
                    role.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                ParseUser.getCurrentUser().remove("myDietitian");
                                ParseUser.getCurrentUser().saveInBackground();
                                Toast.makeText(
                                        cont,
                                        targetUser.getUsername() + " is no longer your dietitian",
                                        Toast.LENGTH_LONG
                                ).show();

                            } else{
                                Toast.makeText(
                                        cont,
                                        "Failed to remove dietitian" + targetUser.getUsername(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    });
                } else{
                    Toast.makeText(
                            cont,
                            "Failed to remove dietitian " + targetUser.getUsername()
                            + " (" + e.getMessage() + ")",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
//        final ParseACL noOthers = new ParseACL();
//        ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);
//        query.whereEqualTo("owner", ParseUser.getCurrentUser());
//        query.findInBackground(new FindCallback<SnackEntry>() {
//            @Override
//            public void done(List<SnackEntry> refreshedSnacks, ParseException e) {
//                if(e == null)
//                {
//                    ParseUser owner = ParseUser.getCurrentUser();
//                    noOthers.setReadAccess(owner, true);
//                    noOthers.setWriteAccess(owner, true);
//                    noOthers.setReadAccess(targetUser, false);
//                    noOthers.setWriteAccess(targetUser, false);
//
//                    for(ParseObject entry : refreshedSnacks)
//                    {
//                        entry.setACL(noOthers);
//                        entry.saveInBackground();
//                    }
//
//                    owner.remove("myDietitian");
//                    owner.saveInBackground();
//
//                    Toast.makeText(cont, targetUser.getUsername().toString() + " is no longer your Dietitian", Toast.LENGTH_LONG).show();
//
//                }
//
//                else
//                    Toast.makeText(cont, "Something went wrong when fetching SnackList...", Toast.LENGTH_LONG).show();
//            }
//        });
    }
}
