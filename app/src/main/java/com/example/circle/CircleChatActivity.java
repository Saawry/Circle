package com.example.circle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class CircleChatActivity extends AppCompatActivity {

    String CurrentCircleID,CurrentCircleName,CurrentUserId,
            CurrentUserName,currentDate,currentTime;

    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,circleMessageRef,CircleMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_chat);

        CurrentCircleID = getIntent().getExtras().get("shareCircleId").toString();
        CurrentCircleName = getIntent().getExtras().get("shareCircleName").toString();

        mAuth=FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        circleMessageRef= FirebaseDatabase.getInstance().getReference().child("All Circle").child(CurrentCircleID).child("Messages");

        InitializeFields();

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageIntoDatabase();

                userMessageInput.setText("");
                userMessageInput.setHint("Type Message");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }



    private void GetUserInfo() {
        UsersRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    CurrentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendMessageIntoDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = circleMessageRef.push().getKey();
        if (TextUtils.isEmpty(message)){
            Toast.makeText(this, "Write Message", Toast.LENGTH_SHORT).show();
        }else{
            Calendar calForDate= Calendar.getInstance();
            SimpleDateFormat  currentDateFormat = new SimpleDateFormat("dd:MM:yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime= Calendar.getInstance();
            SimpleDateFormat  currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());


            HashMap<String,Object> circleMessageHashKey = new HashMap<>();
            circleMessageRef.updateChildren(circleMessageHashKey);

            CircleMessageKeyRef= circleMessageRef.child(messageKey);


            HashMap<String,Object> circleMessageInfoMap = new HashMap<>();
            circleMessageInfoMap.put("name",CurrentUserName);
            circleMessageInfoMap.put("message",message);
            circleMessageInfoMap.put("date",currentDate);
            circleMessageInfoMap.put("time",currentTime);
            circleMessageInfoMap.put("type","text");

            CircleMessageKeyRef.updateChildren(circleMessageInfoMap);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        circleMessageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void InitializeFields() {

        mToolbar= findViewById(R.id.circle_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(CurrentCircleName);

        SendMessageButton= findViewById(R.id.circle_send_message_button);
        userMessageInput= findViewById(R.id.input_circle_message);
        displayTextMessage= findViewById(R.id.circle_chat_text_display);
        mScrollView= findViewById(R.id.my_scroll_view);

    }



    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){
            String chatDate= (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage= (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName= (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime= (String) ((DataSnapshot) iterator.next()).getValue();
            String chatType= (String) ((DataSnapshot) iterator.next()).getValue();

            displayTextMessage.append("    "+chatName+" :\n" +chatMessage+"\n" +"          "+ chatTime+"      "+chatDate+"\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
