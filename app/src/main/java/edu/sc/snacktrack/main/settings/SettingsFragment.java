package edu.sc.snacktrack.main.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.sc.snacktrack.R;

/**
 * Created by spitzfor on 1/25/2016.
 */
public class SettingsFragment extends Fragment {

    public Context cont;

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

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button passwordButton = (Button) view.findViewById(R.id.password_change);
        //Button emailButton = (Button) view.findViewById(R.id.email_change);
        Button myDietitianButton = (Button) view.findViewById(R.id.my_dietitian);

        Button refreshAccountButton = (Button) view.findViewById(R.id.refresh_account);

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





    // Original myDietitianButton
//        myDietitianButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                DietitianList.getInstance().refresh(null);
//                DisplayDietitiansFragment dispDietFrag = new DisplayDietitiansFragment();
//                getFragmentManager().beginTransaction().replace(R.id.content_frame, dispDietFrag).addToBackStack(null).commit();
//            }
//        });

        myDietitianButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    DietitianSettingsFragment dietitianSettingsFragment = new DietitianSettingsFragment();
                    transaction.replace(R.id.content_frame, dietitianSettingsFragment).addToBackStack(null).commit();


//                Fragment fragment = new DietitianSettingsFragment();
//                FragmentManager fm = getSupportFragmentManager();
//                fm.beginTransaction()
//                        .replace(R.id.content_frame, fragment, CURRENT_FRAGMENT_TAG)
//                        .commit();
//
//                DietitianList.getInstance().refresh(null);
//                DisplayDietitiansFragment dispDietFrag = new DisplayDietitiansFragment();
//                getFragmentManager().beginTransaction().replace(R.id.content_frame, dispDietFrag).addToBackStack(null).commit();
            }
        });

        refreshAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshAccountDialog fragment = new RefreshAccountDialog();
                fragment.show(getFragmentManager(), "refreshAccount");
            }
        });

        return view;
    }
}
