package edu.sc.snacktrack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Activity for viewing a full-sized version of a single image.
 */
public class ImageViewerActivity extends AppCompatActivity{

    private static final String TAG = "ImageViewerActivity";

    public static final String FILE_PATH_KEY = "filePath";

    private static final String STATE_IMAGE_VIEW = "imageView";

    private SubsamplingScaleImageView imageView;

    private View progressOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize progress overlay
        progressOverlay = findViewById(R.id.progressOverlay);

        // Initialize image view
        imageView = (SubsamplingScaleImageView) findViewById(R.id.imageView);
        imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        imageView.setAlpha(0f); // for animation later

        // Hide/show the action bar when the image is tapped.
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportActionBar() != null) {
                    if (getSupportActionBar().isShowing()) {
                        getSupportActionBar().hide();
                    } else {
                        getSupportActionBar().show();
                    }
                }
            }
        });

        // Show fade-in animation when image loading is complete.
        imageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
            @Override
            public void onImageLoaded() {
                imageView.animate().alpha(1f).setDuration(500).setStartDelay(200);
                progressOverlay.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        progressOverlay.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onImageLoadError(Exception e) {
                progressOverlay.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        progressOverlay.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onReady() {
            }

            @Override
            public void onTileLoadError(Exception e) {
            }

            @Override
            public void onPreviewLoadError(Exception e) {
            }
        });

        // Load image in background and show progress overlay
        imageView.setImage(ImageSource.uri(getIntent().getStringExtra(FILE_PATH_KEY)));
        progressOverlay.setAlpha(0f);
        progressOverlay.setVisibility(View.VISIBLE);
        progressOverlay.animate().alpha(1f).setDuration(500).setStartDelay(200);
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
        outState.putSerializable(STATE_IMAGE_VIEW, imageView.getState());
    }
}
