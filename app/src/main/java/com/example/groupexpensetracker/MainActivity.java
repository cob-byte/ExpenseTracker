package com.example.groupexpensetracker;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupexpensetracker.DialogFragments.TripCreatorDialog;
import com.example.groupexpensetracker.DialogFragments.TripInviteDialog;
import com.example.groupexpensetracker.Entities.Notification;
import com.example.groupexpensetracker.RecyclerAdapters.NotificationsAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, notificationsRef, tripRequestRef;
    private String current_user_id;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;

    private CircleImageView drawerProfileImage;
    private TextView drawerUserName;

    private Button mainButton;

    private RecyclerView mRecyclerView;
    private NotificationsAdapter mAdapter;

    private ArrayList<Notification> notificationsList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Firebase
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        tripRequestRef = FirebaseDatabase.getInstance().getReference().child("TripRequests");

        //Toolbar
        toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        //Drawer icon, toggle function
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout,R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //drawer views
        //navigation view, click listener
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        View navHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);

        drawerProfileImage = (CircleImageView) navHeader.findViewById(R.id.drawerProfilePicture);
        drawerUserName = (TextView) navHeader.findViewById(R.id.drawerUsername);

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("fullname")){
                        String full_name = dataSnapshot.child("fullname").getValue().toString();
                        drawerUserName.setText(full_name);
                    }
                    if(dataSnapshot.hasChild("profile_image")){
                        String image = dataSnapshot.child("profile_image").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.ic_launcher_background).into(drawerProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // On click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                userMenuItemSelector(menuItem);
                return false;
            }
        });

        //Button
        mainButton = (Button) findViewById(R.id.mainActivityButton);
        maintenanceOfButton();

        recyclerViewInit();
        fillNotificationList();
        updateToken();
    }

    private void fillNotificationList() {
        notificationsRef.child(current_user_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificationsList.clear();
                        if(dataSnapshot.exists()){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                if(snapshot.exists()){
                                    Notification notification = new Notification
                                            (
                                                    snapshot.child("username").getValue().toString(),
                                                    snapshot.child("date").getValue().toString(),
                                                    snapshot.child("time").getValue().toString(),
                                                    snapshot.child("type").getValue().toString()
                                            );
                                    if(snapshot.child("userId").exists()){
                                        notification.setUserId(snapshot.child("userId").getValue().toString());
                                    }
                                    if(snapshot.child("tripId").exists()){
                                        notification.setTripId(snapshot.child("tripId").getValue().toString());
                                    }
                                    notificationsList.add(notification);
                                }
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public void onResume()
    {
        super.onResume();
        maintenanceOfButton();
    }

    private void maintenanceOfButton() {

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("on_trip")){
                    final String current_trip = dataSnapshot.child("on_trip").getValue().toString();
                    if(current_trip.equals("false")){
                        mainButton.setText("Create a trip");
                        mainButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openCreateTripDialog();
                            }
                        });
                    }else{
                        mainButton.setText("Update current trip");
                        mainButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendUserToCurrentTripActivity(current_trip);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openCreateTripDialog() {
        TripCreatorDialog tripCreatorDialog = new TripCreatorDialog();
        tripCreatorDialog.show(getSupportFragmentManager(), "trip creator dialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendUserToLoginActivity();
        }else{
            checkUserExistance();
        }
    }

    private void checkUserExistance() {
        final String current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void recyclerViewInit() {
        notificationsList = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.mainActivityRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mAdapter = new NotificationsAdapter(notificationsList);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new NotificationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                resolveNotificationAt(position);
            }
        });
    }

    private void resolveNotificationAt(final int position) {
        String type = notificationsList.get(position).getType();
        String request_user_id = notificationsList.get(position).getUserId();


        if(type.equals("traveler_request")){
            Intent intent = new Intent(MainActivity.this, PersonProfileActivity.class);
            intent.putExtra("visit_user_id", request_user_id);
            startActivity(intent);
        }
        if(type.equals("trip_request")){
            String request_user_name = notificationsList.get(position).getUserName();
            openTripInviteDialog(current_user_id, request_user_id, request_user_name);
        }
    }

    private void openTripInviteDialog(String current_user_id, String request_user_id, String request_user_name) {
        TripInviteDialog tripInviteDialog = new TripInviteDialog(current_user_id, request_user_id, request_user_name);
        tripInviteDialog.show(getSupportFragmentManager(), "trip invite dialog");
    }

    //enables drawer icon
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userMenuItemSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menuProfile:
                sendUserToProfileActivity();
                break;
            case R.id.menuMyTraveler:
                sendUserToTravelersActivity();
                break;
            case R.id.menuSearchTravelers:
                sendUserToFindTravelersActivity();
                break;
            case R.id.menuTravelHistory:
                sendUserToTripHistoryActivity();
                break;
            case R.id.menuSettings:
                sendUserToSettingsActivity();
                break;
            case R.id.menuLogout:
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
        }
    }

    private void updateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id).child("device_token").setValue(refreshToken);
    }

    private void sendUserToTripHistoryActivity(){
        Intent intent = new Intent(MainActivity.this, TripHistoryActivity.class);
        startActivity(intent);
    }

    private void sendUserToCurrentTripActivity(String current_trip_id) {
        Intent intent = new Intent(MainActivity.this, CurrentTripActivity.class);
        intent.putExtra("current_trip_id", current_trip_id);
        intent.putExtra("state", "live");
        startActivity(intent);
    }

    private void sendUserToSetupActivity() {
        Intent intent = new Intent(MainActivity.this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToTravelersActivity() {
        Intent intent = new Intent(MainActivity.this, TravelersActivity.class);
        startActivity(intent);
    }

    private void sendUserToFindTravelersActivity() {
        Intent intent = new Intent(MainActivity.this, FindTravelersActivity.class);
        startActivity(intent);
    }

    private void sendUserToProfileActivity() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void sendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

}
