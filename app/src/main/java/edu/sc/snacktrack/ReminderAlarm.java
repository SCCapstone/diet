package edu.sc.snacktrack;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.Calendar;

/**
 * Created by spitzfor on 2/23/2016.
 */
public class ReminderAlarm extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(400);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

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
        Log.i("Testing", "Time alarm went off: " + Calendar.getInstance().getTimeInMillis());

        AlertDialog aDiag = builder.create();
        aDiag.show();
    }
}
