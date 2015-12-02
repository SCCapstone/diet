package edu.sc.snacktrack;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * This Fragment manages retrieving data from Parse and retains itself across configuration changes.
 *
 * @author Alex Lockwood
 * (Modified for SnackTrack)
 * See: http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class RemoteDataTaskFragment extends Fragment {

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    interface RDTTaskCallbacks {
        void onRDTPreExecute();
        void onRDTProgressUpdate(int percent);
        void onRDTCancelled();
        void onRDTPostExecute(List<SnackEntry> snackList);
    }

    private RDTTaskCallbacks mCallbacks;
    private RemoteDataTask mTask;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof RDTTaskCallbacks){
            mCallbacks = (RDTTaskCallbacks) context;
        } else{
            mCallbacks = null;
        }
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
        this.restart();
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

    /**
     * Restarts the task if it is not currently running or is cancelled.
     */
    public void restart(){
        if(mTask != null){
            final AsyncTask.Status STATUS = mTask.getStatus();

            if(STATUS == AsyncTask.Status.FINISHED || mTask.isCancelled()){
                mTask = new RemoteDataTask();
                mTask.execute();
            }
        } else{
            mTask = new RemoteDataTask();
            mTask.execute();
        }
    }

    public boolean isRunning(){
        return mTask != null && !(mTask.getStatus() == AsyncTask.Status.RUNNING);
    }

    /**
     * The async task to execute.
     */
    private class RemoteDataTask extends AsyncTask<Void, Integer, List<SnackEntry>> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onRDTPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected List<SnackEntry> doInBackground(Void... ignore) {
            List<SnackEntry> snackList = new ArrayList<SnackEntry>();

            try {
                // Locate the SnackEntry class table named in Parse.com
                ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);

                query.whereEqualTo("owner", ParseUser.getCurrentUser());

                // Locate the column named "createdAt" in Parse.com and order list
                // by descending
                query.orderByDescending("createdAt"); //_created_at
                snackList = query.find();
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return snackList;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                mCallbacks.onRDTProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onRDTCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<SnackEntry> snackList) {
            if (mCallbacks != null) {
                mCallbacks.onRDTPostExecute(snackList);
            }
        }
    }

}