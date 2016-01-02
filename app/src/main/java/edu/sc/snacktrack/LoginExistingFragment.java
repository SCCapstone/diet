package edu.sc.snacktrack;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginExistingFragment extends Fragment {

    private View rootView;

    private EditText usernameET;
    private EditText passwordET;

    private TextView newAccountLink;
    private TextView forgotPasswordLink;

    private Button logInButton;

    private Toast toast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_existing, container, false);

        // Initialize view fields
        rootView = view.findViewById(R.id.loginRootView);
        usernameET = (EditText) view.findViewById(R.id.usernameEditText);
        passwordET = (EditText) view.findViewById(R.id.passwordEditText);
        logInButton = (Button) view.findViewById(R.id.signInButton);
        newAccountLink = (TextView) view.findViewById(R.id.createNewAccountLink);
        forgotPasswordLink = (TextView) view.findViewById(R.id.forgotPasswordLink);

        // When the root view gains focus, we should hide the soft keyboard.
        rootView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Utils.closeSoftKeyboard(getContext(), v);
                }
            }
        });

        // Go back to the new account activity when the new account link is pressed.
        newAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) getActivity()).newAccountMode();
            }
        });

        // TODO: Implement a "forgot my password/username" feature
        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToast("Feature not yet available", Toast.LENGTH_SHORT);
            }
        });

        // Attempt to sign the user in with the sign in button is pressed
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeSoftKeyboard(getContext(), v);
                attemptLogIn();
            }
        });

        return view;
    }

    /**
     * Cancels the current toast and displays a new toast.
     *
     * @param text The text to display.
     * @param length The length to display the toast
     */
    private void updateToast(String text, int length){
        if(toast != null){
            toast.cancel();
        }

        toast = Toast.makeText(
                getContext(),
                text,
                length
        );
        toast.show();
    }

    /**
     * Enables or disables all user input widgets.
     *
     * @param enabled true to enable; false to disable
     */
    private void setWidgetsEnabled(boolean enabled){
        usernameET.setEnabled(enabled);
        passwordET.setEnabled(enabled);
        logInButton.setEnabled(enabled);
        newAccountLink.setEnabled(enabled);
        forgotPasswordLink.setEnabled(enabled);
    }

    /**
     * Attempts to log in the user. If unable to, displays an error message to the user.
     * If successful, sets result to RESULT_OK and finishes this activity.
     */
    private void attemptLogIn(){
        setWidgetsEnabled(false);

        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    // Sign in was successful
                    Activity activity = getActivity();

                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                } else {
                    updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                }

                setWidgetsEnabled(true);
            }
        });
    }
}
