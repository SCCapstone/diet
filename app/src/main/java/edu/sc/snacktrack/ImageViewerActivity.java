package edu.sc.snacktrack;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

/**
 * Activity for viewing a full-sized version of a single image.
 */
public class ImageViewerActivity extends AppCompatActivity{

    private static final String TAG = "ImageViewerActivity";

    public static final String FILE_PATH_KEY = "filePath";

    private static final String STATE_BITMAP = "bitmap";

    private ImageView imageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getSupportActionBar() != null){
                    if(getSupportActionBar().isShowing()){
                        getSupportActionBar().hide();
                    } else{
                        getSupportActionBar().show();
                    }
                }
            }
        });

        if(savedInstanceState != null){
            bitmap = savedInstanceState.getParcelable(STATE_BITMAP);
        } else{
            bitmap = null;
        }

        if(bitmap == null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String filePath = getIntent().getStringExtra(FILE_PATH_KEY);

                    // Load full size bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                    // Rotate based on exif data
                    int rotation;
                    try{
                        rotation = Utils.getExifRotation(filePath);
                    } catch(IOException e){
                        rotation = 0;
                    }
                    Matrix matrix = new Matrix();
                    matrix.preRotate(rotation);

                    // Get bitmap with the correct rotation applied.
                    ImageViewerActivity.this.bitmap = Bitmap.createBitmap(
                            bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
                    );

                    // Display the image
                    ImageViewerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setAlpha(0f);
                            imageView.setImageBitmap(ImageViewerActivity.this.bitmap);
                            imageView.animate()
                                    .alpha(1f)
                                    .setDuration(500);
                        }
                    });
                }
            }).start();
        } else{
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_BITMAP, bitmap);
    }
}