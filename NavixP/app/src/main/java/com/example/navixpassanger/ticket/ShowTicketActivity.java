package com.example.navixpassanger.ticket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navixpassanger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowTicketActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private List<Ticket> ticketList;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ticket);

        setupViews();
        loadTickets();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ticketList = new ArrayList<>();
        adapter = new TicketAdapter(ticketList, this::onTicketClick);
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading tickets...");
        progressDialog.setCancelable(false);

        db = FirebaseFirestore.getInstance();
    }

    private void loadTickets() {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        progressDialog.show();

        // First try with composite index
        db.collection("tickets")
                .whereEqualTo("userEmail", userEmail)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    processTickets(queryDocumentSnapshots);
                })
                .addOnFailureListener(e -> {
                    // If index error, fallback to simple query
                    if (e instanceof FirebaseFirestoreException &&
                            ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {

                        // Fallback to simple query without ordering
                        db.collection("tickets")
                                .whereEqualTo("userEmail", userEmail)
                                .get()
                                .addOnSuccessListener(this::processTickets)
                                .addOnFailureListener(e2 -> {
                                    handleError(e2);
                                });
                    } else {
                        handleError(e);
                    }
                });
    }

    private void processTickets(QuerySnapshot queryDocumentSnapshots) {
        Log.d("TicketDebug", "Got " + queryDocumentSnapshots.size() + " documents");
        ticketList.clear();

        for (DocumentSnapshot document : queryDocumentSnapshots) {
            Log.d("TicketDebug", "Processing document: " + document.getId());

            Ticket ticket = document.toObject(Ticket.class);
            if (ticket != null) {
                ticketList.add(ticket);
                Log.d("TicketDebug", "Added ticket: " + ticket.toString());
            }
        }

        // Sort manually if using fallback query
        if (!ticketList.isEmpty()) {
            Collections.sort(ticketList, (t1, t2) ->
                    Long.compare(t2.getTimestamp(), t1.getTimestamp()));
        }

        adapter.notifyDataSetChanged();
        progressDialog.dismiss();

        if (ticketList.isEmpty()) {
            Toast.makeText(this, "No tickets found", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(Exception e) {
        Log.e("TicketDebug", "Error loading tickets", e);
        progressDialog.dismiss();
        Toast.makeText(this, "Failed to load tickets: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
    }

    private void onTicketClick(Ticket ticket) {
        // Check if ticket is active
        boolean isActive = (System.currentTimeMillis() - ticket.getTimestamp())
                <= (4 * 60 * 60 * 1000);

        // Only proceed if ticket is active and not cancelled
        if (isActive && (ticket.getStatus() == null || !ticket.getStatus().equals("CANCELLED"))) {
            if (ticket.getPdfUrl() != null && !ticket.getPdfUrl().isEmpty()) {
                progressDialog.setMessage("Loading ticket...");
                progressDialog.show();

                File outputFile = new File(getCacheDir(), "ticket_" + ticket.getPnr() + ".pdf");
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(ticket.getPdfUrl());

                storageRef.getFile(outputFile)
                        .addOnSuccessListener(taskSnapshot -> {
                            progressDialog.dismiss();
                            openPDF(outputFile);
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Log.e("PDFDebug", "Error downloading PDF", e);
                            Toast.makeText(this, "Failed to load PDF: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                                    taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Loading: " + (int)progress + "%");
                        });
            } else {
                Toast.makeText(this, "PDF not available for this ticket",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Show appropriate message based on ticket status
            if (ticket.getStatus() != null && ticket.getStatus().equals("CANCELLED")) {
                Toast.makeText(this, "Cannot view cancelled ticket",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cannot view expired ticket",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPDF(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);
        } catch (Exception e) {
            Log.e("PDFDebug", "Error opening PDF", e);
            Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show();
        }
    }
}