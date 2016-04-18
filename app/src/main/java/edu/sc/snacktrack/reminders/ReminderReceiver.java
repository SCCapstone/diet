package edu.sc.snacktrack.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.List;

import edu.sc.snacktrack.snacks.SnackEntry;

/**
 * Created by spitzfor on 2/23/2016.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final long mealPeriod = 14400000;
//    private static final long mealPeriod = 300000;

    @Override
    public void onReceive(final Context context, Intent intent) {

        /**
         * Fire alarms only if user hasn't posted an entry in the past 4 hours (14400000 milliseconds)
         */
        ParseQuery<SnackEntry> query = ParseQuery.getQuery(SnackEntry.class);
        query.whereEqualTo("owner", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<SnackEntry>() {
            @Override
            public void done(List<SnackEntry> refreshedSnacks, ParseException e) {
                if (e == null)
                {
                    if(refreshedSnacks.size() != 0)
                    {
                        long entryDate = refreshedSnacks.get(0).getCreatedAt().getTime();
                        long now = Calendar.getInstance().getTimeInMillis();

                        if(entryDate < now - mealPeriod)
                        {
                            Intent alarm = new Intent(context, ReminderAlarm.class);
                            alarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(alarm);
                        }
                    }
                }

                else
                    Toast.makeText(context, "Something went wrong when fetching SnackList...", Toast.LENGTH_LONG).show();
            }
        });
    }
}
