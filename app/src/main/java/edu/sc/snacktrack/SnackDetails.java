package edu.sc.snacktrack;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SnackDetails extends AppCompatActivity {

    public static final String DESCRIPTION_KEY = "description";
    public static final String MEAL_TYPE_KEY = "mealType";
    public static final String PHOTO_URL_KEY = "photoURL";

    private ImageView imageView;
    private TextView descriptionTextView;
    private TextView mealTypeTextView;

    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snack_details);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent data = getIntent();

        String photoURL = data.getStringExtra(PHOTO_URL_KEY);
        String description = data.getStringExtra(DESCRIPTION_KEY);
        String mealType = data.getStringExtra(MEAL_TYPE_KEY);

        imageLoader = new ImageLoader(this);

        imageView = (ImageView) findViewById(R.id.imageView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        mealTypeTextView = (TextView) findViewById(R.id.mealTypeTextView);

        imageLoader.DisplayImage(photoURL, imageView);
        descriptionTextView.setText(description != null ? description : "No description");
        mealTypeTextView.setText(mealType != null ? mealType : "No meal type");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
