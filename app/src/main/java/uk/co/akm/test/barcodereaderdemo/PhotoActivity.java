package uk.co.akm.test.barcodereaderdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Helper activity for taking a picture. Subclasses can call the {@link #takePhoto()} or {@link #takePhoto(int, int)}
 * method to take a photo and then receive a callback with the {@link #onPhotoTaken(Bitmap)} method when the photo
 * has been taken. This activity contains all the required photo-taking code so that we can present the barcode reading
 * code in isolation.
 *
 * Created by Thanos Mavroidis on 05/05/2017.
 */
public abstract class PhotoActivity extends AppCompatActivity {
    private static final int REQUEST_TAKE_PHOTO = 7351;
    private static final String PHOTO_FILE_NAME = "last_photo_taken.jpg";
    private static final String PHOTO_FILE_PROVIDER_AUTHORITY = "uk.co.akm.test.barcodereaderdemo"; // This must match the authorities string specified in the file provider definition in AndroidManifest.xml

    private int targetBitmapWidth;
    private int targetBitmapHeight;
    private String imageFilePath;

    @Override
    public void onDestroy() {
        super.onDestroy();

        deleteStoredImage();
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
    protected final void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                final Bitmap photo = readImageFile(targetBitmapWidth, targetBitmapHeight, imageFilePath);
                if (photo == null) {
                    toast("Could not read stored image.");
                } else {
                    onPhotoTaken(photo);
                }
            } else {
                toast("Error when taking photo. Result code: " + resultCode);
            }
        }
    }

    private Bitmap readImageFile(int targetBitmapWidth, int targetBitmapHeight, String imageFilePath) {
        if (targetBitmapWidth <= 0 || targetBitmapHeight <= 0) {
            return BitmapFactory.decodeFile(imageFilePath); // No scale. Just read the image file.
        } else {
            try {
                return decodeFileToScale(targetBitmapWidth, targetBitmapHeight, imageFilePath); // Read the image file to the given scale.
            } catch (FileNotFoundException fnfe) {
                toast("Could not decode stored image: image file not found.");
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

    /**
     * Process the bitmap resulting from the photo taken.
     *
     * @param photo the bitmap of the photo taken
     */
    protected abstract void onPhotoTaken(Bitmap photo);

    private void deleteStoredImage() {
        if (imageFilePath != null) {
            if (!(new File(imageFilePath)).delete()) {
                toast("Could not delete stored image.");
            } else {
                imageFilePath = null;
                targetBitmapWidth = 0;
                targetBitmapHeight = 0;
            }
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
