package com.example.navixpassanger.user;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.navixpassanger.R;
import com.example.navixpassanger.home.HomeScreen;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth auth;
    private AppCompatTextView email, phone;
    private AppCompatEditText name;
    private AppCompatButton updateButton;
    private DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Uri imageUri;
    CircleImageView profileImage;
    String role = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();


        name = findViewById(R.id.name100);
        email = findViewById(R.id.email100);
        updateButton = findViewById(R.id.updatebtn);
        profileImage = findViewById(R.id.profileImageView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        navigationView.bringToFront();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav_bar, R.string.close_nav_bar);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_update_profile);

        populateUserProfile();


        profileImage.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), EditProfile.class));
        });
        updateButton.setOnClickListener(v -> showConfirmationDialog());
    }

    private void populateUserProfile() {
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
                            String userName = document.getString("name");
                            String userImage = document.getString("imageUri");
                            name.setText(userName);
                            Picasso.get().load(userImage).into(profileImage);
                            role = "Male";
                        }
                        email.setText(targetEmail);
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Error getting student: " + e.getMessage());
                        progressDialog.dismiss();
                    }
                });

//        CollectionReference teacherCollection = FirebaseFirestore.getInstance().collection("teacher");
//        teacherCollection.whereEqualTo("email", targetEmail).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (queryDocumentSnapshots.isEmpty()) {
////                            Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_SHORT).show();
//            } else {
//                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
//                String userName = document.getString("name");
//                String userImage = document.getString("imageUri");
//                name.setText(userName);
//                Picasso.get().load(userImage).into(profileImage);
//                role = "teacher";
//
//            }
//        })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        System.out.println("Error getting student: " + e.getMessage());
//                    }
//                });

    }

    private void getUserInformation() {
        databaseReference.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    if (snapshot.hasChild("imageUri")) {
                        String image = snapshot.child("imageUri").getValue().toString();
                        Picasso.get().load(image).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            // TODO: Implement another thing in life
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
        } else if (id == R.id.nav_home) {
            startActivity(new Intent(getApplicationContext(), HomeScreen.class));
        }
        return true;
    }

    // Inside your activity or fragment
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Update")
                .setMessage("Want to continue with the update?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();
                        updateDataInFirestore();
                        progressDialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDataInFirestore() {
        String userEmail = auth.getCurrentUser().getEmail();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("student").document(userEmail);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name.getText().toString().trim());
        userRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update data", Toast.LENGTH_SHORT).show();
                });

    }

    // Call this method when the user clicks the update button
    private void onUpdateButtonClick() {
        showConfirmationDialog();
    }

}
