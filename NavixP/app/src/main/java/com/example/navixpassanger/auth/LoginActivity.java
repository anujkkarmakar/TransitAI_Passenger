package com.example.navixpassanger.auth;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.navixpassanger.R;
//import com.example.navixpassanger.home.HomeScreen;
import com.example.navixpassanger.home.HomeScreen;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private AppCompatEditText email100, password100;
    private AppCompatTextView textView6, forgotPassword, sendVerificationEmail;
    private AppCompatButton button100;
    LinearLayout linearLayout;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email100 = findViewById(R.id.email100);
        password100 = findViewById(R.id.password100);
        button100 = findViewById(R.id.button100);
        mAuth = FirebaseAuth.getInstance();
        textView6 = findViewById(R.id.textView6);
        forgotPassword = findViewById(R.id.forgotPassword);
        linearLayout = findViewById(R.id.linearLayout3);
        sendVerificationEmail = findViewById(R.id.sendVerificationEmail);

        AppCompatSpinner spinner = findViewById(R.id.roleSpinner);
        String[] roles = getResources().getStringArray(R.array.role_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, roles);
        spinner.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        textView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                finish();
            }
        });

        button100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });

        sendVerificationEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailConfirmation();
            }
        });
    }

    private void sendEmailConfirmation() {
        mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Email sent. Please verify", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public String toProperCase(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return the input as it is if it's null or empty
        }

        String[] words = input.toLowerCase().split("\\s+"); // Split input by spaces
        StringBuilder properCaseString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                // Capitalize the first letter of each word and append the rest of the word
                properCaseString.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        // Trim the final string to remove any extra trailing space
        return properCaseString.toString().trim();
    }

    private void loginUser() {
        //now check if the user enters correct results with the firebase database
        String email = email100.getText().toString().toLowerCase(Locale.ROOT).trim();
        String password = password100.getText().toString().trim();
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).

                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if (mAuth.getCurrentUser().isEmailVerified()) {

                                //Get selected role from spinner
                                AppCompatSpinner spinner = findViewById(R.id.roleSpinner);
                                String selectedRole = spinner.getSelectedItem().toString().toLowerCase();

                                progressDialog.dismiss();

                                FirebaseFirestore.getInstance()
                                        .collection(toProperCase(selectedRole))
                                        .whereEqualTo("email", email)
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Change the Intent
                                                startActivity(new Intent(getApplicationContext(), HomeScreen.class));
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Something went wrong: " + e.getMessage(), Toast.LENGTH_SHORT).show());


//
//
//
//
//                                // Redirect based on role
                                if (selectedRole.equalsIgnoreCase("Male")) {
                                    startActivity(new Intent(LoginActivity.this, HomeScreen.class));
                                }
//                                } else if (selectedRole.equalsIgnoreCase("teacher")) {
//                                    startActivity(new Intent(LoginActivity.this, TeacherDashboardActivity.class));
//                                } else {
//                                    // Handle other roles if needed
//                                    Toast.makeText(LoginActivity.this, "Role not recognized", Toast.LENGTH_SHORT).show();
//                                    return;
//                                }
//                                startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
//                                Log.d(TAG, "Login:success");
//                                //Changed from HomeScreen.class to StudentDashboard.class
//
//                                Toast.makeText(LoginActivity.this, "Authentication Successful", Toast.LENGTH_LONG).show();
//                                finish();
                            } else {
                                progressDialog.dismiss();
                                verifyEmailAltertDialog();
                                sendVerificationEmail.setVisibility(View.VISIBLE);
                            }
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendPasswordResetEmail() {
        String emailAddress = email100.getText().toString().trim();

        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                });
    }

    private void verifyEmailAltertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info !")
                .setMessage("Please verify your email address")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

}