package edu.sc.snacktrack.main.new_entry;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.barcode.IntentIntegrator;
import edu.sc.snacktrack.barcode.IntentResult;
import edu.sc.snacktrack.barcode.RetrieveFeedTask;
import edu.sc.snacktrack.snacks.SnackEntry;
import edu.sc.snacktrack.snacks.SnackList;
import edu.sc.snacktrack.utils.FileCache;
import edu.sc.snacktrack.utils.Utils;

public class NewEntryActivity extends AppCompatActivity implements OnClickListener{

    private static final String TAG = "NewEntryDebug";

    private Toast toast;

    private TextView descriptionTextView;
    private ImageView imageView;
    private Spinner mealTypeSpinner;
  //  private Spinner mealLocationSpinner;

    private View progressOverlay;

   private File currentImageFile;

    private static final int DESCRIPTION_CHANGE_CODE = 1;
//    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int BARCODE_SCAN_REQUEST = 3;

    private static final int PREVIEW_WIDTH = 100;
    private static final int PREVIEW_HEIGHT = 100;

    private static final String STATE_DESCRIPTION_STRING = "descriptionString";
    private static final String STATE_SCAN_CONTENT_STRING = "scanContentString";
    private static final String STATE_SCAN_DETAILS_STRING = "scanDetailsString";
    private static final String STATE_SAVING = "saving";

    public static final String PHOTO_FILE_KEY = "photo";

    private static final String TASK_FRAGMENT_TAG = "taskFragment";

    private PhotoPreviewLoader photoPreviewLoader;

    private boolean saving = false;

    private Button scanBtn;
    //private TextView formatTxt, contentTxt;
    private TextView contentText;
    private TextView detailsText;

    //private TextView detailsTxt;
    private String barcodeContent;

    private FileCache fileCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);


        contentText = (TextView) findViewById(R.id.scan_content);
        detailsText = (TextView) findViewById(R.id.scan_details);

        mealTypeSpinner = (Spinner) findViewById(R.id.meal_type_spinner);
      //  mealLocationSpinner = (Spinner) findViewById(R.id.meal_location_spinner);

        // Set up the meal type spinner
        mealTypeSpinner.setAdapter(ArrayAdapter.createFromResource(
                this, R.array.meal_types, android.R.layout.simple_spinner_dropdown_item
        ));

        // Set up the description box
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        descriptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewEntryActivity.this, EditDescriptionActivity.class);
                intent.putExtra(EditDescriptionActivity.DESCRIPTION_STRING_KEY, descriptionTextView.getText().toString());
                startActivityForResult(intent, DESCRIPTION_CHANGE_CODE);
                overridePendingTransition(R.animator.animation, R.animator.animation2);
            }
        });

        // Set up the image view
        imageView = (ImageView) findViewById(R.id.imageView);

//        Old implementation for retaking a photo by tapping on the image view.
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dispatchPictureIntent();
//            }
//        });

        // Set up the progress overlay
        progressOverlay = findViewById(R.id.progressOverlay);

        // Restore instance state
        if(savedInstanceState != null){
//            String currentPhotoPath, newPhotoPath;

            descriptionTextView.setText(savedInstanceState.getString(STATE_DESCRIPTION_STRING, ""));
            contentText.setText(savedInstanceState.getString(STATE_SCAN_CONTENT_STRING, ""));
            detailsText.setText(savedInstanceState.getString(STATE_SCAN_DETAILS_STRING, ""));

            this.saving = savedInstanceState.getBoolean(STATE_SAVING, false);

//            currentPhotoPath = savedInstanceState.getString(STATE_CURRENT_PHOTO_PATH, null);
//            newPhotoPath = savedInstanceState.getString(STATE_NEW_PHOTO_PATH, null);

//            currentImageFile = currentPhotoPath == null ? null : new File(currentPhotoPath);
//            newImageFile = newPhotoPath == null ? null : new File(newPhotoPath);
        }

        // Get the snack photo and display the preview if there is one
        currentImageFile = (File) getIntent().getSerializableExtra(PHOTO_FILE_KEY);
        if(currentImageFile != null){
            loadPhotoPreview(currentImageFile);
        }

