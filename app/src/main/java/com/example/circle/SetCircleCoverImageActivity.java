package com.example.circle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetCircleCoverImageActivity extends AppCompatActivity {

    private String CurrentCircleID, currentCircleName;

    private Toolbar newToolbar;
    private ProgressDialog loadingBar;
    private Button setImageButton;
    private ImageView coverImage;

    private Uri ImageUri;
    private static final int GalleryPick = 1;

    private DatabaseReference circlesImageRef;
    private StorageReference coverImageStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_circle_cover_image);

        CurrentCircleID = getIntent().getExtras().get("shareCircleId").toString();

        newToolbar = findViewById(R.id.setCover_image_toolbar);
        setSupportActionBar(newToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Set Cover Image");

        loadingBar = new ProgressDialog(this);
        setImageButton  =findViewById(R.id.setCoverImageBtn);
        coverImage = findViewById(R.id.CoverImage);

        circlesImageRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(CurrentCircleID).child("CirclesInfo").child("image");
        coverImageStorageRef = FirebaseStorage.getInstance().getReference().child("Cover Images");


        coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeCoverImage();
            }
        });

        setImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateImage();
            }
        });
    }



    private void ChangeCoverImage() {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);

    }

    private void UpdateImage() {
        loadingBar.setTitle("Update Cover Image");
        loadingBar.setMessage("Please Wait, Cover Image is Updating");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        final StorageReference filePath = coverImageStorageRef.child(CurrentCircleID + ".jpg");

        UploadTask uploadTask=null;
        uploadTask=filePath.putFile(ImageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                coverImageStorageRef.child(CurrentCircleID + ".jpg").getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                UpdateCoverImageLink(task.getResult().toString());
                            }
                        });
            }
        });

    }
    private void UpdateCoverImageLink(String ImageLink) {


        circlesImageRef.setValue(ImageLink).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    loadingBar.dismiss();
                    Toast.makeText(SetCircleCoverImageActivity.this, "Cover Image Updated", Toast.LENGTH_SHORT).show();
                    Intent circleHomeActivity = new Intent(SetCircleCoverImageActivity.this, CircleHomeActivity.class);
                    circleHomeActivity.putExtra("shareCircleId", CurrentCircleID);

                    startActivity(circleHomeActivity);
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
                coverImage.setImageURI(ImageUri);
            }
        }
    }
}
