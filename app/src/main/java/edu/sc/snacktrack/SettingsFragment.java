package edu.sc.snacktrack;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by spitzfor on 1/25/2016.
 */
public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button passwordButton = (Button) view.findViewById(R.id.password_change);
        Button emailButton = (Button) view.findViewById(R.id.email_change);

        passwordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");

                if(previousFrag != null)
                    transaction.remove(previousFrag);
                transaction.addToBackStack(null);

                DialogFragment changePasswordDialog = new ChangePasswordDialog();
                //changePasswordDialog.setStyle(DialogFragment.STYLE_NORMAL,0);
                changePasswordDialog.show(transaction, "dialog");
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");

                if(previousFrag != null)
                    transaction.remove(previousFrag);
                transaction.addToBackStack(null);

                DialogFragment changeEmailDialog = new ChangeEmailDialog();
                changeEmailDialog.show(transaction, "dialog");
            }
        });

        return view;
    }
}
