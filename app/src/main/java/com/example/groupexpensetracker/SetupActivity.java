package com.example.groupexpensetracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity  implements
        View.OnClickListener{

    private TextView mFullName, mPhoneNumber;
    private CircleImageView profileImage;
    private ProgressBar progressBar;

    private Spinner genderSpinner, countrySpinner;
    private ArrayAdapter<String> genderAdapter;
    private ArrayAdapter<String> countryAdapter;
    private int genderPosition = 0;
    private int countryPosition = 0;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private boolean loading;
    private String currentUserID;

    private final static int GALLERY_REQUEST_CODE = 1;
    private StorageReference userProfileImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //Firebase init
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        mFullName = (TextView) findViewById(R.id.setupFullnameTW);
        mPhoneNumber = (TextView) findViewById(R.id.setupPhoneTW);
        profileImage = (CircleImageView) findViewById(R.id.setupProfilePicture);

        progressBar = (ProgressBar) findViewById(R.id.setupProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        findViewById(R.id.setupProfilePicture).setOnClickListener(this);
        findViewById(R.id.setupButton).setOnClickListener(this);
        loading = false;

        //Gender spinner
        genderSpinner = (Spinner) findViewById(R.id.setupGenderSpinner);

        List<String> gender_list = new ArrayList<String>();
        gender_list.add(0,"Choose gender:");
        gender_list.add("Male");
        gender_list.add("Female");

        genderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gender_list);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).equals("Choose gender:")){
                    genderPosition = position;
                }else{
                    genderPosition = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //dohvacanje drzava
        List<String> countries = readCountriesFromJSON();

        //country spinner
        countrySpinner = (Spinner) findViewById(R.id.setupCountrySpinner);

        List<String> country_list = new ArrayList<String>();
        country_list.add(0, "Choose country:");
        country_list.addAll(countries);

        countryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, country_list);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).equals("Choose country:")){
                    countryPosition = position;
                }else{
                    countryPosition = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Setting profile picture after being selected
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profile_image")){
                        String image = dataSnapshot.child("profile_image").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.ic_launcher_background).into(profileImage);
                    }else{
                        Toast.makeText(SetupActivity.this, "Please insert profile image first!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<String> readCountriesFromJSON(){
        ArrayList<String> countries = new ArrayList<>();
        CountryJsonReader jsonReader = null;
        try {
            jsonReader = new CountryJsonReader(getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            countries = jsonReader.getCountryNameList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return countries;
    }

    private void saveAccountSetupInfo() {
        String fullname = mFullName.getText().toString();
        final String phone = mPhoneNumber.getText().toString();
        String gender = genderSpinner.getItemAtPosition(genderPosition).toString();
        String country = countrySpinner.getItemAtPosition(countryPosition).toString();

        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(SetupActivity.this, "Please write username", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(SetupActivity.this, "Please write username", Toast.LENGTH_SHORT).show();
        }
        if(genderPosition <= 0 || countryPosition <= 0){
            Toast.makeText(SetupActivity.this, "Please select gender and country!", Toast.LENGTH_SHORT).show();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            loading = true;

            String deviceToken = FirebaseInstanceId.getInstance().getToken();

            HashMap userMap = new HashMap();
            userMap.put("fullname", fullname);
            userMap.put("phone", phone);
            userMap.put("gender", gender);
            userMap.put("country", country);
            userMap.put("description", "Hello!");
            userMap.put("numberOfTrips", 0);
            userMap.put("on_trip", "false");
            userMap.put("device_token", deviceToken);

            usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account created successfully!", Toast.LENGTH_SHORT).show();
                    }else {
                        String mess = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: " + mess, Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                    loading = false;
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if(!loading){
            int i = v.getId();
            if(i == R.id.setupButton){
                saveAccountSetupInfo();
            }
            if(i == R.id.setupProfilePicture){
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //find image on your device, pass it to image cropper
        if(requestCode==GALLERY_REQUEST_CODE && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        //recieve image from image cropper
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                loading = true;
                progressBar.setVisibility(View.VISIBLE);

                //Upload profile picture and get URL for the image to store it
                Uri resultUri = result.getUri();
                final StorageReference filePath = userProfileImageRef.child(currentUserID+".jpg");
                UploadTask uploadTask = filePath.putFile(resultUri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SetupActivity.this, "Profile Image stored successfully!", Toast.LENGTH_SHORT).show();
                            Uri downloadUri = task.getResult();

                            //save the URL of users profile picture to his profile info!
                            usersRef.child("profile_image").setValue(downloadUri.toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "URL saved to database!", Toast.LENGTH_SHORT).show();
                                            }else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "URL ERROR occured: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Couldn't get image URL ERROR: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Toast.makeText(SetupActivity.this, "Error: Image CROP Error! Try again!", Toast.LENGTH_SHORT).show();
            }
        }
        loading = false;
        progressBar.setVisibility(View.INVISIBLE);
    }
}
