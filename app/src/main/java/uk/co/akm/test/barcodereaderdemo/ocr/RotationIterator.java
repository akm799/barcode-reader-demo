package uk.co.akm.test.barcodereaderdemo.ocr;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.util.Iterator;

/**
 * //TODO Add comments.
 * Created by Thanos Mavroidis on 30/05/2017.
 */
final class RotationIterator implements Iterator<Bitmap> {
    private static final int NUMBER_OF_POSITIONS = 4;
    private static final int ROTATION_ANGLE_DEG = 90;

    private int count;
    private Bitmap bitmap;

    private final Matrix rotationMatrix = new Matrix();

    RotationIterator(Bitmap initial) {
        rotationMatrix.postRotate(ROTATION_ANGLE_DEG);
        bitmap = initial.copy(initial.getConfig(), false);
    }

    @Override
    public boolean hasNext() {
        final boolean hasNext = (count < NUMBER_OF_POSITIONS);
        if (!hasNext && bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        return hasNext;
    }

    @Override
    public Bitmap next() {
        return (hasNext() ? nextPosition() : null);
    }

    private Bitmap nextPosition() {
        bitmap = (count == 0 ? bitmap : rotate(bitmap));
        count++;

        return bitmap;
    }

    private Bitmap rotate(Bitmap bitmap) {
        final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, true);
        bitmap.recycle();

        return rotatedBitmap;
    }
}
