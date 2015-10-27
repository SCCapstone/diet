package edu.sc.snacktrack;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    private View rootView;

    private EditText usernameET;
    private EditText passwordET;

    private TextView newAccountLink;
    private TextView forgotPasswordLink;

    private Button logInButton;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize view fields
        rootView = findViewById(R.id.loginRootView);
        usernameET = (EditText) findViewById(R.id.usernameEditText);
        passwordET = (EditText) findViewById(R.id.passwordEditText);
        logInButton = (Button) findViewById(R.id.signInButton);
        newAccountLink = (TextView) findViewById(R.id.createNewAccountLink);
        forgotPasswordLink = (TextView) findViewById(R.id.forgotPasswordLink);

        // When the root view gains focus, we should hide the soft keyboard.
        rootView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    closeSoftKeyboard(v);
                }
            }
        });

        // Go back to the new account activity when the new account link is pressed.
        newAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
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
                closeSoftKeyboard(v);
                attemptLogIn();
            }
        });
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
                if(e == null){
                    // Sign in was successful
                    setResult(RESULT_OK);
                    finish();
                } else{
                    updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                }

                setWidgetsEnabled(true);
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
        logInButton.setEnabled(enabled);
        newAccountLink.setEnabled(enabled);
        forgotPasswordLink.setEnabled(enabled);
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
                this,
                text,
                length
        );
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
        // When the hard back button is pressed, cancel the login and go back to new account
        // activity.
        setResult(RESULT_CANCELED);
        finish();
    }
}
