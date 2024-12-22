package com.example.navixpassanger.bus;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navixpassanger.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusRouteActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Spinner fromSpinner, toSpinner;
    private Button searchButton;
    private RecyclerView routeRecyclerView;
    private ProgressBar progressBar;
    private View directDistanceCard;
    private TextView textViewAvailableRoutes;
    private Map<String, Stop> stopsMap;
    private List<BusRoute> allRoutes;
    private RouteAdapter routeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route);

        initializeViews();
        initializeData();
        loadAllStops();
    }

    private void initializeViews() {
        fromSpinner = findViewById(R.id.spinnerFrom);
        toSpinner = findViewById(R.id.spinnerTo);
        searchButton = findViewById(R.id.buttonSearch);
        routeRecyclerView = findViewById(R.id.recyclerViewRoutes);
        progressBar = findViewById(R.id.progressBar);
        directDistanceCard = findViewById(R.id.directDistanceCard);
        textViewAvailableRoutes = findViewById(R.id.textViewAvailableRoutes);

        routeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(v -> {
            showLoading();
            findBestRoutes();
        });
    }

    private void initializeData() {
        db = FirebaseFirestore.getInstance();
        stopsMap = new HashMap<>();
        allRoutes = new ArrayList<>();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        directDistanceCard.setVisibility(View.GONE);
        textViewAvailableRoutes.setVisibility(View.GONE);
        routeRecyclerView.setVisibility(View.GONE);
    }

    private void showResults() {
        progressBar.setVisibility(View.GONE);
        directDistanceCard.setVisibility(View.VISIBLE);
        textViewAvailableRoutes.setVisibility(View.VISIBLE);
        routeRecyclerView.setVisibility(View.VISIBLE);
    }

    private void loadAllStops() {
        showLoading();
        db.collection("Stops")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Stop stop = new Stop(
                                document.getString("stop_id"),
                                document.getString("stop_name"),
                                document.getDouble("stop_lat"),
                                document.getDouble("stop_lon")
                        );
                        stopsMap.put(stop.getStop_id(), stop);
                    }
                    loadAllRoutes();
                    populateSpinners();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading stops: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showResults();
                });
    }

    private void loadAllRoutes() {
        db.collection("Bus_Route")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allRoutes.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        BusRoute route = document.toObject(BusRoute.class);
                        if (route != null) {
                            route.setId(document.getId());
                            allRoutes.add(route);
                        }
                    }
                    showResults();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading routes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showResults();
                });
    }

    private void populateSpinners() {
        List<String> stopNames = new ArrayList<>();
        for (Stop stop : stopsMap.values()) {
            stopNames.add(stop.getStop_name());
        }
        Collections.sort(stopNames);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                stopNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);
    }

    private void findBestRoutes() {
        String fromStopName = fromSpinner.getSelectedItem().toString();
        String toStopName = toSpinner.getSelectedItem().toString();

        if (fromStopName.equals(toStopName)) {
            Toast.makeText(this, "Please select different stops",
                    Toast.LENGTH_SHORT).show();
            showResults();
            return;
        }

        Stop fromStop = null;
        Stop toStop = null;

        for (Stop stop : stopsMap.values()) {
            if (stop.getStop_name().equals(fromStopName)) fromStop = stop;
            if (stop.getStop_name().equals(toStopName)) toStop = stop;
        }

        if (fromStop == null || toStop == null) {
            Toast.makeText(this, "Invalid stops selected",
                    Toast.LENGTH_SHORT).show();
            showResults();
            return;
        }

        final double directDistance = calculateDistance(
                fromStop.getLat(), fromStop.getLon(),
                toStop.getLat(), toStop.getLon()
        );

        List<BusRoute> validRoutes = getValidRoutes(fromStop, toStop);
        if (validRoutes.isEmpty()) {
            Toast.makeText(this, "No routes found between selected stops",
                    Toast.LENGTH_SHORT).show();
            showResults();
            return;
        }

        updateUI(validRoutes, directDistance);
    }

    private List<BusRoute> getValidRoutes(Stop fromStop, Stop toStop) {
        List<BusRoute> validRoutes = new ArrayList<>();

        for (BusRoute route : allRoutes) {
            if (isRouteValid(route, fromStop.getStop_id(), toStop.getStop_id())) {
                route.setDistance(calculateRouteDistance(route));
                validRoutes.add(route);
            }
        }

        Collections.sort(validRoutes,
                (r1, r2) -> Double.compare(r2.getDistance(), r1.getDistance()));

        return validRoutes.subList(0, Math.min(3, validRoutes.size()));
    }

    private boolean isRouteValid(BusRoute route, String fromStopId, String toStopId) {
        List<String> stops = route.getStops();
        int fromIndex = stops.indexOf(fromStopId);
        int toIndex = stops.indexOf(toStopId);
        return fromIndex != -1 && toIndex != -1 && fromIndex != toIndex;
    }

    private double calculateRouteDistance(BusRoute route) {
        double totalDistance = 0;
        List<String> stops = route.getStops();

        for (int i = 0; i < stops.size() - 1; i++) {
            Stop currentStop = stopsMap.get(stops.get(i));
            Stop nextStop = stopsMap.get(stops.get(i + 1));

            if (currentStop != null && nextStop != null) {
                totalDistance += calculateDistance(
                        currentStop.getLat(), currentStop.getLon(),
                        nextStop.getLat(), nextStop.getLon()
                );
            }
        }
        return totalDistance;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void updateUI(List<BusRoute> validRoutes, double directDistance) {
        TextView directDistanceText = findViewById(R.id.textViewDirectDistance);
        directDistanceText.setText(String.format("%.2f km", directDistance));

        routeAdapter = new RouteAdapter(validRoutes, directDistance);
        routeRecyclerView.setAdapter(routeAdapter);

        showResults();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (routeAdapter != null) {
            routeAdapter = null;
        }
    }
}