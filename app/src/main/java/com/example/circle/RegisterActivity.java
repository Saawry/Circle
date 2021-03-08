package com.example.circle;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button CreateAccountButton,loginButton,phoneLoginBtn;
    private EditText  UserEmail, UserPassword;

    private FirebaseAuth mAuth;

    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();


        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Account");

        InitializeFields();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();

            }
        });


        phoneLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendToPhoneLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

    }

    private void SendToPhoneLoginActivity() {
        Intent phnLoginIntent = new Intent(RegisterActivity.this, PhoneLoginActivity.class);
        startActivity(phnLoginIntent);
        finish();
    }

    private void CreateNewAccount() {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();



        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Enter Email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Enter Password", Toast.LENGTH_SHORT).show();
        }
        else{



            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please wait While Processing");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();



            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String mailOrPhn  = mAuth.getCurrentUser().getEmail();
                                RootRef.child("Users").child(currentUserID).child("mailOrPhn").setValue(mailOrPhn);

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                String message= task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error.!"+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

        }
    }

    private void InitializeFields() {
        loginButton = findViewById(R.id.already_have_an_account_btn);
        phoneLoginBtn = findViewById(R.id.phn_register);
        CreateAccountButton = findViewById(R.id.register_button);
        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        loadingBar = new ProgressDialog(this);

    }
    private void SendUserToLoginActivity(){
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
