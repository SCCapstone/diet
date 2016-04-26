package edu.sc.snacktrack.main.settings;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import edu.sc.snacktrack.login.LoginActivity;
import edu.sc.snacktrack.R;
import edu.sc.snacktrack.login.PasswordChecker;
import edu.sc.snacktrack.utils.Utils;

public class ChangePasswordDialog extends DialogFragment {

    private EditText passwordText;
    private EditText passwordConfirmText;
    private TextView passwordMatchStatus;
    private TextView passwordReqStatus;
    private Button submitButton;
    private Button cancelButton;
    public Context cont;

    private static final PasswordChecker passwordChecker = new PasswordChecker();

    public ChangePasswordDialog() {
        //Empty constructor required
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cont = context;
    }

    /**
     * Enables or disables all user input widgets.
     *
     * @param enabled true to enable; false to disable
     */
    private void setWidgetsEnabled(boolean enabled){
        submitButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        passwordText.setEnabled(enabled);
        passwordConfirmText.setEnabled(enabled);
    }

    /**
     * Sets the text watchers for each of the EditTexts
     */
    private void setTextWatchers(){
        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordText.getText().toString();
                String passwordConfirm = passwordConfirmText.getText().toString();
                updatePasswordErrorStatus(password, passwordConfirm);
            }
        });

        passwordConfirmText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordText.getText().toString();
                String passwordConfirm = passwordConfirmText.getText().toString();
                updatePasswordErrorStatus(password, passwordConfirm);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_password_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        passwordText = (EditText) view.findViewById(R.id.password);
        passwordConfirmText = (EditText) view.findViewById(R.id.confirmPassword);
        passwordMatchStatus = (TextView) view.findViewById(R.id.passwordMatchStatus);
        passwordReqStatus = (TextView) view.findViewById(R.id.passwordReqTextView);
        submitButton = (Button) view.findViewById(R.id.new_password_submit);
        cancelButton = (Button) view.findViewById(R.id.new_password_cancel);

        setTextWatchers();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPasswordChange();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    /**
     * Checks if a password and its confirmation match
     *
     * @param password The password
     * @param passwordConfirm The confirmed password
     * @param reason When not null, this method will append the reason the passwords do not match
     *               (or "OK" if they do).
     * @return true if the passwords match. false otherwise.
     */
    private boolean doPasswordsMatch(String password, String passwordConfirm,
                                     @Nullable StringBuilder reason){
        if(password.equals("")){
            if(reason != null){
                reason.append("Password is blank");
            }
            return false;
        }
        else if(passwordConfirm.equals("")){
            if(reason != null){
                reason.append("Password confirm is blank");
            }
            return false;
        }
        else if(!password.equals(passwordConfirm)){
            if(reason != null){
                reason.append("Passwords do not match");
            }
            return false;
        }
        else{
            if(reason != null){
                reason.append("OK");
            }
            return true;
        }
    }

    /**
     * Updates the password error status. That is, displays to the user whether or not the passwords
     * match and if the password meets the strength requirements.
     */
    private void updatePasswordErrorStatus(String password, String passwordConfirm){
        StringBuilder matchProblem = new StringBuilder();
        if(doPasswordsMatch(password, passwordConfirm, matchProblem)){
            passwordMatchStatus.setTextColor(Color.parseColor("#006600"));
            passwordMatchStatus.setText("Passwords match");
        } else{
            passwordMatchStatus.setTextColor(Color.RED);
            passwordMatchStatus.setText(matchProblem.toString());
        }

        PasswordChecker.CheckResult checkResult = PasswordChecker.checkPassword(password);

        // Color each of the requirements red (requirement not satisfied) or green (requirement
        // satisfied).
        passwordReqStatus.setText(Html.fromHtml(new StringBuilder()
                .append(checkResult.hasMixedCase() && checkResult.hasNumbers()
                        && checkResult.length() >= 8?
                        "<font color='#006600'>Your password must: </font>" :
                        "<font color='#ff0000'>Your password must: </font>"
                )
                .append(checkResult.hasMixedCase() && checkResult.hasNumbers() ?
                        "<font color='#006600'>use </font>" :
                        "<font color='#ff0000'>use </font>"
                )
                .append(checkResult.hasLowerCase() ?
                        "<font color='#006600'>a lowercase letter, </font>" :
                        "<font color='#ff0000'>a lowercase letter, </font>"
                )
                .append(checkResult.hasUpperCase() ?
                        "<font color='#006600'>an uppercase letter, </font>" :
                        "<font color='#ff0000'>an uppercase letter, </font>"
                )
                .append(checkResult.hasNumbers() ?
                        "<font color='#006600'>a number, </font>" :
                        "<font color='#ff0000'>a number, </font>"
                )
                .append(checkResult.length() >= passwordChecker.getMinimumLength() ?
                        "<font color='#006600'>and have at least 8 characters." :
                        "<font color='#ff0000'>and have at least 8 characters."
                )
                .toString()
        ));
    }

    private void attemptPasswordChange() {
        final String password = passwordText.getText().toString();
        final String confirmPassword = passwordConfirmText.getText().toString();

        final ParseUser currentUser = ParseUser.getCurrentUser();

        StringBuilder matchProblem = new StringBuilder();

        if(!doPasswordsMatch(password, confirmPassword, matchProblem)){
            if(cont != null){
                Toast.makeText(
                        cont,
                        matchProblem.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }
            return;
        }

        if(!passwordChecker.meetsRequirements(password)){
            if(cont != null){
                Toast.makeText(
                        cont,
                        "Password does not meet requirements",
                        Toast.LENGTH_LONG
                ).show();
            }
            return;
        }

        // If this line is reached, the provided credentials are valid, so we attempt the password
        // change.

        currentUser.setPassword(passwordText.getText().toString());
        setWidgetsEnabled(false);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    ParseUser.logInInBackground(currentUser.getUsername(), password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(e == null){
                                Toast.makeText(
                                        cont,
                                        "Password change successful",
                                        Toast.LENGTH_SHORT
                                ).show();
                                dismiss();
                            } else{
                                Toast.makeText(
                                        cont,
                                        "Password change successful - please reenter your new credentials",
                                        Toast.LENGTH_LONG
                                ).show();
                                dismiss();
                                if(getActivity() != null){
                                    getActivity().finish();
                                    Intent loginIntent = new Intent(cont, LoginActivity.class);
                                    startActivity(loginIntent);
                                }
                            }
                            setWidgetsEnabled(true);
                        }
                    });
                } else if(cont != null){
                    Toast.makeText(
                            cont,
                            Utils.getErrorMessage(e),
                            Toast.LENGTH_LONG
                    ).show();
                    setWidgetsEnabled(true);
                }
            }
        });
    }
}
