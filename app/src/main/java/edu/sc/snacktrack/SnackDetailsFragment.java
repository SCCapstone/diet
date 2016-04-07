package edu.sc.snacktrack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;

public class SnackDetailsFragment extends Fragment {

    private static final String TAG = "SnackDetailsFragment";

    public static final String SNACK_POSITION_KEY = "snackPosition";

    private static final int PHOTO_UPDATE = 10;

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

    private File newImageFile;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        fileCache = new FileCache(getActivity());

        snackPosition = getArguments().getInt(SNACK_POSITION_KEY);
        snackEntry = SnackList.getInstance().get(snackPosition);
    }

    private void setEditModeEnabled(boolean enabled){
        imageView.setEnabled(enabled);
        descriptionEditText.setEnabled(enabled);
        mealTypeSpinner.setEnabled(enabled);
        scanContentText.setEnabled(enabled);
        scanDetailsText.setEnabled(enabled);
        saveButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void setSaveButtonClickListener() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                if (newImageFile == null) {
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
                    ParseFile newPhoto = new ParseFile(newImageFile);
                    ParseFile oldPhoto = snackEntry.getPhoto();
                    final File oldImageFile;
                    if(oldPhoto != null){
                        oldImageFile = fileCache.getFile(snackEntry.getPhoto().getUrl());
                    } else{
                        oldImageFile = null;
                    }

                    SnackList.getInstance().editSnack(snackEntry, newPhoto, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                updateToast("Update successful", Toast.LENGTH_SHORT);

                                // Delete old image file if necessary
                                if(oldImageFile != null && oldImageFile.exists()){
                                    oldImageFile.delete();
                                }

                                // Cache the new image
                                newImageFile.renameTo(fileCache.getFile(snackEntry.getPhoto().getUrl()));


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
        });
    }

    public void takePhoto() {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_snack_details, container, false);

        // Initialize views
        imageView = (ImageView) view.findViewById(R.id.imageView);
        descriptionEditText = (EditText) view.findViewById(R.id.descriptionEditTextView);
        mealTypeSpinner = (Spinner) view.findViewById(R.id.meal_type_spinner);
        progressOverlay = view.findViewById(R.id.progressOverlay);
        scanContentText = (TextView) view.findViewById(R.id.scan_content);
        scanDetailsText = (TextView) view.findViewById(R.id.scan_details);
        saveButton = (Button) view.findViewById(R.id.save_button);
        mealTypeSpinner.setAdapter(ArrayAdapter.createFromResource(
                this.getActivity(), R.array.meal_types, R.layout.spinner_item
        ));
        setEditModeEnabled(false);

        // Display snack details
        ImageLoader.getInstance(getContext()).displayImage(snackEntry.getPhoto().getUrl(), imageView);
        descriptionEditText.setText(snackEntry.getDescription());
        mealTypeSpinner.setSelection(((ArrayAdapter) mealTypeSpinner.getAdapter()).getPosition(snackEntry.getMealType()));
        scanDetailsText.setText(snackEntry.getScanDetails());
        scanContentText.setText(snackEntry.getScanContent());

        // Set listeners
        setSaveButtonClickListener();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_snack_details, menu);
        editButton = menu.findItem(R.id.edit_item);

        // If this entry doesn't belong to the current user, don't show the edit button.
        if(!ParseUser.getCurrentUser().getObjectId().equals(snackEntry.getOwner().getObjectId())){
            editButton.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_item:
                setEditModeEnabled(true);
                item.setVisible(false);
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
                    ImageLoader.getInstance(getContext()).displayImage(newImageFile, imageView);
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
                updateToast("Something's not right in SnackDetailsFragment", Toast.LENGTH_LONG);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity().setTitle("Details");
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
                getContext(),
                text,
                length
        );
        toast.show();
    }

}