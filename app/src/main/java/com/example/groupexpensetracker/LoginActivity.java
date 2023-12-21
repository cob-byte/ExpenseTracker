package com.example.groupexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener{

    private static final String TAG = "EmailPassword";

    private TextView mEmailField, mPasswordField, loginToRegisterLink;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private boolean loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mEmailField = (TextView) findViewById(R.id.loginUserEmailTW);
        mPasswordField = (TextView) findViewById(R.id.loginUserPassTW);

        findViewById(R.id.loginButton).setOnClickListener(this);
        findViewById(R.id.loginToRegisterLink).setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        loading = false;

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendUserToMainActivity();
        }
    }

    private void signIn(String email, String password) {
        //Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loading = true;


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String current_user_id = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            usersRef.child(current_user_id)
                                    .child("device_token").setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendUserToMainActivity();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        loading = false;
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        if(!loading){
            int i = v.getId();
            if(i == R.id.loginButton){
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
            if(i == R.id.loginToRegisterLink){
                sendUserToRegisterActivity();
            }
        }
    }

    private void sendUserToRegisterActivity() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }
}
