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

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements
        View.OnClickListener{

    private CircleImageView profilePicture;
    private TextView mFullName, mPhoneNumber, mDescription;
    private ProgressBar progressBar;

    private Spinner genderSpinner, countrySpinner;
    private ArrayAdapter<String> genderAdapter;
    private ArrayAdapter<String> countryAdapter;
    private int genderPosition = 0;
    private int countryPosition = 0;

    private boolean loading;

    private final static int GALLERY_REQUEST_CODE = 1;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference usersRef;
    private StorageReference userProfileImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        //Firebase init
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        profilePicture = (CircleImageView) findViewById(R.id.settingsProfilePicture);
        mFullName = (TextView) findViewById(R.id.settingsFullname);
        mPhoneNumber = (TextView) findViewById(R.id.settingsPhone);
        mDescription = (TextView) findViewById(R.id.settingsDescription);
        profilePicture = (CircleImageView) findViewById(R.id.settingsProfilePicture);

        findViewById(R.id.settingsProfilePicture).setOnClickListener(this);
        findViewById(R.id.settingsButton).setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.settingsProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        loading = false;


        //Gender Spinner
        genderSpinner = (Spinner) findViewById(R.id.settingsGenderSpinner);

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

        //instanciranje mog jsonreadera
        CountryJsonReader jsonReader = null;
        try {
            jsonReader = new CountryJsonReader(getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //dohvacanje drzava
        List<String> countries = new ArrayList<String>();
        try {
            countries = jsonReader.getCountryNameList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //country spinner
        countrySpinner = (Spinner) findViewById(R.id.settingsCountrySpinner);

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

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String name = dataSnapshot.child("fullname").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String description = dataSnapshot.child("description").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String country = dataSnapshot.child("country").getValue().toString();

                    genderPosition = getIndex(genderSpinner, gender);
                    countryPosition = getIndex(countrySpinner, country);

                    mFullName.setText(name);
                    mPhoneNumber.setText(phone);
                    mDescription.setText(description);
                    genderSpinner.setSelection(genderPosition);
                    countrySpinner.setSelection(countryPosition);

                    if(dataSnapshot.hasChild("profile_image")){
                        String image = dataSnapshot.child("profile_image").getValue().toString();
                        Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.ic_launcher_background).into(profilePicture);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }


    @Override
    public void onClick(View v) {
        if(!loading){
            int i = v.getId();
            if(i == R.id.settingsButton){
                saveAccountSetupInfo();
            }
            if(i == R.id.settingsProfilePicture){
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        }
    }

    private void saveAccountSetupInfo() {
        String fullname = mFullName.getText().toString();
        String phone = mPhoneNumber.getText().toString();
        String description = mDescription.getText().toString();
        String gender = genderSpinner.getItemAtPosition(genderPosition).toString();
        String country = countrySpinner.getItemAtPosition(countryPosition).toString();

        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(SettingsActivity.this, "Please write username", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(SettingsActivity.this, "Please write username", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(description)){
            Toast.makeText(SettingsActivity.this, "Please write description", Toast.LENGTH_SHORT).show();
        }
        else if(genderPosition <= 0 || countryPosition <= 0){
            Toast.makeText(SettingsActivity.this, "Please select gender and country!", Toast.LENGTH_SHORT).show();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            loading = true;

            HashMap userMap = new HashMap();
            userMap.put("fullname", fullname);
            userMap.put("phone", phone);
            userMap.put("gender", gender);
            userMap.put("country", country);
            userMap.put("description", description);


            usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Your account updated successfully!", Toast.LENGTH_SHORT).show();
                    }else {
                        String mess = task.getException().getMessage();
                        Toast.makeText(SettingsActivity.this, "Error: " + mess, Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                    loading = false;
                }
            });
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
                            Toast.makeText(SettingsActivity.this, "Profile Image stored successfully!", Toast.LENGTH_SHORT).show();
                            Uri downloadUri = task.getResult();

                            //save the URL of users profile picture to his profile info!
                            usersRef.child("profile_image").setValue(downloadUri.toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SettingsActivity.this, "URL saved to database!", Toast.LENGTH_SHORT).show();
                                            }else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "URL ERROR occured: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(SettingsActivity.this, "Couldn't get image URL ERROR: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Toast.makeText(SettingsActivity.this, "Error: Image CROP Error! Try again!", Toast.LENGTH_SHORT).show();
            }
        }
        loading = false;
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
