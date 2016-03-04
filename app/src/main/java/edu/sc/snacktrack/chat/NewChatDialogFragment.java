package edu.sc.snacktrack.chat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import edu.sc.snacktrack.R;
import edu.sc.snacktrack.Utils;

/**
 * This DialogFragment asks the user to input a username to chat with and returns the entered
 * username string.
 *
 * The target fragment must be set for this fragment. The username is passed to the target
 * fragment in an intent by calling
 *
 * getTargetFragment().onActivityResult(...);
 *
 * The username has the name "username" in the returned intent.
 *
 */
public class NewChatDialogFragment extends DialogFragment{

    private static final String TAG = "NewChatDialogFragment";

    private AppCompatActivity myActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = (AppCompatActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_chat_dialog, container, false);
        final EditText usernameEditText = (EditText) view.findViewById(R.id.usernameEditText);
        final Button chatButton = (Button) view.findViewById(R.id.chatButton);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEditText.getText().toString();
                final Intent result = new Intent();
                result.putExtra("username", username);

                Utils.closeSoftKeyboard(myActivity, chatButton);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
                dismiss();
            }
        });
        getDialog().setTitle("New chat");
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
