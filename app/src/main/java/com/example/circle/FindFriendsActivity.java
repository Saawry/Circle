package com.example.circle;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef;

    private String searchSuggestion;
    private EditText searchTextBox;
    private Button searchBtn;

    private String selfID;
    private FirebaseAuth myAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        myAuth = FirebaseAuth.getInstance();
        selfID = myAuth.getCurrentUser().getUid();


        searchTextBox=findViewById(R.id.et_search_box);
        searchBtn=findViewById(R.id.search_button);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find People");


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFriend();
                searchTextBox.setText("");
                searchTextBox.setHint("Enter Friend's Name :");
            }
        });

    }


    void SearchFriend() {
        searchSuggestion = searchTextBox.getText().toString();

        if (TextUtils.isEmpty(searchSuggestion)){
            Toast.makeText(this, "Enter Friend's Name to Search..", Toast.LENGTH_SHORT).show();
        }else{

            Query findUserQuery = UsersRef.orderByChild("name").startAt(searchSuggestion).endAt(searchSuggestion+ "\uf8ff");

            FirebaseRecyclerOptions<Contacts> options =
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(findUserQuery, Contacts.class)
                            .build();

            FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model)
                        {

                            String visit_user_id = getRef(position).getKey();

//                            if(visit_user_id.equals(selfID))
//                            { /////////no name status for self
//                                holder.userName.setText("");
//                                holder.userStatus.setText("");
//                            }else{
//
//                            }

                            holder.userName.setText(model.getName());
                            holder.userStatus.setText(model.getStatus());

                            Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                            holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String visit_user_id = getRef(position).getKey();
                                    if(!visit_user_id.equals(selfID)){

                                        Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                        profileIntent.putExtra("visit_user_id", visit_user_id);
                                        startActivity(profileIntent);

                                    }else{
                                        Toast.makeText(FindFriendsActivity.this, "Your Own Account", Toast.LENGTH_SHORT).show();
                                    }


                                }
                            });
                        }

                        @NonNull
                        @Override
                        public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                        {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                            FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                            return viewHolder;
                        }
                    };
            FindFriendsRecyclerList.setAdapter(adapter);
            adapter.startListening();
        }
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus, tv1;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView){
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.req_type);
            tv1 = itemView.findViewById(R.id.track_req_date);
            profileImage = itemView.findViewById(R.id.users_profile_image);

            tv1.setVisibility(View.INVISIBLE);
        }
    }
}
