package com.example.circle;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class CircleHomeActivity extends AppCompatActivity {

    private boolean admin;
    String CurrentCircleID, dbCircleName,selfID;

    long currentMemberAmount;
    private ImageView headerCircleCoverImage;
    private TextView headerCircleTitle;
    private TextView headerCircleMemberCount;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar circleToolbar;


    private DatabaseReference usersRef, circlesRef,circleMemberRef,CirclePostRef,myCirclesRef,allCircleRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_home);


        mAuth =FirebaseAuth.getInstance();
        selfID = mAuth.getCurrentUser().getUid();

        CurrentCircleID = getIntent().getExtras().get("shareCircleId").toString();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        myCirclesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(selfID).child("My Circles");
        allCircleRef = FirebaseDatabase.getInstance().getReference().child("All Circle");
        circlesRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(CurrentCircleID).child("CirclesInfo");
        circleMemberRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(CurrentCircleID).child("Members");
        CirclePostRef = FirebaseDatabase.getInstance().getReference().child("All Circle").child(CurrentCircleID).child("Posts");

        //Toast.makeText(CircleHomeActivity.this, CurrentCircleID, Toast.LENGTH_SHORT).show();
        circleToolbar = findViewById(R.id.circle_home_toolbar);
        setSupportActionBar(circleToolbar);
        getSupportActionBar().setTitle("Circle Home");


        postList = findViewById(R.id.current_circle_posts_list);
        ///// attempt to update real time
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        /////

        //postList.setLayoutManager(new LinearLayoutManager(this));

        drawerLayout = findViewById(R.id.circle_drawer_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = findViewById(R.id.circle_navigation_view);
        View  navView = navigationView.inflateHeaderView(R.layout.circle_navigation_header);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, circleToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        headerCircleCoverImage = navView.findViewById(R.id.circle_cover_image);
        headerCircleTitle = navView.findViewById(R.id.header_circle_name);
        headerCircleMemberCount = navView.findViewById(R.id.circle_member_amount);


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

        headerCircleCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setCircleCoverImageActivity = new Intent(CircleHomeActivity.this, SetCircleCoverImageActivity.class);
                setCircleCoverImageActivity.putExtra("shareCircleId", CurrentCircleID);

                startActivity(setCircleCoverImageActivity);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserItemSelector(menuItem);
                return false;
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        GetOrUpdateMemberCount();

        circlesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String dbCircleImage = dataSnapshot.child("image").getValue().toString();
                    dbCircleName = dataSnapshot.child("name").getValue().toString();

                    Picasso.get().load(dbCircleImage).placeholder(R.drawable.circle_avatar).into(headerCircleCoverImage);
                    headerCircleTitle.setText(dbCircleName);
                    if (currentMemberAmount!=1){
                        headerCircleMemberCount.setText(currentMemberAmount+" Members");
                    }else{
                        headerCircleMemberCount.setText(currentMemberAmount+" Member");
                    }

                }else{
                    dbCircleName = dataSnapshot.child("name").getValue().toString();
                    headerCircleTitle.setText(dbCircleName);
                    if (currentMemberAmount!=1){
                        headerCircleMemberCount.setText(currentMemberAmount+" Members");
                    }else{
                        headerCircleMemberCount.setText(currentMemberAmount+" Member");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        ///////section for post view
        FirebaseRecyclerOptions<PostInfo> options =
                new FirebaseRecyclerOptions.Builder<PostInfo>()
                        .setQuery(CirclePostRef, PostInfo.class)
                        .build();

        FirebaseRecyclerAdapter<PostInfo, CircleHomeActivity.CurrentCirclePostViewHolder> adapter =
                new FirebaseRecyclerAdapter<PostInfo, CircleHomeActivity.CurrentCirclePostViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final CircleHomeActivity.CurrentCirclePostViewHolder holder, final int position, @NonNull final PostInfo model)
                    {

                        CirclePostRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    final String post_Key = getRef(position).getKey();

                                    holder.tvCreator.setText(model.getCreator());
                                    holder.tvTime.setText(model.getPostTime());
                                    holder.tvDate.setText(model.getPostDate());
                                    holder.tvCaption.setText(model.getPostText());
                                    Picasso.get().load(model.getPostImage()).placeholder(R.drawable.post_image).into(holder.postImage);

                                    holder.postDeleteImgBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (admin){
                                                DeletePost(post_Key);
                                            }else{
                                                Toast.makeText(CircleHomeActivity.this, "Can't Delete, You are Admin", Toast.LENGTH_SHORT).show();

                                            }
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
                    public CircleHomeActivity.CurrentCirclePostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_view_layout, viewGroup, false);
                        CircleHomeActivity.CurrentCirclePostViewHolder viewHolder = new CircleHomeActivity.CurrentCirclePostViewHolder(view);
                        return viewHolder;
                    }
                };
        postList.setAdapter(adapter);
        adapter.startListening();

    }

    private void GetOrUpdateMemberCount() {
        circleMemberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentMemberAmount = dataSnapshot.getChildrenCount();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        circlesRef.child("memberAmount").setValue(currentMemberAmount)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(CircleHomeActivity.this, "Welcome to the Circle..!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void DeletePost(final String postKey) {

        CharSequence options[] = new CharSequence[]{
                "Delete",
                "No Action"
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(CircleHomeActivity.this);
        builder.setTitle("Delete Post");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (i == 0){
                    CirclePostRef.child(postKey)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                Toast.makeText(CircleHomeActivity.this, "Post Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                if (i == 1){

                }
            }
        });
        builder.show();

    }


    private void UserItemSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.circle_create_post:
                Intent createPostActivity = new Intent(CircleHomeActivity.this, CreatePostActivity.class);
                createPostActivity.putExtra("shareCircleName", dbCircleName);
                createPostActivity.putExtra("shareCircleId", CurrentCircleID);
                startActivity(createPostActivity);
                break;

            case R.id.circle_talk:
                Intent circleChatActivity = new Intent(CircleHomeActivity.this, CircleChatActivity.class);
                circleChatActivity.putExtra("shareCircleName", dbCircleName);
                circleChatActivity.putExtra("shareCircleId", CurrentCircleID);

                startActivity(circleChatActivity);
                break;

            case R.id.circle_member_list:
                Intent membersListActivity = new Intent(CircleHomeActivity.this, CircleMembersActivity.class);
                membersListActivity.putExtra("shareCircleName", dbCircleName);
                membersListActivity.putExtra("shareCircleId", CurrentCircleID);

                startActivity(membersListActivity);
                break;

            case R.id.circle_add_member:
                Intent addMemberActivity = new Intent(CircleHomeActivity.this, AddMemberActivity.class);
                addMemberActivity.putExtra("shareCircleName", dbCircleName);
                addMemberActivity.putExtra("shareCircleId", CurrentCircleID);

                startActivity(addMemberActivity);
                break;

            case R.id.leave_circle:
                if (admin){
                    Toast.makeText(this, "Can't Leave, You are Admin", Toast.LENGTH_SHORT).show();
                }else{
                    LeaveCircle();
                }
                break;

