package com.example.circle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyLocActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView locLatLngTv;
    private Button getBtn, showOnMapBtn;
    private String selfID, selfName;
    private FirebaseAuth myAuth;
    private DatabaseReference myRef;
    private FusedLocationProviderClient fusedLocationClient;

    double lat, lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_loc);

        mToolbar = findViewById(R.id.get_my_location_toolbar);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Location");


        getBtn = findViewById(R.id.get_loc_btn);
        locLatLngTv = findViewById(R.id.my_loc_lat_lng);
        showOnMapBtn = findViewById(R.id.show_my_location_on_map_btn);

        myAuth = FirebaseAuth.getInstance();
        selfID = myAuth.getCurrentUser().getUid();

        myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(selfID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MyLocActivity.this);

        myRef.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    selfName = dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        getBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetLocation();
            }
        });

        showOnMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locInt = new Intent(MyLocActivity.this, LocationTrackActivity.class);
                locInt.putExtra("user_name", selfName);
                locInt.putExtra("lat", lat);
                locInt.putExtra("lang", lang);
                startActivity(locInt);
            }
        });
    }

    private void GetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(MyLocActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lat=location.getLatitude();
                            lang=location.getLongitude();
                            showOnMapBtn.setVisibility(View.VISIBLE);
                            locLatLngTv.setVisibility(View.VISIBLE);
                            getBtn.setVisibility(View.INVISIBLE);
                            locLatLngTv.setText("Location : "+String.valueOf(lat)+","+String.valueOf(lang));
                            Toast.makeText(MyLocActivity.this, "Location Gotten", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyLocActivity.this, "Location Empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(MyLocActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String msg = e.getMessage();
                Toast.makeText(MyLocActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
