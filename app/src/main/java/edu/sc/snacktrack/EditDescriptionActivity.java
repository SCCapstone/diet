package edu.sc.snacktrack;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditDescriptionActivity extends AppCompatActivity {

    private EditText editText;

    public static final String DESCRIPTION_STRING_KEY = "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_description);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editText = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        String currentText = intent.getStringExtra(DESCRIPTION_STRING_KEY);
        if(currentText != null) {
            editText.setText(currentText);

            // Move the cursor to the end.
            editText.setSelection(currentText.length());
        }

        // Restore instance state
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_description, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_done:
                Intent intent = new Intent();
                intent.putExtra(DESCRIPTION_STRING_KEY, editText.getText().toString());
                setResult(RESULT_OK, intent);
                Utils.closeSoftKeyboard(this, editText);
                finish();
                return true;
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                Utils.closeSoftKeyboard(this, editText);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
