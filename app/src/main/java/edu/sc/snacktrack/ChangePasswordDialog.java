package edu.sc.snacktrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseUser;

public class ChangePasswordDialog extends DialogFragment {

    private EditText passwordText;
    private Button submitButton;

    public ChangePasswordDialog() {
        //Empty constructor required
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_password_dialog, container);
        getDialog().setTitle("Change Password");

        passwordText = (EditText) view.findViewById(R.id.new_password);
        submitButton = (Button) view.findViewById(R.id.new_password_button);

        //Submit changes and take user back to LoginActivity
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeSoftKeyboard(getContext(), v);
                attemptPasswordChange();
                Intent intent = new Intent(getActivity(),LoginActivity.class);
                startActivity(intent);
                dismiss();
            }
        });

        //int style = DialogFragment.STYLE_NORMAL, theme = 0;
        //setStyle(style,theme);
        return view;
    }

    //TODO: Fix session token error when changing password
    private void attemptPasswordChange() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.setPassword(passwordText.getText().toString());
        currentUser.saveInBackground();
    }
}
