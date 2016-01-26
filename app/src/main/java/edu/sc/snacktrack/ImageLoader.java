package edu.sc.snacktrack;

/**
 * Created by dowdw on 11/9/2015.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Matrix;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class ImageLoader {

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    // Handler to display images in UI thread
    Handler handler = new Handler();

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    final int stub_id = R.drawable.temp_img;

    public void DisplayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(url, imageView);
            //imageView.setImageResource(stub_id);
            imageView.setImageBitmap(null);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url, @Nullable final Integer scaleWidth,
                             @Nullable final Integer scaleHeight) {
        File f = fileCache.getFile(url);

        Bitmap b = decodeFile(f, scaleWidth, scaleHeight);
        if (b != null)
            return b;

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
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f, scaleWidth, scaleHeight);
            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }

    // Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f, @Nullable final Integer SCALE_WIDTH,
                              @Nullable final Integer SCALE_HEIGHT) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
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
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();

            // Rotate the bitmap based on the image file's EXIF data
            // This is a quick fix and likely not the most efficient solution, as two bitmaps must
            // be simultaneously loaded into memory.
            int rotation = Utils.getExifRotation(f);
            Matrix matrix = new Matrix();
            matrix.preRotate(rotation);

            // Return a bitmap with the correct rotation applied.
            return Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
            );

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;

                ViewGroup.LayoutParams imageViewLayoutParams = photoToLoad.imageView.getLayoutParams();
                Bitmap bmp = getBitmap(photoToLoad.url, imageViewLayoutParams.width, imageViewLayoutParams.height);
                memoryCache.put(photoToLoad.url, bmp);
                if (imageViewReused(photoToLoad))
                    return;
                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
                photoToLoad.imageView.startAnimation(
                        AnimationUtils.loadAnimation(photoToLoad.imageView.getContext(), R.anim.fadein)
                );
            }
            else {
                //photoToLoad.imageView.setImageResource(stub_id);
                photoToLoad.imageView.setImageBitmap(null);
            }

        }
    }

    /**
     * Releases the memory allocated to this ImageLoader.
     */
    public void releaseMemory(){
        memoryCache.clear();
    }

    /**
     * Deletes all files in this ImageLoader's cache.
     */
    public void clearFileCache(){
        fileCache.clear();
    }

    /**
     * Calls releaseMemory() and clearFileCache().
     *
     * That is, releases the memory allocated to this ImageLoader and deletes all files in
     * its cache.
     */
    public void clearEverything() {
        memoryCache.clear();
        fileCache.clear();
    }

}