//            case R.id.circle_delete:
//                if (!admin){
//                    Toast.makeText(this, "Can't Delete, You aren't Admin", Toast.LENGTH_SHORT).show();
//                }else{
//                    DeleteCircle();
//                }
//                break;
        }
    }

//    private void DeleteCircle() {
//
//
//        allCircleRef.child(CurrentCircleID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()){
//                    loadingBar.dismiss();
//                    Toast.makeText(CircleHomeActivity.this, "Deleted Circle Successfully..!!", Toast.LENGTH_SHORT).show();
//                    Intent mainIntent = new Intent(CircleHomeActivity.this, MainActivity.class);
//
//                    startActivity(mainIntent);
//                }
//            }
//        });
//    }

    private void LeaveCircle() {
        circleMemberRef.child(selfID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    myCirclesRef.child(dbCircleName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                GetOrUpdateMemberCount();
                                Toast.makeText(CircleHomeActivity.this, "Leaved Circle Successfully..!!", Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(CircleHomeActivity.this, MainActivity.class);

                                startActivity(mainIntent);
                            }
                        }
                    });
                }
            }
        });

    }


    public static class CurrentCirclePostViewHolder extends RecyclerView.ViewHolder{

        TextView tvCreator,tvTime,tvDate,tvCaption;
        ImageView postImage,line,postDeleteImgBtn;

        public CurrentCirclePostViewHolder(@NonNull View itemView){
            super(itemView);

            tvCreator = itemView.findViewById(R.id.postCreator);
            tvTime = itemView.findViewById(R.id.postTime);
            tvDate = itemView.findViewById(R.id.postDate);
            tvCaption = itemView.findViewById(R.id.postCaption);

            postImage = itemView.findViewById(R.id.postImage);
            line = itemView.findViewById(R.id.line);
            postDeleteImgBtn = itemView.findViewById(R.id.post_delete_img_btn);

        }
    }


}
