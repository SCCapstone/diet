package edu.sc.snacktrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by spitzfor on 2/23/2016.
 */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

//        Intent alarm = new Intent(context, ReminderAlarm.class);

        Intent alarm = new Intent(".MainActivity");
        alarm.setClass(context, ReminderAlarm.class);
        alarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String pHour = hour > 12 ? String.valueOf(hour - 12) : String.valueOf(hour);
        int minute = now.get(Calendar.MINUTE);
        String pMinute = minute < 10 ? "" + String.valueOf(minute) : String.valueOf(minute);

        //Toast.makeText(context, "Fired at " + now.getTime(),Toast.LENGTH_LONG).show();
        Toast.makeText(context, "Fired at " + pHour + ":" + pMinute,Toast.LENGTH_LONG).show();
        context.startActivity(alarm);

    }
}
