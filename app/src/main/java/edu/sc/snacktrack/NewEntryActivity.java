package edu.sc.snacktrack;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class NewEntryActivity extends AppCompatActivity {

    private static final String TAG = "NewEntryDebug";

    private TextView description;

    private static final int DESCRIPTION_CHANGE_REQUEST = 1;

    private static final String STATE_DESCRIPTION_STRING = "descriptionString";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

//        spinner = (Spinner) findViewById(R.id.spinner);

        // Set up the spinner
//        spinner.setAdapter(ArrayAdapter.createFromResource(
//                        this, R.array.meal_types, android.R.layout.simple_spinner_dropdown_item
//        ));

        description = (TextView) findViewById(R.id.descriptionTextView);
        description.setMovementMethod(new ScrollingMovementMethod());
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewEntryActivity.this, EditDescriptionActivity.class);
                intent.putExtra(EditDescriptionActivity.DESCRIPTION_STRING_KEY, description.getText().toString());
                startActivityForResult(intent, DESCRIPTION_CHANGE_REQUEST);
            }
        });

        // Restore instance state
        if(savedInstanceState != null){
            description.setText(savedInstanceState.getString(STATE_DESCRIPTION_STRING, ""));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == DESCRIPTION_CHANGE_REQUEST){
            if(resultCode == RESULT_OK){
                Log.d(TAG, "OK");
                String newText = data.getStringExtra(EditDescriptionActivity.DESCRIPTION_STRING_KEY);
                if(newText != null){
                    description.setText(newText);
                }
            } else{
                Log.d(TAG, "NOT OK");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_DESCRIPTION_STRING, description.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_entry, menu);
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
}
