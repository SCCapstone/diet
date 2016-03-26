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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.security.acl.Owner;
import java.util.List;

public class SnackDetailsFragment extends Fragment {

    private static final String TAG = "SnackDetailsFragment";
    private static final int CAMERA_REQUEST = 3;
    public static final String OBJECT_ID_KEY = "objectId";
    private static final int PHOTO_UPDATE = 10;
    private static final int NEW_ENTRY_REQUEST = 2;
    private Menu myMenu;
    private ImageView imageView;
    // private TextView descriptionTextView;
    private EditText descriptionEditText;
    private String oldMealType;
    private String oldDescription;
    //private TextView mealTypeTextView;
    private Spinner mealTypeSpinner;
    //  private Spinner mealLocationSpinner;

    private View progressOverlay;
    private Button saveButton;

    private FileCache fileCache;
    private File newImageFile;
    private String objectId;
    private String OwnerId;
    private String UserId;
    private Toast toast;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

       // getActivity().setTitle("Details");
        getActivity().invalidateOptionsMenu();
        View view = inflater.inflate(R.layout.fragment_snack_details, container, false);
        fileCache = new FileCache(getActivity());
        imageView = (ImageView) view.findViewById(R.id.imageView);
        //descriptionTextView = (TextView) view.findViewById(R.id.descriptionTextView);
        descriptionEditText = (EditText) view.findViewById(R.id.descriptionEditTextView);
        //descriptionEditText.setTextColor();
        saveButton = (Button) view.findViewById(R.id.save_button);
        saveButton.setVisibility(View.GONE);
        //saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final ParseObject snackEntry = ParseObject.createWithoutData("SnackEntry", objectId);

                if (!(oldMealType.equals(mealTypeSpinner.getSelectedItem().toString()))) ;
                {
                    snackEntry.put("mealType", mealTypeSpinner.getSelectedItem().toString());
                }
                if (!(oldDescription.equals(descriptionEditText.getText().toString()))) {
                    snackEntry.put("description", descriptionEditText.getText().toString());
                }
                mealTypeSpinner.setEnabled(false);
                snackEntry.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        progressOverlay.setVisibility(View.GONE);
                        if (e == null) {
                            // Saved successfully.
                            updateToast("Update Successful", Toast.LENGTH_LONG);
                        } else {
                            // The save failed.
                            updateToast("Update Failed", Toast.LENGTH_LONG);
                            //updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                        }
                    }
                });
                myMenu.findItem(R.id.edit_item).setVisible(true);
                saveButton.setVisibility(View.GONE);
                imageView.setEnabled(true);
                descriptionEditText.setEnabled(false);
                //endhere
            }
        });

        mealTypeSpinner = (Spinner) view.findViewById(R.id.meal_type_spinner);
        //   mealLocationSpinner = (Spinner) view.findViewById(R.id.meal_location_spinner);

        //  mealTypeTextView = (TextView) view.findViewById(R.id.mealTypeTextView);


        progressOverlay = view.findViewById(R.id.progressOverlay);

        progressOverlay.setVisibility(View.VISIBLE);
        //descriptionTextView.setText(R.string.loading);
        descriptionEditText.setText("Loading...");

        //mealTypeSp.setText(R.string.loading);
        mealTypeSpinner.setEnabled(false);
        descriptionEditText.setEnabled(false);
        //  descriptionEditText.setTextColor(333);
        // mealLocationSpinner.setEnabled(false);
        // Set up the meal type spinner

        mealTypeSpinner.setAdapter(ArrayAdapter.createFromResource(
                this.getActivity(), R.array.meal_types, R.layout.spinner_item
        ));
//android.R.layout.simple_spinner_dropdown_item
        Bundle bundle = getArguments();
        objectId = bundle.getString(OBJECT_ID_KEY);
        UserId = ParseUser.getCurrentUser().getUsername();
        ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);
        try{
            OwnerId = query.get(objectId).getOwner().getUsername();
        }
        catch (com.parse.ParseException e){

        }


        query.getInBackground(objectId, new GetCallback<SnackEntry>() {
            @Override
            public void done(SnackEntry snackEntry, ParseException e) {
                if (e == null) {
                    String photoURL = snackEntry.getPhoto().getUrl();
                    String description = snackEntry.getDescription();
                    String mealType = snackEntry.getMealType();

                            ImageLoader.getInstance(getContext()).displayImage(photoURL, imageView);
                    descriptionEditText.setText(description != null ? description : "No description");
                    // mealTypeTextView.setText(mealType != null ? mealType : "No meal type");
                    if (mealType != null) {
                        mealTypeSpinner.setSelection(((ArrayAdapter) mealTypeSpinner.getAdapter()).getPosition(mealType));
                        oldMealType = snackEntry.getMealType();

                    } else {
                        oldMealType = "Select an option";
                    }
                    if (description != null) {
                        oldDescription = description;
                    }

                } else {
                    updateToast("Unable to retrieve snack details", Toast.LENGTH_LONG);
                    Log.e(TAG, e.getMessage());
                }
                progressOverlay.setVisibility(View.GONE);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                takePhoto();

            }

        });
        imageView.setEnabled(false);
        setHasOptionsMenu(true);
        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

            menu.clear();
            inflater.inflate(R.menu.menu_edit_snack_details, menu);
