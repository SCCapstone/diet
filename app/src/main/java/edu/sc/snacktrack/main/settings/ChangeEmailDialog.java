package edu.sc.snacktrack.main.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.utils.Utils;

/**
 * Created by spitzfor on 2/1/2016.
 */
public class ChangeEmailDialog extends DialogFragment {

    private EditText emailText;
    private Button submitButton;
    public Context cont;

    public ChangeEmailDialog() {
        //Empty constructor required
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cont = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_email_dialog, container);

        getDialog().setTitle("Change Email");

        emailText = (EditText) view.findViewById(R.id.new_email);
        submitButton = (Button) view.findViewById(R.id.new_email_button);

        //Submit new password and close dialog when Submit button is pressed
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeSoftKeyboard(getContext(), v);
                //attemptPasswordChange();
                dismiss();
                //logout();
            }
        });

        //int style = DialogFragment.STYLE_NORMAL, theme = 0;
        //setStyle(style,theme);
        return view;
    }
}
