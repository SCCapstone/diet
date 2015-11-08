package edu.sc.snacktrack;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraTestActivity extends AppCompatActivity {

    private static final String TAG = "CameraTestDebug";

    private static final int CAMERA_REQUEST_CODE = 1;

    private Toast toast;

    private ImageView imageView;

    private String newPhotoPath;
    private String currentPhotoPath;

    private Button saveButton;

    private static final String STATE_CURRENT_PHOTO_PATH = "currentPhotoPath";
    private static final String STATE_NEW_PHOTO_PATH = "newPhotoPath";

    private static final int PREVIEW_PRESSED_TINT = Color.argb(100, 255, 255, 255);
    private static final int PREVIEW_WIDTH = 100;
    private static final int PREVIEW_HEIGHT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);

        // Enable back button on action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPictureIntent();
            }
        });


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        imageView.setColorFilter(PREVIEW_PRESSED_TINT);
                        break;
                    case MotionEvent.ACTION_UP:
                        imageView.setColorFilter(null);
                        imageView.refreshDrawableState();
                        break;
                }

                return false;
            }
        });

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentPhoto();
            }
        });

        // Restore instance state
        if(savedInstanceState != null){
            currentPhotoPath = savedInstanceState.getString(STATE_CURRENT_PHOTO_PATH, null);
            newPhotoPath = savedInstanceState.getString(STATE_NEW_PHOTO_PATH, null);

            // If there is an image to restore, restore it.
            if(currentPhotoPath != null){
                loadPhotoPreview();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString(STATE_CURRENT_PHOTO_PATH, currentPhotoPath);
        savedInstanceState.putString(STATE_NEW_PHOTO_PATH, newPhotoPath);
    }

    private void dispatchPictureIntent(){
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File imageFile = createImageFile();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Enables or disables all user input widgets.
     *
     * @param enabled true to enable; false to disable
     */
    private void setWidgetsEnabled(boolean enabled){
        saveButton.setEnabled(enabled);
        imageView.setEnabled(enabled);
    }

    private void saveCurrentPhoto(){
        setWidgetsEnabled(false);
        saveButton.setText(R.string.uploading);

        // First, check if there is a photo to upload.
        File localFile;
        if(currentPhotoPath != null){
            localFile = new File(currentPhotoPath);
            if(!localFile.exists()){
                updateToast("No photo to upload (file does not exist)", Toast.LENGTH_SHORT);
                setWidgetsEnabled(true);
                saveButton.setText(R.string.upload);
                return;
            }
        } else{
            updateToast("No photo to upload (path is null)", Toast.LENGTH_SHORT);
            setWidgetsEnabled(true);
            saveButton.setText(R.string.upload);
            return;
        }

        final ParseFile parseFile = new ParseFile(localFile);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){

                    ParseObject testPhoto = new ParseObject("TestPhoto");
                    testPhoto.put("photo", parseFile);
                    testPhoto.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                updateToast("Upload complete", Toast.LENGTH_SHORT);
                            } else{
                                updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
                            }
                            setWidgetsEnabled(true);
                            saveButton.setText(R.string.upload);
                        }
                    });
                } else{
                    updateToast(Utils.getErrorMessage(e), Toast.LENGTH_SHORT);
                    setWidgetsEnabled(true);
                    saveButton.setText(R.string.upload);
                }
            }
        }, new ProgressCallback() {

            @Override
            public void done(Integer percentDone) {
                // TODO: Show progress in percentage form
            }
        });

//        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
//
//        if(bitmap == null){
//            updateToast("No image to save", Toast.LENGTH_SHORT);
//            return;
//        }
//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] imageBytes = stream.toByteArray();
//
//        final ParseFile parseFile = new ParseFile("TestPhoto.png", imageBytes);
//        parseFile.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                ParseObject testPhoto = new ParseObject("TestPhoto");
//                testPhoto.put("photo", parseFile);
//                testPhoto.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        Log.d(TAG, "Photo saved to parse.");
//                    }
//                });
//            }
//        }, new ProgressCallback() {
//            @Override
//            public void done(Integer percentDone) {
//                Log.d(TAG, "Saving photo progress: " + percentDone + "%");
//            }
//        });
    }


    private File createImageFile() throws IOException{
        // Create an image file name based on the current date

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save the path to this file
        newPhotoPath = imageFile.getPath();
        Log.d(TAG, "New image file path: " + newPhotoPath);

        return imageFile;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                // Image capture successful
                Log.d(TAG, "Image capture successful.");

                currentPhotoPath = newPhotoPath;
                loadPhotoPreview();

            } else if(resultCode == RESULT_CANCELED){
                // Image capture canceled
                Log.d(TAG, "Image capture canceled.");

            } else{
                // Image capture failed
                Log.d(TAG, "Image capture failed.");
                updateToast("Image capture failed", Toast.LENGTH_SHORT);

            }
        }
    }

    /**
     * Asynchronously loads a scaled-down preview of the current photo and displays it in imageView.
     * The preview is scaled based on PREVIEW_WIDTH and PREVIEW_HEIGHT
     */
    private void loadPhotoPreview(){

        new AsyncTask<Void, Void, Bitmap>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                imageView.setImageBitmap(null);
            }

            protected Bitmap doInBackground(Void... params){
                final int targetWidth = PREVIEW_WIDTH;
                final int targetHeight = PREVIEW_HEIGHT;

                // Get the width and height of the full-sized image
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(currentPhotoPath, options);
                int originalWidth = options.outWidth;
                int originalHeight = options.outHeight;

                // Calculate a reasonable sample size.
                int scaleSampleSize = Math.min(originalWidth/targetWidth, originalHeight/targetHeight);

                options.inJustDecodeBounds = false;
                options.inSampleSize = scaleSampleSize;
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, options);

                // Check the image's EXIF data and rotate the preview if necessary.
                ExifInterface exif;
                try{
                    exif = new ExifInterface(currentPhotoPath);
                } catch(IOException e){
                    Log.e(TAG, e.getMessage());
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
        }.execute();
    }

    private int exifToDegrees(int exifOrientation){
        switch(exifOrientation){
            case ExifInterface.ORIENTATION_ROTATE_90: return 90;
            case ExifInterface.ORIENTATION_ROTATE_180: return 180;
            case ExifInterface.ORIENTATION_ROTATE_270: return 270;
            default: return 0;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                finish();
                break;
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
}
