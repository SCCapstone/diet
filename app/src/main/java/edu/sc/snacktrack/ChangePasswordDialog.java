package edu.sc.snacktrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class ChangePasswordDialog extends DialogFragment {

    private static final int LOGIN_REQUEST = 1;

    private EditText passwordText;
    private Button submitButton;

    public ChangePasswordDialog() {
        //Empty constructor required
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_password_dialog, container);
        passwordText = (EditText) view.findViewById(R.id.new_password);
        submitButton = (Button) view.findViewById(R.id.new_password_button);

        //Submit new password and close dialog when Submit button is pressed
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeSoftKeyboard(getContext(), v);
                //attemptPasswordChange();
                dismiss();
                //logout();
            }
        });

        //int style = DialogFragment.STYLE_NORMAL, theme = 0;
        //setStyle(style,theme);
        return view;
    }

    private void attemptPasswordChange() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.setPassword(passwordText.getText().toString());
        currentUser.saveInBackground();
    }

    private void startLoginActivity(){
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }



    /**
     * Logs out the current user and starts the new account activity.
     * Just starts the new account activity is no user is logged in
     */
    private void logout(){
        ParseUser.logOutInBackground(new LogOutCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null) {
                    startLoginActivity();
                } else {

                }
            }
        });
    }
}
