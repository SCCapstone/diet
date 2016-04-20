package edu.sc.snacktrack.main.existing_entries;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.snacks.SnackEntry;
import edu.sc.snacktrack.snacks.SnackList;
import edu.sc.snacktrack.utils.FileCache;
import edu.sc.snacktrack.utils.ImageLoader;

public class SnackDetailsActivity extends AppCompatActivity {

    private static final String TAG = "SnackDetailsActivity";

    public static final String SNACK_POSITION_KEY = "snackPosition";

    private static final int PHOTO_UPDATE = 10;

    private static final String STATE_DESCRIPTION_ET = "stateDescriptionET";
    private static final String STATE_MEAL_TYPE_SPINNER = "stateMealTypeSpinner";
    private static final String STATE_SCAN_CONTENT_TEXT = "stateScanContentText";
    private static final String STATE_SCAN_DETAILS_TEXT = "stateScanDetailsText";
    private static final String STATE_NEW_IMAGE_FILE = "stateNewImageFile";
    private static final String STATE_CURRENT_IMAGE_FILE = "stateLastNewImageFile";
    private static final String STATE_EDIT_MODE = "stateEditMode";

    private ImageView imageView;
    private EditText descriptionEditText;
    private TextView scanContentText;
    private TextView scanDetailsText;
    private MenuItem editButton;
    private Spinner mealTypeSpinner;
    private View progressOverlay;
    private Button saveButton;

    private FileCache fileCache;
    private Toast toast;

    private SnackEntry snackEntry;
    private int snackPosition;

    private File currentImageFile;
    private File newImageFile;

