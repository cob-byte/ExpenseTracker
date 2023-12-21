package com.example.groupexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username, phonenumber, description, numOfTrips, gender, country;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String sender_user_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //firebase init
        mAuth = FirebaseAuth.getInstance();
        sender_user_ID =mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(sender_user_ID);

        //init views
        profileImage = (CircleImageView) findViewById(R.id.profilePicture);
        username = (TextView) findViewById(R.id.profileUsernameTW);
        phonenumber = (TextView) findViewById(R.id.profilePhoneNumberTW);
        description = (TextView) findViewById(R.id.profileDescriptionTW);
        numOfTrips = (TextView) findViewById(R.id.profileNumOfTripsTW);
        gender = (TextView) findViewById(R.id.profileGenderTW);
        country = (TextView) findViewById(R.id.profileCountryTW);


        userRef.addValueEventListener(new ValueEventListener() {
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
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.ic_launcher_background).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
