package com.example.navixpassanger.ticket;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navixpassanger.R;
import com.example.navixpassanger.email.EmailSender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CancelTicketActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CancelTicketAdapter adapter;
    private List<Ticket> activeTickets;
    private ProgressBar progressBar;
    private TextView noActiveTicketsText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_ticket);

        initViews();
        loadActiveTickets();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewCancelTickets);
        progressBar = findViewById(R.id.progressBar);
        noActiveTicketsText = findViewById(R.id.noActiveTicketsText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activeTickets = new ArrayList<>();
        adapter = new CancelTicketAdapter(activeTickets, this::showCancelConfirmation);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
    }

    private void loadActiveTickets() {
        showLoading();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("tickets")
                .whereEqualTo("userEmail", userEmail)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activeTickets.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Ticket ticket = document.toObject(Ticket.class);
                        if (ticket != null) {
                            boolean isWithinCancellationWindow =
                                    (System.currentTimeMillis() - ticket.getTimestamp()) <= (4 * 60 * 60 * 1000);
                            if (isWithinCancellationWindow) {
                                activeTickets.add(ticket);
                            }
                        }
                    }

                    if (activeTickets.isEmpty()) {
                        showNoActiveTickets();
                    } else {
                        showTickets();
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this,
                            "Error loading tickets: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showCancelConfirmation(Ticket ticket) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Ticket")
                .setMessage("Are you sure you want to cancel ticket with PNR: " + ticket.getPnr() + "?")
                .setPositiveButton("Yes", (dialog, which) -> cancelTicket(ticket))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelTicket(Ticket ticket) {
        showLoading();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CANCELLED");
        updates.put("cancellationTime", System.currentTimeMillis());
        updates.put("active", false);

        db.collection("tickets")
                .document(ticket.getPnr())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    EmailSender.sendCancellationEmail(this, ticket, 0);
                    sendCancellationEmail(ticket);
                    hideLoading();
                    showCancellationSuccess(ticket.getPnr());
                    loadActiveTickets();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this,
                            "Failed to cancel ticket: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void sendCancellationEmail(Ticket ticket) {
        // Calculate refund amount (90% of ticket price)
        //double refundAmount = ticket.getAmount() * 0.90;

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("to", ticket.getUserEmail());
        emailData.put("template", createCancellationEmail(ticket, 0));
        emailData.put("subject", "Ticket Cancellation Confirmation - PNR: " + ticket.getPnr());

        db.collection("mail")
                .add(emailData)
                .addOnSuccessListener(documentReference ->
                        Log.d("Email", "Cancellation email queued successfully"))
                .addOnFailureListener(e ->
                        Log.e("Email", "Error queueing cancellation email", e));
    }

    private String createCancellationEmail(Ticket ticket, double refundAmount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #1976D2;'>Ticket Cancellation Confirmation</h2>" +
                "<p>Dear Passenger,</p>" +
                "<p>Your ticket has been successfully cancelled. Here are the details:</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px;'>" +
                "<p><strong>PNR:</strong> " + ticket.getPnr() + "</p>" +
                "<p><strong>Route:</strong> " + ticket.getFromStop() + " → " + ticket.getToStop() + "</p>" +
                "<p>The refund amount of ₹" +
                " will be credited back to your original payment method within 3-7 business days.</p>" +
                "<p>We understand plans can change and appreciate your understanding of our " +
                "cancellation policy. We hope to serve you again soon.</p>" +
                "<p>If you have any questions about your refund, please don't hesitate to contact " +
                "our customer support.</p>" +
                "<p>Thank you for choosing our service!</p>" +
                "<p style='margin-top: 20px;'>Best regards,<br>Bus Service Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void showCancellationSuccess(String pnr) {
        new AlertDialog.Builder(this)
                .setTitle("Ticket Cancelled")
                .setMessage("Your ticket with PNR: " + pnr +
                        " has been cancelled successfully. You will receive a confirmation email shortly.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        noActiveTicketsText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showNoActiveTickets() {
        recyclerView.setVisibility(View.GONE);
        noActiveTicketsText.setVisibility(View.VISIBLE);
        hideLoading();
    }

    private void showTickets() {
        recyclerView.setVisibility(View.VISIBLE);
        noActiveTicketsText.setVisibility(View.GONE);
        hideLoading();
    }
}