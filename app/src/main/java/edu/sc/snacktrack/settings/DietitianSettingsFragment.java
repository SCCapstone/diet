package edu.sc.snacktrack.settings;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import edu.sc.snacktrack.R;

/**
 * Created by spitzfor on 3/25/2016.
 */
public class DietitianSettingsFragment extends Fragment {

    public Context cont;
    private Boolean hasDietitian = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cont = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dietitian_settings, container, false);
        final Button pickDietitian = (Button) view.findViewById(R.id.pick_dietitian);
        final Button removeDietitian = (Button) view.findViewById(R.id.remove_my_dietitian);
//        final Button msgDietitian = (Button) view.findViewById(R.id.message_my_dietitian);


        ParseUser checkDietitian = ParseUser.getCurrentUser().getParseUser("myDietitian");
            if(checkDietitian != null) {
                checkDietitian.fetchIfNeededInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser dietitian, ParseException e) {
                        hasDietitian = true;
                        pickDietitian.setHintTextColor(Color.parseColor("#595959"));
                    }
                });
            }

            else {
                removeDietitian.setHintTextColor(Color.parseColor("#595959"));
            }


        pickDietitian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasDietitian == true)
                    Toast.makeText(cont, "You already have a Dietitian!" + "\n" + "Click \"Remove My Dietitian\" and try again", Toast.LENGTH_LONG).show();

                else {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");

                    if(previousFrag != null)
                        transaction.remove(previousFrag);

                    transaction.addToBackStack(null);

                    DialogFragment pickDietitianDialog = new PickDietitianDialog();
                    pickDietitianDialog.show(transaction, "dialog");
                }
            }
        });

        removeDietitian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasDietitian == false)
                    Toast.makeText(cont, "You don't have a Dietitian yet..." + "\n" + "Click \"Pick My Dietitian\" to get started!", Toast.LENGTH_LONG).show();

                else {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");

                    if(previousFrag != null)
                        transaction.remove(previousFrag);

                    transaction.addToBackStack(null);

                    DialogFragment removeDietitianDialog = new RemoveDietitianDialog();
                    removeDietitianDialog.show(transaction, "dialog");
                }

            }
        });

//        msgDietitian.setOnClickListener(new View.);

        return view;
    }
}
