package com.example.groupexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groupexpensetracker.NotificationPackage.APIService;
import com.example.groupexpensetracker.NotificationPackage.Client;
import com.example.groupexpensetracker.NotificationPackage.Data;
import com.example.groupexpensetracker.NotificationPackage.MyResponse;
import com.example.groupexpensetracker.NotificationPackage.NotificationSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username, phonenumber, description, numOfTrips, gender, country;
    private Button sendRequestBtn, declineRequestBtn;

    private DatabaseReference userRef, friendRequestRef, friendsRef, notificationsRef;
    private FirebaseAuth mAuth;
    private String sender_user_id, receiver_user_id, CURRENT_STATE, sender_user_name, notification_unique_id;

    private String saveCurrentDate;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();
        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        getSenderName();

        //servis za notifikacije
        apiService = apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        initializeFields();

        userRef.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String mUsername = dataSnapshot.child("fullname").getValue().toString();
                    String mPhonenumber = dataSnapshot.child("phone").getValue().toString();
                    String mDescription = dataSnapshot.child("description").getValue().toString();
                    String mNumOfTrips = dataSnapshot.child("numberOfTrips").getValue().toString();
                    String mGender = dataSnapshot.child("gender").getValue().toString();
                    String mCountry = dataSnapshot.child("country").getValue().toString();

                    username.setText(mUsername);
                    phonenumber.setText(mPhonenumber);
                    description.setText(mDescription);
                    numOfTrips.setText(mNumOfTrips);
                    gender.setText(mGender);
                    country.setText(mCountry);

                    if(dataSnapshot.hasChild("profile_image")){
                        String image = dataSnapshot.child("profile_image").getValue().toString();
                        Picasso.with(PersonProfileActivity.this).load(image).placeholder(R.drawable.ic_launcher_background).into(profileImage);
                    }

                    maintenanceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineRequestBtn.setVisibility(View.GONE);
        declineRequestBtn.setEnabled(false);

        if(!sender_user_id.equals(receiver_user_id)){
            sendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestBtn.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        sendFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        cancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        acceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){
                        removeFriend();
                    }

                }
            });
        }else{
            declineRequestBtn.setVisibility(View.GONE);
            sendRequestBtn.setVisibility(View.GONE);
        }

    }

    private void getSenderName(){
        userRef.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    sender_user_name = dataSnapshot.child("fullname").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeFriend() {
        friendsRef.child(sender_user_id).child(receiver_user_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiver_user_id).child(sender_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendRequestBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendRequestBtn.setText("Send Friend Request");

                                                declineRequestBtn.setVisibility(View.GONE);
                                                declineRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        friendsRef.child(sender_user_id).child(receiver_user_id)
                .child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendsRef.child(receiver_user_id).child(sender_user_id)
                            .child("date").setValue(saveCurrentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        friendRequestRef.child(sender_user_id).child(receiver_user_id)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.exists()){
                                                            final String notification_id = dataSnapshot.child("notification_id").getValue().toString();

                                                            notificationsRef.child(receiver_user_id).child(notification_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                notificationsRef.child(sender_user_id).child(notification_id)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                friendRequestRef.child(sender_user_id).child(receiver_user_id)
                                                                                                        .removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if(task.isSuccessful()){
                                                                                                                    friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                                                                                                            .removeValue()
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                    if(task.isSuccessful()){
                                                                                                                                        sendRequestBtn.setEnabled(true);
                                                                                                                                        CURRENT_STATE = "friends";
                                                                                                                                        sendRequestBtn.setText("Remove friend");

                                                                                                                                        declineRequestBtn.setVisibility(View.GONE);
                                                                                                                                        declineRequestBtn.setEnabled(false);
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                                }
                                                                                                            }
                                                                                                        });
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
                            });
                }
            }
        });

    }

    private void cancelFriendRequest() {
        friendRequestRef.child(sender_user_id).child(receiver_user_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String notification_id = dataSnapshot.child("notification_id").getValue().toString();

                            notificationsRef.child(receiver_user_id).child(notification_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                notificationsRef.child(sender_user_id).child(notification_id)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                friendRequestRef.child(sender_user_id).child(receiver_user_id)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful()){
                                                                                                        sendRequestBtn.setEnabled(true);
                                                                                                        CURRENT_STATE = "not_friends";
                                                                                                        sendRequestBtn.setText("Send Friend Request");

                                                                                                        declineRequestBtn.setVisibility(View.GONE);
                                                                                                        declineRequestBtn.setEnabled(false);
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
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

    private void sendFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd.MM. HH:mm");
        String date = currentDate.format(calForDate.getTime());

        final String[] parts = date.split(" ");

        HashMap<String, String> notificationMap = new HashMap<>();
        notificationMap.put("date", parts[0]);
        notificationMap.put("time", parts[1]);
        notificationMap.put("username", sender_user_name);
        notificationMap.put("userId", sender_user_id);
        notificationMap.put("type", "traveler_request");

        notification_unique_id = notificationsRef.child(receiver_user_id).push().getKey();

        notificationsRef.child(receiver_user_id)
                .child(notification_unique_id)
                .setValue(notificationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            HashMap<String, String> sender_receiverMap = new HashMap<>();
                            sender_receiverMap.put("request_type", "sent");
                            sender_receiverMap.put("notification_id", notification_unique_id);

                            friendRequestRef.child(sender_user_id).child(receiver_user_id)
                                    .setValue(sender_receiverMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        HashMap<String, String> receiver_senderMap = new HashMap<>();
                                        receiver_senderMap.put("request_type", "received");
                                        receiver_senderMap.put("notification_id", notification_unique_id);

                                        friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                                .setValue(receiver_senderMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    sendRequestBtn.setEnabled(true);
                                                    CURRENT_STATE = "request_sent";
                                                    sendRequestBtn.setText("Cancel Friend Request");

                                                    declineRequestBtn.setVisibility(View.GONE);
                                                    declineRequestBtn.setEnabled(false);
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });

        userRef.child(receiver_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String token = dataSnapshot.child("device_token").getValue().toString();
                    sendNotifications(token, "New Traveler request",   "Someone wants to connect with you, check now!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(getApplicationContext(), "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    private void maintenanceOfButtons() {
        friendRequestRef.child(sender_user_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Stanje kad je poslan friend request
                        if(dataSnapshot.hasChild(receiver_user_id)){
                            String request_type = dataSnapshot.child(receiver_user_id)
                                    .child("request_type").getValue().toString();

                            if(request_type.equals("sent")){
                                CURRENT_STATE = "request_sent";
                                sendRequestBtn.setText("Cancel Friend Request");

                                declineRequestBtn.setVisibility(View.GONE);
                                declineRequestBtn.setEnabled(false);
                            }
                            else if(request_type.equals("received")){
                                CURRENT_STATE = "request_received";
                                sendRequestBtn.setText("Accept friend request");

                                declineRequestBtn.setVisibility(View.VISIBLE);
                                declineRequestBtn.setEnabled(true);

                                declineRequestBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelFriendRequest();
                                    }
                                });
                            }
                        }else{
                            //stanje kad nije poslan, provjera jeste li prijatelji
                            friendsRef.child(sender_user_id)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiver_user_id)){
                                                CURRENT_STATE = "friends";
                                                sendRequestBtn.setText("Remove friend");
                                            }else{
                                                CURRENT_STATE = "not_friends";
                                                sendRequestBtn.setText("Send Friend Request");

                                                declineRequestBtn.setVisibility(View.GONE);
                                                declineRequestBtn.setEnabled(false);
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

    public void initializeFields(){
        profileImage = (CircleImageView) findViewById(R.id.personProfilePicture);
        username = (TextView) findViewById(R.id.personProfileUsernameTW);
        phonenumber = (TextView) findViewById(R.id.personProfilePhoneNumberTW);
        description = (TextView) findViewById(R.id.personProfileDescriptionTW);
        numOfTrips = (TextView) findViewById(R.id.personProfileNumOfTripsTW);
        gender = (TextView) findViewById(R.id.personProfileGenderTW);
        country = (TextView) findViewById(R.id.personProfileCountryTW);

        sendRequestBtn = (Button) findViewById(R.id.personProfileSendRequestBTN);
        declineRequestBtn = (Button) findViewById(R.id.personProfileDeclineRequestBTN);

        CURRENT_STATE = "not_friends";
    }
}
