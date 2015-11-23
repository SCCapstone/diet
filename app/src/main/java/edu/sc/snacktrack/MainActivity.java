package edu.sc.snacktrack;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ParseQuery;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int LOGIN_REQUEST = 1;
    private Toast toast;

    private List<SnackEntry> mySnacks = new ArrayList<SnackEntry>();

    ListView listview;
    List<ParseObject> ob;
    ProgressDialog mProgressDialog;
    CustomAdapter adapter;
    private List<SnackEntry> mySnackList = null;


//    ListView listview;
//    List<ParseObject> ob;
//    ProgressDialog mProgressDialog;
//    ArrayAdapter<String> adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If no user is logged in, start the new account activity.
        if(ParseUser.getCurrentUser() == null){
            startNewAccountActivity();
        }
        new RemoteDataTask().execute();
     //   populateList();
//        registerClickCallback();


    }

    // RemoteDataTask AsyncTask
    private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Accessing SnackTrack Database.");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Create the array
            mySnackList = new ArrayList<SnackEntry>();
            try {
                // Locate the class table named "TestObject" in Parse.com
                ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);
                query.whereEqualTo("owner", ParseUser.getCurrentUser());

               // ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
                 //       "SnackEntry");
                // Locate the column named "createdAt" in Parse.com and order list
                // by descending
                query.orderByDescending("createdAt"); //_created_at
                mySnackList = query.find();
//                ob = query.find();
//                for (ParseObject snack : ob) {
//                    // Locate images in flag column
//                    ParseFile image = (ParseFile) snack.get("photo");
//
//                    SnackEntry snackEntry = new SnackEntry();
//                    snackEntry.setDescription(snackEntry.getDescription());
//                    snackEntry.setTypeOfMeal((String) snack.get("mealType"));
//                    snackEntry.setPhoto(image);
//                    mySnackList.add(snackEntry);
//                }
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Locate the listview in listview_main.xml
            listview = (ListView) findViewById(R.id.SnackList);
            // Pass the results into ListViewAdapter.java
            adapter = new CustomAdapter(MainActivity.this,
                    mySnackList);
            // Binds the Adapter to the ListView
            listview.setAdapter(adapter);
            // Close the progressdialog
            mProgressDialog.dismiss();
        }
    }
//private void registerClickCallback(){
//    ListView list = (ListView) findViewById(R.id.SnackList);
//list.setOnItemClickListener(new AdaptervView.OnItemClickListener() {
//    @override
//    public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
//        TextView textView = (TextView) viewClicked;
//        String message = "You clicked # " + position + " " + textView.getText().toString();
//        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
//    }
//});
//}
    private void populateList(){
        String[] myItems = {"Bill","Bob","Joyce", "Gail"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.snack_entry,myItems);

        ListView list = (ListView) findViewById(R.id.SnackList);
        list.setAdapter(adapter);
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