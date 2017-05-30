package uk.co.akm.test.barcodereaderdemo.barcode;

import com.google.android.gms.vision.barcode.Barcode;

/**
 *  Helper class to help format barcode display data.
 *
 *  Created by Thanos Mavroidis on 05/05/2017.
 */
public class BarcodeDataFormatter {
    private static final int BARCODE_NUMBER_GROUP_LENGTH = 6;

    /**
     * Formats the input barcode number as displayed in most product barcodes, i.e. separated into
     * groups of 6 digits, starting from the RHS. If the input cannot be formatted in that way, then
     * it is simply returned unchanged.
     */
    static String formatBarCodeNumber(String number) {
        try {
            Long.parseLong(number);
        } catch (NumberFormatException nfe) {
            return number;
        }

        final int len = number.length();
        if (len <= BARCODE_NUMBER_GROUP_LENGTH) {
            return number;
        }

        final int lastIndex = len - 1;
        final StringBuilder sb = new StringBuilder(len + len/BARCODE_NUMBER_GROUP_LENGTH + 1);
        for (int i=0 ; i<len ; i++) {
            if (i > 0 && i%BARCODE_NUMBER_GROUP_LENGTH == 0) {
                sb.append(' ');
            }
            sb.append(number.charAt(lastIndex - i));
        }

        return sb.reverse().toString();
    }

    static String getFormatString(Barcode barcode) {
        if (barcode == null) {
            return null;
        }

        switch (barcode.format) {
            case Barcode.AZTEC: return "AZTEC";
            case Barcode.CODABAR: return "CODABAR";
            case Barcode.CODE_39: return "CODE_39";
            case Barcode.CODE_93: return "CODE_93";
            case Barcode.CODE_128: return "CODE_128";
            case Barcode.DATA_MATRIX: return "DATA_MATRIX";
            case Barcode.EAN_8: return "EAN_8";
            case Barcode.EAN_13: return "EAN_13";
            case Barcode.ITF: return "ITF";
            case Barcode.PDF417: return "PDF417";
            case Barcode.QR_CODE: return "QR_CODE";
            case Barcode.UPC_A: return "UPC_A";
            case Barcode.UPC_E: return "UPC_E";

            default: return "UNKNOWN (" + barcode.format + ")";
        }
    }

    static String getValueFormatString(Barcode barcode) {
        if (barcode == null) {
            return null;
        }

        switch (barcode.valueFormat) {
            case Barcode.CALENDAR_EVENT: return "CALENDAR_EVENT";
            case Barcode.CONTACT_INFO: return "CONTACT_INFO";
            case Barcode.DRIVER_LICENSE: return "DRIVER_LICENSE";
            case Barcode.EMAIL: return "EMAIL";
            case Barcode.GEO: return "GEO";
            case Barcode.ISBN: return "ISBN";
            case Barcode.PHONE: return "PHONE";
            case Barcode.PRODUCT: return "PRODUCT";
            case Barcode.SMS: return "SMS";
            case Barcode.TEXT: return "TEXT";
            case Barcode.URL: return "URL";
            case Barcode.WIFI: return "WIFI";

            default: return "UNKNOWN (" + barcode.valueFormat + ")";
        }
    }

    private BarcodeDataFormatter() {}
}
