package com.example.circle;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View RequestsFragmentsView;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestsRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;

    private String currentUserID;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        RequestsFragmentsView = inflater.inflate(R.layout.fragment_request, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestsList = RequestsFragmentsView.findViewById(R.id.chat_request_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestsFragmentsView;

    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions <Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(currentUserID), Contacts.class).build();


        final FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {

                final String list_user_id = getRef(position).getKey();

                DatabaseReference getRequestTypeRef = getRef(position).child("request_type").getRef();
                getRequestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();

                            if (type.equals("sent")){

                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")){
                                            //setting only image
                                            String requestProfileImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String userStatus = dataSnapshot.child("status").getValue().toString();


                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("("+userStatus+")");
                                        holder.reqType.setText("Sent Chat Request");

                                        //remove this condition later after db reset and checking
                                        ChatRequestsRef.child(currentUserID).child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("requestDate")){
                                                    String requsetedDate = dataSnapshot.child("requestDate").getValue().toString();
                                                    holder.reqDate.setText(requsetedDate);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                                        holder.ResponseRequestButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Accept",
                                                        "Reject"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Respond "+ requestUserName + "'s Request");


                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if (i == 0){
                                                            ContactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        ContactsRef.child(list_user_id ).child(currentUserID).child("Contacts")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){
                                                                                    ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                            if (task.isSuccessful()){
                                                                                                Toast.makeText(getContext(), "Accepted", Toast.LENGTH_SHORT).show();
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

                                                            ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(getContext(), "Person Rejected", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }



            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup,false);
                RequestsViewHolder holder = new RequestsViewHolder(view);
                return holder;

            }
        };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus,reqType,reqDate;
        CircleImageView profileImage;
        Button ResponseRequestButton;


        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            ResponseRequestButton = itemView.findViewById(R.id.response_request_or_add_or_remove_btn);

            userName = itemView.findViewById(R.id.user_profile_name);
            reqType = itemView.findViewById(R.id.req_type);
            userStatus = itemView.findViewById(R.id.track_req_date);
            reqDate = itemView.findViewById(R.id.track_req_time);
            profileImage = itemView.findViewById(R.id.users_profile_image);

            ResponseRequestButton.setVisibility(View.VISIBLE);
            reqDate.setVisibility(View.VISIBLE);
        }
    }
}
