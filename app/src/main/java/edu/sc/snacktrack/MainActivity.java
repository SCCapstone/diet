package edu.sc.snacktrack;

import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import edu.sc.snacktrack.chat.ChatActivity;
import edu.sc.snacktrack.chat.ChatChooserFragment;
import edu.sc.snacktrack.chat.Conversations;

public class MainActivity extends AppCompatActivity{

    private static final int NEW_ENTRY_REQUEST = 2;
    private static final int CAMERA_REQUEST = 3;

    private static final String STATE_NEW_IMAGE_FILE = "newImageFile";
    private static final String STATE_CURRENT_TITLE = "currentTitle";
    private static final String STATE_MTITLE = "mTitle";

    private static final String CURRENT_FRAGMENT_TAG = "mainActivityCurrentFragment";

    private SnackTrackApplication application;

    private Toast toast;

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private RelativeLayout mRelativeLayout;
    private TextView footerDietitian;
    private TextView footerUsername;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle = "";
    private String[] drawerItems;

    private static final int BF_ALARM_REQUEST = 1;
    private static final int LUN_ALARM_REQUEST = 2;
    private static final int DIN_ALARM_REQUEST = 3;
//    private static final int TEST_ALARM_REQUEST = 4;

    private FileCache fileCache;

    private File newImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = (SnackTrackApplication) getApplicationContext();

        // Ensure current user's SnackList is displayed first
        SnackList.getInstance().setUser(ParseUser.getCurrentUser());

        // BEGIN DRAWER STUFF
        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.drawer_relative);
        footerDietitian = (TextView) findViewById(R.id.drawerFooter1);