//        menu.findItem(R.id.save_item).setVisible(false);
            myMenu = menu;
        if(OwnerId != UserId){
            menu.findItem(R.id.edit_item).setVisible(false);
        }

    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(OwnerId != UserId){
            menu.findItem(R.id.edit_item).setVisible(false);
        }
        else{
            menu.findItem(R.id.edit_item).setVisible(true);
        }
    }



    public Menu getMenu() {
        return myMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_item:
                mealTypeSpinner.setEnabled(true);
                //myMenu.findItem(R.id.save_item).setVisible(true);
                //saveButton.setEnabled(true);
                saveButton.setVisibility(View.VISIBLE);
                item.setVisible(false);
                // descriptionEditText.setTextColor();
                imageView.setEnabled(true);
                descriptionEditText.setEnabled(true);
                return true;
//            case R.id.save_item:
//                //Save
//                final ParseObject snackEntry = ParseObject.createWithoutData("SnackEntry", objectId);
//
//                if (!(oldMealType.equals(mealTypeSpinner.getSelectedItem().toString())));{
//                snackEntry.put("mealType",mealTypeSpinner.getSelectedItem().toString());
//            }
//            if(!(oldDescription.equals(descriptionEditText.getText().toString()))) {
//                snackEntry.put("description", descriptionEditText.getText().toString());
//            }
//            mealTypeSpinner.setEnabled(false);
//            snackEntry.saveInBackground(new SaveCallback() {
//                public void done(ParseException e) {
//                    progressOverlay.setVisibility(View.GONE);
//                    if (e == null) {
//                        // Saved successfully.
//                        updateToast("Update Successful", Toast.LENGTH_LONG);
//                    } else {
//                        // The save failed.
//                        updateToast("Update Failed", Toast.LENGTH_LONG);
//                        //updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
//                    }
//                }
//            });
//            myMenu.findItem(R.id.edit_item).setVisible(true);
//                item.setVisible(false);
//                imageView.setEnabled(true);
//                descriptionEditText.setEnabled(false);
////            descriptionEditText.setTextColor(333);
//                //EditText txt = (EditText)imageView.findViewById(R.id.descriptionEditTextView);
//                //txt.setEnabled(false);
//                //txt.setTextColor(R.color.material_grey_100);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_UPDATE:
                if (resultCode == Activity.RESULT_OK) {
                    //Update photo file for the parse object
                    final ParseObject snackEntry = ParseObject.createWithoutData("SnackEntry", objectId);
                    final ParseFile parseFile = new ParseFile(newImageFile);
                    progressOverlay.setVisibility(View.VISIBLE);
                    snackEntry.put("photo", parseFile);
                    snackEntry.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            progressOverlay.setVisibility(View.GONE);
                            if (e == null) {
                                // Saved successfully.
                                ImageLoader.getInstance(getContext()).displayImage(snackEntry.getParseFile("photo").getUrl(), imageView);

                                //updateToast("Update Successful", Toast.LENGTH_LONG);
                            } else {
                                // The save failed.
                                updateToast("Picture load failed.", Toast.LENGTH_LONG);
                                //updateToast(Utils.getErrorMessage(e), Toast.LENGTH_LONG);
                            }
                        }
                    });
                } else {
                    // Attempt to delete the empty image file
                    if (newImageFile != null) {
                        if (newImageFile.delete()) {
                            Log.d(TAG, "Unused image file successfully deleted.");
                        } else {
                            Log.d(TAG, "Unable to delete unused image file.");
                        }
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
//     @Override
//       public void onBackPressed() {
//
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        WelcomeActivity.super.onBackPressed();
//                    }
//                }).create().show();
  //  }






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
                getContext(),
                text,
                length
        );
        toast.show();
    }

}