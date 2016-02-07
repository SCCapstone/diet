package edu.sc.snacktrack;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogInCallback;
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
        getDialog().setTitle("Change Password");

        passwordText = (EditText) view.findViewById(R.id.new_password);
        submitButton = (Button) view.findViewById(R.id.new_password_button);

        //Submit new password and close dialog when Submit button is pressed
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeSoftKeyboard(getContext(), v);
                attemptPasswordChange();
                dismiss();
                //logout();
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
        currentUser.logInInBackground(currentUser.getUsername().toString(), passwordText.getText().toString(), new LogInCallback() {
           @Override
            public void done(ParseUser user, com.parse.ParseException e) {
               if(user != null)
                   Log.i("PasswordSuccess","Successful password change");
                   //Toast.makeText(getActivity(),"Successful password change",Toast.LENGTH_LONG).show();
               else
               {
                   Log.i("Username",user.getUsername().toString());
                   Log.i("Password",passwordText.getText().toString());
               }
                   //Log.e("PasswordFAIL","PASSWORD CHANGE UNSUCCESSFUL");
                   //Toast.makeText(getActivity(),"PASSWORD CHANGE UNSUCCESSFUL",Toast.LENGTH_LONG).show();
           }
        });
    }

    /*
    private void startLoginActivity(){
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }
    */


    /**
     * Logs out the current user and starts the new account activity.
     * Just starts the new account activity is no user is logged in
     */
    /*
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
    */
}
