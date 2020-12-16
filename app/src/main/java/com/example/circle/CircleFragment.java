package com.example.circle;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CircleFragment extends Fragment {


    private View circleFragmentView;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_circles = new ArrayList<String>();

    private DatabaseReference circlesRef,allCirclesRef;

    private String currentUserId;
    private FirebaseAuth myAuth;

    public CircleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        circleFragmentView = inflater.inflate(R.layout.fragment_circle, container, false);
        myAuth =FirebaseAuth.getInstance();
        currentUserId = myAuth.getCurrentUser().getUid();
        circlesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("My Circles");
        allCirclesRef = FirebaseDatabase.getInstance().getReference().child("All Circle");

        list_view = circleFragmentView.findViewById(R.id.circle_list_view);

        arrayAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,list_of_circles);
        list_view.setAdapter(arrayAdapter);



        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                String currentCircleName = adapterView.getItemAtPosition(position).toString();
                circlesRef.child(currentCircleName).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String currentCircleId = dataSnapshot.getValue().toString();
                        Intent circleHomeActivity = new Intent(getContext(), CircleHomeActivity.class);
                        circleHomeActivity.putExtra("shareCircleId", currentCircleId);

                        startActivity(circleHomeActivity);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        return  circleFragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();
        circlesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while( iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey()) ;
                }
                list_of_circles.clear();
                list_of_circles.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



}
