package edu.sc.snacktrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private static final int LOGIN_REQUEST = 1;
    private static final int NEW_ENTRY_REQUEST = 2;
    private static final int CAMERA_REQUEST = 3;

    private static final String STATE_NEW_IMAGE_FILE = "newImageFile";
    private static final String STATE_CURRENT_TITLE = "currentTitle";
    private static final String STATE_MTITLE = "mTitle";

    private static final String CURRENT_FRAGMENT_TAG = "mainActivityCurrentFragment";

    private Toast toast;

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle = "";
    private String[] drawerItems;
    private Boolean disableEntryFlag = false;

    private static final String ALARM_ACTION_NAME = "edu.sc.snacktrack.broadcast.ALARM";

    private FileCache fileCache;

    private File newImageFile;

    private static final String ARGUMENT_PRODUCT_ID = "product_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //////////////////////setAlarms();
        setContentView(R.layout.activity_main);

        // If no user is logged in, start the login activity.
        if(ParseUser.getCurrentUser() == null){
            Log.d(TAG, "No user is logged in, starting new account activity.");
            startLoginActivity();
        }

        // Ensure current user's SnackList is displayed first
        SnackList.getInstance().setUser(ParseUser.getCurrentUser());

        // BEGIN DRAWER STUFF
        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        /**
         * condition if currentUser is client or dietitian
         */
        drawerItems = getResources().getStringArray(R.array.main_drawer_items);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
                disableEntryFlag = false;
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Select an option");
                invalidateOptionsMenu();
                disableEntryFlag = true;
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.drawer_list_item, drawerItems);

        mDrawerList.setAdapter(adapter);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                displayView(position);
            }
        });
        // END DRAWER STUFF

        // Initialize the file cache
        fileCache = new FileCache(this);

        // Restore instance state
        if(savedInstanceState == null){
            // If the activity is not being restored from an instance state, then there is no
            // fragment in view, so we must add one.
            displayView(0);

        } else{
            newImageFile = (File) savedInstanceState.getSerializable(STATE_NEW_IMAGE_FILE);
            mTitle = savedInstanceState.getCharSequence(STATE_MTITLE, getTitle());
            getSupportActionBar().setTitle(savedInstanceState.getCharSequence(STATE_CURRENT_TITLE, getTitle()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(SnackList.getInstance().size() == 0){
            SnackList.getInstance().refresh(new FindCallback<SnackEntry>() {
                @Override
                public void done(List<SnackEntry> objects, ParseException e) {
                    if(e != null){
                        updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                    }
                }
            });
        }
    }

    /**
     * Displays a fragment based on a position selected from the navigation drawer.
     *
     * @param position The position on the navigation drawer
     */
    private void displayView(int position){

        Fragment fragment = null;

        switch(position){
            case 0:
                fragment = new PreviousEntriesFragment();
                SnackList.getInstance().setUser(ParseUser.getCurrentUser());
                SnackList.getInstance().refresh(null);
                break;

            case 1:
                fragment = new DisplayClientsFragment();
                ClientList.getInstance().refresh(null);
                break;

            case 2:
                fragment = new SettingsFragment();
                break;

            default:
                fragment = new TestFragment();
                Bundle data = new Bundle();
                data.putInt("position", position);
                fragment.setArguments(data);
                break;
        }

        if(fragment != null){
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.content_frame, fragment, CURRENT_FRAGMENT_TAG)
                    .commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(drawerItems[position]);

        } else{
            updateToast("Something went wrong with the drawer :/", Toast.LENGTH_LONG);
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle = title;
        getSupportActionBar().setTitle(title);
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
        outState.putCharSequence(STATE_CURRENT_TITLE, getSupportActionBar().getTitle());
        outState.putCharSequence(STATE_MTITLE, mTitle);
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
                    ClientList.getInstance().refresh(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if (e != null) {
                                updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                            }
                        }
                    });

                    SnackList.getInstance().setUser(ParseUser.getCurrentUser());
                    SnackList.getInstance().refresh(new FindCallback<SnackEntry>() {
                        @Override
                        public void done(List<SnackEntry> objects, ParseException e) {
                            if(e != null){
                                updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                            }
                        }
                    });

                    //Start AlarmManager if successful login
                    setAlarms();



                    //if time is between

                    updateToast("Log in successful!", Toast.LENGTH_SHORT);

                } else{
                    startLoginActivity();
                }

                break;
            case NEW_ENTRY_REQUEST:

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

        if(disableEntryFlag)
        {
            menu.findItem(R.id.action_new).setEnabled(false);
            menu.findItem(R.id.action_new).setVisible(false);
        }
        else
        {
            menu.findItem(R.id.action_new).setEnabled(true);
            menu.findItem(R.id.action_new).setVisible(true);
        }

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

    private void setAlarms() {

        AlarmManager amTest = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        //Breakfast period reminder
        Calendar breakfastTime = Calendar.getInstance();
        breakfastTime.set(Calendar.HOUR_OF_DAY, 10);
        breakfastTime.set(Calendar.MINUTE, 30);

        //Lunch period reminder
        Calendar lunchTime = Calendar.getInstance();
        lunchTime.set(Calendar.HOUR_OF_DAY, 15);
        lunchTime.set(Calendar.MINUTE, 30);

        //Dinner period reminder
        Calendar dinnerTime = Calendar.getInstance();
        dinnerTime.set(Calendar.HOUR_OF_DAY, 21);
        dinnerTime.set(Calendar.MINUTE, 30);

        //Testing reminder
        Calendar testing = Calendar.getInstance();
        testing.setTimeInMillis(System.currentTimeMillis());
        Log.i("Testing", "Initial time set: " + testing.getTimeInMillis());
        testing.set(Calendar.HOUR_OF_DAY, 15);
        testing.set(Calendar.MINUTE, 25);
        testing.set(Calendar.SECOND, 00);
        //testing.set(Calendar.AM_PM, Calendar.PM);
        Log.i("Testing", "Time set to go off: " + testing.getTimeInMillis());
//        long trigTime = testing.getTimeInMillis();
//        Toast.makeText(this, "testing time set is: " + trigTime + "\ncurrent time is: " + Calendar.getInstance().getTimeInMillis(), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "testing date set is: " + testing.getTimeZone() + "\ncurrent zone is: " + Calendar.getInstance().getTimeZone(), Toast.LENGTH_LONG).show();


        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        //Test alarm
        amTest.setRepeating(AlarmManager.RTC_WAKEUP, testing.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
//        amTest.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigTime, AlarmManager.INTERVAL_DAY, sender);

        //AlarmManager am1 = (AlarmManager) getSystemService(ALARM_SERVICE);
        //am1.set(AlarmManager.RTC_WAKEUP, breakfastTime.getTimeInMillis(), sender);
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
                    displayView(0);
                    startLoginActivity();
                } else{
                    updateToast(e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        });
    }
}