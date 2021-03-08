package com.example.circle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreatePostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private String circleId,circleName,currentDate,currentTime;
    private String postText,selfId,selfName;


    private static final int GalleryPick = 1;
    private Uri ImageUri;


    private FirebaseAuth mAuth;
    private StorageReference PostImageRef;
    private DatabaseReference CirclePostRef,CurrentPostRef;

    ImageView postImage;
    EditText caption;
    Button postBtn;

    public CreatePostActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        circleId = getIntent().getExtras().get("shareCircleId").toString();
        circleName = getIntent().getExtras().get("shareCircleName").toString();


        postImage=findViewById(R.id.create_post_image);
        caption=findViewById(R.id.create_post_caption);
        postBtn=findViewById(R.id.create_post_button);
        loadingBar = new ProgressDialog(this);


        mToolbar = findViewById(R.id.create_post_toolbar);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New Announcement");

        PostImageRef = FirebaseStorage.getInstance().getReference().child("Post Images").child(circleId);
        CirclePostRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(circleId).child("Posts");


        mAuth= FirebaseAuth.getInstance();
        selfId = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("Users").child(selfId).child("name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            selfName=dataSnapshot.getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postText = caption.getText().toString();
                if (TextUtils.isEmpty(postText)) {
                    Toast.makeText(CreatePostActivity.this, "Write Something..", Toast.LENGTH_SHORT).show();
                }if (ImageUri==null) {
                    Toast.makeText(CreatePostActivity.this, "Select Image..", Toast.LENGTH_SHORT).show();
                }else{
                    UploadImage();
                }
            }
        });

    }

    private void UploadImage() {
        final String postKey = CirclePostRef.push().getKey();
        final StorageReference filePath = PostImageRef.child(postKey + ".jpg");

        loadingBar.setTitle("New Announcement");
        loadingBar.setMessage("Please Wait, New Announcement is being Created..");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        UploadTask uploadTask=null;
        uploadTask=filePath.putFile(ImageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                PostImageRef.child(postKey + ".jpg").getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                UpdatePostToDB(postKey,task.getResult().toString());
                            }
                        });
            }
        });

    }

    private void UpdatePostToDB(String currentPostKey,String imgDownloadUrl) {


        CurrentPostRef=CirclePostRef.child(currentPostKey);
        Calendar calForDate= Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd:MM:yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime= Calendar.getInstance();
        SimpleDateFormat  currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());

        PostInfo postInfo=new PostInfo(currentPostKey,selfName,circleName,postText,imgDownloadUrl,currentTime,currentDate);


        CurrentPostRef.setValue(postInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    loadingBar.dismiss();
                    caption.setText("");
                    Toast.makeText(CreatePostActivity.this, "Announcement Created", Toast.LENGTH_SHORT).show();
                    Intent circleHome = new Intent(CreatePostActivity.this, CircleHomeActivity.class);
                    circleHome.putExtra("shareCircleId", circleId);
                    startActivity(circleHome);
                    finish();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(5, 3)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                ImageUri = result.getUri();
                postImage.setImageURI(ImageUri);

            }
        }
    }
}
