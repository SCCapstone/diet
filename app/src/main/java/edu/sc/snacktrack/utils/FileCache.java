package edu.sc.snacktrack.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;

/**
 * This class represents a file cache, primarily for pictures downloaded from the Internet.
 */
public class FileCache {

    /**
     * The directory of the file cache.
     */
    private File cacheDir;

    /**
     * Creates a new FileCache for a specific context.
     *
     * @param context The context.
     */
    public FileCache(Context context) {
        // Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(
                    android.os.Environment.getExternalStorageDirectory(),
                    "snacktrack");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    /**
     * Gets the file for a specific url. The file may not exist or be incomplete.
     *
     * @param url The url the file was downloaded from.
     * @return The file object
     */
    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        // String filename = URLEncoder.encode(url);
        File f = new File(cacheDir, filename);
        return f;

    }

    /**
     * Creates an empty "temporary" file in the cache directory. This file should eventually be
     * renamed or deleted.
     *
     * @param suffix The suffix of the file name
     * @param prefix Typically the file extension (.png, .jpg, etc.)
     * @return The temporary file
     * @throws IOException If an error occurs writing to the file.
     */
    public File createTempFile(String suffix, String prefix) throws IOException{
        return File.createTempFile(suffix, prefix, cacheDir);
    }

    /**
     * Creates an empty "temporary" file with the name "temp" in the cache directory. This file
     * should eventually be renamed or deleted.
     *
     * @return The temporary file
     * @throws IOException
     */
    public File createTempFile() throws IOException{
        return this.createTempFile("temp", "");
    }

    /**
     * Attempts to delete all files in this file cache.
     */
    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}
