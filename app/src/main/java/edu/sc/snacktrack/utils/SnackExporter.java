package edu.sc.snacktrack.utils;

import android.os.Environment;

import com.opencsv.CSVWriter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.sc.snacktrack.snacks.SnackEntry;

public class SnackExporter {

    private static final String TAG = "SnackExporter";

    public static final int EXPORT_LAST_24_HOURS = 0;
    public static final int EXPORT_LAST_48_HOURS = 1;
    public static final int EXPORT_EVERYTHING = 2;

    public interface Callback{
        void callback(File file);
    }

    public static void export(final ParseUser user, int mode, final Callback callback){
        getSnacks(user, mode, new FindCallback<SnackEntry>() {
            @Override
            public void done(List<SnackEntry> objects, ParseException e) {
                File file = null;
                if(e == null){
                    try{
                        file = generateCSV(user, objects);
                    } catch(IOException ioe){
                        file = null;
                    }
                }

                callback.callback(file);
            }
        });
    }

    /**
     * Returns the export directory for user with path like
     *   path/to/downloads/dir/SnackTrackExports/username/
     *
     * @param user The user
     * @return The directory
     */
    public static File getExportDirectory(ParseUser user){
        return new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "SnackTrackExports/" + user.getUsername() + "/"
        );
    }

    private static void getSnacks(ParseUser user, int mode, FindCallback<SnackEntry> callback){

        final long millis24Hours = 1000 * 60 * 60 * 24;

        ParseQuery<SnackEntry> query = new ParseQuery<>(SnackEntry.class);
        query.whereEqualTo("owner", user);

        switch(mode){
            case EXPORT_LAST_24_HOURS:
                query.whereGreaterThanOrEqualTo("createdAt", new Date(System.currentTimeMillis() - millis24Hours));
                break;
            case EXPORT_LAST_48_HOURS:
                query.whereGreaterThanOrEqualTo("createdAt", new Date(System.currentTimeMillis() - millis24Hours * 2));
        }
        query.findInBackground(callback);
    }

    private static void getSnacks(ParseUser user, Date lowerBound, Date upperBound, FindCallback<SnackEntry> callback){
        ParseQuery<SnackEntry> query = new ParseQuery<>(SnackEntry.class);
        query.whereEqualTo("owner", user);
        query.whereGreaterThanOrEqualTo("createdAt", lowerBound);
        query.whereLessThanOrEqualTo("createdAt", upperBound);
        query.findInBackground(callback);
    }

    private static File getFile(ParseUser user){
        final String filePrefix = "SnackTrackExport-"
                + new SimpleDateFormat("yy-MM-dd").format(System.currentTimeMillis());
        final String extension = ".csv";

        if(!getExportDirectory(user).exists()){
            getExportDirectory(user).mkdirs();
        }

        File file = new File(getExportDirectory(user), filePrefix + extension);

        // Do not overwrite existing exports
        if(file.exists()){
            int appendInt = 0;
            while(file.exists()){
                file = new File(getExportDirectory(user), filePrefix + '-' + (++appendInt) + extension);
            }
        }

        return file;
    }

    private static File generateCSV(ParseUser user, List<SnackEntry> entries) throws IOException {
        final File file = getFile(user);

        final CSVWriter writer = new CSVWriter(new FileWriter(file));

        final String[] headerRow = new String[]{
                "username", "date", "photo link", "meal type", "description"
        };


        writer.writeNext(headerRow);

        for(int i = 0; i < entries.size(); ++i){
            SnackEntry entry = entries.get(i);
            if(entry == null) continue;

            String[] row = new String[]{
                    entry.getOwner().getUsername(),
                    SimpleDateFormat.getInstance().format(entry.getCreatedAt()),
                    entry.getPhoto() != null ? entry.getPhoto().getUrl() : "",
                    entry.getMealType() != null ? entry.getMealType() : "",
                    entry.getDescription() != null ? entry.getDescription() : "",
            };
            writer.writeNext(row);
        }

        writer.close();

        return file;
    }
}