//        // Initialize the file cache
//        this.fileCache = new FileCache(this);
//
//        // If a photo has not been taken, start the camera app.
//        if(newImageFile == null){
//            dispatchPictureIntent();
//        }

        scanBtn = (Button)findViewById(R.id.scan_button);
//        formatTxt = (TextView)findViewById(R.id.scan_format);
//        contentTxt = (TextView)findViewById(R.id.scan_content);
        scanBtn.setOnClickListener(this);

        // If saving is in progress, show the progress overlay and disable widgets.
        if(saving){
            progressOverlay.setVisibility(View.VISIBLE);
            setWidgetsEnabled(false);
        }

        fileCache = new FileCache(this);
    }

//    /**
//     * Dispatches the picture intent. If an IOException occurs, sets result to RESULT_CANCELED
//     * and finishes this activity.
//     */
//    private void dispatchPictureIntent(){
//        try {
//            File imageFile = fileCache.createTempFile("SnackPhoto", ".jpg");
//            this.newImageFile = imageFile;
//
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
//
//            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
//            overridePendingTransition(R.animator.animation, R.animator.animation2);
//        } catch (IOException e) {
//            Toast.makeText(
//                    this,
//                    "Error accessing SD Card.\nCheck that the SD card is mounted.",
//                    Toast.LENGTH_LONG
//            ).show();
//            Log.e(TAG, e.getMessage());
//
//            setResult(RESULT_CANCELED);
//            finish();
//            overridePendingTransition(R.animator.animation, R.animator.animation2);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == DESCRIPTION_CHANGE_CODE){
            if(resultCode == RESULT_OK){
                String newText = data.getStringExtra(EditDescriptionActivity.DESCRIPTION_STRING_KEY);
                if(newText != null){
                    descriptionTextView.setText(newText);
                }
            }
        } else if(requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanningResult != null) {
                String scanContent = scanningResult.getContents();
                String scanFormat = scanningResult.getFormatName();
//            formatTxt.setText("FORMAT: " + scanFormat);
//                contentTxt.setText("CONTENT: " + scanContent);
                //detailsTxt.setText("DETAILS");
            /*
            try {
                contentTxt.setText(httpGet(scanContent));
            }catch(Exception e) {
                Log.e("httpget", scanContent);
            }
            barcodeContent = scanContent;
            */
                try {
                    int commaIndex = 3;
                    String brand = "Brand";
                    String scannerInfo = new RetrieveFeedTask().execute(scanContent).get();
                    int nameBegin = scannerInfo.lastIndexOf("name") + 7;
                    int nameEnd = scannerInfo.indexOf("attributes") - 8;
                    final String productName = scannerInfo.substring(nameBegin, nameEnd);
                    //brand name if statement currently causes an exception, not sure why
/*
                if(scannerInfo.toLowerCase().contains(brand.toLowerCase())) {
                    commaIndex++;
                    Integer brandBegin = scannerInfo.lastIndexOf("Brand") + 7;
                    int brandEnd = scannerInfo.indexOf(",", 0);
                    while (commaIndex-- > 0 && brandEnd != -1)
                        brandEnd = scannerInfo.indexOf(",", brandEnd+1);
                    brandEnd = brandEnd - 3;
                    productName = productName.concat("\r\n" + scannerInfo.substring(brandBegin, brandEnd));
                }
*/
                    nameBegin = scannerInfo.lastIndexOf("attributes") + 16;
                    nameEnd = scannerInfo.indexOf("images") - 8;
                    String productDetails = scannerInfo.substring(nameBegin, nameEnd);
                    //detailsTxt.setText(productDetails);
//                    contentTxt.setText(productName);
                    contentText.setText(productName);
                    detailsText.setText(productDetails);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                barcodeContent = scanContent;
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

//        This (hacky) case is no longer needed since the camera intent is now handled in MainActivity
//        else if(requestCode == CAMERA_REQUEST_CODE){
//
//            if(resultCode == RESULT_OK){
//                //Image capture successful
//
//                if(currentImageFile != null) {
//                    if (currentImageFile.delete()) {
//                        Log.d(TAG, "Deleted old photo after retake.");
//                    } else {
//                        Log.d(TAG, "Could not delete old photo after retake.");
//                    }
//                }
//
//                currentImageFile = newImageFile;
//
//                loadPhotoPreview(currentImageFile);
//            } else if(resultCode == RESULT_CANCELED){
//                // Image capture canceled
//
//                // If the user never took a photo, leave this activity.
//                if(currentImageFile == null){
//                    setResult(RESULT_CANCELED);
//                    finish();
//                    overridePendingTransition(R.animator.animation, R.animator.animation2);
//                }
//
//                if(newImageFile != null) {
//                    if (newImageFile.delete()) {
//                        Log.d(TAG, "Unused image file deleted.");
//                    } else {
//                        Log.d(TAG, "Could not delete unused image file.");
//                    }
//                }
//
//                newImageFile = currentImageFile;
//            } else{
//                updateToast("Image capture failed.", Toast.LENGTH_SHORT);
//                // Image capture failed.
//            }
//        }
    }

    /**
     * Asynchronously loads a scaled-down preview of an image and displays it in imageView.
     * The preview is scaled based on PREVIEW_WIDTH and PREVIEW_HEIGHT
     *
     * @param imageFile Image file to preview
     */
    private void loadPhotoPreview(File imageFile){

        if(imageFile.exists()){
            if(photoPreviewLoader != null){
                photoPreviewLoader.cancel(true);
            }
            photoPreviewLoader = new PhotoPreviewLoader(imageFile);
            photoPreviewLoader.execute();
        } else{
            updateToast("Unable to load preview image.", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Passes the specified arguments to a new saveSnackTaskFragment and starts
     * saveSnackTaskFragment.
     */
    private void saveEntry(){
        String description;
        String mealType;
        String scanDetails;
        String scanContent;
        ParseACL acl;

        final SnackEntry entry = new SnackEntry();
        final ParseFile parseFile;

        if(currentImageFile != null){
            parseFile = new ParseFile(currentImageFile);
        } else{
            parseFile = null;
        }

        description = descriptionTextView.getText().toString();
        mealType = mealTypeSpinner.getSelectedItem().toString();
        scanContent = contentText.getText().toString();
        scanDetails = detailsText.getText().toString();
        acl = new ParseACL(ParseUser.getCurrentUser());
        acl.setRoleReadAccess("role_" + ParseUser.getCurrentUser().getObjectId(), true);

        saving = true;
        setWidgetsEnabled(false);
        progressOverlay.setVisibility(View.VISIBLE);

        if(description != null && !description.trim().equals("")){
            entry.setDescription(descriptionTextView.getText().toString());
        }

        if(scanContent != null && !scanContent.trim().equals("")){
            entry.setScanContent(contentText.getText().toString());
            Log.d("contentText1", contentText.getText().toString());
        }

        if(scanDetails != null && !scanDetails.trim().equals("")){
            entry.setScanDetails(detailsText.getText().toString());
            Log.d("detailsText1", detailsText.getText().toString());
        }

        if(mealType != null){
            entry.setTypeOfMeal(mealTypeSpinner.getSelectedItem().toString());
        }

//        if(myDietitian != null){
//            acl.setReadAccess(myDietitian, true);
//        }

        entry.setOwner(ParseUser.getCurrentUser());
        entry.setACL(acl);

        if(parseFile != null){
            SnackList.getInstance().addSnack(entry, parseFile, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        setResult(RESULT_OK);
                        // Rename the uploaded image file to correct cache name.
                        currentImageFile.renameTo(fileCache.getFile(parseFile.getUrl()));
                        finish();
                    } else{
                        updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                    }

                    progressOverlay.setVisibility(View.GONE);
                    NewEntryActivity.this.saving = false;
                    setWidgetsEnabled(true);
                }
            });
        } else{
            SnackList.getInstance().addSnack(entry, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        ParseUser.getCurrentUser().put("lastActiveAt", entry.getCreatedAt());
                        ParseUser.getCurrentUser().saveInBackground();
                        setResult(RESULT_OK);
                        finish();
                    } else{
                        updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                    }

                    progressOverlay.setVisibility(View.GONE);
                    NewEntryActivity.this.saving = false;
                    setWidgetsEnabled(true);
                }
            });
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
     * Enables or disables all user input widgets.
     *
     * @param enabled true to enable; false to disable
     */
    private void setWidgetsEnabled(boolean enabled){
        imageView.setEnabled(enabled);
        mealTypeSpinner.setEnabled(enabled);
        descriptionTextView.setEnabled(enabled);
        scanBtn.setEnabled(enabled);
    }

    @Override
    public void onBackPressed(){
        // Do not interrupt saving
        if(!saving){
            new ConfirmBackDialogFragment().show(getSupportFragmentManager(), null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_DESCRIPTION_STRING, descriptionTextView.getText().toString());
        outState.putString(STATE_SCAN_CONTENT_STRING, contentText.getText().toString());
        outState.putString(STATE_SCAN_DETAILS_STRING, detailsText.getText().toString());
//        if(currentImageFile != null){
//            outState.putString(STATE_CURRENT_PHOTO_PATH, currentImageFile.getAbsolutePath());
//        }
//        if(newImageFile != null){
//            outState.putString(STATE_NEW_PHOTO_PATH, newImageFile.getAbsolutePath());
//        }
        outState.putBoolean(STATE_SAVING, saving);
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

        // Do not interrupt saving.
        if(saving){
            return false;
        }

        switch(id){
            case R.id.action_done:
                saveEntry();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(photoPreviewLoader != null){
            photoPreviewLoader.cancel(true);
        }
    }

    public static class ConfirmBackDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Discard this entry?")
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("No, wait!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .create();
        }
    }

    /**
     * Asynchronously loads a scaled-down preview of an image and displays it in imageView.
     * The preview is scaled based on PREVIEW_WIDTH and PREVIEW_HEIGHT
     */
    private class PhotoPreviewLoader extends AsyncTask<Void, Void, Bitmap>{

        private File imageFile;

        public PhotoPreviewLoader(File imageFile){
            this.imageFile = new File(imageFile.getAbsolutePath());
        }

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
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            // Calculate a reasonable sample size.
            int scaleSampleSize = Math.min(originalWidth/targetWidth, originalHeight/targetHeight);

            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleSampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);


            if(bitmap == null){
                Log.d(TAG, "Resulting bitmap is null after decoding file " + imageFile);
                return null;
            }

            // Check the image's EXIF data and rotate the preview if necessary.
            Matrix matrix = new Matrix();
            int rotationDeg;
            try{
                rotationDeg = Utils.getExifRotation(imageFile);
            } catch(IOException e){
                rotationDeg = 0;
            }
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
        }
    }

