package edu.sc.snacktrack;

import android.content.Context;
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
 * This Fragment manages a single background task and retains itself across configuration changes.
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

    public static final String MEAL_TYPE_KEY = "mealType";
    public static final String DESCRIPTION_KEY = "description";
    public static final String PHOTO_PATH_KEY = "photoPath";

    private TaskCallbacks mCallbacks;
    private TheTask mTask;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        //mCallbacks = (TaskCallbacks) activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

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
     * A dummy task that performs some (dumb) background work and
     * proxies progress updates and results back to the Activity.
     *
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class TheTask extends AsyncTask<Void, Integer, ParseException> {

        private ParseUser owner;
        private String mealType;
        private String description;
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
            String photoPath = args.getString(PHOTO_PATH_KEY, null);
            if(photoPath != null){
                parseFile = new ParseFile(new File(photoPath));
            } else{
                parseFile = null;
            }

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
                parseFile.save();
                snackEntry.setPhoto(parseFile);
                snackEntry.save();
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