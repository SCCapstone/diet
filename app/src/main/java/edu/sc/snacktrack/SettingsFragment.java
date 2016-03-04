package edu.sc.snacktrack;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by spitzfor on 1/25/2016.
 */
public class SettingsFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.action_new).setEnabled(false);
        menu.findItem(R.id.action_new).setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button passwordButton = (Button) view.findViewById(R.id.password_change);
        //Button emailButton = (Button) view.findViewById(R.id.email_change);
        Button myDietitianButton = (Button) view.findViewById(R.id.my_dietitian);

        passwordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");

                if(previousFrag != null)
                    transaction.remove(previousFrag);
                transaction.addToBackStack(null);

                DialogFragment changePasswordDialog = new ChangePasswordDialog();
                changePasswordDialog.show(transaction, "dialog");
            }

        });

//        emailButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");
//
//                if(previousFrag != null)
//                    transaction.remove(previousFrag);
//                transaction.addToBackStack(null);
//
//                DialogFragment changeEmailDialog = new ChangeEmailDialog();
//                changeEmailDialog.show(transaction, "dialog");
//            }
//        });

        myDietitianButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");

                if (previousFrag != null)
                    transaction.remove(previousFrag);
                transaction.addToBackStack(null);

                DialogFragment myDietitianDialog = new MyDietitianDialog();
                myDietitianDialog.show(transaction, "dialog");
            }
        });

        return view;
    }
}
