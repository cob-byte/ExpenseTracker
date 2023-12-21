package com.example.groupexpensetracker.DialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupexpensetracker.Entities.FindTraveler;
import com.example.groupexpensetracker.NotificationPackage.APIService;
import com.example.groupexpensetracker.NotificationPackage.Client;
import com.example.groupexpensetracker.NotificationPackage.Data;
import com.example.groupexpensetracker.NotificationPackage.MyResponse;
import com.example.groupexpensetracker.NotificationPackage.NotificationSender;
import com.example.groupexpensetracker.R;
import com.example.groupexpensetracker.RecyclerAdapters.TravelersAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddParticipantsDialog extends AppCompatDialogFragment {

    private String current_trip_id, current_user_id, current_user_name;

    private View view;

    private DatabaseReference friendsRef, usersRef, tripRequestRef, notificationRef;
    private RecyclerView mRecyclerView;
    private TravelersAdapter mAdapter;

    private ArrayList<String> friendIds;
    private ArrayList<FindTraveler> travelerList;

    private ArrayList<String> selectedTravelers;

    private APIService apiService;


    public AddParticipantsDialog(String current_user_id){
        this.current_user_id = current_user_id;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        tripRequestRef = FirebaseDatabase.getInstance().getReference().child("TripRequests");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        getCurrentTripId();
        getCurrentUserName();

        //servis za notifikacije
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        selectedTravelers = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.add_participants_form_layout, null);

        builder.setView(view)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Add participant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendInviteToSelectedParticipants();
                    }
                });

        fillTravelerListWithAddableTravelers();
        initViews(view);
        return builder.create();
    }


    private void getCurrentUserName(){
        usersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    current_user_name = dataSnapshot.child("fullname").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentTripId(){
        usersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    current_trip_id = dataSnapshot.child("on_trip").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initViews(final View view) {
            mRecyclerView = (RecyclerView) view.findViewById(R.id.addParticipantsFormRecyclerView);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            mAdapter = new TravelersAdapter(travelerList);
            mRecyclerView.setAdapter(mAdapter);


            mAdapter.setOnItemClickListener(new TravelersAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    check_uncheckTraveler(position);
                }
            });
    }

    private void check_uncheckTraveler(int pos) {
        if(selectedTravelers != null){
            String name = travelerList.get(pos).getFullname();
            if(selectedTravelers.contains(friendIds.get(pos))){
                selectedTravelers.remove(friendIds.get(pos));
                travelerList.get(pos).setFullname(name.replace(" Selected!", ""));
            }else{
                selectedTravelers.add(friendIds.get(pos));
                travelerList.get(pos).setFullname(name + " Selected!");
            }
            mAdapter.notifyItemChanged(pos);
        }
    }

    private void fillTravelerListWithAddableTravelers() {
        travelerList = new ArrayList<>();

        friendsRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    friendIds = new ArrayList<>();
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        String friendId = snapshot.getKey();
                        friendIds.add(friendId);
                    }

                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(String friendId : friendIds){
                                    if(dataSnapshot.hasChild(friendId)){
                                        String on_trip = dataSnapshot.child(friendId).child("on_trip").getValue().toString();
                                        if(on_trip.equals("false")){
                                            FindTraveler addableFriend = new FindTraveler
                                                    (
                                                            dataSnapshot.child(friendId).child("profile_image").getValue().toString(),
                                                            dataSnapshot.child(friendId).child("fullname").getValue().toString(),
                                                            dataSnapshot.child(friendId).child("country").getValue().toString()
                                                    );
                                            travelerList.add(addableFriend);
                                        }
                                    }
                                }
                                mAdapter.notifyDataSetChanged();
                                if(travelerList.isEmpty()){
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendInviteToSelectedParticipants() {
        if(selectedTravelers != null){
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd.MM. HH:mm");
            String date = currentDate.format(calForDate.getTime());

            final String[] parts = date.split(" ");

            for(final String travelerId : selectedTravelers){
                HashMap<String, String> notificationMap = new HashMap<>();
                notificationMap.put("date", parts[0]);
                notificationMap.put("time", parts[1]);
                notificationMap.put("username", current_user_name);
                notificationMap.put("tripId", current_trip_id);
                notificationMap.put("userId", current_user_id);
                notificationMap.put("type", "trip_request");

                final String unique_notification_id = notificationRef.child(travelerId).push().getKey();
                //write notification in database
                notificationRef.child(travelerId)
                        .child(unique_notification_id)
                        .setValue(notificationMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    tripRequestRef.child(travelerId).child(current_user_id)
                                            .child("notification_id").setValue(unique_notification_id)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        //Bravo
                                                     }
                                                }
                                            });
                                }
                            }
                        });
                //send notification to device
                usersRef.child(travelerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String token = dataSnapshot.child("device_token").getValue().toString();
                            sendNotifications(token, "New Trip invite",   "You have a new trip invite from...");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(view.getContext(), "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }
}
