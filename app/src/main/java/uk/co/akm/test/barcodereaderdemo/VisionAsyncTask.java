package uk.co.akm.test.barcodereaderdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Task that reads an image file and processes its contents using some Google Vision API function.
 * The Google Vision API processing is delegated to the parent activity. The result of such
 * processing is a string (e.g. barcode reading or OCR).
 *
 * Created by Thanos Mavroidis on 29/05/2017.
 */
public final class VisionAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = VisionAsyncTask.class.getSimpleName();

    private int targetBitmapWidth;
    private int targetBitmapHeight;
    private String imageFilePath;

    private AbstractVisionActivity parent;

    VisionAsyncTask(AbstractVisionActivity parent) {
        this.parent = parent;
    }

    /**
     * @param targetBitmapWidth the required x-scale of the bitmap or zero if the default scale is enough
     * @param targetBitmapHeight the required y-scale of the bitmap or zero if the default scale is enough
     * @param imageFilePath the absolute path of the stored image file
     */
    void setImageParameters(int targetBitmapWidth, int targetBitmapHeight, String imageFilePath) {
        this.targetBitmapWidth = targetBitmapWidth;
        this.targetBitmapHeight = targetBitmapHeight;
        this.imageFilePath = imageFilePath;
    }

    /**
     * Returns the decoded image as a string by performing the following sequence:
     *
     * <ol>
     *     <li>Read the stored image file</li>
     *     <li>Convert the stored image file to a bitmap</li>
     *     <li>Scale the bitmap appropriately, if required</li>
     *     <li>Decode the bitmap into a string using some Google Vision API functionality</li>
     * <ol/>
     *
     * @param params ignored
     *
     * @return the decoded image as a string
     */
    @Override
    protected String doInBackground(Void... params) {
        final Bitmap bitmap = readImageFile(targetBitmapWidth, targetBitmapHeight, imageFilePath);
        if (bitmap == null) {
            return null;
        }

        if (parent == null) {
            return null;
        } else {
            parent.setImageView(bitmap);
            return parent.decodeBitmapAsString(bitmap);
        }
    }

    private Bitmap readImageFile(int targetBitmapWidth, int targetBitmapHeight, String imageFilePath) {
        if (targetBitmapWidth <= 0 || targetBitmapHeight <= 0) {
            return BitmapFactory.decodeFile(imageFilePath); // No scale. Just read the image file.
        } else {
            try {
                return decodeFileToScale(targetBitmapWidth, targetBitmapHeight, imageFilePath); // Read the image file to the given scale.
            } catch (FileNotFoundException fnfe) {
                Log.d(TAG, "Could not decode stored image: image file not found.");
                return null;
            }
        }
    }

    private Bitmap decodeFileToScale(int targetBitmapWidth, int targetBitmapHeight, String imageFilePath) throws FileNotFoundException {
        final File imageFile = new File(imageFilePath);

        final BitmapFactory.Options bmOptions = measurePhotoDimensions(imageFile);
        final int photoWidth = bmOptions.outWidth;
        final int photoHeight = bmOptions.outHeight;

        final int scaleFactor = Math.min(photoWidth / targetBitmapWidth, photoHeight / targetBitmapHeight);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(new FileInputStream(imageFile), null, bmOptions);
    }

    private BitmapFactory.Options measurePhotoDimensions(File photoFile) throws FileNotFoundException {
        final BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(new FileInputStream(photoFile), null, bmOptions);

        return bmOptions;
    }

    public void onParentPause() {
        parent = null;
    }

    @Override
    protected void onPostExecute(String text) {
        if (text != null && parent != null) {
            try {
                parent.setTextView(text);
            } finally {
                parent.deleteStoredImage();
                parent.cancelVisionTask();
            }
        }
    }
}
