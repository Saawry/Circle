package com.example.circle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddMemberActivity extends AppCompatActivity {

    long currentMemberAmount ;

    private Toolbar mToolbar;
    private RecyclerView AddMemberRecyclerList;
    private DatabaseReference UsersRef,ContactsRef,circleMemberRef,circleInfoRef;




    private String selfID,CurrentCircleName,circleId;
    private FirebaseAuth myAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        circleId = getIntent().getExtras().get("shareCircleId").toString();
        CurrentCircleName = getIntent().getExtras().get("shareCircleName").toString();

        AddMemberRecyclerList = findViewById(R.id.circle_add_member_recycler_list);
        AddMemberRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = findViewById(R.id.circle_add_member_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add Members");


        myAuth = FirebaseAuth.getInstance();
        selfID = myAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(selfID);
        circleMemberRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(circleId).child("Members");
        circleInfoRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(circleId).child("CirclesInfo");


    }


    @Override
    protected void onStart() {
        super.onStart();

        //check if exists on members list, may need queue & get children, need flag
        //alternatives can be search like find people or all contacts, if hashSet exists, toast msg


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContactsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, AddMembersViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, AddMembersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final AddMembersViewHolder holder, final int position, @NonNull Contacts model)
                    {

                        final String usersIDs = getRef(position).getKey();

                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
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

                                holder.AddMemberBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        UsersRef.child(usersIDs).child("My Circles").child(CurrentCircleName).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    Toast.makeText(AddMemberActivity.this, "Already a Member", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    AddMember(usersIDs,CurrentCircleName,circleId,dbProfileName);
                                                    //GetOrUpdateMemberCount();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });


                                    }
                                });

                                holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent profileIntent = new Intent(AddMemberActivity.this, ProfileActivity.class);
                                        profileIntent.putExtra("visit_user_id", usersIDs);
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
                    public AddMembersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        AddMembersViewHolder viewHolder = new AddMembersViewHolder(view);
                        return viewHolder;
                    }
                };
        AddMemberRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

//    private void GetOrUpdateMemberCount() {
//        circleMemberRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists())
//                {
//                    currentMemberAmount = dataSnapshot.getChildrenCount();
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//        circleInfoRef.child("memberAmount").setValue(currentMemberAmount)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()){
//
//                        }
//                    }
//                });
//    }

    private void AddMember(final String userID, String circleName, String circleId, final String ProfileName) {
        UsersRef.child(userID).child("My Circles").child(circleName).setValue(circleId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    circleMemberRef.child(userID).setValue("Member").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(AddMemberActivity.this, ProfileName+" Added to Circle", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }


    public static class AddMembersViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus, trDate;
        CircleImageView profileImage;
        Button AddMemberBtn;

        public AddMembersViewHolder(@NonNull View itemView){
            super(itemView);

            AddMemberBtn = itemView.findViewById(R.id.response_request_or_add_or_remove_btn);
            AddMemberBtn.setVisibility(View.VISIBLE);
            AddMemberBtn.setText("Add");

            userName = itemView.findViewById(R.id.user_profile_name);
            trDate = itemView.findViewById(R.id.track_req_date);
            userStatus = itemView.findViewById(R.id.req_type);
            profileImage = itemView.findViewById(R.id.users_profile_image);

            trDate.setVisibility(View.INVISIBLE);
        }
    }

}
