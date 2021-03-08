package com.example.circle;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;


    private DatabaseReference circleReference;

    private String currentUserId;


    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Circle");



        mAuth =FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        else{
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        RootRef = FirebaseDatabase.getInstance().getReference();


        circleReference = FirebaseDatabase.getInstance().getReference().child("All Circle");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        myTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                myViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    protected void onStart(){
        super.onStart();

        if(currentUser == null){
            SendUserToLoginActivity();
        }else{
            VerifyUserExistance();
        }
    }

    private void VerifyUserExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }else{
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                SendUserToSettingsActivity();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_option){
            mAuth.signOut();
            SendUserToLoginActivity();

        }if(item.getItemId() == R.id.main_profile_set_option){
            SendUserToSettingsActivity();

        }
        if(item.getItemId() == R.id.main_find_people_option){

            SendUserToFindFriendsActivity();
        }
        if(item.getItemId() == R.id.main_create_circle_option){

            SendToCreateNewCircleMethod();
        }
        if(item.getItemId() == R.id.main_my_location_option){
            Intent  myLocInt = new Intent(MainActivity.this, MyLocActivity.class);
            startActivity(myLocInt);

        }
        return true;
    }

    private void SendToCreateNewCircleMethod() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Circle Name : ");

        final EditText circleNameField = new EditText(MainActivity.this);
        circleNameField.setHint("e.g: Friends");
        builder.setView(circleNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String circleName = circleNameField.getText().toString();

                if (TextUtils.isEmpty(circleName)){
                    Toast.makeText(MainActivity.this, "Enter Circle Name..!!", Toast.LENGTH_SHORT).show();
                }else{
                    final DatabaseReference newCircleKeyRef = circleReference.push();

                    final String newCircleID = newCircleKeyRef.getKey();

                    CirclesInfo circleInfo = new CirclesInfo(circleName,"1");

                    circleReference.child(newCircleID).child("CirclesInfo").setValue(circleInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        newCircleKeyRef.child("Members").child(currentUserId).setValue("Admin").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    DatabaseReference myCircles = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("My Circles");
                                                    myCircles.child( circleName).setValue( newCircleID).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(MainActivity.this, circleName + " is Created..!!", Toast.LENGTH_SHORT).show();
                                                            Intent setCircleCoverImage = new Intent(MainActivity.this, SetCircleCoverImageActivity.class);
                                                            setCircleCoverImage.putExtra("shareCircleId", newCircleID);
                                                            setCircleCoverImage.putExtra("shareCircleName", circleName);
                                                            startActivity(setCircleCoverImage);
                                                        }
                                                    });
                                                }else{
                                                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }else{
                                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        builder.show();
    }


    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        //finish();
    }
}
