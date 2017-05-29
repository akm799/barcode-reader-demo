package uk.co.akm.test.barcodereaderdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Helper activity for taking a picture. Subclasses can call the {@link #takePhoto()} or {@link #takePhoto(int, int)}
 * method to take a photo and then provide a {@link VisionAsyncTask} instance to read the stored image file, convert
 * it to a bitmap and then process the bitmap to, finally, return the processed result as a string. This activity
 * contains all the required photo-taking code so that we can present the bitmap processing code in isolation.
 *
 * Created by Thanos Mavroidis on 05/05/2017.
 */
public abstract class PhotoActivity extends AppCompatActivity {
    private static final String TAG = PhotoActivity.class.getSimpleName();

    private static final int REQUEST_TAKE_PHOTO = 7351;
    private static final String PHOTO_FILE_NAME = "last_photo_taken.jpg";
    private static final String PHOTO_FILE_PROVIDER_AUTHORITY = "uk.co.akm.test.barcodereaderdemo"; // This must match the authorities string specified in the file provider definition in AndroidManifest.xml

    private int targetBitmapWidth;
    private int targetBitmapHeight;
    private String imageFilePath;

    private VisionAsyncTask visionTask;

    @Override
    protected void onPause() {
        super.onPause();

        cancelVisionTask();
    }

    protected final void cancelVisionTask() {
        if (visionTask != null) {
            visionTask.onParentPause();
            visionTask = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        deleteStoredImage(); // Make sure we have no image file left over.
    }

    /**
     * Take a photo that will result in a bitmap.
     */
    protected final void takePhoto() {
        takePhoto(0, 0);
    }

    /**
     * Take a photo that will result in a bitmap scaled with the minimum sample factor according to
     * the specified target width or height.
     *
     * @param targetBitmapWidth the specified target bitmap width
     * @param targetBitmapHeight the specified target bitmap height
     */
    protected final void takePhoto(int targetBitmapWidth, int targetBitmapHeight) {
        deleteStoredImage(); // Delete any previous image, before we start.

        this.targetBitmapWidth = targetBitmapWidth;
        this.targetBitmapHeight = targetBitmapHeight;
        dispatchPhotoCaptureIntent();
    }

    private void dispatchPhotoCaptureIntent() {
        final Intent takePhotoIntent = buildPhotoCaptureIntent();
        if (takePhotoIntent != null) {
            startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
        }
    }

    private Intent buildPhotoCaptureIntent() {
        final Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent.
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go.
            final File photoFile = createImageFile();

            // Continue only if the File was successfully created.
            if (photoFile != null) {
                final Uri photoURI = FileProvider.getUriForFile(this, PHOTO_FILE_PROVIDER_AUTHORITY, photoFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                return takePhotoIntent;
            } else {
                toast("Could not create file to store photo.");
                return null;
            }
        } else {
            toast("No app found to take photo.");
            return null;
        }
    }

    private File createImageFile() {
        final File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            final File imageFile = new File(storageDir, PHOTO_FILE_NAME);
            imageFile.createNewFile();

            // Save a file: path for used with ACTION_IMAGE_CAPTURE intent to create the file that will hold the photo image.
            // We use this path to access and read the image file and to delete it, once we are done with it.
            imageFilePath = imageFile.getAbsolutePath();

            return imageFile;
        } catch (IOException ioe) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                launchImageReadingAndProcessingTask();
            } else {
                toast("Error when taking photo. Result code: " + resultCode);
            }
        }
    }

    private void launchImageReadingAndProcessingTask() {
        visionTask = buildVisionTask();
        visionTask.setImageParameters(targetBitmapWidth, targetBitmapHeight, imageFilePath);
        visionTask.execute((Void) null);
    }

    /**
     * Returns a {@link VisionAsyncTask} instance that will perform the following sequence:
     * <ol>
     *     <li>Read the stored image file</li>
     *     <li>Convert the stored image file to a bitmap</li>
     *     <li>Scale the bitmap appropriately</li>
     *     <li>Decode the bitmap into a string using some Google Vision API functionality</li>
     *     <li>Communicate the decoded string to the parent activity</li>
     *     <li>Delete the image file processed, since it is no longer needed</li>
     * <ol/>
     */
    protected abstract VisionAsyncTask buildVisionTask();

    protected final void deleteStoredImage() {
        if (imageFilePath != null) {
            if (!(new File(imageFilePath)).delete()) {
                Log.w(TAG, "Could not delete stored image.");
            } else {
                imageFilePath = null;
                targetBitmapWidth = 0;
                targetBitmapHeight = 0;
                Log.d(TAG, "Stored image file deleted.");
            }
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
