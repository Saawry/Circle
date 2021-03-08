package com.example.circle;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class CircleMembersActivity extends AppCompatActivity {

    private boolean admin;

    private Toolbar nToolbar;
    private RecyclerView MemberRecyclerList;
    private DatabaseReference UsersRef,circleMemberRef;


    private String selfID,CurrentCircleName,circleId;
    private FirebaseAuth myAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_members);



        CurrentCircleName = getIntent().getExtras().get("shareCircleName").toString();
        circleId = getIntent().getExtras().get("shareCircleId").toString();
        //circleId = "-Lb-DvWCu54ejLx2Q2VW";


        myAuth = FirebaseAuth.getInstance();
        selfID = myAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        circleMemberRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(circleId).child("Members");
        //circleInfoRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(circleId).child("CirclesInfo");

        MemberRecyclerList = findViewById(R.id.members_recycler_list);
        MemberRecyclerList.setLayoutManager(new LinearLayoutManager(this));



        nToolbar = findViewById(R.id.circle_members_list_bar_layout);
        setSupportActionBar(nToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Members");

        circleMemberRef.child(selfID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String status = dataSnapshot.getValue().toString();
                    if (status.equals("Admin")){
                        admin=true;
                    }else{
                        admin=false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(circleMemberRef, Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, MembersViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, MembersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MembersViewHolder holder, final int position, @NonNull Contacts model)
                    {

                        final String usersIDs = getRef(position).getKey();

                        circleMemberRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot uniqueKey : dataSnapshot.getChildren()){

                                    UsersRef.child(String.valueOf(uniqueKey)).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                            final String[] dbUserImage = {"default_image"};
                                            final String dbProfileName;

                                            if (dataSnapshot.hasChild("image")){
                                                dbUserImage[0] = dataSnapshot.child("image").getValue().toString();
                                                dbProfileName = dataSnapshot.child("name").getValue().toString();
                                                String dbProfileStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(dbProfileName);
                                                holder.userStatus.setText(dbProfileStatus);
                                                Picasso.get().load(dbUserImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                            }else{
                                                dbProfileName = dataSnapshot.child("name").getValue().toString();
                                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(dbProfileName);
                                                holder.userStatus.setText(profileStatus);

                                            }

                                            holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    if (!selfID.equals(usersIDs)){
                                                        Intent profileIntent = new Intent(CircleMembersActivity.this, ProfileActivity.class);
                                                        profileIntent.putExtra("visit_user_id", usersIDs);
                                                        startActivity(profileIntent);
                                                    }else{
                                                        Toast.makeText(CircleMembersActivity.this, "Your own Profile.", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });
                                            holder.RemoveMemberBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //set.add(usersIDs);
                                                    if (admin && !usersIDs.equals(selfID)){
                                                        UsersRef.child(usersIDs).child("My Circles").child(CurrentCircleName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){

                                                                    circleMemberRef.child(usersIDs).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                Toast.makeText(CircleMembersActivity.this, dbProfileName+" Removed", Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        }
                                                                    });

                                                                }
                                                            }
                                                        });
                                                    }else{
                                                        Toast.makeText(CircleMembersActivity.this, "Can't Remove..!!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        MembersViewHolder viewHolder = new MembersViewHolder(view);
                        return viewHolder;
                    }
                };
        MemberRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class MembersViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus,trDate;
        CircleImageView profileImage;
        Button RemoveMemberBtn;

        public MembersViewHolder(@NonNull View itemView){
            super(itemView);

            RemoveMemberBtn = itemView.findViewById(R.id.response_request_or_add_or_remove_btn);
            RemoveMemberBtn.setVisibility(View.VISIBLE);
            RemoveMemberBtn.setText("Remove");

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.req_type);
            trDate = itemView.findViewById(R.id.track_req_date);
            profileImage = itemView.findViewById(R.id.users_profile_image);

            trDate.setVisibility(View.INVISIBLE);
        }
    }
}
