package edu.sc.snacktrack;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class NewEntryActivity extends AppCompatActivity {

    private static final String TAG = "NewEntryDebug";

    private Toast toast;

    private TextView description;
    private ImageView imageView;
    private Spinner spinner;

    private String currentPhotoPath;
    private String newPhotoPath;

    private static final int DESCRIPTION_CHANGE_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    private static final int PREVIEW_WIDTH = 100;
    private static final int PREVIEW_HEIGHT = 100;

    private static final String STATE_DESCRIPTION_STRING = "descriptionString";
    private static final String STATE_CURRENT_PHOTO_PATH = "currentPhotoPath";
    private static final String STATE_NEW_PHOTO_PATH = "newPhotoPath";

    private PhotoPreviewLoader photoPreviewLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        spinner = (Spinner) findViewById(R.id.meal_type_spinner);

        // Set up the spinner
        spinner.setAdapter(ArrayAdapter.createFromResource(
                this, R.array.meal_types, android.R.layout.simple_spinner_dropdown_item
        ));

        // Set up the description box
        description = (TextView) findViewById(R.id.descriptionTextView);
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewEntryActivity.this, EditDescriptionActivity.class);
                intent.putExtra(EditDescriptionActivity.DESCRIPTION_STRING_KEY, description.getText().toString());
                startActivityForResult(intent, DESCRIPTION_CHANGE_CODE);
            }
        });

        // Set up the image view
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPictureIntent();
            }
        });

        // Restore instance state
        if(savedInstanceState != null){
            description.setText(savedInstanceState.getString(STATE_DESCRIPTION_STRING, ""));

            this.currentPhotoPath = savedInstanceState.getString(STATE_CURRENT_PHOTO_PATH, null);
            this.newPhotoPath = savedInstanceState.getString(STATE_NEW_PHOTO_PATH, null);

            if(currentPhotoPath != null){
                loadPhotoPreview(currentPhotoPath);
            }
        }

        // If a photo has not been taken, start the camera app
        if(newPhotoPath == null){
            dispatchPictureIntent();
        }
    }

    /**
     * Dispatches the picture intent. If an IOException occurs, sets result to RESULT_CANCELED
     * and finishes this activity.
     */
    private void dispatchPictureIntent(){
        try {
            File imageFile = createImageFile();
            this.newPhotoPath = imageFile.getAbsolutePath();

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));

            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        } catch (IOException e) {
            Toast.makeText(
                    this,
                    "Error accessing SD Card.\nCheck that the SD card is mounted.",
                    Toast.LENGTH_LONG
            ).show();
            Log.e(TAG, e.getMessage());

            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Attempts to creates a new, empty image file.
     *
     * @return The empty image file
     * @throws IOException
     */
    private File createImageFile() throws IOException{

        String imageFilePrefix = "SnackPhoto";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // If the storage directory does not exist, attempt to make it.
        if(storageDir != null){
            if(!storageDir.exists()){
                if(!storageDir.mkdirs()){
                    throw new IOException("Could not create storage directory " + storageDir.getAbsolutePath());
                }
            }
        } else{
            throw new IOException("Could not create storage directory");
        }

        File imageFile = File.createTempFile(
                imageFilePrefix,  /* prefix */
                ".jpg",           /* suffix */
                storageDir        /* directory */
        );

        Log.d(TAG, "New image file path: " + imageFile.getAbsolutePath());

        return imageFile;
    }

    /**
     * Converts exif attribute to a rotation in degrees.
     *
     * @param exifOrientation The orientation attribute
     * @return
     */
    private int exifToDegrees(int exifOrientation){
        switch(exifOrientation){
            case ExifInterface.ORIENTATION_ROTATE_90: return 90;
            case ExifInterface.ORIENTATION_ROTATE_180: return 180;
            case ExifInterface.ORIENTATION_ROTATE_270: return 270;
            default: return 0;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == DESCRIPTION_CHANGE_CODE){
            if(resultCode == RESULT_OK){
                String newText = data.getStringExtra(EditDescriptionActivity.DESCRIPTION_STRING_KEY);
                if(newText != null){
                    description.setText(newText);
                }
            }
        } else if(requestCode == CAMERA_REQUEST_CODE){

            if(resultCode == RESULT_OK){
                //Image capture successful

                this.currentPhotoPath = this.newPhotoPath;
                loadPhotoPreview(currentPhotoPath);
            } else if(resultCode == RESULT_CANCELED){
                // Image capture canceled

                // If the user never took a photo, leave this activity.
                if(currentPhotoPath == null){
                    setResult(RESULT_CANCELED);
                    finish();
                }

                if(newPhotoPath != null) {
                    if (new File(newPhotoPath).delete()) {
                        Log.d(TAG, "Unused image file deleted.");
                    } else {
                        Log.d(TAG, "Could not delete unused image file.");
                    }
                }

                newPhotoPath = null;
            } else{
                updateToast("Image capture failed.", Toast.LENGTH_SHORT);
                // Image capture failed.
            }
        }
    }

    /**
     * Asynchronously loads a scaled-down preview of an image and displays it in imageView.
     * The preview is scaled based on PREVIEW_WIDTH and PREVIEW_HEIGHT
     *
     * @param photoPath Path to the image to preview
     */
    private void loadPhotoPreview(String photoPath){

        if(new File(photoPath).exists()){
            if(photoPreviewLoader != null){
                photoPreviewLoader.cancel(true);
            }
            photoPreviewLoader = new PhotoPreviewLoader(photoPath);
            photoPreviewLoader.execute();
        } else{
            updateToast("Unable to load preview image.", Toast.LENGTH_SHORT);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_DESCRIPTION_STRING, description.getText().toString());
        outState.putString(STATE_CURRENT_PHOTO_PATH, currentPhotoPath);
        outState.putString(STATE_NEW_PHOTO_PATH, newPhotoPath);
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

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(photoPreviewLoader != null){
            photoPreviewLoader.cancel(true);
        }
    }

    /**
     * Asynchronously loads a scaled-down preview of an image and displays it in imageView.
     * The preview is scaled based on PREVIEW_WIDTH and PREVIEW_HEIGHT
     */
    private class PhotoPreviewLoader extends AsyncTask<String, Void, Bitmap>{

        private String photoPath;

        public PhotoPreviewLoader(String photoPath){
            this.photoPath = photoPath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            imageView.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... params){
            final int targetWidth = PREVIEW_WIDTH;
            final int targetHeight = PREVIEW_HEIGHT;

            // Get the width and height of the full-sized image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoPath, options);
            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            // Calculate a reasonable sample size.
            int scaleSampleSize = Math.min(originalWidth/targetWidth, originalHeight/targetHeight);

            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleSampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

            // Check the image's EXIF data and rotate the preview if necessary.
            ExifInterface exif;
            try{
                exif = new ExifInterface(photoPath);
            } catch(IOException e){
                return null;
            }

            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationDeg = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            matrix.preRotate(rotationDeg);

            // Return a bitmap with the correct rotation applied.
            return Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
            );

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if(bitmap != null){
                imageView.setImageBitmap(bitmap);
            } else{
                updateToast("Unable to load preview image", Toast.LENGTH_SHORT);
                imageView.setImageResource(R.drawable.ic_photo_camera_black_24dp);
            }
        }

        @Override
        protected void onCancelled(){
            super.onCancelled();
            imageView.setImageResource(R.drawable.ic_photo_camera_black_24dp);
        }
    }
}