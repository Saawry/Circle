package com.example.circle;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationPermissionActivity extends AppCompatActivity {



    private ProgressDialog loadingBar;
    private Double lat,lon;
    private Toolbar mToolbar;
    private Button showMap;
    private TextView locLatLngTv,locUpdateTimeTv;
    private String userID,currentUserID, firendsName,locUpdateTime;

    private DatabaseReference TrackRequestRef, locRef,locUpTimeRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_permission);

        userID = getIntent().getExtras().get("visit_user_id").toString();
        firendsName = getIntent().getExtras().get("visit_user_name").toString();
        currentUserID = getIntent().getExtras().get("current_user_id").toString();

        mToolbar = findViewById(R.id.location_permission_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(firendsName+" 's Current Location");

        showMap = findViewById(R.id.show_user_location_on_map_btn);
        locLatLngTv = findViewById(R.id.user_loc_lat_lng);
        locUpdateTimeTv = findViewById(R.id.user_loc_update_time);

        TrackRequestRef = FirebaseDatabase.getInstance().getReference().child("Track Request");
        locRef = TrackRequestRef.child(userID).child(currentUserID).child("Location");
        locUpTimeRef = TrackRequestRef.child(userID).child(currentUserID).child("requestTime");

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Getting Location Permission");
        loadingBar.setMessage("Please Wait, it will auto Redirected");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();


        locUpTimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    locUpdateTime=dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        locRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    lat= (double) dataSnapshot.child("latitude").getValue();
                    lon= (double) dataSnapshot.child("longitude").getValue();
                    Toast.makeText(LocationPermissionActivity.this, "Location Gotten..!!", Toast.LENGTH_SHORT).show();

                    loadingBar.dismiss();
                    showMap.setVisibility(View.VISIBLE);
                    locLatLngTv.setVisibility(View.VISIBLE);
                    locUpdateTimeTv.setVisibility(View.VISIBLE);

                    locLatLngTv.setText("Location : "+String.valueOf(lat)+","+String.valueOf(lon));
                    locUpdateTimeTv.setText("Location Last Update Time : "+locUpdateTime);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trackIntent = new Intent(LocationPermissionActivity.this, LocationTrackActivity.class);
                trackIntent.putExtra("user_name", firendsName);
                trackIntent.putExtra("lat", lat);
                trackIntent.putExtra("lang", lon);
                startActivity(trackIntent);
            }
        });
    }
}
