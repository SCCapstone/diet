package edu.sc.snacktrack.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import edu.sc.snacktrack.R;

/**
 * Class for loading images from urls into ImageViews.
 */
public class ImageLoader {

    private LruCache<String, Bitmap> memoryCache;
    private FileCache fileCache;

    private Context context;

    private static ImageLoader instance;

    /**
     * Private constructor to prevent multiple instances. Use getInstance() to get the current
     * instance of ImageLoader.
     */
    private ImageLoader(Context context) {
        // We will use 25% of available memory for the memory cache.
        final int ratio = 4; // Use 1/ratio of available memory (1/4 = 25%)
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / ratio;

        this.context = context;
        fileCache = new FileCache(context);
        memoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    /**
     * Returns the current ImageLoader instance.
     *
     * @return The ImageLoader instance
     */
    public static ImageLoader getInstance(Context context){
        if(instance == null){
            instance = new ImageLoader(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Displays an image at a specified url in a specified ImageView.
     *
     * @param file The image file to display
     * @param imageView The image view to hold the image.
     */
    public void displayImage(File file, ImageView imageView){
        if(cancelPotentialWork(file.getAbsolutePath(), imageView)){
            Bitmap bitmap = memoryCache.get(file.getAbsolutePath());

            if(bitmap != null){
                imageView.setImageBitmap(bitmap);
            } else{
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute("file", file.getAbsolutePath());
            }
        }
    }

    /**
     * Displays an image at a specified url in a specified ImageView.
     *
     * @param url The url pointing to an image.
     * @param imageView The image view to hold the image.
     */
    public void displayImage(String url, ImageView imageView){
        if(cancelPotentialWork(url, imageView)){
            Bitmap bitmap = memoryCache.get(url);

            if(bitmap != null){
                imageView.setImageBitmap(bitmap);
            } else{
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(url);
            }
        }
    }

    /**
     * Retrieves the BitmapWorkerTask associated with a specified ImageView.
     *
     * @param imageView The ImageView in question
     * @return The BitmapWorkerTask associated with imageView or null if imageView does
     *         not have one.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView){
        if(imageView != null){
            final Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable){
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * Checks if another task is already associated with a specified ImageView. If so, attempts
     * to cancel it. If the existing task is already loading the specified url, the task is not
     * canceled.
     *
     * @param url The new image url to load.
     * @param imageView The ImageView to check.
     * @return true if the task was canceled or does not exist. false otherwise.
     */
    public static boolean cancelPotentialWork(String url, ImageView imageView){
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if(bitmapWorkerTask != null){
            final String bitmapUrl = bitmapWorkerTask.url;
            // if the task's url is null or differs from the new url, cancel the task
            if(bitmapUrl == null || bitmapUrl != url){
                bitmapWorkerTask.cancel(true);
            }
            // Otherwise, the some work is already in progress
            else{
                return false;
            }
        }

        return true;
    }

    /**
     * Releases the memory allocated to this ImageLoader.
     */
    public void releaseMemory(){
        memoryCache.evictAll();
    }

    /**
     * Decodes image and scales it to reduce memory consumption.
     *
     * @param file The file to load from.
     * @param SCALE_WIDTH The width to scale the bitmap to.
     * @param SCALE_HEIGHT The height to scale the bitmap to.
     * @return The Bitmap decoded from the specified file. null if an exception occurs.
     */
    private static Bitmap decodeFile(File file, @Nullable final Integer SCALE_WIDTH,
                              @Nullable final Integer SCALE_HEIGHT) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(file);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE;
            final int DEFAULT_REQUIRED_SIZE = 100;
            final boolean SCALE_WIDTH_VALID = SCALE_WIDTH != null && SCALE_WIDTH > 0;
            final boolean SCALE_HEIGHT_VALID = SCALE_HEIGHT != null && SCALE_HEIGHT > 0;

            if(!SCALE_WIDTH_VALID && !SCALE_HEIGHT_VALID){
                // Case 1: Both the width and height are invalid
                // Use the default required size.

                REQUIRED_SIZE = DEFAULT_REQUIRED_SIZE;
            } else if(!SCALE_WIDTH_VALID && SCALE_HEIGHT_VALID){
                // Case 2: Only the height is valid.
                // Use the height as the required size.

                REQUIRED_SIZE = SCALE_HEIGHT;
            } else if(SCALE_WIDTH_VALID && !SCALE_HEIGHT_VALID){
                // Case 3: Only the width is valid.
                // Use the width as the required size.

                REQUIRED_SIZE = SCALE_WIDTH;
            } else{
                // Case 4: Both the width and height are valid.
                // Use the largest of the two as the required size.

                REQUIRED_SIZE = Math.max(SCALE_WIDTH, SCALE_HEIGHT);
            }

            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), o2);

            // BitmapFactory.decodeFile() will return null if the file is corrupt (that is,
            // incomplete). The file may be corrupt if a previous async task was interrupted
            // (cancelled) while writing it. In this case, we return null.
            if(bitmap == null){
                return null;
            }

            // Rotate the bitmap based on the image file's EXIF data
            // This is a quick fix and likely not the most efficient solution, as two bitmaps must
            // be simultaneously loaded into memory.
            int rotation = Utils.getExifRotation(file);
            Matrix matrix = new Matrix();
            matrix.preRotate(rotation);

            // Return a bitmap with the correct rotation applied.
            return Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
            );

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * AsyncTask for loading an image from url into an ImageView
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewRef;
        private int scaleWidth;
        private int scaleHeight;

        private String url;

        public BitmapWorkerTask(ImageView imageView){
            ViewGroup.LayoutParams imageViewLayoutParams = imageView.getLayoutParams();

            imageViewRef = new WeakReference<>(imageView);
            scaleWidth = imageViewLayoutParams.width;
            scaleHeight = imageViewLayoutParams.height;

        }


        /**
         * Downloads an image from the internet or retrieves it from the file cache if it is there.
         *
         * @param url The url of the image.
         * @param scaleWidth The width to scale the bitmap to.
         * @param scaleHeight The height to scale the bitmap to.
         * @return The resulting Bitmap.
         */
        private Bitmap getBitmap(String url, @Nullable final Integer scaleWidth,
                                 @Nullable final Integer scaleHeight) {
            File file = fileCache.getFile(url);

            if(file.exists()){
                Bitmap b = decodeFile(file, scaleWidth, scaleHeight);
                if (b != null)
                    return b;
            }

            // Download Images from the Internet
            try {
                Bitmap bitmap = null;
                URL imageUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageUrl
                        .openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(file);
                Utils.CopyStream(is, os);
                os.close();
                conn.disconnect();
                bitmap = decodeFile(file, scaleWidth, scaleHeight);
                return bitmap;
            } catch (Throwable ex) {
                ex.printStackTrace();
                if (ex instanceof OutOfMemoryError) {
                    memoryCache.evictAll();
                }
                return null;
            }
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            if(params.length == 1){
                url = params[0];
                bitmap = getBitmap(url, scaleWidth, scaleHeight);
            } else if(params.length == 2 && params[0].equals("file")){
                url = params[1];
                bitmap = decodeFile(new File(params[1]), scaleWidth, scaleHeight);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(isCancelled()){
                bitmap = null;
            }

            if(imageViewRef != null && bitmap != null){
                final ImageView imageView = imageViewRef.get();

                memoryCache.put(url, bitmap);

                if(imageView != null){
                    imageView.setImageBitmap(bitmap);
                    imageView.startAnimation(
                        AnimationUtils.loadAnimation(imageView.getContext(), R.anim.fadein)
                    );
                }
            }
        }
    }

    /**
     * This BitmapDrawable subclass is used to display a placeholder image in an ImageView while a
     * worker task loads the real image. To alleviate concurrency problems that may arise when
     * loading images into a ListView, this class stores a weak reference back the the worker task.
     * The worker task may be retrieved by calling getBitmapWorkerTask().
     */
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskRef;

        public AsyncDrawable(Resources resources, Bitmap bitmap, BitmapWorkerTask workerTask){
            super(resources, bitmap);
            bitmapWorkerTaskRef = new WeakReference<>(workerTask);
        }

        /**
         * Gets the worker task for this AsyncDrawable.
         *
         * @return The worker task.
         */
        public BitmapWorkerTask getBitmapWorkerTask(){
            return bitmapWorkerTaskRef.get();
        }
    }
}