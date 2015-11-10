package edu.sc.snacktrack;

import android.content.Intent;
import android.graphics.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    public static final int LOGIN_REQUEST = 1;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If no user is logged in, start the new account activity.
        if(ParseUser.getCurrentUser() == null){
            startNewAccountActivity();
        }
    }

    private void startNewAccountActivity(){
        Intent intent = new Intent(this, NewAccountActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == MainActivity.LOGIN_REQUEST){
            if(resultCode == RESULT_OK){
                updateToast("Log in successful!", Toast.LENGTH_SHORT);
            } else{
                startNewAccountActivity();
            }
        }
        else{
            updateToast("Something's not right", Toast.LENGTH_LONG);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_logout:
                logout();
                break;
            case R.id.action_new:
                Intent intent = new Intent(this, NewEntryActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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
     * Logs out the current user and starts the new account activity.
     * Just starts the new account activity is no user is logged in
     */
    private void logout(){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    startNewAccountActivity();
                } else{
                    updateToast(e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        });
    }
}