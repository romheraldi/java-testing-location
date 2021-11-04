package com.romheraldi.testinglocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener {

    Button get_loc;
    Button move_location;
    TextView last_loc;
    TextView last_loc_lat_long;
    EditText name_store;
    LocationManager locationManager;
    ClipboardManager myClipboard;
    ClipData myClip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Runtime permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        last_loc = findViewById(R.id.lastLoc);
        get_loc = findViewById(R.id.getLoc);
        last_loc_lat_long = findViewById(R.id.lastLocLatLong);
        name_store = findViewById(R.id.nameStore);
        move_location = findViewById(R.id.moveLocation);

        get_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        last_loc_lat_long.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String latLong;

                latLong = last_loc_lat_long.getText().toString();

                myClip = ClipData.newPlainText("text", latLong);
                myClipboard.setPrimaryClip(myClip);

                Toast.makeText(MainActivity.this, "Data copied: " + latLong, Toast.LENGTH_SHORT).show();
            }
        });

        move_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LocationActivity.class));
            }
        });
    }

    public void getLocation() {
        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            criteria.setAltitudeRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setCostAllowed(true);
            criteria.setBearingRequired(false);

            //API level 9 and up
            criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

            //API level 31 and up


            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 100);
            }

            locationManager.addGpsStatusListener(this);

            Integer gpsFreqInMillis = 5000;
            Integer gpsFreqInDistance = 10;  // in meters
            locationManager.requestLocationUpdates(gpsFreqInMillis, gpsFreqInDistance, criteria, this, null);
        } catch (IllegalArgumentException e) {
            Log.e("ERROR_LOCATION", e.getLocalizedMessage());
        } catch (SecurityException e) {
            Log.e("ERROR_LOCATION", e.getLocalizedMessage());
        } catch (RuntimeException e) {
            Log.e("ERROR_LOCATION", e.getLocalizedMessage());
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Toast.makeText(this, "" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
        Log.d("LOCATION_CHANGE", "(" + location.getLatitude() + "," + location.getLongitude() + ")");

        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);

            String latLong = location.getLatitude() + "," + location.getLongitude();
            Float accuracy = location.getAccuracy();

            last_loc.setText(address);
            last_loc_lat_long.setText(latLong + " with " + accuracy.toString() + " accuracy");


            String nameStore = name_store.getText().toString().trim().toUpperCase();

            FirebaseDatabase database = FirebaseDatabase.getInstance("https://romheraldi-7f058-default-rtdb.firebaseio.com/");
            DatabaseReference ref = database.getReference();
            Log.d("LOCATION_CHANGE", ref.toString());

            Data newData = new Data();

            newData.setNameStore(nameStore);
            newData.setLatLong(latLong);
            newData.setAccuracy(accuracy);

            String dataId = ref.push().getKey();
            newData.setDataId(dataId);

            ref.child(dataId).setValue(newData);

            stopUpdateLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            if (status == LocationProvider.OUT_OF_SERVICE) {
                notifyLocationProviderStatusUpdated(false);
            } else {
                notifyLocationProviderStatusUpdated(true);
            }
        }
    }

    private void notifyLocationProviderStatusUpdated(boolean isLocationProviderAvailable) {
        //Broadcast location provider status change here
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(true);
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(false);
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    public void stopUpdateLocation() {
        locationManager.removeUpdates(this);
    }
}