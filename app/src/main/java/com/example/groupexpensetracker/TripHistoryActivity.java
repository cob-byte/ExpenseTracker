package com.example.groupexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.groupexpensetracker.Entities.HistoryTrip;
import com.example.groupexpensetracker.RecyclerAdapters.TripsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TripHistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private TripsAdapter mAdapter;

    private ArrayList<HistoryTrip> tripsList;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, tripsRef;
    private String current_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        tripsRef = FirebaseDatabase.getInstance().getReference().child("Trips");

        fillTripsList();
        initRecyclerView();
    }

    private void fillTripsList() {
        Log.d("U FILL TRIP LIST", "počinjem!");
         usersRef.child(current_user_id).child("trips")
                 .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         tripsList.clear();
                         Log.d("U FILL TRIP LIST", "sad ću provjerit dal postoji iti jedan trip!");
                         if(dataSnapshot.exists()){
                             for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                 if(snapshot.exists()){
                                     final String tripId = snapshot.getKey();
                                     Log.d("U FILL TRIP LIST", "Postoji trip i ovo mu je ID: " + tripId);
                                     tripsRef.child(tripId)
                                             .addListenerForSingleValueEvent(new ValueEventListener() {
                                                 @Override
                                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                     Log.d("U FILL TRIP LIST", "Trazim trip pod gornjim Id-om!");
                                                     if(dataSnapshot.exists()){
                                                         Log.d("U FILL TRIP LIST", "Našo ga!");
                                                         HistoryTrip historyTrip = new HistoryTrip
                                                                 (
                                                                     dataSnapshot.child("start_date").getValue().toString(),
                                                                     dataSnapshot.child("end_date").getValue().toString(),
                                                                     dataSnapshot.child("trip_name").getValue().toString(),
                                                                     dataSnapshot.child("trip_country").getValue().toString(),
                                                                     tripId
                                                                 );
                                                         tripsList.add(historyTrip);
                                                     }
                                                     if(tripsList.isEmpty()){
                                                         Log.d("U FILL TRIP LIST", "Lista je PRAZNAAA!");
                                                     }
                                                     mAdapter.notifyDataSetChanged();
                                                 }

                                                 @Override
                                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                                 }
                                             });
                                 }
                             }

                         }

                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });
    }

    private void initRecyclerView() {
        tripsList = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.tripHistoryRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(TripHistoryActivity.this));
        mAdapter = new TripsAdapter(tripsList);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new TripsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String tripId = tripsList.get(position).getTripId();
                sendUserToTripActivity(tripId);
            }
        });
    }

    private void sendUserToTripActivity(String tripId) {
        Intent intent = new Intent(TripHistoryActivity.this, CurrentTripActivity.class);
        intent.putExtra("current_trip_id", tripId);
        intent.putExtra("state", "finished");
        startActivity(intent);
    }
}
