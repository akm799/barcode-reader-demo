package uk.co.akm.test.barcodereaderdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import uk.co.akm.test.barcodereaderdemo.barcode.BarcodeReaderActivity;
import uk.co.akm.test.barcodereaderdemo.ocr.OcrProcessorActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBarCodeDemo(View view) {
        startDemoActivity(BarcodeReaderActivity.class);
    }

    public void onOcrDemo(View view) {
        startDemoActivity(OcrProcessorActivity.class);
    }

    private void startDemoActivity(Class clazz) {
        startActivity(new Intent(this, clazz));
    }
}
