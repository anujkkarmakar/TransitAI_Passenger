package com.example.navixpassanger.auth;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.navixpassanger.models.Users;
import com.example.navixpassanger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Objects;


public class SignUpActivity extends AppCompatActivity {
    private AppCompatEditText name100, email100, password100;
    private AppCompatButton button100, buttonCalender;
    private AppCompatTextView google, loginPage, dateOfBirth;

    ProgressDialog progressDialog;
    private final String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    FirebaseFirestore storage;
    private AppCompatSpinner spinner;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        name100 = findViewById(R.id.name100);
        email100 = findViewById(R.id.email100);
        password100 = findViewById(R.id.password100);
        button100 = findViewById(R.id.button100);
        buttonCalender = findViewById(R.id.buttonDOB);
        dateOfBirth = findViewById(R.id.calendarTV);
        loginPage = findViewById(R.id.textView8);
        mAuth = FirebaseAuth.getInstance();
        int c = email100.getCurrentHintTextColor();
        Log.d("Hint color", String.format("%X", c));
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        spinner = findViewById(R.id.roleSpinner);
        String[] roles = getResources().getStringArray(R.array.role_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, roles);
        spinner.setAdapter(adapter);

        storage = FirebaseFirestore.getInstance();

        loginPage.setOnClickListener(v -> {
            //start the Login Activity and finish the ongoing activity
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        button100.setOnClickListener(v -> signUpUser());
        buttonCalender.setOnClickListener(v -> selectDate());
    }

    private void selectDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> dateOfBirth.setText(String.format("%d-%d-%d", dayOfMonth, month + 1, year)), 2000, 0, 1);
        if (!isFinishing() && !isDestroyed()) {
            datePickerDialog.show();
        }
    }

    /**
     * Using Firebase default sign in options
     */

    private void signUpUser() {
        String name = name100.getText().toString().trim();
        String email = email100.getText().toString().trim().toLowerCase(Locale.ROOT);
        String password = password100.getText().toString().trim().toLowerCase(Locale.ROOT);
        String dob = dateOfBirth.getText().toString().trim();

        progressDialog.show();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
        } else if (!email.matches(emailPattern)) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Email pattern invalid", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Password length must be at-least 6 characters", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            String role = getRole();
                            if (role.equals("Select Role")) {
                                Toast.makeText(getApplicationContext(), "Please select a valid Gender", Toast.LENGTH_SHORT).show();
                            } else {
                                Users user = new Users(name, email, dob, null, mAuth.getUid());
                                storage.collection(toProperCase(role)).document(email).set(user);

                                progressDialog.dismiss();
                                sendEmailConfirmation();

                                Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(SignUpActivity.this, ParentActivity.class));


                            }
                        } else {
                            Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    });
        }
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


    private String getRole() {
        return spinner.getSelectedItem().toString();
    }

    private void sendEmailConfirmation() {
        Objects.requireNonNull(mAuth.getCurrentUser()).sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Email sent. Please verify", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_SHORT).show());
    }
}
