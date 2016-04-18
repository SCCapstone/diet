package edu.sc.snacktrack.reminders;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import edu.sc.snacktrack.R;

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
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("SnackTrack Reminder");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage("Make sure to keep posting your entries!");
            builder.setCancelable(false);
            builder.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });

        AlertDialog aDiag = builder.create();
        aDiag.show();

        TextView messageView = (TextView)aDiag.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);

        TextView titleView = (TextView)aDiag.findViewById(getApplicationContext().getResources().getIdentifier("alertTitle", "id", "android"));
        if (titleView != null) {
            titleView.setGravity(Gravity.CENTER);
        }
    }
}
