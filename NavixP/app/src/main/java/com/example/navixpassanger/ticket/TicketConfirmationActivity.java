package com.example.navixpassanger.ticket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.navixpassanger.R;
import com.example.navixpassanger.carbon.CarbonCalculator;
import com.example.navixpassanger.pdf.PDFGenerator;
import com.example.navixpassanger.user.DashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.example.navixpassanger.sound.SoundPlayer;

public class TicketConfirmationActivity extends AppCompatActivity {
    private static final String TAG = "TicketConfirmation";
    private FirebaseFirestore db;
    private EditText mobileNumberInput;
    private Button confirmButton;
    private TextView userNameText, fareText, fromToText, journeyTypeText, busTypeText;
    private String userName, userEmail, fromStop, toStop, journeyType, busType;
    private double totalFare, distance;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_confirmation);

        initializeViews();
        getIntentData();
        setupListeners();
    }

    private void initializeViews() {
        db = FirebaseFirestore.getInstance();
        mobileNumberInput = findViewById(R.id.mobileNumberInput);
        confirmButton = findViewById(R.id.confirmButton);
        userNameText = findViewById(R.id.userNameText);
        fareText = findViewById(R.id.fareText);
        fromToText = findViewById(R.id.fromToText);
        journeyTypeText = findViewById(R.id.journeyTypeText);
        busTypeText = findViewById(R.id.busTypeText);
    }

    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userName = extras.getString("userName");
            userEmail = extras.getString("userEmail");
            fromStop = extras.getString("fromStop");
            toStop = extras.getString("toStop");
            journeyType = extras.getString("journeyType");
            busType = extras.getString("busType");
            totalFare = extras.getDouble("totalFare");
            distance = extras.getDouble("distance");

            updateUI();
        }
    }

    private void updateUI() {
        userNameText.setText("Passenger: " + userName);
        fareText.setText("Total Fare: ₹" + String.format("%.2f", totalFare));
        fromToText.setText(fromStop + " to " + toStop);
        journeyTypeText.setText("Journey Type: " + journeyType);
        busTypeText.setText("Bus Type: " + busType);
    }

    private void setupListeners() {
        confirmButton.setOnClickListener(v -> {
            if (validateInput()) {
                processBooking();
            }
        });
    }

    private boolean validateInput() {
        String mobile = mobileNumberInput.getText().toString();
        if (mobile.isEmpty()) {
            mobileNumberInput.setError("Mobile number is required");
            return false;
        }

        if (mobile.length() != 10) {
            mobileNumberInput.setError("Please enter valid 10-digit mobile number");
            return false;
        }

        if (!isValidIndianMobileNumber(mobile)) {
            mobileNumberInput.setError("Please enter valid 10-digit Indian mobile number");
            return false;
        }

        return true;
    }

    private void processBooking() {
        try {
            showProgressDialog("Processing Booking...");

            // Generate PNR
            String pnr = generatePNR();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()).format(new Date());

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("pnr", pnr);
            bookingData.put("userName", userName);
            bookingData.put("userEmail", userEmail);
            bookingData.put("mobileNumber", mobileNumberInput.getText().toString());
            bookingData.put("fromStop", fromStop);
            bookingData.put("toStop", toStop);
            bookingData.put("journeyType", journeyType);
            bookingData.put("busType", busType);
            bookingData.put("fare", totalFare);
            bookingData.put("timestamp", timestamp);
            bookingData.put("status", "ACTIVE");

            Log.d("PDFDebug", "Generating PDF file");
            File pdfFile = PDFGenerator.generateTicketPDF(this, bookingData);

            if (pdfFile != null && pdfFile.exists()) {
                Log.d("PDFDebug", "PDF generated successfully at: " + pdfFile.getAbsolutePath());
                sendConfirmationEmail(bookingData, pdfFile);
            } else {
                throw new IOException("Failed to generate PDF file");
            }
        } catch (Exception e) {
            Log.e("PDFDebug", "Error in processBooking", e);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Error processing booking: " +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void sendConfirmationEmail(Map<String, Object> bookingData, File pdfFile) {
        // Using JavaMail API
        new Thread(() -> {
            try {

                final String username = "anujgamerz2002@gmail.com";
                final String password = "fpzw uztq bnkr rgmb";

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(userEmail));
                message.setSubject("Navix Passenger Information - PNR: " +
                        bookingData.get("pnr"));


                // Create the message body part
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(generateEmailContent(bookingData), "text/html; charset=utf-8");

                // Create PDF attachment part
                BodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setDataHandler(new DataHandler(new FileDataSource(pdfFile)));
                attachmentPart.setFileName("ticket_" + bookingData.get("pnr") + ".pdf");

                // Create multipart message
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(attachmentPart);

                // Set the complete message parts
                message.setContent(multipart);

                // Send message
                Transport.send(message);

                savePDFAndTicketInfo(pdfFile, bookingData);

                // Delete temporary PDF file
                pdfFile.delete();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showBookingConfirmation(bookingData.get("pnr").toString());
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(TicketConfirmationActivity.this,
                            "Failed to send email confirmation: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private boolean isValidIndianMobileNumber(String mobileNumber) {
        // Regex pattern for Indian mobile numbers
        // First digit must be 6,7,8 or 9 followed by 9 digits
        String indianMobilePattern = "^[6-9]\\d{9}$";
        return mobileNumber.matches(indianMobilePattern);
    }

    private String generateEmailContent(Map<String, Object> bookingData) {
        return String.format("""
        <html>
        <body style='font-family: Arial, sans-serif;'>
            <div style='background-color: #1a237e; color: white; padding: 20px;'>
                <h2>Navix Passenger Information</h2>
                <p>%s to %s</p>
                <p>PNR: %s</p>
            </div>
            <div style='padding: 20px;'>
                <h3>Ticket Details</h3>
                <table style='width: 100%%; border-collapse: collapse;'>
                    <tr><td style='padding: 8px;'><strong>Passenger:</strong></td><td>%s</td></tr>
                    <tr><td style='padding: 8px;'><strong>Journey Type:</strong></td><td>%s</td></tr>
                    <tr><td style='padding: 8px;'><strong>Bus Type:</strong></td><td>%s</td></tr>
                    <tr><td style='padding: 8px;'><strong>Fare:</strong></td><td>₹%.2f</td></tr>
                    <tr><td style='padding: 8px;'><strong>Mobile:</strong></td><td>%s</td></tr>
                    <tr><td style='padding: 8px;'><strong>Booking Date:</strong></td><td>%s</td></tr>
                </table>
                <p style='color: #666; margin-top: 20px;'>
                    Your ticket is attached as a PDF. The PDF includes a QR code that can be 
                    scanned to verify your ticket details.
                </p>
                <p style='color: #666;'>
                    <strong>Note:</strong> For detailed Terms & Conditions, please refer to the attached PDF.
                </p>
            </div>
            <div style='background-color: #f5f5f5; padding: 20px; text-align: center;'>
                <p>Thank you for choosing Navix!</p>
            </div>
        </body>
        </html>
        """,
                bookingData.get("fromStop"),
                bookingData.get("toStop"),
                bookingData.get("pnr"),
                bookingData.get("userName"),
                bookingData.get("journeyType"),
                bookingData.get("busType"),
                bookingData.get("fare"),
                bookingData.get("mobileNumber"),
                bookingData.get("timestamp")
        );
    }

    private String generatePNR() {
        // Generate a unique PNR number
        return "NVIX" + System.currentTimeMillis() +
                new Random().nextInt(1000);
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private MediaPlayer mediaPlayer;  // Add this as a class variable

    private void showBookingConfirmation(String pnr) {
        new AlertDialog.Builder(this)
                .setTitle("Booking Confirmed")
                .setMessage("Your ticket has been booked successfully!\nPNR: " + pnr)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Stop music when dialog is dismissed
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    // Create an intent to return to the home/dashboard activity
                    Intent intent = new Intent(TicketConfirmationActivity.this,
                            DashboardActivity.class); // Replace with your main activity
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setOnCancelListener(dialog -> {
                    // Also stop music if dialog is cancelled
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                })
                .setCancelable(false)
                .show();

        // Rest of the code remains same...
    }

    // Also add cleanup in onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void savePDFAndTicketInfo(File pdfFile, Map<String, Object> bookingData) {
        // Add debug logging
        Log.d("PDFDebug", "Starting PDF upload process");
        Log.d("PDFDebug", "PDF File path: " + pdfFile.getAbsolutePath());
        Log.d("PDFDebug", "File exists: " + pdfFile.exists());
        Log.d("PDFDebug", "File size: " + pdfFile.length());

        if (pdfFile == null || !pdfFile.exists()) {
            Log.e("PDFDebug", "PDF file is null or doesn't exist");
            Toast.makeText(this, "PDF file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Saving ticket...");
        progressDialog.show();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String pnr = bookingData.get("pnr").toString();

        updateUserCarbonSavings(userEmail, distance);

        // Create byte array from file
        byte[] pdfBytes;
        try {
            pdfBytes = new byte[(int) pdfFile.length()];
            FileInputStream fis = new FileInputStream(pdfFile);
            fis.read(pdfBytes);
            fis.close();

            Log.d("PDFDebug", "Successfully read PDF bytes: " + pdfBytes.length);

            // Create Storage reference
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("tickets")
                    .child(userId)
                    .child(pnr + ".pdf");

            // Create metadata
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("application/pdf")
                    .build();

            // Upload bytes directly
            UploadTask uploadTask = storageRef.putBytes(pdfBytes, metadata);

            uploadTask
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d("PDFDebug", "Upload progress: " + progress + "%");
                        progressDialog.setMessage("Uploading: " + (int)progress + "%");
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("PDFDebug", "PDF upload successful");
                        // Get download URL
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    Log.d("PDFDebug", "Got download URL: " + uri.toString());
                                    // Create ticket document
                                    Ticket ticket = new Ticket(
                                            pnr,
                                            bookingData.get("fromStop").toString(),
                                            bookingData.get("toStop").toString(),
                                            bookingData.get("journeyType").toString(),
                                            userEmail,
                                            uri.toString(),
                                            System.currentTimeMillis()
                                    );
                                    ticket.setStatus("ACTIVE");

                                    // Save to Firestore
                                    FirebaseFirestore.getInstance()
                                            .collection("tickets")
                                            .document(pnr)
                                            .set(ticket)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("PDFDebug", "Ticket saved to Firestore");
                                                progressDialog.dismiss();
                                                // Clean up
                                                boolean deleted = pdfFile.delete();
                                                Log.d("PDFDebug", "PDF file deleted: " + deleted);
                                                showBookingConfirmation(pnr);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("PDFDebug", "Firestore save failed", e);
                                                progressDialog.dismiss();
                                                Toast.makeText(this, "Failed to save ticket info: " +
                                                        e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("PDFDebug", "Failed to get download URL", e);
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Failed to get download URL: " +
                                            e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PDFDebug", "Upload failed", e);
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to upload PDF: " +
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }
        catch (IOException e) {
            Log.e("PDFDebug", "File reading error", e);
            progressDialog.dismiss();
            Toast.makeText(this, "Error reading PDF file: " +
                    e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserCarbonSavings(String userId, double distance) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Male").document(userEmail);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    DocumentSnapshot snapshot = transaction.get(userRef);

                    // Get current values or set to 0 if not exists
                    double currentCarbonSaved = snapshot.getDouble("totalCarbonSaved") != null ?
                            snapshot.getDouble("totalCarbonSaved") : 0;
                    int currentRides = snapshot.getLong("totalBusRides") != null ?
                            snapshot.getLong("totalBusRides").intValue() : 0;
                    double currentDistance = snapshot.getDouble("totalDistanceTraveled") != null ?
                            snapshot.getDouble("totalDistanceTraveled") : 0;

                    // Calculate new carbon savings
                    double newCarbonSaved = CarbonCalculator.calculateCarbonSaved(distance);

                    // Update the values
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("totalCarbonSaved", currentCarbonSaved + newCarbonSaved);
                    updates.put("totalBusRides", currentRides + 1);
                    updates.put("totalDistanceTraveled", currentDistance + distance);

                    transaction.update(userRef, updates);
                    return null;
                }).addOnSuccessListener(aVoid ->
                        Log.d("Carbon", "Carbon savings updated successfully"))
                .addOnFailureListener(e ->
                        Log.e("Carbon", "Error updating carbon savings", e));
    }
}