package edu.sc.snacktrack;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;

/**
 * This Fragment manages saving a single snack entry in the background retains itself across
 * configuration changes.
 *
 * This fragment expects the following arguments (via setArguments()):
 *   String mealType (key: MEAL_TYPE_KEY)      - The meal type
 *   String description (key: DESCRIPTION_KEY) - The description
 *   String photoPath (key: PHOTO_PATH_KEY)    - The file path to the photo.
 *
 * The current user is automatically added to the snack entry via getCurrentUser().
 *
 * @author Alex Lockwood
 * (Modified for SnackTrack)
 * See: http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class SaveSnackTaskFragment extends Fragment {

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute(ParseException e);
    }

    private static final String TAG = "SaveSnackDebug";

    /**
     * The key for the meal type argument. The meal type is a String.
     */
    public static final String MEAL_TYPE_KEY = "mealType";

    /**
     * The key for the description argument. The description is a String.
     */
    public static final String DESCRIPTION_KEY = "description";

    /**
     * The key for the photo path argument. The photo path is a String.
     */
    public static final String PHOTO_PATH_KEY = "photoPath";

    private TaskCallbacks mCallbacks;
    private TheTask mTask;

    private FileCache fileCache;

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        fileCache = new FileCache(this.getContext());

        // Create and execute the background task.
        mTask = new TheTask();
        mTask.execute();
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void setCallbacks(TaskCallbacks callbacks){
        mCallbacks = callbacks;
    }

    /**
     * Attempts to upload this SnackEntry to Parse in the background.
     */
    private class TheTask extends AsyncTask<Void, Integer, ParseException> {

        private ParseUser owner;
        private String mealType;
        private String description;
        private File cacheFile;
        private ParseFile parseFile;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mCallbacks != null){
                mCallbacks.onPreExecute();
            }

            Bundle args = getArguments();

            owner = ParseUser.getCurrentUser();
            mealType = args.getString(MEAL_TYPE_KEY, null);
            description = args.getString(DESCRIPTION_KEY, null);

            String filePath;
            filePath = args.getString(PHOTO_PATH_KEY, null);
            cacheFile = filePath == null ? null : new File(filePath);
            parseFile = filePath == null ? null : new ParseFile(cacheFile);

        }

        @Override
        protected ParseException doInBackground(Void... params) {
            SnackEntry snackEntry = new SnackEntry();

            snackEntry.setOwner(owner);
            snackEntry.setACL(new ParseACL(owner));

            // If the meal type was specified, set the snackEntry's mealType
            if(mealType != null && !mealType.equals(getResources().getString(R.string.default_spinner_item))){
                snackEntry.setTypeOfMeal(mealType);
            }

            // If the description is not empty, set the snackEntry's description
            if(description != null && !description.trim().equals("")){
                snackEntry.setDescription(description);
            }

            // Save the image to parse, then save the snackEntry to parse
            try{
                if(cacheFile != null && cacheFile.exists()){
                    parseFile.save();

                    // Attempt to rename the cache file to point to the parse file.
                    if(!cacheFile.renameTo(fileCache.getFile(parseFile.getUrl()))){
                        Log.e(TAG, "Failed to rename cache file to point to url of parse file.");
                    }

                    snackEntry.setPhoto(parseFile);
                    snackEntry.save();
                } else{
                    throw new ParseException(ParseException.OTHER_CAUSE, "File to upload does not exist");
                }
            } catch(ParseException e){
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(ParseException e) {
            super.onPostExecute(e);
            if(mCallbacks != null){
                mCallbacks.onPostExecute(e);
            } else{
                Log.d(TAG, "onPostExecute mCallbacks is null!!");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if(mCallbacks != null){
                mCallbacks.onCancelled();
            }
        }
    }
}