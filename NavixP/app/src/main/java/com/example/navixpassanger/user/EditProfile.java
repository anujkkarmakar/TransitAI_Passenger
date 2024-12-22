package com.example.navixpassanger.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navixpassanger.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 0;
    private CircleImageView profileImageView;
    private Button closeButton, saveButton;
    private TextView profileChangeButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri imageUri;
    private String myUri = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicsRef;

    private String role ="";
    ActivityResultLauncher<Intent> resultLauncher;

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        try {
                            Uri uri = o.getData().getData();
                            profileImageView.setImageURI(uri);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile Pic");

        profileImageView = findViewById(R.id.profileImageView);
        closeButton = findViewById(R.id.closeButton);
        saveButton = findViewById(R.id.saveButton);
        profileChangeButton = findViewById(R.id.profileChangeButton);

        closeButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UpdateProfile.class)));

        saveButton.setOnClickListener(v -> uploadProfileImage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
            profileChangeButton.setOnClickListener(v -> getImage());
        }

        registerResult();
        getUserInformation();
    }

    private void getUserInformation() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Fetching...");
        progressDialog.show();

        String targetEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        CollectionReference studentCollection = FirebaseFirestore.getInstance().collection("Male");
        studentCollection.whereEqualTo("email", targetEmail).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_SHORT).show();
                        } else {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            String userImage = document.getString("imageUri");
                            Picasso.get().load(userImage).into(profileImageView);
                            role = "Male";
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Error getting student: " + e.getMessage());
                    }
                });

//        CollectionReference teacherCollection = FirebaseFirestore.getInstance().collection("teacher");
//        teacherCollection.whereEqualTo("email", targetEmail).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots.isEmpty()) {
////                            Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_SHORT).show();
//                        } else {
//                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
//                            String userImage = document.getString("imageUri");
//                            Picasso.get().load(userImage).into(profileImageView);
//                            role = "teacher";
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        System.out.println("Error getting student: " + e.getMessage());
//                    }
//                });
        progressDialog.dismiss();
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private void getImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
            profileImageView.setImageURI(imageUri);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();

        }
    }


    private void uploadProfileImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageProfilePicsRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileReference.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = (Uri) task.getResult();
                        myUri = downloadUri.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("image", myUri);

                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                        updateUserData();
                        progressDialog.dismiss();
                    }
                }
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserData() {

        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Male").document(mAuth.getCurrentUser().getEmail());

        userRef.update("imageUri", myUri).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });

//        if (role.equals("student")) {
//            DocumentReference userRef = FirebaseFirestore.getInstance().collection("student").document(mAuth.getCurrentUser().getEmail());
//
//            userRef.update("imageUri", myUri).addOnSuccessListener(aVoid -> {
//                        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
//                    });
//        } else {
//
//            DocumentReference teacherRef = FirebaseFirestore.getInstance().collection("teacher").document(mAuth.getCurrentUser().getEmail());
//
//            teacherRef.update("imageUri", myUri).addOnSuccessListener(aVoid -> {
//                        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
//                    });
//        }
    }
}
