package com.example.navixpassanger.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.navixpassanger.R;
import com.example.navixpassanger.carbon.CarbonCalculator;
import com.example.navixpassanger.home.HomeScreen;
import com.example.navixpassanger.nav.AboutUs;
import com.example.navixpassanger.ticket.CancelTicketActivity;
import com.example.navixpassanger.ticket.ShowActiveTicketActivity;
import com.example.navixpassanger.ticket.ShowTicketActivity;
import com.example.navixpassanger.ticket.TicketBookingActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        loadUserMetrics();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        navigationView.bringToFront();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav_bar, R.string.close_nav_bar);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_dashboard);

        findViewById(R.id.cardBookTicket).setOnClickListener(v -> startActivity(new Intent(this, TicketBookingActivity.class)));
        findViewById(R.id.showTicketCard).setOnClickListener(v -> startActivity(new Intent(this, ShowActiveTicketActivity.class)));
        findViewById(R.id.cancelTicketCard).setOnClickListener(v -> startActivity(new Intent(this, CancelTicketActivity.class)));
        findViewById(R.id.bookingHistoryCard).setOnClickListener(v -> startActivity(new Intent(this, ShowTicketActivity.class)));

    }

    private void loadUserMetrics() {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore.getInstance()
                .collection("Male")
                .document(userEmail)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        double carbonSaved = document.getDouble("totalCarbonSaved") != null ?
                                document.getDouble("totalCarbonSaved") : 0;
                        int totalRides = document.getLong("totalBusRides") != null ?
                                document.getLong("totalBusRides").intValue() : 0;
                        double distance = document.getDouble("totalDistanceTraveled") != null ?
                                document.getDouble("totalDistanceTraveled") : 0;

                        // Update UI
                        TextView carbonSavedText = findViewById(R.id.carbonSavedText);
                        TextView totalRidesText = findViewById(R.id.totalRidesText);
                        TextView distanceText = findViewById(R.id.distanceText);
                        TextView impactText = findViewById(R.id.environmentalImpactText);

                        carbonSavedText.setText(String.format("%.1f", carbonSaved));
                        totalRidesText.setText(String.valueOf(totalRides));
                        distanceText.setText(String.format("%.1f", distance));
                        impactText.setText(CarbonCalculator.getEnvironmentalImpact(carbonSaved));
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
        if(id == R.id.nav_dashboard) {}
        else if( id == R.id.nav_home) {
            startActivity(new Intent(getApplicationContext(), HomeScreen.class));
        } else if ( id == R.id.nav_update_profile) {
            startActivity(new Intent(getApplicationContext(), UpdateProfile.class));
        } else if ( id == R.id.nav_about_us ) {
            startActivity(new Intent(getApplicationContext(), AboutUs.class));
        }
        return true;
    }
}

