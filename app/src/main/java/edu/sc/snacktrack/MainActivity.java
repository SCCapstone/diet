package edu.sc.snacktrack;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RemoteDataTaskFragment.RDTTaskCallbacks{

    private static final int LOGIN_REQUEST = 1;
    private static final int NEW_ENTRY_REQUEST = 2;

    private static final String REMOTE_DATA_TASK_FRAGMENT_TAG = "remoteDataTaskFragment";

    private Toast toast;

    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    String mTitle = "";

    // TODO figure out why getSupportActionBar() functions result in null-pointer exceptions

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

        mTitle = (String) getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Select an option");
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.drawer_list_item, getResources().getStringArray(R.array.tests));

        mDrawerList.setAdapter(adapter);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String[] tests = getResources().getStringArray(R.array.tests);
                mTitle = tests[position];
                testFragment tFragment = new testFragment();

                Bundle data = new Bundle();
                data.putInt("position", position);
                tFragment.setArguments(data);

                android.app.FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.content_frame, tFragment);
                ft.commit();

                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

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
    public void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);

        // Synchronize the state of the drawer toggle.
        // This causes the drawer icon to appear and animate when the drawer opens/closes.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
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
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_logout).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        else {
            int id = item.getItemId();

            switch (id) {
                case R.id.action_logout:
                    logout();
                    break;
                case R.id.action_new:
                    Intent intent = new Intent(this, NewEntryActivity.class);
                    startActivity(intent);
            }
        }

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