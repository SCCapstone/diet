package edu.sc.snacktrack;

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

/**
 * Created by spitzfor on 3/25/2016.
 */
public class DietitianSettingsFragment extends Fragment {

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

        View view = inflater.inflate(R.layout.fragment_dietitian_settings, container, false);
        final Button pickDietitian = (Button) view.findViewById(R.id.pick_dietitian);
        Button removeDietitian = (Button) view.findViewById(R.id.remove_my_dietitian);
        Button msgDietitian = (Button) view.findViewById(R.id.message_my_dietitian);

//        pickDietitian.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");
//
//                if(previousFrag != null)
//                    transaction.remove(previousFrag);
//
//                transaction.addToBackStack(null);
//
//                DialogFragment pickDietitianDialog = new PickDietitianDialog();
//                pickDietitianDialog.show(transaction, "dialog");
//            }
//        });

        removeDietitian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment previousFrag = getFragmentManager().findFragmentByTag("dialog");

                if(previousFrag != null)
                    transaction.remove(previousFrag);

                transaction.addToBackStack(null);

                DialogFragment removeDietitianDialog = new RemoveDietitianDialog();
                removeDietitianDialog.show(transaction, "dialog");
            }
        });

        return view;
    }
}
