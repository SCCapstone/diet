package edu.sc.snacktrack.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import edu.sc.snacktrack.main.MainActivity;
import edu.sc.snacktrack.R;
import edu.sc.snacktrack.utils.Utils;

public class LoginExistingFragment extends Fragment {

    private View rootView;

    private EditText usernameET;
    private EditText passwordET;

    private TextView newAccountLink;
    private TextView forgotPasswordLink;

    private Button logInButton;

    private Toast toast;

    private Context context;

    private TextWatcher passwordTextWatcher;

    private boolean dummyPassMode;
    private boolean loggingIn = false;

    private static final String DUMMY_PASS = " h/%@]]()wNb";

    private static final String STATE_DUMMY_PASS = "stateDummyPass";
    private static final String STATE_LOGGING_IN = "stateLoggingIn";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        loggingIn = false;
        if(ParseUser.getCurrentUser() != null){
            dummyPassMode = true;
        }
    }

    @Override
    public void onPause() {
        if(passwordTextWatcher != null && passwordET != null){
            passwordET.removeTextChangedListener(passwordTextWatcher);
        }
        super.onPause();
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

        setWidgetsEnabled(!loggingIn);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If the user is already logged in, "fill in the fields"
        if(ParseUser.getCurrentUser() != null && dummyPassMode){
            usernameET.setText(ParseUser.getCurrentUser().getUsername());
            passwordET.setText(DUMMY_PASS);
            passwordTextWatcher = new TextWatcher() {

                private boolean init;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                    if(dummyPassMode && !init){
                        dummyPassMode = false;
                        passwordET.post(new Runnable() {
                            @Override
                            public void run() {
                                passwordET.setText(s.subSequence(start, start + count));
                                passwordET.setSelection(count);
                            }
                        });
                    }
                }
            };

            passwordET.addTextChangedListener(passwordTextWatcher);
        }
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

        if(context != null){
            toast = Toast.makeText(
                    context,
                    text,
                    length
            );
            toast.show();
        }
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
     * If successful, sets result to RESULT_OK, finishes this activity and starts MainActivity.
     */
    private void attemptLogIn(){
        setWidgetsEnabled(false);
        loggingIn = true;
        // If the user is already logged in, attempt to log them in with their current session token.
        // Otherwise, log in normally with a username and password.
        if(dummyPassMode){
            if(ParseUser.getCurrentUser() != null){
                ParseUser.becomeInBackground(ParseUser.getCurrentUser().getSessionToken(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(e == null){
                            // Sign in successful
                            startMainActivity();
                        } else{
                            if(e.getCode() == ParseException.INVALID_SESSION_TOKEN){
                                // If the session token is invalid, the user must retype their password
                                // to log in (we don't really know their password).
                                dummyPassMode = false;
                                passwordET.setText("");
                            }
                            updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                        }

                        setWidgetsEnabled(true);
                        loggingIn = false;
                    }
                });
            }
        } else{
            String username = usernameET.getText().toString();
            String password = passwordET.getText().toString();
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        // Sign in was successful
                        startMainActivity();
                    } else {
                        updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                    }

                    setWidgetsEnabled(true);
                    loggingIn = false;
                }
            });
        }
    }

    private void startMainActivity(){
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
