package uk.co.akm.test.barcodereaderdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Detector;

/**
 * This is a simple demo activity that uses the Google Vision API to process a photo taken by the
 * user and display the processing result as a single string. For example, the string could be the
 * number of a barcode or the text read by some OCR function. The activity contains a single text
 * view that will display the decoding result and an image view to display the photo taken.
 *
 * @param <D> the object detected by the Gogle Vision API inside the image. Examples of such
 *           parameters are {@link com.google.android.gms.vision.barcode.Barcode}
 *           and {@link com.google.android.gms.vision.text.TextBlock}
 *
 * Created by Thanos Mavroidis on 29/05/2017.
 */
public abstract class AbstractVisionActivity<D> extends PhotoActivity {
    private static final String TAG = AbstractVisionActivity.class.getSimpleName();

    private TextView textView;
    private ImageView photoView;

    private Detector<D> detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        setTitle(getTitleText());

        detector = setUpDetector();

        textView = (TextView) findViewById(getTextViewResId());
        photoView = (ImageView) findViewById(getPhotoViewResId());
    }

    protected abstract CharSequence getTitleText();

    @LayoutRes
    protected abstract int getLayoutResId();

    @IdRes
    protected abstract int getTextViewResId();

    @IdRes
    protected abstract int getPhotoViewResId();

    private Detector<D> setUpDetector() {
        final Detector<D> detector = buildDetector(getApplicationContext());
        if (detector == null) {
            return null;
        }

        if (detector.isOperational()) {
            return detector;
        } else {
            Log.d(TAG, "Detector is not operational.");
            return null;
        }
    }

    /**
     * Returns a suitable Google Vision API detector.
     *
     * @param context the context required to build the detector
     * @return a suitable Google Vision API detector
     */
    protected abstract Detector<D> buildDetector(Context context);

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (detector != null) {
            detector.release();
            detector = null;
        }
    }

    // Scan button clicked.
    public final void onScan(View view) {
        if (detector == null) {
            textView.setText("Could not set up the detector.");
        } else {
            final int scale = getPhotoScale();
            if (scale > 0) {
                takePhoto(600, 600);
            } else {
                takePhoto();
            }
        }
    }

    /**
     * Override this method to provide a specific scale for the bitmap that will be read from the
     * stored image file. If this method is not overriden, then the default scale will be used.
     */
    protected int getPhotoScale() {
        return 0;
    }

    public final boolean hasDetector() {
        return (detector != null);
    }

    public final Detector<D> getDetector() {
        return detector;
    }

    public final void setImageView(final Bitmap photo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                photoView.setImageBitmap(photo);
            }
        });
    }

    public final void setTextView(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }
}