//old way of httpGet
/*
    //httpurlconnection to outpan
    public static String httpGet(String urlStr) throws IOException {
        try {
            String yourProduct = urlStr;
            Log.d("urlStr", urlStr);
            urlStr = "https://api.outpan.com/v2/products/";
            urlStr.concat(yourProduct);
            urlStr.concat("?apikey=0486422639a1db6223dd81a97c9f996f");
            URL url = new URL(urlStr);
            HttpsURLConnection conn =
                    (HttpsURLConnection) url.openConnection();

            if (conn.getResponseCode() != 200) {
                throw new IOException(conn.getResponseMessage());
            }

            // Buffer the result into a string
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();

            conn.disconnect();
            return sb.toString();
        }catch(IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
*/


    //new way of httpGet
/*
    public static String httpGet(String urlStr) {
        String yourProduct = urlStr;
        // Do some validation here

        try {
            //URL url = new URL(API_URL + "email=" + email + "&apiKey=" + API_KEY);
            yourProduct = "https://api.outpan.com/v2/products/" + yourProduct;
            yourProduct = yourProduct.concat("?apikey=0486422639a1db6223dd81a97c9f996f");
            Log.d("myTag", yourProduct);
            URL url = new URL(yourProduct);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }
    */

    public void onClick(View v){
        if(v.getId()==R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }
}