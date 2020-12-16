package com.example.circle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private Button UpdateAccountSettings,DeleteAccountBtn;
    private EditText userName, userStatus;
    private TextView displayUserName,displayMailOrPhn;
    private CircleImageView userProfileImage;

    private String currentUserID,mailOrPhn;
    private FirebaseAuth mAuth;

    private StorageReference UserProfileImageRef;
    private DatabaseReference RootRef;

    private Uri ImageUri;
    private static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.my_profile_toolbar);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Profile");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();


        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();
        userName.setVisibility(View.INVISIBLE);


        RetrieveUserInfo();


        DeleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);

                                    startActivity(mainIntent);
                                }
                            }
                        });
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingBar.setTitle("Update Profile");
                loadingBar.setMessage("Please Wait, Profile Is Updating");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();
                UpdateImage();
            }
        });

    }

    private void RetrieveUserInfo() {


        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))  ) {

                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    if (dataSnapshot.hasChild("mailOrPhn")){
                        mailOrPhn = dataSnapshot.child("mailOrPhn").getValue().toString();
                    }



                    DeleteAccountBtn.setVisibility(View.VISIBLE);

                    displayUserName.setVisibility(View.VISIBLE);
                    displayMailOrPhn.setVisibility(View.VISIBLE);
                    displayUserName.setText(retrieveUserName);
                    displayMailOrPhn.setText(mailOrPhn);//update here

                    userStatus.setText(retrieveStatus);
                    userName.setText(retrieveUserName);

                    Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {

                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    if (dataSnapshot.hasChild("mailOrPhn")){
                        mailOrPhn = dataSnapshot.child("mailOrPhn").getValue().toString();
                    }

                    DeleteAccountBtn.setVisibility(View.VISIBLE);

                    displayUserName.setVisibility(View.VISIBLE);
                    displayMailOrPhn.setVisibility(View.VISIBLE);
                    displayUserName.setText(retrieveUserName);
                    displayMailOrPhn.setText(mailOrPhn);//update here

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                } else {
                    if (dataSnapshot.hasChild("mailOrPhn")){
                        mailOrPhn = dataSnapshot.child("mailOrPhn").getValue().toString();
                    }
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please Update Profile Info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void UpdateSettings(String imageLink) {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please Provide User Name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please Provide Your Status", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("mailOrPhn", mailOrPhn);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            profileMap.put("image", imageLink);


            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                loadingBar.dismiss();
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error ! " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void UpdateImage() {

        final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

        UploadTask uploadTask=null;
        uploadTask=filePath.putFile(ImageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                UserProfileImageRef.child(currentUserID + ".jpg").getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                UpdateSettings(task.getResult().toString());
                            }
                        });
            }
        });
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void InitializeFields() {

        UpdateAccountSettings = findViewById(R.id.update_setting_button);
        DeleteAccountBtn = findViewById(R.id.delete_account_button);
        displayUserName = findViewById(R.id.user_name_display);
        displayMailOrPhn = findViewById(R.id.user_identifier_display);

        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                ImageUri = result.getUri();
                userProfileImage.setImageURI(ImageUri);
            }
        }
    }
}
