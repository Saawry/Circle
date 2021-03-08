package com.example.circle;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class TrackerFragment extends Fragment {


    private View TrackerFragmentsView;
    private RecyclerView myTrackersList;
    private DatabaseReference TrackRequestsRef, UsersRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    //for location sharing
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng latLng;
    //private double lon,lat;

    public TrackerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TrackerFragmentsView = inflater.inflate(R.layout.fragment_tracker, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        TrackRequestsRef = FirebaseDatabase.getInstance().getReference().child("Track Request");
        //ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myTrackersList = TrackerFragmentsView.findViewById(R.id.track_request_list);
        myTrackersList.setLayoutManager(new LinearLayoutManager(getContext()));


        //for location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        return TrackerFragmentsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(TrackRequestsRef.child(currentUserID), Contacts.class).build();


        final FirebaseRecyclerAdapter<Contacts, TrackerFragment.TrackersViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, TrackerFragment.TrackersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final TrackerFragment.TrackersViewHolder holder, int position, @NonNull Contacts model) {

                //holder.itemView.findViewById(R.id.response_request_or_add_or_remove_btn).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(position).getKey();

                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("image")) {

                            String requestProfileImage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        }

                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                        holder.userName.setText(requestUserName);
                        holder.reqType.setText("Wants to Track");

                        /////////////////checking for time and date
                        TrackRequestsRef.child(currentUserID).child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("requestTime")){
                                    String time = dataSnapshot.child("requestTime").getValue().toString();
                                    holder.trReqTime.setText(time);
                                }
                                if (dataSnapshot.hasChild("requestDate")){
                                    String date = dataSnapshot.child("requestDate").getValue().toString();
                                    holder.trReqDate.setText(date);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        ////////////////////////////remove if don't work

                        holder.ResponseRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{
                                        "Accept",
                                        "No Action",
                                        "Reject"
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Respond " + requestUserName + "'s Track Request");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (i == 0) {
                                            Calendar calForTime= Calendar.getInstance(Locale.ENGLISH);
                                            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
                                            final String currentTime = currentTimeFormat.format(calForTime.getTime());

                                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                // TODO: Consider calling
                                                return;
                                            }

                                            fusedLocationClient.getLastLocation()
                                                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                                        @Override
                                                        public void onSuccess(Location location) {
                                                            // Got last known location. In some rare situations this can be null.
                                                            if (location != null) {
                                                                latLng = new LatLng(location.getLatitude(),location.getLongitude());
                                                                Toast.makeText(getActivity(), String.valueOf(latLng), Toast.LENGTH_SHORT).show();
                                                            }else{
                                                                Toast.makeText(getActivity(), "Location Empty", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                            DatabaseReference reqStatusRef =TrackRequestsRef.child(currentUserID).
                                                    child(list_user_id).child("requestStatus").getRef();
                                            reqStatusRef.setValue("Allowed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        TrackRequestsRef.child(currentUserID).
                                                                child(list_user_id).child("Location").setValue(latLng).
                                                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            TrackRequestsRef.child(currentUserID).child(list_user_id).child("requestTime")
                                                                                    .setValue(currentTime).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        Toast.makeText(getContext(), "Location Shared", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });

                                        }
                                        if (i == 1){

                                            TrackRequestsRef.child(currentUserID).child(list_user_id).child("requestStatus")
                                                    .setValue("Reject").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){
                                                        DatabaseReference myLocRef =TrackRequestsRef.child(currentUserID).
                                                                child(list_user_id).child("Location").getRef();
                                                        myLocRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(getContext(), "Track Request Disallowed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }

                                        if (i == 2){

                                            TrackRequestsRef.child(currentUserID).child(list_user_id).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(getContext(), "Track Request Rejected", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", list_user_id);
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }


            @NonNull
            @Override
            public TrackerFragment.TrackersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup,false);
                TrackersViewHolder holder = new TrackerFragment.TrackersViewHolder(view);
                return holder;

            }
        };

        myTrackersList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class TrackersViewHolder extends RecyclerView.ViewHolder{

        TextView userName, trReqTime,trReqDate,reqType;
        CircleImageView profileImage;
        Button ResponseRequestButton;


        public TrackersViewHolder(@NonNull View itemView) {
            super(itemView);

            ResponseRequestButton = itemView.findViewById(R.id.response_request_or_add_or_remove_btn);

            userName = itemView.findViewById(R.id.user_profile_name);
            trReqDate = itemView.findViewById(R.id.track_req_date);
            reqType = itemView.findViewById(R.id.req_type);
            trReqTime = itemView.findViewById(R.id.track_req_time);
            profileImage = itemView.findViewById(R.id.users_profile_image);

            trReqTime.setVisibility(View.VISIBLE);
            ResponseRequestButton.setVisibility(View.VISIBLE);
            ResponseRequestButton.setText("Response");

        }
    }
}