    private boolean editMode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snack_details);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fileCache = new FileCache(this);

        snackPosition = getIntent().getIntExtra(SNACK_POSITION_KEY, -1);
        snackEntry = SnackList.getInstance().get(snackPosition);

        // Set title to the snack entry's created at time.
        setTitle(SimpleDateFormat.getInstance().format(snackEntry.getCreatedAt()));

        // Initialize views
        imageView = (ImageView) findViewById(R.id.imageView);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditTextView);
        mealTypeSpinner = (Spinner) findViewById(R.id.meal_type_spinner);
        progressOverlay = findViewById(R.id.progressOverlay);
        scanContentText = (TextView) findViewById(R.id.scan_content);
        scanDetailsText = (TextView) findViewById(R.id.scan_details);
        saveButton = (Button) findViewById(R.id.save_button);
        mealTypeSpinner.setAdapter(ArrayAdapter.createFromResource(
                this, R.array.meal_types, R.layout.spinner_item
        ));

        // Set listeners
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEntry();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editMode){
                    takePhoto();
                } else if(snackEntry.getPhoto() != null){
                    // Show full sized image
                    Intent intent = new Intent(SnackDetailsActivity.this, ImageViewerActivity.class);
                    intent.putExtra(ImageViewerActivity.FILE_PATH_KEY, fileCache.getFile(
                            snackEntry.getPhoto().getUrl()
                    ).getAbsolutePath());
                    startActivity(intent);
                } else{
                    updateToast("No photo to view.", Toast.LENGTH_SHORT);
                }
            }
        });

        // Check saved state
        if(savedInstanceState == null){
            // Display original snack details
            loadEntry();
        } else{
            // Load saved instance state and display possibly edited snack entry
            loadEntrySavedState(savedInstanceState);
        }
    }

    /**
     * Loads the original entry.
     */
    private void loadEntry(){
        if(snackEntry.getPhoto() != null){
            ImageLoader.getInstance(this).displayImage(snackEntry.getPhoto().getUrl(), imageView);
        } else{
            imageView.setImageResource(R.drawable.ic_photo_camera_black_24dp);
        }
        descriptionEditText.setText(snackEntry.getDescription());
        mealTypeSpinner.setSelection(((ArrayAdapter) mealTypeSpinner.getAdapter()).getPosition(snackEntry.getMealType()));
        scanDetailsText.setText(snackEntry.getScanDetails());
        scanContentText.setText(snackEntry.getScanContent());

        setEditModeEnabled(false);
    }

    /**
     * Loads possibly edited entry from a saved state.
     *
     * @param savedInstanceState The saved state
     */
    private void loadEntrySavedState(Bundle savedInstanceState){
        editMode = savedInstanceState.getBoolean(STATE_EDIT_MODE);
        newImageFile = (File) savedInstanceState.getSerializable(STATE_NEW_IMAGE_FILE);
        currentImageFile = (File) savedInstanceState.getSerializable(STATE_CURRENT_IMAGE_FILE);

        if(editMode){
            if(currentImageFile != null){
                ImageLoader.getInstance(this).displayImage(currentImageFile, imageView);
            } else if(snackEntry.getPhoto() != null){
                ImageLoader.getInstance(this).displayImage(snackEntry.getPhoto().getUrl(), imageView);
            }
            descriptionEditText.setText(savedInstanceState.getString(STATE_DESCRIPTION_ET));
            mealTypeSpinner.setSelection(savedInstanceState.getInt(STATE_MEAL_TYPE_SPINNER));
            scanDetailsText.setText(savedInstanceState.getString(STATE_SCAN_DETAILS_TEXT));
            scanContentText.setText(savedInstanceState.getString(STATE_SCAN_CONTENT_TEXT));
        } else{
            loadEntry();
        }

        setEditModeEnabled(editMode);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_NEW_IMAGE_FILE, newImageFile);
        outState.putSerializable(STATE_CURRENT_IMAGE_FILE, currentImageFile);
        outState.putString(STATE_DESCRIPTION_ET, descriptionEditText.getText().toString());
        outState.putString(STATE_SCAN_CONTENT_TEXT, scanContentText.getText().toString());
        outState.putString(STATE_SCAN_DETAILS_TEXT, scanDetailsText.getText().toString());
        outState.putInt(STATE_MEAL_TYPE_SPINNER, mealTypeSpinner.getSelectedItemPosition());
        outState.putBoolean(STATE_EDIT_MODE, editMode);
    }

    private void setEditModeEnabled(boolean enabled){
        //imageView.setEnabled(enabled);
        descriptionEditText.setEnabled(enabled);
        mealTypeSpinner.setEnabled(enabled);
        scanContentText.setEnabled(enabled);
        scanDetailsText.setEnabled(enabled);
        saveButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        editMode = enabled;

        if(!enabled){
            newImageFile = null;
            currentImageFile = null;
        }
    }

    private void saveEntry() {
        final File currentImageFile = this.currentImageFile;

        setEditModeEnabled(false);
        progressOverlay.setVisibility(View.VISIBLE);

        String newDescription = descriptionEditText.getText().toString();
        String newMealType = mealTypeSpinner.getSelectedItem().toString();
        String newScanContent = scanContentText.getText().toString();
        String newScanDetails = scanDetailsText.getText().toString();

        snackEntry.setDescription(newDescription);
        snackEntry.setTypeOfMeal(newMealType);
        snackEntry.setScanContent(newScanContent);
        snackEntry.setScanDetails(newScanDetails);

        if (currentImageFile == null) {
            SnackList.getInstance().editSnack(snackEntry, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        updateToast("Update successful", Toast.LENGTH_SHORT);
                    } else {
                        updateToast("Update failed", Toast.LENGTH_SHORT);
                        Log.e(TAG, e.getMessage());
                    }

                    editButton.setVisible(true);
                    progressOverlay.setVisibility(View.GONE);
                }
            });
        } else {
            ParseFile newPhoto = new ParseFile(currentImageFile);
            ParseFile oldPhoto = snackEntry.getPhoto();
            final File oldImageFile;
            if (oldPhoto != null) {
                oldImageFile = fileCache.getFile(snackEntry.getPhoto().getUrl());
            } else {
                oldImageFile = null;
            }

            SnackList.getInstance().editSnack(snackEntry, newPhoto, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        updateToast("Update successful", Toast.LENGTH_SHORT);

                        // Delete old image file if necessary
                        if (oldImageFile != null && oldImageFile.exists()) {
                            oldImageFile.delete();
                        }

                        // Cache the new image
                        currentImageFile.renameTo(fileCache.getFile(snackEntry.getPhoto().getUrl()));


                    } else {
                        updateToast("Update failed", Toast.LENGTH_SHORT);
                        Log.e(TAG, e.getMessage());
                    }

                    editButton.setVisible(true);
                    progressOverlay.setVisibility(View.GONE);
                }
            });
        }
    }

    private void takePhoto() {
        try {
            File imageFile = fileCache.createTempFile("SnackPhoto", ".jpg");
            this.newImageFile = imageFile;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(takePictureIntent, PHOTO_UPDATE);
        } catch (IOException e) {
            updateToast("Error accessing SD Card\nCheck that the SD card is mounted.", Toast.LENGTH_LONG);
            Log.e(TAG, e.getMessage());
        }
    }

    private void deleteEntry(){
        if(progressOverlay != null)
            progressOverlay.setVisibility(View.VISIBLE);

        SnackList.getInstance().deleteSnack(snackPosition, snackEntry, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    updateToast("Entry deleted", Toast.LENGTH_SHORT);

                    // Attempt to delete entry's cached image
                    if(snackEntry != null){
                        ParseFile parseFile = snackEntry.getPhoto();
                        if(parseFile != null){
                            String url = parseFile.getUrl();
                            if(url != null){
                                if(fileCache.getFile(url).delete()){
                                    Log.d(TAG, "deleteEntry() successfully deleted entry's cached image");
                                } else{
                                    Log.d(TAG, "deleteEntry() failed to delete entry's cached image");
                                }
                            }
                        }
                    }
                    finish();
                } else{
                    updateToast("Unable to delete entry", Toast.LENGTH_SHORT);
                }

                if(progressOverlay != null)
                    progressOverlay.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_edit_snack_details, menu);

        MenuItem deleteButton = menu.findItem(R.id.delete_item);

        editButton = menu.findItem(R.id.edit_item);

        // If this entry doesn't belong to the current user, don't show the edit button
        // or the delete button.
        // OR if edit mode is already enabled, don't show show the edit button.
        if(!ParseUser.getCurrentUser().getObjectId().equals(snackEntry.getOwner().getObjectId())){
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        } else if(editMode){
            editButton.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_item:
                setEditModeEnabled(true);
                item.setVisible(false);
                return true;
            case R.id.delete_item:
                new ConfirmDeleteDialog().show(getSupportFragmentManager(), null);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_UPDATE:
                if (resultCode == Activity.RESULT_OK) {
                    currentImageFile = newImageFile;
                    ImageLoader.getInstance(this).displayImage(newImageFile, imageView);
                } else {
                    // Attempt to delete the empty image file
                    if (newImageFile != null) {
                        if (newImageFile.delete()) {
                            Log.d(TAG, "Unused image file successfully deleted.");
                        } else {
                            Log.d(TAG, "Unable to delete unused image file.");
                        }
                        newImageFile = null;
                    }
                }
                break;
            default:
                updateToast("Something's not right in SnackDetailsActivity", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onBackPressed() {
        if(!editMode){
            super.onBackPressed();
        } else{
            new ConfirmBackDialogFragment().show(getSupportFragmentManager(), null);
        }
    }

    public static class ConfirmBackDialogFragment extends DialogFragment{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Discard your changes?")
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SnackDetailsActivity activity = (SnackDetailsActivity) getActivity();
                            // Reload the entry's original values
                            activity.loadEntry();
                            activity.setEditModeEnabled(false);
                            activity.editButton.setVisible(true);
                            dismiss();
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

    public static class ConfirmDeleteDialog extends DialogFragment{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Obliterate this entry?")
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SnackDetailsActivity activity = (SnackDetailsActivity) getActivity();
                            activity.deleteEntry();
                            dismiss();
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
     * Cancels the current toast and displays a new toast.
     *
     * @param text   The text to display
     * @param length The length to display the toast
     */
    private void updateToast(String text, int length) {
        if (toast != null) {
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