package com.example.navixpassanger.pdf;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import org.json.JSONObject;

import java.util.Map;

public class QRGenerator {
    public static Bitmap generateQRCode(Map<String, Object> bookingData) {
        try {
            // Create JSON string of booking data
            JSONObject jsonData = new JSONObject();
            jsonData.put("pnr", bookingData.get("pnr"));
            jsonData.put("passenger", bookingData.get("userName"));
            jsonData.put("from", bookingData.get("fromStop"));
            jsonData.put("to", bookingData.get("toStop"));
            jsonData.put("journeyType", bookingData.get("journeyType"));
            jsonData.put("busType", bookingData.get("busType"));
            jsonData.put("fare", bookingData.get("fare"));
            jsonData.put("mobile", bookingData.get("mobileNumber"));
            jsonData.put("timestamp", bookingData.get("timestamp"));

            String qrContent = jsonData.toString();

            // Generate QR code
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE,
                    512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // Convert BitMatrix to Bitmap
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}