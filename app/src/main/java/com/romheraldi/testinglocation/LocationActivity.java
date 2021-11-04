package com.romheraldi.testinglocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener, Adapter.ActionData {

    List<Data> dataList;
    RecyclerView listData;
    DatabaseReference databaseReference;
    Adapter helperAdapter;
    LocationManager locationManager;

    Location lLocation;
    GnssStatus.Callback mGnssStatusCallback;

    Double dataLat;
    Double dataLong;

    Boolean onCheck;
    Integer failureCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        listData = findViewById(R.id.listData);
        listData.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://romheraldi-7f058-default-rtdb.firebaseio.com/");
        databaseReference = database.getReference();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Data data=ds.getValue(Data.class);

                    dataList.add(data);
                }

                helperAdapter = new Adapter(dataList);

                helperAdapter.setListener(LocationActivity.this);

                listData.setAdapter(helperAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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

            if (ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(LocationActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 100);
            }

//            locationManager.addGpsStatusListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mGnssStatusCallback = new GnssStatus.Callback() {
                    //
                };
            } else {
                locationManager.addGpsStatusListener(this);
            }
            Integer gpsFreqInMillis = 100;
            Integer gpsFreqInDistance = 1;  // in meters

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
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
//        Toast.makeText(this, "" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
        Log.d("LOCATION_CHANGE", "(" + location.getLatitude() + "," + location.getLongitude() + ")");

//        Log.d("Check Location", String.valueOf(dataLat)+","+String.valueOf(dataLong) +" | "+ String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()));

//        if (onCheck) {
            Double dataDistance = distance(location.getLatitude(), location.getLongitude(), dataLat, dataLong,"M");
//            Log.d("Distace", String.valueOf(dataDistance));
//            if (failureCheck <= 10) {
//                if (dataDistance > 10) {
//                    Log.d("Distace", "Gagal");
//                    getLocation();
//                    failureCheck = failureCheck + 1;
//                    Log.d("Distace", String.valueOf(failureCheck));
//                } else {
//                    Log.d("LOCATION TEST ", "STOP AT " + String.valueOf(dataDistance) + " m");
//                    onCheck = false;
//                    failureCheck = 0;
//                    stopUpdateLocation();
//                }
//            } else {
//                Log.d("LOCATION TEST ", "Gagal konfirmasi data");
//
//                onCheck = false;
//                failureCheck = 0;
//                stopUpdateLocation();
//            }
//        } else {
//            stopUpdateLocation();
//        }
        Log.d("LOCATION TEST ", "STOP AT " + String.valueOf(dataDistance) + " m");
        Toast.makeText(this, String.valueOf(dataDistance), Toast.LENGTH_LONG).show();
        stopUpdateLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void stopUpdateLocation() {
        Log.d("CLOSE", "Close session");
        locationManager.removeUpdates(this);
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            } else if (unit.equals("M")) {
                dist = dist * 1.609344 * 1000;
            }
            return (dist);
        }
    }

    @Override
    public void getLatLong(Double latitude, Double longitude) {
        getLocation();

        dataLat = latitude;
        dataLong = longitude;

        Log.d("ASDASDAS", String.valueOf(dataLat));
        Log.d("ASDASDAS", String.valueOf(dataLong));
        onCheck = true;
        failureCheck = 0;
    }
}