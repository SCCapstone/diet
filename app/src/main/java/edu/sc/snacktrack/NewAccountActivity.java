package edu.sc.snacktrack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class NewAccountActivity extends AppCompatActivity {

    private static final String TAG = "NewAccountDebug";

    public static final int EXISTING_ACCOUNT_LOGIN_REQUEST = 1;

    private View rootView;

    private EditText usernameET;
    private EditText passwordET;
    private EditText passwordConfirmET;

    private TextView usernameErrorStatus;
    private TextView passwordErrorStatus;

    private Button signUpButton;
    //private TextView existingAccountLink;
    private Button existingAccountButton;

    private Toast toast;

    private UsernameErrorStatusUpdater usernameErrorStatusUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        // Initialize view fields
        rootView = findViewById(R.id.newAccountRootView);
        usernameET = (EditText) findViewById(R.id.usernameEditText);
        passwordET = (EditText) findViewById(R.id.passwordEditText);
        passwordConfirmET = (EditText) findViewById(R.id.passwordConfirmEditText);
        usernameErrorStatus = (TextView) findViewById(R.id.usernameErrorStatus);
        passwordErrorStatus = (TextView) findViewById(R.id.passwordErrorStatus);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        //existingAccountLink = (TextView) findViewById(R.id.exisitingAccountLink);
        existingAccountButton = (Button) findViewById(R.id.existingAccountButton);

        // When the root view gains focus, we should hide the soft keyboard.
        rootView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    closeSoftKeyboard(v);
                }
            }
        });

        // Set the text watchers
        setTextWatchers();

        // The existing account link should start the login activity.
        existingAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity();
            }
        });


        // Attempt to sign the user up when the sign up button is pressed
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSoftKeyboard(v);
                attemptSignup();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        // If the user logged in an existing account, finish this activity.
        if(requestCode == EXISTING_ACCOUNT_LOGIN_REQUEST){
            if(resultCode == RESULT_OK && ParseUser.getCurrentUser() != null){
                setResult(RESULT_OK);
                finish();
            }
        } else{
            updateToast("Something's not right", Toast.LENGTH_LONG);
        }
    }

    /**
     * Attempts to sign up the new user. If unable to, displays an error message to the user.
     * If successful, sets the result to RESULT_OK and finishes this activity.
     */
    private void attemptSignup(){
        setWidgetsEnabled(false);

        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordConfirm = passwordConfirmET.getText().toString();

        StringBuilder usernameInvalidReason = new StringBuilder();
        StringBuilder passwordInvalidReason = new StringBuilder();

        // First check if the username is valid
        if(isUsernameValid(username, usernameInvalidReason)){

            // Next, check if the passwords are valid.
            if(isPasswordValid(password, passwordConfirm, passwordInvalidReason)){
                ParseUser newUser = new ParseUser();
                newUser.setUsername(username);
                newUser.setPassword(password);

                newUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            // Sign up was successful
                            setResult(RESULT_OK);
                            finish();
                        } else{
                            updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
                        }
                        setWidgetsEnabled(true);
                    }
                });
            }

            // If the passwords are invalid, display the reason to the user.
            else{
                updateToast(passwordInvalidReason.toString(), Toast.LENGTH_LONG);
                setWidgetsEnabled(true);
            }
        }

        // If the username is invalid, display the reason to the user
        else{
            updateToast(usernameInvalidReason.toString(), Toast.LENGTH_LONG);
            setWidgetsEnabled(true);
        }
    }

    /**
     * Starts the login activity for an existing account.
     */
    private void startLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, EXISTING_ACCOUNT_LOGIN_REQUEST);
        overridePendingTransition(R.animator.animation, R.animator.animation2);
    }

    /**
     * Sets the text watchers for each of the EditText views.
     */
    private void setTextWatchers(){
        // When the contents of the username EditText changes, we check the username.
        usernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (usernameErrorStatusUpdater != null) {
                    usernameErrorStatusUpdater.cancel(true);
                }

                usernameErrorStatusUpdater = new UsernameErrorStatusUpdater();
                usernameErrorStatusUpdater.execute(s.toString());
            }
        });

        // When the contents of the password or passwordConfirm EditText changes, we check the passwords.
        passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordET.getText().toString();
                String passwordConfirm = passwordConfirmET.getText().toString();
                updatePasswordErrorStatus(password, passwordConfirm);
            }
        });
        passwordConfirmET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordET.getText().toString();
                String passwordConfirm = passwordConfirmET.getText().toString();
                updatePasswordErrorStatus(password, passwordConfirm);
            }
        });
    }

    /**
     * Enables or disables all user input widgets.
     *
     * @param enabled true to enable; false to disable
     */
    private void setWidgetsEnabled(boolean enabled){
        usernameET.setEnabled(enabled);
        passwordET.setEnabled(enabled);
        passwordConfirmET.setEnabled(enabled);
        signUpButton.setEnabled(enabled);
        //existingAccountLink.setEnabled(enabled);
        existingAccountButton.setEnabled(enabled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        // The default behavior here is to go back to the previous activity.
        // We override this behavior as the user must be logged in to continue.
    }

    /**
     * Magic to close the soft keyboard.
     *
     * See http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
     *
     * @param focusedView The focused view
     */
    private void closeSoftKeyboard(View focusedView){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    /**
     * Checks if a username is valid.
     *
     * For now, this method applies the following constraints on usernames:
     *
     *   1. The username cannot be blank
     *   2. The username must be alphanumeric (only numbers and letters, no spaces)
     *
     * This method *does not* check if the username is taken.
     *
     * @param username The username to check
     * @param reason When not null, this method will append the reason the username is invalid
     *               (or "OK" if the username is valid).
     *
     * @return true if the username is valid. false otherwise.
     */
    private boolean isUsernameValid(String username, @Nullable StringBuilder reason){
        if(username.length() == 0){
            if(reason != null){
                reason.append("Username is blank");
            }
            return false;
        }

        // Usernames must be alphanumeric
        for(char c : username.toCharArray()){
            if(!Character.isLetterOrDigit(c)){
                if(reason != null){
                    reason.append("Username must be alphanumeric");
                }
                return false;
            }
        }

        if(reason != null){
            reason.append("OK");
        }
        return true;
    }

    /**
     * Checks if passwords are valid.
     *
     * Currently this method only checks if the passwords match or if either is blank.
     *
     * @param password The password
     * @param passwordConfirm The confirmed password
     * @param reason When not null, this method will append the reason the password is invalid
     *               (or "OK" if the username is valid).
     * @return true if the passwords are valid. false otherwise.
     */
    private boolean isPasswordValid(String password, String passwordConfirm,
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
     * Updates the passwordErrorStatus based on a reason returned by isPasswordValid().
     */
    private void updatePasswordErrorStatus(String password, String passwordConfirm){
        StringBuilder reason = new StringBuilder();
        String reasonString;
        isPasswordValid(password, passwordConfirm, reason);
        reasonString = reason.toString();

        if(reasonString.equalsIgnoreCase("OK")){
            passwordErrorStatus.setTextColor(Color.BLACK);
            passwordErrorStatus.setText(reasonString);
        } else{
            passwordErrorStatus.setTextColor(Color.RED);
            passwordErrorStatus.setText(reasonString);
        }
    }

    /**
     * Cancels the current toast and displays a new toast.
     *
     * @param text The text to display
     * @param length The length to display the toast
     */
    private void updateToast(String text, int length){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(
                this,
                text,
                length
        );
        toast.show();
    }

    /**
     * This class checks if a username is valid and then checks if the username is taken.
     * Once complete, updates usernameErrorStatus.
     *
     * The username should be passed to the execute() method.
     *
     * This process must be done in an AsyncTask because querying for a username may take a long
     * time and will interfere the UI thread.
     */
    private class UsernameErrorStatusUpdater extends AsyncTask<String, Void, String>{

        private TextView usernameErrorStatus;
        private ParseQuery<ParseUser> query;

        private boolean queryCanceled;

        public UsernameErrorStatusUpdater(){
            super();
            this.queryCanceled = false;
        }

        @Override
        protected void onPreExecute(){
            usernameErrorStatus = NewAccountActivity.this.usernameErrorStatus;

            usernameErrorStatus.setText("Checking username...");
            usernameErrorStatus.setTextColor(Color.BLACK);
        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            return checkUsername(username);
        }

        @Override
        protected void onPostExecute(String result){
           usernameErrorStatus.setText(result);
            if(result.equalsIgnoreCase("OK")){
                usernameErrorStatus.setTextColor(Color.BLACK);
            } else{
                usernameErrorStatus.setTextColor(Color.RED);
            }
        }

        @Override
        protected void onCancelled(String result){
            queryCanceled = true;
            if(query != null){
                query.cancel();
            }
        }

        /**
         * First, this method calls isUsernameValid(). If the username is valid, we then check if
         * it is already taken.
         *
         * @param username The username to check
         *
         * @return "OK" if the username checks out. Error message otherwise.
         */
        private String checkUsername(String username){

            // First, check constraints in isUserNameValid()
            StringBuilder temp = new StringBuilder();
            if(!isUsernameValid(username, temp)){
                return temp.toString();
            }

            // If the query hasn't already been canceled, start the query.
            if(!queryCanceled){
                // Next, check if the username is taken
                query = ParseUser.getQuery();
                query.whereEqualTo("username", username);
                try {
                    if (query.find().size() > 0) {
                        // Username is taken.
                        return "Username is taken";
                    } else{
                        // User name is not taken
                        return "OK";
                    }
                } catch(ParseException e){
                    return "Unable to check username";
                }
            } else{
                return "Unable to check username";
            }

            // This line is unreachable.
        }
    }
}