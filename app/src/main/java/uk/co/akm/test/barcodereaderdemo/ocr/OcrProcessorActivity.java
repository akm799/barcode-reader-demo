package uk.co.akm.test.barcodereaderdemo.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.Collection;
import java.util.Iterator;

import uk.co.akm.test.barcodereaderdemo.R;
import uk.co.akm.test.barcodereaderdemo.base.AbstractVisionActivity;
import uk.co.akm.test.barcodereaderdemo.base.VisionAsyncTask;

/**
 * Created by Thanos Mavroidis on 30/05/2017.
 */
public final class OcrProcessorActivity extends AbstractVisionActivity<TextBlock> {

    @Override
    protected CharSequence getTitleText() {
        return "OCR Processing Demo";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_ocr_processor;
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
    protected Detector<TextBlock> buildDetector(Context context) {
        return new TextRecognizer.Builder(context).build();
    }

    @Override
    protected VisionAsyncTask buildVisionTask() {
        return new OcrProcessingTask(this);
    }

    private static final class OcrProcessingTask extends VisionAsyncTask<TextBlock> {
        private static final String TAG = OcrProcessingTask.class.getSimpleName();

        OcrProcessingTask(AbstractVisionActivity<TextBlock> parent) {
            super(parent);
        }

        //TODO Add comments.
        @Override
        protected String decodeBitmapAsString(Detector<TextBlock> detector, Bitmap textImage) {
            int maxLength = 0;
            String longestText = null;
            final Iterator<Bitmap> iterator = new RotationIterator(textImage);
            while (iterator.hasNext()) {
                final String text = recognizeTextInBitmap(detector, iterator.next());
                if (text != null && text.length() > maxLength) {
                    maxLength = text.length();
                    longestText = text;
                }
            }

            return longestText;
        }

        private String recognizeTextInBitmap(Detector<TextBlock> detector, Bitmap textImage) {
            final SparseArray<TextBlock> textBlocks = recognizeText(detector, textImage);
            if (textBlocks == null) {
                return null;
            } else {
                return convertToString(textBlocks);
            }
        }

        private SparseArray<TextBlock> recognizeText(Detector<TextBlock> detector, Bitmap textImage) {
            final Frame frame = new Frame.Builder().setBitmap(textImage).build();
            final SparseArray<TextBlock> textBlocks = detector.detect(frame);

            if (textBlocks == null || textBlocks.size() == 0) {
                return null;
            } else {
                return textBlocks;
            }
        }

        private String convertToString(SparseArray<TextBlock> textBlocks) {
            final StringBuilder sb = new StringBuilder();
            appendTextBlocks(textBlocks, sb);

            return sb.toString();
        }

        private void appendTextBlocks(SparseArray<TextBlock> textBlocks, StringBuilder sb) {
            for (int i=0 ; i<textBlocks.size() ; i++) {
                final TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                if (textBlock != null) {
                    appendTextBlock(textBlock, sb);
                    sb.append('\n');
                }
            }
        }

        private void appendTextBlock(TextBlock textBlock, StringBuilder sb) {
            final Collection<? extends Text> components = textBlock.getComponents();
            if (components != null && !components.isEmpty()) {
                appendComponents(components, sb);
            }
        }

        private void appendComponents(Collection<? extends Text> components, StringBuilder sb) {
            for (Text text : components) {
                if (text != null) {
                    sb.append(text.getValue());
                    sb.append(' ');
                }
            }
        }
    }
}
