package edu.sc.snacktrack;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class ChangePasswordDialog extends DialogFragment {

    private EditText passwordText;

    public ChangePasswordDialog() {
        //Empty constructor required
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_password_dialog, container);
        passwordText = (EditText) view.findViewById(R.id.new_password);



        return view;
    }
}
