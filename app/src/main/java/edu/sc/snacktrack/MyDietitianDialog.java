package edu.sc.snacktrack;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by spitzfor on 2/10/2016.
 */
public class MyDietitianDialog extends DialogFragment {

    private EditText searchEditText;
    private Button searchSubmitButton;
    private Button searchCancelButton;

    public MyDietitianDialog() {
        //Empty constructor required
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mydietitian_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        searchEditText = (EditText) view.findViewById(R.id.dietitian_edit_text);
        searchSubmitButton = (Button) view.findViewById(R.id.my_search_submit);
        searchCancelButton = (Button) view.findViewById(R.id.search_cancel);

        searchSubmitButton.setEnabled(false);
        searchSubmitButton.setTextColor(Color.parseColor("#FF6666"));

        searchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    searchEditText.setFocusable(false);

                    if (searchEditText.getText().length() != 0)
                    {
                        searchSubmitButton.setEnabled(true);
                        searchSubmitButton.setTextColor(Color.parseColor("#4DFF4D"));
                    }

                    else
                    {
                        searchSubmitButton.setEnabled(false);
                    }
                }

                return false;
            }
        });

        //Submit search and return result
        searchSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(searchSubmitButton.isEnabled() == true)
                {
                    Utils.closeSoftKeyboard(getContext(), v);
                    dietitianSearch(searchEditText.getText().toString());
                    dismiss();
                }
            }
        });

        //Cancel username search and dismiss dialogFragment
        searchCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeSoftKeyboard(getContext(), v);
                dismiss();
            }
        });

        return view;
    }

    private void dietitianSearch(String user) {

        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("username", user);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null) {
                    //query successful
                    if (objects.isEmpty() != true)
                    {
                        ParseUser test = objects.get(0);
                        Log.e("User found",test.getObjectId().toString());
                    }

                    else
                    {
                        Log.e("USER NOT FOUND", "USER WAS NOT FOUND");
                    }

                }

                else {
                    Log.e("SEARCH ERROR", "ERROR IN SEARCHING");
                }
            }
        });
    }
}