//        footerUsername = (TextView) findViewById(R.id.drawerFooter2);
//            footerUsername.setText("My Username: " + ParseUser.getCurrentUser().getUsername());

        /**
         * condition if currentUser is client or dietitian
         */
        if(ParseUser.getCurrentUser().getBoolean("isDietitian") == true)
        {
            drawerItems = getResources().getStringArray(R.array.main_drawer_items);
            footerDietitian.setVisibility(View.INVISIBLE);
            Log.i("Testing","isDietitian = true");
        }

        else
        {
            drawerItems = getResources().getStringArray(R.array.main_drawer_items_2);
            //ParseUser myDietitian = ParseUser.getCurrentUser().getParseUser("myDietitian").fetchIfNeeded();
            String uName = "";
            try
            {
                //ParseUser myDietitian = ParseUser.getCurrentUser().getParseUser
//                uName = ParseUser.getCurrentUser().fetchIfNeeded().getParseUser("myDietitian");
                Log.i("Testing","myDietitian is " + uName);
            }

            catch(Exception e) {
                Log.i("Testing","Something has gone wrong fetching myDietitian");
            }
        }

        //drawerItems = getResources().getStringArray(R.array.main_drawer_items);
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
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
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

            // Restore fragment state
        }

        checkChatPushData();
    }

    /**
     * Checks if this MainActivity was started from a chat push notification. If so, starts
     * ChatActivity with the user specified in the notification's JSON Object.
     */
    private void checkChatPushData(){
        if(getIntent() != null){
            boolean startChat = getIntent().getBooleanExtra("isChat", false);

            Log.d(TAG, "isChat " + startChat);

            if(startChat){
                Intent startChatIntent = new Intent(MainActivity.this, ChatActivity.class);
                String otherUserId = getIntent().getStringExtra("fromUserId");
                String otherUserName = getIntent().getStringExtra("fromUserName");
                startChatIntent.putExtra(ChatActivity.OTHER_USER_ID_KEY, otherUserId);
                startChatIntent.putExtra(ChatActivity.OTHER_USER_NAME_KEY, otherUserName);
                startActivity(startChatIntent);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If no user is logged in, start the login activity.
        if(ParseUser.getCurrentUser() == null){
            Log.d(TAG, "No user is logged in, starting new account activity.");
            startLoginActivity();
        } else{
            // Associate the device with the current user
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("user",ParseUser.getCurrentUser());
            installation.saveInBackground();
        }

        SnackList.getInstance().setUser(ParseUser.getCurrentUser());
        if(SnackList.getInstance().size() == 0){
            SnackList.getInstance().refresh(new FindCallback<SnackEntry>() {
                @Override
                public void done(List<SnackEntry> objects, ParseException e) {
                    if (e != null) {
                        updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                    } else
                        setAlarms(objects);
                }
            });
        }

        // Update conversations if needed
        if(!Conversations.getInstance().isUpdating() && Conversations.getInstance().needsRefresh()){
            Conversations.getInstance().refresh(null);
        }

        application.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        clearReferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearReferences();
    }

    private void clearReferences(){
        Activity currentActivity = application.getCurrentActivity();
        if(this == currentActivity){
            application.setCurrentActivity(null);
        }
    }

    /**
     * Displays a fragment based on a position selected from the navigation drawer.
     *
     * @param position The position on the navigation drawer
     */
    private void displayView(int position){

        Fragment fragment = null;

        if(ParseUser.getCurrentUser().getBoolean("isDietitian") == true)
        {
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

                case 3:
                    fragment = new ChatChooserFragment();
//                Bundle args = new Bundle();
//                args.putString(ChatFragment.ARG_OTHER_USER_ID, "Audel3iEFb");
//                fragment.setArguments(args);
                    break;

                default:
                    fragment = new TestFragment();
                    Bundle data = new Bundle();
                    data.putInt("position", position);
                    fragment.setArguments(data);
                    break;
            }
        }

        else
        {
            switch(position){
                case 0:
                    fragment = new PreviousEntriesFragment();
                    SnackList.getInstance().setUser(ParseUser.getCurrentUser());
                    SnackList.getInstance().refresh(null);
                    break;

                case 1:
                    fragment = new SettingsFragment();
                    break;

                case 2:
                    fragment = new ChatChooserFragment();
//                Bundle args = new Bundle();
//                args.putString(ChatFragment.ARG_OTHER_USER_ID, "Audel3iEFb");
//                fragment.setArguments(args);
                    break;

                default:
                    fragment = new TestFragment();
                    Bundle data = new Bundle();
                    data.putInt("position", position);
                    fragment.setArguments(data);
                    break;
            }
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
//        mDrawerLayout.closeDrawer(mDrawerList);
        mDrawerLayout.closeDrawer(mRelativeLayout);
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
        startActivity(intent);
        finish();
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
//            case LOGIN_REQUEST:
//                // If successful login, refresh SnackLists and attempt to set reminder alarms
//                if(resultCode == RESULT_OK){
//                    ClientList.getInstance().refresh(new FindCallback<ParseUser>() {
//                        @Override
//                        public void done(List<ParseUser> objects, ParseException e) {
//                            if (e != null) {
//                                updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
//                            }
//                        }
//                    });
//
//                    SnackList.getInstance().setUser(ParseUser.getCurrentUser());
//                    SnackList.getInstance().refresh(new FindCallback<SnackEntry>() {
//                        @Override
//                        public void done(List<SnackEntry> objects, ParseException e) {
//                            if (e != null) {
//                                updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
//                            }
//
//                            else
//                                setAlarms(objects);
//                        }
//                    });
//
//                    updateToast("Log in successful!", Toast.LENGTH_SHORT);
//
//                } else{
//                    startLoginActivity();
//                }
//
//                break;
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

    private void setAlarms(List<SnackEntry> objects) {

        // TODO: If current user's SnackList is empty, set a daily reminder asking them to submit an entry (and make this notification optional in settings).
        if(objects.size() == 0)
            Log.i("Testing","Error: Current user's SnackList is empty or there was a problem fetching entries. [No reminder alarms set]");

        // If current user's SnackList is not empty, set alarms for the day to remind user to post entries.
        else
        {
            // Initialize intents
            Intent intent = new Intent(this, ReminderReceiver.class);
                PendingIntent bfSender = PendingIntent.getBroadcast(this, BF_ALARM_REQUEST, intent, 0);
                PendingIntent lunSender = PendingIntent.getBroadcast(this, LUN_ALARM_REQUEST, intent, 0);
                PendingIntent dinSender = PendingIntent.getBroadcast(this, DIN_ALARM_REQUEST, intent, 0);

//                PendingIntent testSender = PendingIntent.getBroadcast(this, TEST_ALARM_REQUEST, intent, 0);

            // Breakfast period reminder
            long _bfTime = 0;
            Calendar bfTime = Calendar.getInstance();
                bfTime.setTimeInMillis(System.currentTimeMillis());
                bfTime.set(Calendar.HOUR_OF_DAY, 10);
                bfTime.set(Calendar.MINUTE, 30);
                bfTime.set(Calendar.SECOND, 00);
                bfTime.set(Calendar.AM_PM, Calendar.AM);

                if(bfTime.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
                    _bfTime = bfTime.getTimeInMillis() + (AlarmManager.INTERVAL_DAY + 1);

                else
                    _bfTime = bfTime.getTimeInMillis();

                AlarmManager bfAlarm = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
                bfAlarm.setRepeating(AlarmManager.RTC_WAKEUP, _bfTime, AlarmManager.INTERVAL_DAY, bfSender);

            // Lunch period reminder
            long _lunTime = 0;
            Calendar lunTime = Calendar.getInstance();
                lunTime.setTimeInMillis(System.currentTimeMillis());
                lunTime.set(Calendar.HOUR_OF_DAY, 3);
                lunTime.set(Calendar.MINUTE, 30);
                lunTime.set(Calendar.SECOND, 00);
                lunTime.set(Calendar.AM_PM, Calendar.PM);

                if(lunTime.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
                    _lunTime = lunTime.getTimeInMillis() + (AlarmManager.INTERVAL_DAY + 1);

                else
                    _lunTime = lunTime.getTimeInMillis();

                AlarmManager lunAlarm = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
                lunAlarm.setRepeating(AlarmManager.RTC_WAKEUP, _lunTime, AlarmManager.INTERVAL_DAY, lunSender);

            // Dinner period reminder
            long _dinTime = 0;
            Calendar dinTime = Calendar.getInstance();
                dinTime.setTimeInMillis(System.currentTimeMillis());
                dinTime.set(Calendar.HOUR_OF_DAY, 9);
                dinTime.set(Calendar.MINUTE, 30);
                dinTime.set(Calendar.SECOND, 00);
                dinTime.set(Calendar.AM_PM, Calendar.PM);

                if(dinTime.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
                    _dinTime = dinTime.getTimeInMillis() + (AlarmManager.INTERVAL_DAY + 1);

                else
                    _dinTime = dinTime.getTimeInMillis();

                AlarmManager dinAlarm = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
                dinAlarm.setRepeating(AlarmManager.RTC_WAKEUP, _dinTime, AlarmManager.INTERVAL_DAY, dinSender);

//************************************************* TEST CASE ********************************************************
//            long _testTime = 0;
//            Calendar testTime = Calendar.getInstance();
//            testTime.setTimeInMillis(System.currentTimeMillis());
//            testTime.set(Calendar.HOUR_OF_DAY, 9);
//            testTime.set(Calendar.MINUTE, 37);
//            testTime.set(Calendar.SECOND, 00);
//            testTime.set(Calendar.AM_PM, Calendar.PM);
//
//            if(testTime.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
//                _testTime = testTime.getTimeInMillis() + (AlarmManager.INTERVAL_DAY + 1);
//
//            else
//                _testTime = testTime.getTimeInMillis();
//
//            AlarmManager testAlarm = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
//            testAlarm.setRepeating(AlarmManager.RTC_WAKEUP, _testTime, AlarmManager.INTERVAL_FIFTEEN_MINUTES, testSender);
//************************************************** TEST CASE *******************************************************
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