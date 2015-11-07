package edu.sc.snacktrack;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraTestActivity extends AppCompatActivity {

    private static final String TAG = "CameraTestDebug";

    private static final int CAMERA_REQUEST_CODE = 1;

    private Toast toast;

    private ImageView imageView;

    private String currentPhotoPath;

    private static final String STATE_CURRENT_PHOTO_PATH = "currentPhotoPath";

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

        // Restore instance state
        if(savedInstanceState != null){
            currentPhotoPath = savedInstanceState.getString(STATE_CURRENT_PHOTO_PATH);
            Bitmap image = loadCurrentPhoto();
            imageView.setImageBitmap(image);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString(STATE_CURRENT_PHOTO_PATH, currentPhotoPath);
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

    private File createImageFile() throws IOException{
        // Create an image file name based on the current date
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save the path to this file
        currentPhotoPath = imageFile.getAbsolutePath();
        Log.d(TAG, "New image file path: " + currentPhotoPath);

        return imageFile;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                // Image capture successful
                Log.d(TAG, "Image capture successful.");

                Bitmap image = loadCurrentPhoto();

                imageView.setImageBitmap(image);

            } else if(resultCode == RESULT_CANCELED){
                // Image capture canceled

            } else{
                // Image capture failed
            }
        }
    }

    private Bitmap loadCurrentPhoto(){
        Bitmap image = BitmapFactory.decodeFile(currentPhotoPath);

        if(image == null){
            updateToast("Failed to load current photo.", Toast.LENGTH_SHORT);
            return null;
        }


        ExifInterface exif;
        try{
            exif = new ExifInterface(currentPhotoPath);
        } catch(IOException e){
            updateToast("Failed to load exif data", Toast.LENGTH_SHORT);
            return image;
        }

        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationDeg = exifToDegrees(rotation);
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDeg);

        Bitmap rotatedImage = Bitmap.createBitmap(
                image, 0, 0, image.getWidth(), image.getHeight(), matrix, true
        );
        image.recycle();

        return rotatedImage;
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
