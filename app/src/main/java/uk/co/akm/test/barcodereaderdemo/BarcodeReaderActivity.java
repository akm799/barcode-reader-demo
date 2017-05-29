package uk.co.akm.test.barcodereaderdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Detector;
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
public class BarcodeReaderActivity extends AbstractVisionActivity<Barcode> {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_barcode_reader;
    }

    @Override
    protected int getTextViewResId() {
        return R.id.message;
    }

    @Override
    protected int getPhotoViewResId() {
        return R.id.photo;
    }

    @Override
    protected Detector<Barcode> buildDetector(Context context) {
        return new BarcodeDetector.Builder(context).build();
    }

    @Override
    protected int getPhotoScale() {
        return 600; // Important: If the default scale is used, then the bitmap will be too large for QR-code reading. So here we provide a scale that will suitably limit the image size.
    }

    @Override
    protected String decodeBitmapAsString(Detector<Barcode> detector, Bitmap bitmap) {
        final Barcode barcode = readBarCode(detector, bitmap);
        if (barcode == null) {
            return null;
        } else {
            return buildBarCodeInfo(barcode);
        }
    }

    private Barcode readBarCode(Detector<Barcode> detector, Bitmap barcodeImage) {
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
