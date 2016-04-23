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

/**
 * Utilities class for exporting a user's entries.
 */
public class SnackExporter {

    private static final String TAG = "SnackExporter";

    /**
     * Mode for exporting the last 24 hours.
     */
    public static final int EXPORT_LAST_24_HOURS = 0;

    /**
     * Mode for exporting the last 48 hours.
     */
    public static final int EXPORT_LAST_48_HOURS = 1;

    /**
     * Mode for exporting everything (no constraint on createdAt).
     */
    public static final int EXPORT_EVERYTHING = 2;

    /**
     * Callback interface.
     *
     * Interface definition for a callback to be invoked when exporting is complete.
     */
    public interface Callback{

        /**
         * Called when exporting is complete.
         *
         * @param file File containing the CSV export. null if exporting failed.
         */
        void callback(File file);
    }

    /**
     * Exports the snack entries for a specified user. The current user must have access to the
     * specified user's entries for this method to work.
     *
     * @param user The user for which to export entries.
     * @param mode Export mode (EXPORT_LAST_24_HOURS, EXPORT_LAST_48_HOURS, EXPORT_EVERYTHING).
     * @param callback The callback to invoke upon completion. The callback will specify the
     *                 export file.
     */
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
     * Returns the export directory for a user with path like
     *   path/to/downloads/dir/SnackTrackExports/username/
     *
     * @param user Which user's export directory.
     * @return The directory.
     */
    public static File getExportDirectory(ParseUser user){
        return new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "SnackTrackExports/" + user.getUsername() + "/"
        );
    }

    /**
     * Fetches the list of snack entries for a user using a specified mode. If the mode is not
     * recognized, the mode defaults to EXPORT_EVERYTHING.
     *
     * @param user Which user's entries to fetch.
     * @param mode The mode to use when fetching (EXPORT_LAST_24_HOURS, EXPORT_LAST_48_HOURS,
     *             EXPORT_EVERYTHING).
     * @param callback The callback to invoke upon completion.
     */
    private static void getSnacks(ParseUser user, int mode, FindCallback<SnackEntry> callback){

        // Milliseconds in a day.
        final long millis24Hours = 1000 * 60 * 60 * 24;

        ParseQuery<SnackEntry> query = new ParseQuery<>(SnackEntry.class);
        query.whereEqualTo("owner", user);

        switch(mode){
            case EXPORT_LAST_24_HOURS:
                query.whereGreaterThanOrEqualTo("createdAt", new Date(System.currentTimeMillis() - millis24Hours));
                break;
            case EXPORT_LAST_48_HOURS:
                query.whereGreaterThanOrEqualTo("createdAt", new Date(System.currentTimeMillis() - millis24Hours * 2));
                break;

            // case EXPORT_EVERYTHING: no constraint on createdAt.

            // default: no constraint on createdAt (export everything)
        }
        query.findInBackground(callback);
    }

    /**
     * Fetches the list of snack entries for a user using an explicit lower and upper bound
     * on the createdAt field.
     *
     * @param user Which user's entries to fetch.
     * @param lowerBound The lower bound on the createdAt date.
     * @param upperBound The upper bound on the createdAt date.
     * @param callback The callback to invoke upon completion.
     */
    private static void getSnacks(ParseUser user, Date lowerBound, Date upperBound, FindCallback<SnackEntry> callback){
        ParseQuery<SnackEntry> query = new ParseQuery<>(SnackEntry.class);
        query.whereEqualTo("owner", user);
        query.whereGreaterThanOrEqualTo("createdAt", lowerBound);
        query.whereLessThanOrEqualTo("createdAt", upperBound);
        query.findInBackground(callback);
    }

    /**
     * Gets a file with a unique name to store a user's export. The file's directory is
     * SnackExporter.getExportDirectory().
     *
     * The file's name is formatted based on the current date like:
     *
     * SnackTrackExport-yy-MM-dd-n.csv
     *
     * where yy is year, MM is month, dd is day. n is an integer that is only appended if necessary
     * to avoid overwriting existing exports.
     *
     * @param user Which user the export is for.
     * @return The file to store the export in.
     */
    private static File getFile(ParseUser user){
        final String filePrefix = "SnackTrackExport-"
                + new SimpleDateFormat("yy-MM-dd").format(System.currentTimeMillis());
        final String extension = ".csv";

        if(!getExportDirectory(user).exists()){
            getExportDirectory(user).mkdirs();
        }

        File file = new File(getExportDirectory(user), filePrefix + extension);

        // Do not overwrite existing exports.
        if(file.exists()){
            int appendInt = 0;
            while(file.exists()){
                file = new File(getExportDirectory(user), filePrefix + '-' + (++appendInt) + extension);
            }
        }

        return file;
    }

    /**
     * Generates the export CSV file after the entries have been fetched.
     *
     * @param user The user whose entries we are exporting.
     * @param entries The fetched entries to export.
     *
     * @return The export file.
     *
     * @throws IOException If the file cannot be opened for writing or other bad things happen.
     */
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