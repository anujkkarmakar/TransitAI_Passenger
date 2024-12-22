package com.example.navixpassanger.ticket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navixpassanger.R;
import com.example.navixpassanger.bus.Stop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketBookingActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Spinner fromSpinner, toSpinner;
    private RadioGroup journeyTypeGroup, busTypeGroup;
    private Button calculateFareButton;
    private View fareDetailsCard;
    private TextView distanceText, baseFareText, acChargeText, returnChargeText, totalFareText;
    private Map<String, Stop> stopsMap;
    private ProgressDialog progressDialog;
    private Button bookTicketButton;
    private double totalFare = 0.0;

    // Fare calculation constants
    private static final double BASE_RATE_PER_KM = 2.0;  // Base fare per km
    private static final double AC_SURCHARGE = 1.5;      // 50% extra for AC
    private static final double RETURN_DISCOUNT = 0.9;   // 10% discount on return journey
    private static final double MIN_FARE = 20.0;         // Minimum fare

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_ticket);

        bookTicketAlertDialog();
        initializeVariables();
        initializeViews();
        setupProgressDialog();
        setupListeners();
        loadStops();
    }

    private void bookTicketAlertDialog() {
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

    private void initializeVariables() {
        db = FirebaseFirestore.getInstance();
        stopsMap = new HashMap<>();
    }

    private void initializeViews() {
        fromSpinner = findViewById(R.id.spinnerFrom);
        toSpinner = findViewById(R.id.spinnerTo);
        journeyTypeGroup = findViewById(R.id.journeyTypeGroup);
        busTypeGroup = findViewById(R.id.busTypeGroup);
        calculateFareButton = findViewById(R.id.calculateFareButton);
        fareDetailsCard = findViewById(R.id.fareDetailsCard);
        distanceText = findViewById(R.id.distanceText);
        baseFareText = findViewById(R.id.baseFareText);
        acChargeText = findViewById(R.id.acChargeText);
        returnChargeText = findViewById(R.id.returnChargeText);
        totalFareText = findViewById(R.id.totalFareText);
        bookTicketButton = findViewById(R.id.buttonBookTicket);

        // Initially hide the fare details
        fareDetailsCard.setVisibility(View.GONE);
        bookTicketButton.setVisibility(View.GONE);
        calculateFareButton.setEnabled(false);
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Stops...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        calculateFareButton.setOnClickListener(v -> calculateFare());

        // Add listeners for radio groups to update fare when selection changes
        journeyTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (fareDetailsCard.getVisibility() == View.VISIBLE) {
                calculateFare();
            }
        });

        busTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (fareDetailsCard.getVisibility() == View.VISIBLE) {
                calculateFare();
            }
        });
    }

    private void loadStops() {
        if (progressDialog != null) {
            progressDialog.show(); // Show dialog before starting the Firebase query
        }

        db.collection("Stops")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, List<Stop>> stopNameGroups = new HashMap<>();

                    // Group stops by name
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Stop stop = new Stop(
                                document.getString("stop_id"),
                                document.getString("stop_name"),
                                document.getDouble("stop_lat"),
                                document.getDouble("stop_lon")
                        );

                        String stopName = stop.getStop_name();
                        if (!stopNameGroups.containsKey(stopName)) {
                            stopNameGroups.put(stopName, new ArrayList<>());
                        }
                        stopNameGroups.get(stopName).add(stop);
                    }

                    // Create unique display names
                    List<String> uniqueStopNames = new ArrayList<>();
                    for (Map.Entry<String, List<Stop>> entry : stopNameGroups.entrySet()) {
                        List<Stop> stops = entry.getValue();
                        if (stops.size() == 1) {
                            Stop stop = stops.get(0);
                            uniqueStopNames.add(stop.getStop_name());
                            stopsMap.put(stop.getStop_name(), stop);
                        } else {
                            for (Stop stop : stops) {
                                String displayName = stop.getStop_name() + " (" + stop.getStop_id() + ")";
                                uniqueStopNames.add(displayName);
                                stopsMap.put(displayName, stop);
                            }
                        }
                    }

                    Collections.sort(uniqueStopNames);
                    populateSpinners(uniqueStopNames);
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss(); // Dismiss after data is loaded and UI is updated
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading stops: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });

        calculateFareButton.setEnabled(true);
        progressDialog.dismiss();
    }

    private void populateSpinners(List<String> stopNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                stopNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);
    }

    private void calculateFare() {
        String fromStopDisplay = fromSpinner.getSelectedItem().toString();
        String toStopDisplay = toSpinner.getSelectedItem().toString();

        if (fromStopDisplay.equals(toStopDisplay)) {
            fareDetailsCard.setVisibility(View.GONE);
            bookTicketButton.setVisibility(View.GONE);
            Toast.makeText(this, "Please select different stops",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Stop fromStop = stopsMap.get(fromStopDisplay);
        Stop toStop = stopsMap.get(toStopDisplay);

        if (fromStop == null || toStop == null) {
            Toast.makeText(this, "Error: Stop information not found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate distance
        double distance = calculateDistance(
                fromStop.getLat(), fromStop.getLon(),
                toStop.getLat(), toStop.getLon()
        );

        // Calculate base fare
        double baseFare = Math.max(distance * BASE_RATE_PER_KM, MIN_FARE);

        // Calculate AC charges if applicable
        boolean isAC = busTypeGroup.getCheckedRadioButtonId() == R.id.radioAC;
        double acCharges = isAC ? (baseFare * (AC_SURCHARGE - 1)) : 0;

        // Calculate return journey charges if applicable
        boolean isReturn = journeyTypeGroup.getCheckedRadioButtonId() == R.id.radioReturn;
        double returnCharges = isReturn ? (baseFare + acCharges) : 0;
        if (isReturn) {
            returnCharges *= RETURN_DISCOUNT; // Apply return journey discount
        }

        // Calculate total fare
        totalFare = baseFare + acCharges + returnCharges;

        // Update UI with fare details
        updateFareDetails(distance, baseFare, acCharges, returnCharges, totalFare);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    private void updateFareDetails(double distance, double baseFare,
                                   double acCharges, double returnCharges,
                                   double totalFare) {
        // Show the fare details card
        fareDetailsCard.setVisibility(View.VISIBLE);

        // Update text views with fare details
        distanceText.setText(String.format("Distance: %.2f km", distance));
        baseFareText.setText(String.format("Base Fare: ₹%.2f", baseFare));
        acChargeText.setText(String.format("AC Charges: ₹%.2f", acCharges));
        returnChargeText.setText(String.format("Return Journey Charges: ₹%.2f", returnCharges));
        totalFareText.setText(String.format("Total Fare: ₹%.2f", totalFare));

        // Make AC charges visible only if AC is selected
        acChargeText.setVisibility(
                busTypeGroup.getCheckedRadioButtonId() == R.id.radioAC ?
                        View.VISIBLE : View.GONE
        );

        // Make return charges visible only if Return journey is selected
        returnChargeText.setVisibility(
                journeyTypeGroup.getCheckedRadioButtonId() == R.id.radioReturn ?
                        View.VISIBLE : View.GONE
        );

        bookTicketButton.setVisibility(View.VISIBLE);
        bookTicketButton.setOnClickListener(v -> startBookingProcess(distance));
    }

    private void startBookingProcess(double distance) {
        progressDialog.show();

        // Fetch current user email from Firebase Auth
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Fetch user details from Male collection
        db.collection("Male")
                .document(userEmail)  // Using email as document ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        String imageUrl = documentSnapshot.getString("imageUrl");

                        Intent intent = new Intent(this, TicketConfirmationActivity.class);
                        intent.putExtra("userName", userName);
                        intent.putExtra("userEmail", userEmail);
                        intent.putExtra("fromStop", fromSpinner.getSelectedItem().toString());
                        intent.putExtra("toStop", toSpinner.getSelectedItem().toString());
                        intent.putExtra("journeyType",
                                journeyTypeGroup.getCheckedRadioButtonId() == R.id.radioReturn ?
                                        "Return" : "One Way");
                        intent.putExtra("busType",
                                busTypeGroup.getCheckedRadioButtonId() == R.id.radioAC ?
                                        "AC" : "Non-AC");
                        intent.putExtra("totalFare", totalFare);
                        intent.putExtra("distance", distance);

                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error fetching user details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        progressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear any references if needed
        stopsMap.clear();
    }
}