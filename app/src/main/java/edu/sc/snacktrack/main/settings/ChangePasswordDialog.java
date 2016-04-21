package edu.sc.snacktrack.main.settings;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import edu.sc.snacktrack.login.LoginActivity;
import edu.sc.snacktrack.R;
import edu.sc.snacktrack.utils.Utils;

public class ChangePasswordDialog extends DialogFragment {

    private EditText passwordText;
    private EditText passwordConfirmText;
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

    private void setWidgetsEnabled(boolean enabled){
        submitButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_password_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        passwordText = (EditText) view.findViewById(R.id.password);
        passwordConfirmText = (EditText) view.findViewById(R.id.confirmPassword);
        submitButton = (Button) view.findViewById(R.id.new_password_submit);
        cancelButton = (Button) view.findViewById(R.id.new_password_cancel);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPasswordChange();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

////        final TextView tipText = (TextView) view.findViewById(R.id.password_tip);
////        headerText = (TextView) view.findViewById(R.id.password_dialog);
////        passwordText = (EditText) view.findViewById(R.id.new_password);
//        submitButton = (Button) view.findViewById(R.id.new_password_submit);
//        cancelButton = (Button) view.findViewById(R.id.new_password_cancel);
//
//        submitButton.setEnabled(false);
//        submitButton.setTextColor(Color.parseColor("#FF6666"));
////        tipText.setVisibility(View.GONE);
////        tipText.setTextColor(Color.parseColor("#FFFFFF"));
//
//        passwordText.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                v.setFocusable(true);
//                v.setFocusableInTouchMode(true);
//                return false;
//            }
//        });
//
//        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//
//                if (actionId == EditorInfo.IME_ACTION_DONE)
//                {
//                    passwordText.setFocusable(false);
//
//                    if (passwordText.getText().length() != 0)
//                    {
//                        submitButton.setEnabled(true);
//                        submitButton.setTextColor(Color.parseColor("#4DFF4D"));
////                        tipText.setVisibility(View.GONE);
//                        headerText.setPadding(20,20,20,0);
//                    }
//
//                    else
//                    {
//
//                        submitButton.setEnabled(false);
//                        headerText.setPadding(20,20,20,10);
////                        tipText.setVisibility(View.VISIBLE);
//
//                        ValueAnimator anim = new ValueAnimator();
//                        anim.setIntValues(Color.parseColor("#FFFFFF"), Color.parseColor("#FF6666"));
//                        anim.setEvaluator(new ArgbEvaluator());
//                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
////                                tipText.setTextColor((Integer) valueAnimator.getAnimatedValue());
//                            }
//                        });
//
//                        anim.setDuration(900);
//                        anim.setRepeatCount(2);
//                        anim.setRepeatMode(ValueAnimator.REVERSE);
//                        anim.start();
//                    }
//                }
//
//                return false;
//            }
//        });
//
//        //Submit changes and take user back to LoginActivity
//        submitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if(submitButton.isEnabled() == true)
//                {
//                    Utils.closeSoftKeyboard(getContext(), v);
//                    attemptPasswordChange();
//                    Intent intent = new Intent(getActivity(),LoginActivity.class);
//                    startActivity(intent);
//                    dismiss();
//                }
//            }
//        });
//
//        //Cancel changing password and dismiss dialogFragment
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Utils.closeSoftKeyboard(getContext(), v);
//                dismiss();
//            }
//        });

        return view;
    }

    /**
     * Checks if passwords are valid.
     *
     * Currently this method only checks if the passwords match or if either is blank.
     *
     * @param password The password
     * @param passwordConfirm The confirmed password
     * @param reason When not null, this method will append the reason the password is invalid
     *               (or "OK" if the username is valid).
     * @return true if the passwords are valid. false otherwise.
     */
    private boolean isPasswordValid(String password, String passwordConfirm,
                                    @Nullable StringBuilder reason){
        if(password.equals("")){
            if(reason != null){
                reason.append("Password is blank");
            }
            return false;
        }
        else if(passwordConfirm.equals("")){
            if(reason != null){
                reason.append("Password confirm is blank");
            }
            return false;
        }
        else if(!password.equals(passwordConfirm)){
            if(reason != null){
                reason.append("Passwords do not match");
            }
            return false;
        }
        else{
            if(reason != null){
                reason.append("OK");
            }
            return true;
        }
    }

    private void attemptPasswordChange() {
        final String password = passwordText.getText().toString();
        final String confirmPassword = passwordConfirmText.getText().toString();

        final ParseUser currentUser = ParseUser.getCurrentUser();

        StringBuilder invalidReason = new StringBuilder();

        if(isPasswordValid(password, confirmPassword, invalidReason)){
            currentUser.setPassword(passwordText.getText().toString());
            setWidgetsEnabled(false);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        ParseUser.logInInBackground(currentUser.getUsername(), password, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(e == null){
                                    Toast.makeText(
                                            cont,
                                            "Password change successful",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    dismiss();
                                } else{
                                    Toast.makeText(
                                            cont,
                                            "Please reenter your credentials",
                                            Toast.LENGTH_LONG
                                    ).show();
                                    dismiss();
                                    if(getActivity() != null){
                                        getActivity().finish();
                                        Intent loginIntent = new Intent(cont, LoginActivity.class);
                                        startActivity(loginIntent);
                                    }
                                }
                                setWidgetsEnabled(true);
                            }
                        });
                    } else if(cont != null){
                        Toast.makeText(
                                cont,
                                Utils.getErrorMessage(e),
                                Toast.LENGTH_LONG
                        ).show();
                        setWidgetsEnabled(true);
                    }
                }
            });
        } else if(cont != null){
            Toast.makeText(
                    cont,
                    invalidReason.toString(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
