package uk.co.akm.test.barcodereaderdemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

/**
 * Simple barcode scanning example. This activity contains a single "Scan" button to scan a barcode.
 * Once the photo of the barcode has been taken, the summary of the decoded barcode information is
 * displayed on the screen (together with the barcode photo taken). Please note that all the photo-taking
 * related code is handled by the #PhotoActivity subclass. This activity contains only the barcode-reading
 * related code.
 *
 *  Created by Thanos Mavroidis on 05/05/2017.
 */
public class BarcodeReaderActivity extends PhotoActivity {
    private TextView textView;
    private ImageView photoView;

    private BarcodeDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_reader);

        detector = setUpBarcodeDetector();

        textView = (TextView) findViewById(R.id.message);
        photoView = (ImageView) findViewById(R.id.photo);
    }

    // Please note that first time set up might fail, on the device, due to time required to download necessary files.
    private BarcodeDetector setUpBarcodeDetector() {
        final BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext()).build();

        if (detector.isOperational()) {
            return detector;
        } else {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        detector.release();
        detector = null;
    }

    // Scan button clicked.
    public void onScan(View view) {
        if (detector == null) {
            textView.setText("Could not set up the detector!");
        } else {
            takePhoto(600, 600); // Important: Have to scale down the photo image (in this case to 600x600 or so) from the full picture or else QR codes cannot be recognised.
        }
    }

    @Override
    protected void onPhotoTaken(Bitmap photo) {
        readAndDisplayBarCode(photo);
        photoView.setImageBitmap(photo);
    }

    private void readAndDisplayBarCode(Bitmap barcodeImage) {
        if (barcodeImage == null) {
            textView.setText("No barcode picture.");
        } else {
            final Barcode barcode = readBarCode(barcodeImage);
            if (barcode == null) {
                textView.setText("Could not decode barcode picture.");
            } else {
                textView.setText(buildBarCodeInfo(barcode));
            }
        }
    }

    private Barcode readBarCode(Bitmap barcodeImage) {
        final Frame frame = new Frame.Builder().setBitmap(barcodeImage).build();
        final SparseArray<Barcode> barcodes = detector.detect(frame);

        if (barcodes != null && barcodes.size() > 0) {
            return barcodes.valueAt(0);
        } else {
            return null;
        }
    }

    private String buildBarCodeInfo(Barcode barcode) {
        final String formattedRawValue = BarcodeDataFormatter.formatBarCodeNumber(barcode.rawValue);

        final String format = BarcodeDataFormatter.getFormatString(barcode);
        final String valueFormat = BarcodeDataFormatter.getValueFormatString(barcode);
        final String formats = ("(" + format + ", " + valueFormat + ")");

        return (formattedRawValue + "\n" + formats);
    }
}
