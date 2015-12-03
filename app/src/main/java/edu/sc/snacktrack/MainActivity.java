package edu.sc.snacktrack;

import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RemoteDataTaskFragment.RDTTaskCallbacks{

    private static final int LOGIN_REQUEST = 1;
    private static final int NEW_ENTRY_REQUEST = 2;

    private static final String REMOTE_DATA_TASK_FRAGMENT_TAG = "remoteDataTaskFragment";

    private Toast toast;

    private ListView listview;
    private CustomAdapter adapter;
    private List<SnackEntry> mySnackList = null;

    private RemoteDataTaskFragment remoteDataTaskFragment;

    private View progressOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If no user is logged in, start the new account activity.
        if(ParseUser.getCurrentUser() == null){
            startNewAccountActivity();
        }

        // Set up the progress overlay
        progressOverlay = findViewById(R.id.progressOverlay);
        ((TextView) progressOverlay.findViewById(R.id.progressMessage)).setText(
                "Accessing SnackTrack Database..."
        );

        FragmentManager fm = getSupportFragmentManager();
        remoteDataTaskFragment = (RemoteDataTaskFragment) fm.findFragmentByTag(REMOTE_DATA_TASK_FRAGMENT_TAG);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (remoteDataTaskFragment == null) {
            remoteDataTaskFragment = new RemoteDataTaskFragment();
            fm.beginTransaction().add(remoteDataTaskFragment, REMOTE_DATA_TASK_FRAGMENT_TAG).commit();
        } else{
            remoteDataTaskFragment.restart();
        }

        // If remote data access is in progress, show the progress overlay
        if(remoteDataTaskFragment.isRunning()){
            progressOverlay.setVisibility(View.VISIBLE);
            //setWidgetsEnabled(false);
        }
    }

    @Override
    public void onRDTPreExecute() {
        progressOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRDTProgressUpdate(int percent) {

    }

    @Override
    public void onRDTCancelled() {

    }

    @Override
    public void onRDTPostExecute(List<SnackEntry> snackList) {
        this.mySnackList = snackList;

        // Locate the listview in listview_main.xml
        listview = (ListView) findViewById(R.id.SnackList);
        // Pass the results into ListViewAdapter.java
        adapter = new CustomAdapter(MainActivity.this,
                mySnackList);
        // Binds the Adapter to the ListView
        listview.setAdapter(adapter);

        progressOverlay.setVisibility(View.GONE);
    }

    private void startNewAccountActivity(){
        Intent intent = new Intent(this, NewAccountActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case LOGIN_REQUEST:
                if(resultCode == RESULT_OK){
                    updateToast("Log in successful!", Toast.LENGTH_SHORT);
                } else{
                    startNewAccountActivity();
                }

                remoteDataTaskFragment.restart();
                break;
            case NEW_ENTRY_REQUEST:
                if(resultCode == RESULT_OK){
                    remoteDataTaskFragment.restart();
                }
                break;
            default:
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
                startActivityForResult(intent, NEW_ENTRY_REQUEST);
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