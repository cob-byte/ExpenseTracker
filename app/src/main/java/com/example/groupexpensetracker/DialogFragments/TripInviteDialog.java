package com.example.groupexpensetracker.DialogFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TripInviteDialog extends AppCompatDialogFragment {

    private String current_user_id, request_user_id, request_user_name;

    private DatabaseReference tripRequestRef, notificationsRef, usersRef, tripsRef;

    public TripInviteDialog(String current_user_id, String request_user_id, String request_user_name) {
        this.current_user_id = current_user_id;
        this.request_user_id = request_user_id;
        this.request_user_name = request_user_name;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        tripRequestRef = FirebaseDatabase.getInstance().getReference().child("TripRequests");
        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        tripsRef = FirebaseDatabase.getInstance().getReference().child("Trips");

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Do you accept trip request from " + request_user_name)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addUserToTripAndDeleteNotification(builder.getContext());
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeRequestAndNotification();
                    }
                });

        return builder.create();
    }

    private void addUserToTripAndDeleteNotification(final Context context) {
        usersRef.child(request_user_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String tripId = dataSnapshot.child("on_trip").getValue().toString();
                            usersRef.child(current_user_id)
                                    .child("on_trip").setValue(tripId)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                tripsRef.child(tripId)
                                                        .child("members")
                                                        .child(current_user_id).setValue("invited")
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    removeRequestAndNotification();
                                                                }
                                                            }
                                                        });
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

    private void removeRequestAndNotification() {
        tripRequestRef.child(current_user_id).child(request_user_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String notification_id = dataSnapshot.child("notification_id").getValue().toString();
                            notificationsRef.child(current_user_id).child(notification_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                tripRequestRef.child(current_user_id).child(request_user_id)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){

                                                                }
                                                            }
                                                        });
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
}
