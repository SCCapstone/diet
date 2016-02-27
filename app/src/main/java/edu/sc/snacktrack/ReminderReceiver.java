package edu.sc.snacktrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by spitzfor on 2/23/2016.
 */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Intent alarm = new Intent(context, ReminderAlarm.class);
        alarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        Calendar now = Calendar.getInstance();
//            int hour = now.get(Calendar.HOUR_OF_DAY);
//            String pHour = hour > 12 ? String.valueOf(hour - 12) : String.valueOf(hour);
//            int minute = now.get(Calendar.MINUTE);
//            String pMinute = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
//
//        Log.i("Testing","Alarm fired at: " + Calendar.getInstance().getTimeInMillis());
//        Log.i("Testing","Alarm fired at: " + pHour + ":" + pMinute);
        context.startActivity(alarm);
    }
}
