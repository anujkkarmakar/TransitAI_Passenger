package com.example.navixpassanger.stop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.navixpassanger.R;
import com.example.navixpassanger.location.LocationService;

public class LocationTrackingActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private Button locationButton;
    private boolean isLocationTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracking);

        locationButton = findViewById(R.id.location_button);
        locationButton.setText(R.string.send_location);
        locationButton.setOnClickListener(v -> toggleLocationTracking());
    }

    private void toggleLocationTracking() {
        if (!isLocationTracking) {
            if (checkLocationPermission()) {
                startLocationService();
                locationButton.setText(R.string.stop_location);
            } else {
                requestLocationPermission();
            }
        } else {
            stopLocationService();
            locationButton.setText(R.string.send_location);
        }
        isLocationTracking = !isLocationTracking;
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopLocationService() {
        stopService(new Intent(this, LocationService.class));
    }

    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
                locationButton.setText(R.string.stop_location);
                isLocationTracking = true;
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}