package com.example.navixpassanger.ticket;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navixpassanger.R;
import com.example.navixpassanger.email.EmailSender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookTicket extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private Button sendEmailButton;
    private String userEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_ticket);

        bookTicketAltertDialog(); // to display alert dialog box before booking ticket

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
//        sendEmailButton = findViewById(R.id.sendEmailButton);


        // Next step is to get the value from the user and generate the ticket
        // the ticket generated will be send to the user email
        // TODO: uncheck the following method call
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Get user email from Firestore
            DocumentReference userDoc = firestore.collection("users").document(currentUser.getUid());
            userDoc.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("email")) {
                    userEmail = documentSnapshot.getString("email");
                } else {
                    userEmail = currentUser.getEmail(); // Fallback to Firebase Authentication email
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), "Error fetching email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        // Set up the button click listener
        sendEmailButton.setOnClickListener(v -> {
            if (userEmail != null) {
                sendEmailInBackground();
            } else {
                Toast.makeText(getApplicationContext(), "User email not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailInBackground() {
        // Create a single-threaded executor
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
                // Your email sending logic
                sendEmail();

                // Update the UI after successful email sending
                //runOnUiThread(() -> statusTextView.setText("Email sent successfully!"));
            } catch (Exception e) {
                e.printStackTrace();

                // Update the UI in case of an error
                //runOnUiThread(() -> statusTextView.setText("Failed to send email."));
            }
        });
    }

    private void sendEmail() throws Exception {
        // Replace with your email-sending logic
        // Example using JavaMail (replace placeholders with actual values):
        EmailSender emailSender = new EmailSender();
//        emailSender.send(userEmail, "Subject", "HI"); // Replace with actual recipient, subject, and body
    }

    private void bookTicketAltertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important:")
                .setMessage("As per the Government instructions, all the passengers have to carry ONE of the following documents, for their travel on the route Delhi - Kathmandu - Delhi :\n" +
                        "\n" +
                        "Negative COVID-19 RT-PCR report (The test should have been conducted within 72 hrs prior to undertaking the journey)\n" +
                        "\n" +
                        "OR\n" +
                        "\n" +
                        "Certificate of completing full primary vaccination schedule of COVID-19 vaccination.\n" +
                        "\n" +
                        "A copy of the above document is to be submitted at the time of boarding of the bus. For any queries, please consult the contact numbers available on the homepage of the website.\n" +
                        "\n")
                .setPositiveButton("Close", (dialog, which) -> {
                })
                .show();
    }

    private void generateTicket() {

    }
    private void sendTicket() {

    }
}
