package edu.sc.snacktrack.settings;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

import com.parse.ParseUser;

import edu.sc.snacktrack.LoginActivity;
import edu.sc.snacktrack.R;
import edu.sc.snacktrack.Utils;

public class ChangePasswordDialog extends DialogFragment {

    private TextView headerText;
    private EditText passwordText;
    private Button submitButton;
    private Button cancelButton;
    public Context cont;

    public ChangePasswordDialog() {
        //Empty constructor required
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cont = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_password_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        final TextView tipText = (TextView) view.findViewById(R.id.password_tip);
        headerText = (TextView) view.findViewById(R.id.password_dialog);
        passwordText = (EditText) view.findViewById(R.id.new_password);
        submitButton = (Button) view.findViewById(R.id.new_password_submit);
        cancelButton = (Button) view.findViewById(R.id.new_password_cancel);

        submitButton.setEnabled(false);
        submitButton.setTextColor(Color.parseColor("#FF6666"));
        tipText.setVisibility(View.GONE);
        tipText.setTextColor(Color.parseColor("#FFFFFF"));

        passwordText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });

        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    passwordText.setFocusable(false);

                    if (passwordText.getText().length() != 0)
                    {
                        submitButton.setEnabled(true);
                        submitButton.setTextColor(Color.parseColor("#4DFF4D"));
                        tipText.setVisibility(View.GONE);
                        headerText.setPadding(20,20,20,0);
                    }

                    else
                    {

                        submitButton.setEnabled(false);
                        headerText.setPadding(20,20,20,10);
                        tipText.setVisibility(View.VISIBLE);

                        ValueAnimator anim = new ValueAnimator();
                        anim.setIntValues(Color.parseColor("#FFFFFF"), Color.parseColor("#FF6666"));
                        anim.setEvaluator(new ArgbEvaluator());
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                tipText.setTextColor((Integer) valueAnimator.getAnimatedValue());
                            }
                        });

                        anim.setDuration(900);
                        anim.setRepeatCount(2);
                        anim.setRepeatMode(ValueAnimator.REVERSE);
                        anim.start();
                    }
                }

                return false;
            }
        });

        //Submit changes and take user back to LoginActivity
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(submitButton.isEnabled() == true)
                {
                    Utils.closeSoftKeyboard(getContext(), v);
                    attemptPasswordChange();
                    Intent intent = new Intent(getActivity(),LoginActivity.class);
                    startActivity(intent);
                    dismiss();
                }
            }
        });

        //Cancel changing password and dismiss dialogFragment
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeSoftKeyboard(getContext(), v);
                dismiss();
            }
        });

        return view;
    }

    private void attemptPasswordChange() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.setPassword(passwordText.getText().toString());
        currentUser.saveInBackground();
    }
}
