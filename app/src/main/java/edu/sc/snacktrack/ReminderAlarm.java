package edu.sc.snacktrack;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by spitzfor on 2/23/2016.
 */
public class ReminderAlarm extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //this.getParent().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hey you!");
        builder.setMessage("Did you forget to post an entry of your last snack or meal?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog aDiag = builder.create();
        aDiag.show();
    }
}
