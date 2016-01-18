package edu.sc.snacktrack;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity{

    private static final int LOGIN_REQUEST = 1;
    private static final int NEW_ENTRY_REQUEST = 2;
    private static final int CAMERA_REQUEST = 3;

    private static final String STATE_NEW_IMAGE_FILE = "newImageFile";

    private static final String PREVIOUS_ENTRIES_FRAG_TAG = "previousEntriesFragment";

    private Toast toast;

    private static final String TAG = "MainActivity";

    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    String mTitle = "";

    private FileCache fileCache;

    private File newImageFile;

    private PreviousEntriesFragment previousEntriesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // If no user is logged in, start the login activity.
        if(ParseUser.getCurrentUser() == null){
            Log.d(TAG, "No user is logged in, starting new account activity.");
            startLoginActivity();
        }

        // Restore instance state
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(savedInstanceState == null){
            // If the activity is not being restored from an instance state, then there is no
            // fragment in view, so we must add one.
            previousEntriesFragment = new PreviousEntriesFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.content_frame, previousEntriesFragment, PREVIOUS_ENTRIES_FRAG_TAG)
                    .commit();

        } else{
            newImageFile = (File) savedInstanceState.getSerializable(STATE_NEW_IMAGE_FILE);
            previousEntriesFragment = (PreviousEntriesFragment)
                    fragmentManager.findFragmentByTag(PREVIOUS_ENTRIES_FRAG_TAG);
        }

        // BEGIN DRAWER STUFF
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

//                String[] tests = getResources().getStringArray(R.array.tests);
//                mTitle = tests[position];
//                testFragment tFragment = new testFragment();
//
//                Bundle data = new Bundle();
//                data.putInt("position", position);
//                tFragment.setArguments(data);
//
//                android.app.FragmentManager fragmentManager = getFragmentManager();
//                FragmentTransaction ft = fragmentManager.beginTransaction();
//                ft.replace(R.id.content_frame, tFragment);
//                ft.commit();

                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });
        // END DRAWER STUFF

        // Initialize the file cache
        fileCache = new FileCache(this);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_NEW_IMAGE_FILE, newImageFile);
    }

    private void startLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST);
        overridePendingTransition(R.animator.animation, R.animator.animation2);
    }

    /**
     * Dispatches the picture intent.
     */
    private void dispatchPictureIntent(){
        try {
            File imageFile = fileCache.createTempFile("SnackPhoto", ".jpg");
            this.newImageFile = imageFile;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));

            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            overridePendingTransition(R.animator.animation, R.animator.animation2);
        } catch (IOException e) {
            updateToast("Error accessing SD Card\nCheck that the SD card is mounted.", Toast.LENGTH_LONG);
            Log.e(TAG, e.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case LOGIN_REQUEST:

                if(resultCode == RESULT_OK){
                    updateToast("Log in successful!", Toast.LENGTH_SHORT);
                    previousEntriesFragment.getRemoteDataTaskFragment().restart();
                } else{
                    startLoginActivity();
                }

//                remoteDataTaskFragment.restart();
                break;
            case NEW_ENTRY_REQUEST:
                if(resultCode == RESULT_OK){
//                    remoteDataTaskFragment.restart();

                    previousEntriesFragment.getRemoteDataTaskFragment().restart();
                }
                break;
            case CAMERA_REQUEST:
                if(resultCode == RESULT_OK){
                    Intent intent = new Intent(this, NewEntryActivity.class);
                    intent.putExtra(NewEntryActivity.PHOTO_FILE_KEY, newImageFile);
                    startActivityForResult(intent, NEW_ENTRY_REQUEST);
                } else{
                    // Attempt to delete the empty image file
                    if(newImageFile != null){
                        if(newImageFile.delete()){
                            Log.d(TAG, "Unused image file successfully deleted.");
                        } else{
                            Log.d(TAG, "Unable to delete unused image file.");
                        }
                    }
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
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_logout).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                logout();
                break;
            case R.id.action_new:
//                Intent intent = new Intent(this, NewEntryActivity.class);
//                startActivityForResult(intent, NEW_ENTRY_REQUEST);
//                overridePendingTransition(R.animator.animation, R.animator.animation2);

                dispatchPictureIntent();
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
                    startLoginActivity();
                } else{
                    updateToast(e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        });
    }
}