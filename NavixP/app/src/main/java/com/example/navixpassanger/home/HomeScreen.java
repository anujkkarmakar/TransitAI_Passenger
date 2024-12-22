package com.example.navixpassanger.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.navixpassanger.R;
import com.example.navixpassanger.user.DashboardActivity;
import com.example.navixpassanger.user.UpdateProfile;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppCompatButton signoutbtn;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore storage;
    String userId;
    String role = "";
    private AppCompatTextView email, phone, name;
    ProgressDialog progressDialog;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    CircleImageView profileImage;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_AUTHENTICATED = "key_authenticated";
    private SharedPreferences settings;
    private boolean isAuthenticated = false;

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!isAuthenticated) {
//            authenticateUser();
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save the authentication state
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_AUTHENTICATED, isAuthenticated);
        editor.apply();

        boolean testValue = settings.getBoolean(KEY_AUTHENTICATED, false);
        Log.d("TestSP", "Test value: FROM PAUSE:" + testValue); //
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Save the authentication state
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_AUTHENTICATED, isAuthenticated);
        editor.apply();

        boolean testValue = settings.getBoolean(KEY_AUTHENTICATED, false);
        Log.d("TestSP", "Test value FROM DESTROY: " + testValue); //
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isAuthenticated = settings.getBoolean(KEY_AUTHENTICATED, false);
        if (savedInstanceState != null) {
            isAuthenticated = settings.getBoolean(KEY_AUTHENTICATED, false);
        }

        name = findViewById(R.id.name100);
        email = findViewById(R.id.email100);
        profileImage = findViewById(R.id.profileImage);

        signoutbtn = findViewById(R.id.signoubtn);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        assert firebaseUser != null;
        userId = firebaseUser.getUid();

        storage = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        //set drawer layout visibility only when using Biometric Auth
//        drawerLayout.setVisibility(View.INVISIBLE);

        navigationView.bringToFront();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav_bar, R.string.close_nav_bar);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        populateUserProfile();

        //sign out button
        signoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                firebaseAuth.signOut();
                //update UI
                Toast.makeText(getApplicationContext(), "Successfully sign out", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                startActivity(new Intent(getApplicationContext(), ParentActivity.class));
            }
        });

//        if (!isAuthenticated) {
//            authenticateUser();
//        } else {
//            populateUserProfile();
//            drawerLayout.setVisibility(View.VISIBLE);
//            Log.d("AuthSuccess", "Authentication state: " + isAuthenticated);
//        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_AUTHENTICATED, isAuthenticated);
    }

    private void populateUserProfile() {
        progressDialog.show();

        String targetEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));

        CollectionReference studentCollection = FirebaseFirestore.getInstance().collection("Male");
        studentCollection.whereEqualTo("email", targetEmail).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_SHORT).show();
                    } else {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String userName = document.getString("name");
                        String userImage = document.getString("imageUri");
                        name.setText(userName);
                        Picasso.get().load(userImage).into(profileImage);
                        email.setText(targetEmail);
                        role = "Male";
                    }

                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error getting passanger: " + e.getMessage());
                    progressDialog.dismiss();
                });


//        CollectionReference teacherCollection = FirebaseFirestore.getInstance().collection("teacher");
//        teacherCollection.whereEqualTo("email", targetEmail).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (queryDocumentSnapshots.isEmpty()) {
////                        Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_SHORT).show();
//                    } else {
//                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
//                        String userName = document.getString("name");
//                        String userImage = document.getString("imageUri");
//                        name.setText(userName);
//                        Picasso.get().load(userImage).into(profileImage);
//                        email.setText(targetEmail);
//                        role = "teacher";
//                    }
//
//                    progressDialog.dismiss();
//                })
//                .addOnFailureListener(e -> {
//                    System.out.println("Error getting student: " + e.getMessage());
//                    progressDialog.dismiss();
//                });

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
        if (id == R.id.nav_home) {
        } else if (id == R.id.nav_dashboard) {
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
        } else if (id == R.id.nav_update_profile) {
            startActivity(new Intent(getApplicationContext(), UpdateProfile.class));
//        } else if (id == R.id.nav_exam_section) {
//            startActivity(new Intent(getApplicationContext(), DumbActivity.class));
//        } else if (id == R.id.nav_pdf) {
//            startActivity(new Intent(getApplicationContext(), PdfReportGenerator.class));
//        } else if (id == R.id.nav_assistant) {
//            //TODO
        }
            return true;
    }
}
