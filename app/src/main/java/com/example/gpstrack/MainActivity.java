package com.example.gpstrack;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    // References pour les elements de UI
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_wayPointCounts;
    Button btn_newWayPoint, btn_showWayPointList, btn_showMap;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_locationsupdates, sw_gps;

    // Variable pour se rapper si yous tracker vote localisation ou pas
    boolean updateOn = false;

    // Localisation actualise
    Location currentLocation;

    // List des emplacements unregisters
    List<Location> savedLocations;

    // La demande de localisation est une configuration pour tous les paramètres liés à FusedLocationProviderClient
    LocationRequest locationRequest;

    // API Google pour les services de localisation. La majorite des fonctions utilisee dans cette classe
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des variables
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);
        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPointList);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumbs);
        btn_showMap = findViewById(R.id.btn_showMap);

        // Proprietes de la requete de localisation
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000 * DEFAULT_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(1000 * FAST_UPDATE_INTERVAL)
                .build();

        // Configuration du LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIValues(locationResult.getLastLocation());
            }
        };

        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    MyApplication myApplication = (MyApplication) getApplicationContext();
                    savedLocations = myApplication.getMyLocations();
                    savedLocations.add(currentLocation);
                } else {
                    Toast.makeText(MainActivity.this, "Aucune localisation actuelle disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowSaveLocationList.class);
                startActivity(i);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowSaveLocationList.class);
                startActivity(i);
            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Utiliser des capteurs GPS");
                } else {
                    locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Utilisation des Antennes + WIFI ");
                }
            }
        });

        sw_locationsupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationsupdates.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();
    }

    @SuppressLint("SetTextI18n")
    private void stopLocationUpdates() {
        tv_updates.setText("L'emplacement n'est pas suivi");
        tv_lat.setText("Ne pas suivre l'emplacement");
        tv_lon.setText("Ne pas suivre l'emplacement");
        tv_speed.setText("Ne pas suivre l'emplacement");
        tv_address.setText("Ne pas suivre l'emplacement");
        tv_accuracy.setText("Ne pas suivre l'emplacement");
        tv_altitude.setText("Ne pas suivre l'emplacement");
        tv_sensor.setText("Ne pas suivre l'emplacement");

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @SuppressLint("SetTextI18n")
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            tv_updates.setText("L'emplacement est suivi");
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(this, "Cette application nécessite une autorisation pour pouvoir exploiter la propriété", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        updateUIValues(location);
                        currentLocation = location;
                    } else {
                        Toast.makeText(MainActivity.this, "Impossible d'obtenir la localisation actuelle", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUIValues(Location location) {
        if (location != null) {
            tv_lat.setText(String.valueOf(location.getLatitude()));
            tv_lon.setText(String.valueOf(location.getLongitude()));
            tv_accuracy.setText(String.valueOf(location.getAccuracy()));

            if (location.hasAltitude()) {
                tv_altitude.setText(String.valueOf(location.getAltitude()));
            } else {
                tv_altitude.setText("Non disponible");
            }

            if (location.hasSpeed()) {
                tv_speed.setText(String.valueOf(location.getSpeed()));
            } else {
                tv_speed.setText("Non disponible");
            }

            Geocoder geocoder = new Geocoder(MainActivity.this);

            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    tv_address.setText(addresses.get(0).getAddressLine(0));
                } else {
                    tv_address.setText("Adresse non disponible");
                }
            } catch (IOException e) {
                tv_address.setText("Impossible d'obtenir l'adresse postale");
            }

            MyApplication myApplication = (MyApplication) getApplicationContext();
            savedLocations = myApplication.getMyLocations();

            tv_wayPointCounts.setText(String.valueOf(savedLocations.size()));

        }
    }
}
