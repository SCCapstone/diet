package edu.sc.snacktrack;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class SnackDetailsFragment extends Fragment {

    private static final String TAG = "SnackDetailsFragment";

    public static final String OBJECT_ID_KEY = "objectId";

    private ImageView imageView;
    private TextView descriptionTextView;
    private TextView mealTypeTextView;

    private View progressOverlay;

    private ImageLoader imageLoader;

    private Toast toast;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        imageLoader = new ImageLoader(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_snack_details, container, false);

        imageView = (ImageView) view.findViewById(R.id.imageView);
        descriptionTextView = (TextView) view.findViewById(R.id.descriptionTextView);
        mealTypeTextView = (TextView) view.findViewById(R.id.mealTypeTextView);

        progressOverlay = view.findViewById(R.id.progressOverlay);

        progressOverlay.setVisibility(View.VISIBLE);
        descriptionTextView.setText(R.string.loading);
        mealTypeTextView.setText(R.string.loading);

        Bundle bundle = getArguments();
        String objectId = bundle.getString(OBJECT_ID_KEY);

        ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);
        query.getInBackground(objectId, new GetCallback<SnackEntry>() {
            @Override
            public void done(SnackEntry snackEntry, ParseException e) {
                if(e == null){
                    String photoURL = snackEntry.getPhoto().getUrl();
                    String description = snackEntry.getDescription();
                    String mealType = snackEntry.getMealType();

                    imageLoader.DisplayImage(photoURL, imageView);
                    descriptionTextView.setText(description != null ? description : "No description");
                    mealTypeTextView.setText(mealType != null ? mealType : "No meal type");
                } else{
                    updateToast("Unable to retrieve snack details", Toast.LENGTH_LONG);
                    Log.e(TAG, e.getMessage());
                }
                progressOverlay.setVisibility(View.GONE);
            }
        });

        return view;
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
                getContext(),
                text,
                length
        );
        toast.show();
    }

}
