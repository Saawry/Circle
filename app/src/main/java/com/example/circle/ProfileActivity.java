package com.example.circle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private String receiverUserID,  myUserID, Current_State;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    private FirebaseAuth mAuth;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = findViewById(R.id.friends_profile_toolbar);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        Current_State = "new";

        mAuth = FirebaseAuth.getInstance();
        myUserID = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();


        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);


        RetrieveUserInfo();


//        sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SendChatRequest();
//            }
//        });

    }

    private void RetrieveUserInfo() {

        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();

                }else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        ContactsRef.child(myUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)){
                    Current_State = "friends";
                    sendMessageRequestButton.setText("Unfriend");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        ChatRequestRef.child(myUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserID)){
                    Current_State = "request_received";
                    sendMessageRequestButton.setText("Accept");

                    declineMessageRequestButton.setVisibility(View.VISIBLE);
                    declineMessageRequestButton.setEnabled(true);

                    declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RejectRequest();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        ChatRequestRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(myUserID)){
                    Current_State = "request_sent";
                    sendMessageRequestButton.setText("Cancel Request");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if(!myUserID.equals(receiverUserID)){

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(true);

                    if (Current_State.equals("new")){
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }

    }

    private void RejectRequest() {
        ChatRequestRef.child(myUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            sendMessageRequestButton.setEnabled(true);
                            Current_State = "new";
                            sendMessageRequestButton.setText("Add Request");

                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                            declineMessageRequestButton.setEnabled(false);

                            Toast.makeText(ProfileActivity.this, "Request Rejected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void RemoveSpecificContact() {

        ContactsRef.child(myUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(myUserID)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        sendMessageRequestButton.setEnabled(true);
                                        Current_State = "new";
                                        sendMessageRequestButton.setText("Add Friend");

                                        declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                        declineMessageRequestButton.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });

    }

    private void AcceptChatRequest() {

        ContactsRef.child(myUserID).child(receiverUserID).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(myUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                ChatRequestRef.child(myUserID).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                            declineMessageRequestButton.setEnabled(false);

                                                            sendMessageRequestButton.setEnabled(true);
                                                            Current_State = "friends";
                                                            sendMessageRequestButton.setText("Unfriend");
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

    private void CancelChatRequest() {

        ChatRequestRef.child(receiverUserID).child(myUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            sendMessageRequestButton.setEnabled(true);
                            Current_State = "new";
                            sendMessageRequestButton.setText("Add Request");

                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                            declineMessageRequestButton.setEnabled(false);

                            Toast.makeText(ProfileActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void SendChatRequest() {

        Calendar calForDate= Calendar.getInstance(Locale.ENGLISH);
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd:MM:yyyy",Locale.ENGLISH);
        final String currentDate = currentDateFormat.format(calForDate.getTime());

        ChatRequestRef.child(receiverUserID).child(myUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            ChatRequestRef.child(receiverUserID).child(myUserID).child("requestDate").setValue(currentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "request_sent";
                                                sendMessageRequestButton.setText("Cancel Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
