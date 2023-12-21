package com.example.groupexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.groupexpensetracker.Entities.Traveler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TravelersActivity extends AppCompatActivity {

    private RecyclerView myTravelersList;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private DatabaseReference friendRef, usersRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travelers);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myTravelersList = (RecyclerView) findViewById(R.id.myTravelersRecycleView);
        myTravelersList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myTravelersList.setLayoutManager(linearLayoutManager);

        displayMyTravelers();
    }

    private void displayMyTravelers() {
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Traveler, TravelerViewHolder>(
                Traveler.class,
                R.layout.all_travelers_dispaly_layout,
                TravelerViewHolder.class,
                friendRef.child(current_user_id)
        ) {
            @Override
            protected void populateViewHolder(final TravelerViewHolder holder, final Traveler model, final int i) {
                holder.setDate(model.getDate());

                final String usersIDs = getRef(i).getKey();

                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profile_image").getValue().toString();

                            holder.setFullname(userName);
                            holder.setProfileImage(getApplicationContext(), profileImage);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(TravelersActivity.this, PersonProfileActivity.class);
                        intent.putExtra("visit_user_id", usersIDs);
                        startActivity(intent);
                    }
                });
            }
        };
        myTravelersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class TravelerViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public TravelerViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfileImage(Context ctx, String profileImage) {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.allTravelerProfileImage);
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.ic_launcher_background).into(myImage);
        }

        public void setFullname(String fullname) {
            TextView myName = (TextView) mView.findViewById(R.id.allTravelerProfileFullName);
            myName.setText(fullname);
        }

        public void setDate(String date){
            TextView myDate = (TextView) mView.findViewById(R.id.allTravelerProfileCountry);
            myDate.setText("Friends since: " + date);
        }
    }
}
