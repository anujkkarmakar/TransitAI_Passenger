package com.example.navixpassanger.nav;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.navixpassanger.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComplaintActivity extends AppCompatActivity {
    private AutoCompleteTextView complaintTypeSpinner;
    private AutoCompleteTextView specificTypeSpinner;
    private TextInputEditText complaintMessage;
    private MaterialButton uploadButton;
    private MaterialButton submitButton;
    private TextView selectedFileName;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private Uri selectedFileUri;

    private static final int PICK_FILE_REQUEST = 1;

    private Map<String, String[]> complaintTypes = new HashMap<String, String[]>() {{
        put("Trip", new String[]{"Delay", "Cancellation", "Route Change"});
        put("Bus Service", new String[]{"AC Issues", "Cleanliness", "Maintenance"});
        put("Crew Mismanagement", new String[]{"Rude Behavior", "Unsafe Driving", "No Show"});
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        initializeFirebase();
        initializeViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        complaintTypeSpinner = findViewById(R.id.complaintTypeSpinner);
        specificTypeSpinner = findViewById(R.id.specificTypeSpinner);
        complaintMessage = findViewById(R.id.complaintMessage);
        uploadButton = findViewById(R.id.uploadButton);
        submitButton = findViewById(R.id.submitButton);
        selectedFileName = findViewById(R.id.selectedFileName);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        // Setup main complaint type spinner
        ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                complaintTypes.keySet().toArray(new String[0])
        );
        complaintTypeSpinner.setAdapter(mainAdapter);

        // Setup specific type spinner based on main selection
        complaintTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = parent.getItemAtPosition(position).toString();
            String[] specificTypes = complaintTypes.get(selectedType);
            ArrayAdapter<String> specificAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    specificTypes
            );
            specificTypeSpinner.setAdapter(specificAdapter);
            specificTypeSpinner.setText("", false);
        });
    }

    private void setupClickListeners() {
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_FILE_REQUEST);
        });

        submitButton.setOnClickListener(v -> submitComplaint());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            String fileName = getFileName(selectedFileUri);
            selectedFileName.setText(fileName);
            selectedFileName.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void submitComplaint() {
        String mainType = complaintTypeSpinner.getText().toString();
        String specificType = specificTypeSpinner.getText().toString();
        String message = complaintMessage.getText().toString();

        if (mainType.isEmpty() || specificType.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        // Create complaint document
        Map<String, Object> complaint = new HashMap<>();
        complaint.put("mainType", mainType);
        complaint.put("specificType", specificType);
        complaint.put("message", message);
        complaint.put("status", "submitted");
        complaint.put("userId", auth.getCurrentUser().getUid());
        complaint.put("timestamp", FieldValue.serverTimestamp());

        if (selectedFileUri != null) {
            complaint.put("documentUrl", "null");
            uploadFileAndSubmit(complaint);
        } else {
            submitComplaintToFirestore(complaint);
        }
    }

    private void uploadFileAndSubmit(Map<String, Object> complaint) {
        String fileName = UUID.randomUUID().toString();
        StorageReference fileRef = storage.getReference().child("complaints/" + fileName);

        fileRef.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        complaint.put("documentUrl", uri.toString());
                        submitComplaintToFirestore(complaint);
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitComplaintToFirestore(Map<String, Object> complaint) {
        db.collection("complaints")
                .add(complaint)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Failed to submit complaint", Toast.LENGTH_SHORT).show();
                });
    }
}