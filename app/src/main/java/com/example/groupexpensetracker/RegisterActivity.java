package com.example.groupexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity implements
        View.OnClickListener{

    private static final String TAG = "EmailPassword";

    private TextView mEmailField, mPasswordField, mPasswordField2, registerToLoginLink;
    private ProgressBar progressBar;

    private boolean loading;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mEmailField = (TextView) findViewById(R.id.registerUserEmailTW);
        mPasswordField = (TextView) findViewById(R.id.registerUserPassTW);
        mPasswordField2 = (TextView) findViewById(R.id.registerUserPass2TW);

        findViewById(R.id.registerButton).setOnClickListener(this);
        findViewById(R.id.registerToLoginLink).setOnClickListener(this);


        progressBar = (ProgressBar) findViewById(R.id.registerProgressBar);
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

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loading = true;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToSetupActivity();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        loading = false;
                    }
                });
    }

    private void sendUserToSetupActivity() {
        Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
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
        String password2 = mPasswordField2.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField2.setError("Required.");
            valid = false;
        } else {
            mPasswordField2.setError(null);
        }

        if(!password.equals(password2)){
            valid = false;
            mPasswordField2.setError("Passwords do NOT match!.");
        }else{
            mPasswordField2.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        if(!loading){
            int i = v.getId();
            if(i == R.id.registerButton){
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
            if(i == R.id.registerToLoginLink){
                sendUserToLoginActivity();
            }
        }
    }

    private void sendUserToLoginActivity() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

}
