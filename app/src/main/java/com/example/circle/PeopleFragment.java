package com.example.circle;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class PeopleFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactsRef, UsersRef,TrackRequestRef;
    private FirebaseAuth mAuth;
    private String currentUserID;




    public PeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_people, container, false);

        myContactsList = ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        TrackRequestRef = FirebaseDatabase.getInstance().getReference().child("Track Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ContactsRef, Contacts.class).build();


        final FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {

                final String usersIDs = getRef(position).getKey();
                final String[] userImage = {"default_image"};
                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String profileName;


                        if (dataSnapshot.hasChild("image")){
                            userImage[0] = dataSnapshot.child("image").getValue().toString();
                            profileName = dataSnapshot.child("name").getValue().toString();
                            String profileStatus = dataSnapshot.child("status").getValue().toString();


                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);
                            Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        }else{
                            profileName = dataSnapshot.child("name").getValue().toString();
                            String profileStatus = dataSnapshot.child("status").getValue().toString();


                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);

                        }

                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", usersIDs);
                                startActivity(profileIntent);
                            }
                        });
                        holder.TalkButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("visit_user_id", usersIDs);
                                chatIntent.putExtra("visit_user_name", profileName);
                                chatIntent.putExtra("visit_image", userImage[0]);

                                startActivity(chatIntent);
                            }
                        });
                        holder.TrackButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                SendToTrackMethod(usersIDs,profileName);
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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;

            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();

    }

    private void SendToTrackMethod(final String userID, final String name) {

        Calendar calForTime= Calendar.getInstance(Locale.ENGLISH);
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
        String currentTime = currentTimeFormat.format(calForTime.getTime());

        Calendar calForDate= Calendar.getInstance(Locale.ENGLISH);
        SimpleDateFormat  currentDateFormat = new SimpleDateFormat("dd:MM:yyyy",Locale.ENGLISH);
        final String currentDate = currentDateFormat.format(calForDate.getTime());

        TrackRequestRef.child(userID).child(currentUserID).child("requestTime").setValue(currentTime)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            TrackRequestRef.child(userID).child(currentUserID).child("requestDate").setValue(currentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Intent locPerIntent = new Intent(getContext(), LocationPermissionActivity.class);
                                                locPerIntent.putExtra("visit_user_id", userID);
                                                locPerIntent.putExtra("visit_user_name", name);
                                                locPerIntent.putExtra("current_user_id", currentUserID);
                                                startActivity(locPerIntent);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus, tv1,tv2;
        CircleImageView profileImage;
        Button TalkButton, TrackButton;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            TalkButton = itemView.findViewById(R.id.talk_btn);
            TalkButton.setVisibility(View.VISIBLE);
            TrackButton = itemView.findViewById(R.id.track_btn);
            TrackButton.setVisibility(View.VISIBLE);

            userName = itemView.findViewById(R.id.user_profile_name);
            tv1 = itemView.findViewById(R.id.track_req_date);
            tv2 = itemView.findViewById(R.id.track_req_time);
            userStatus = itemView.findViewById(R.id.req_type);
            profileImage = itemView.findViewById(R.id.users_profile_image);

            tv1.setVisibility(View.INVISIBLE);
            tv2.setVisibility(View.INVISIBLE);

        }
    }
}
