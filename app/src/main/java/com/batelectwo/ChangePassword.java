package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ChangePassword extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private TextInputEditText TextInputEditTextCurrentPassword, TextInputEditTextNewPassword, TextInputEditTextConfirmNewPassword;
    private TextView textViewAuthenticated;
    private Button buttonChangePassword, buttonReAuthenticate;
    private ProgressBar progressBar;
    private String userCurrentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        Objects.requireNonNull(getSupportActionBar()).setTitle("Change Password");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextInputEditTextCurrentPassword = findViewById(R.id.textInputEditTextUpdateChangePasswordCurrent);
        TextInputEditTextNewPassword = findViewById(R.id.textInputEditTextChangePasswordNew);
        TextInputEditTextConfirmNewPassword = findViewById(R.id.textInputEditTextChangePasswordNewConfirmPassword);
        textViewAuthenticated = findViewById(R.id.textViewChangePasswordAuthenticated);
        progressBar = findViewById(R.id.progressBarShow);
        buttonReAuthenticate = findViewById(R.id.authenticationChangePasswordButton);
        buttonChangePassword = findViewById(R.id.changePasswordButton);

        // Disable TextInputEditText for New Password, Confirm New Password and Make Change Password Button unclickable until user is authenticated
        TextInputEditTextNewPassword.setEnabled(false);
        TextInputEditTextConfirmNewPassword.setEnabled(false);
        buttonChangePassword.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")) {
            Toast.makeText(ChangePassword.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePassword.this, UserProfile.class);
            startActivity(intent);
            finish();
        }
        else {
            reAuthenticateUser(firebaseUser);
        }
    }

    // ReAuthenticate User before changing password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCurrentPassword = TextInputEditTextCurrentPassword.getText().toString();

                if (TextUtils.isEmpty(userCurrentPassword)) {
                    TextInputEditTextCurrentPassword.setError("Please enter your current password to authenticate");
                    TextInputEditTextCurrentPassword.requestFocus();
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);

                    // ReAuthenticate User now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userCurrentPassword);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                // Disable TextInputEditText for Current Password. Enable TextInputEditText for New Password and Confirm New Password
                                TextInputEditTextCurrentPassword.setEnabled(false);
                                TextInputEditTextNewPassword.setEnabled(true);
                                TextInputEditTextConfirmNewPassword.setEnabled(true);

                                // Enable Change Password Button. Disable Authenticate Button
                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePassword.setEnabled(true);

                                // Set TextView to show User is authenticated or verified
                                textViewAuthenticated.setText("You are authenticated or verified. " + "You can change password now");
                                Toast.makeText(ChangePassword.this, "Password has been verified. " + "Change password now", Toast.LENGTH_SHORT).show();

                                // Update color of Change Password Button
                                buttonChangePassword.setBackgroundTintList(ContextCompat.getColorStateList(ChangePassword.this, R.color.sage_green));
                                textViewAuthenticated.setTextColor(getResources().getColor(R.color.green)); // Set text color
                                textViewAuthenticated.setBackgroundColor(getResources().getColor(R.color.white)); // Set background color
                                buttonChangePassword.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePassword(firebaseUser);
                                    }
                                });
                            }
                            else {
                                try {
                                    throw task.getException();
                                }
                                catch (Exception e) {
                                    TextInputEditTextCurrentPassword.setError("Invalid password");
                                    TextInputEditTextCurrentPassword.requestFocus();

                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePassword(FirebaseUser firebaseUser) {
        String userNewPassword = TextInputEditTextNewPassword.getText().toString();
        String userConfirmNewPassword = TextInputEditTextConfirmNewPassword.getText().toString();

        if (TextUtils.isEmpty(userNewPassword)) {
            TextInputEditTextNewPassword.setError("Please enter your new password");
            TextInputEditTextNewPassword.requestFocus();
        }
        else if (TextUtils.isEmpty(userConfirmNewPassword)) {
            TextInputEditTextConfirmNewPassword.setError("Please confirm your new password");
            TextInputEditTextConfirmNewPassword.requestFocus();
        }
        else if (!userNewPassword.matches(userConfirmNewPassword)) {
            TextInputEditTextConfirmNewPassword.setError("Password did not match");
            TextInputEditTextConfirmNewPassword.requestFocus();
        }
        else if (userCurrentPassword.matches(userNewPassword)) {
            TextInputEditTextNewPassword.setError("New password cannot be same as old password");
            TextInputEditTextNewPassword.requestFocus();
        }
        else {
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(userNewPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangePassword.this, "Password has been changed", Toast.LENGTH_SHORT).show();

                        // Get the user's role from the Firebase Realtime Database or wherever you store it
                        DatabaseReference roleRef = FirebaseDatabase.getInstance().getReference("Registered Users")
                                .child(firebaseUser.getUid())
                                .child("role");

                        roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String role = dataSnapshot.getValue(String.class);

                                    // Check the user's role and navigate accordingly
                                    if ("admin".equals(role)) {
                                        // Navigate to the AdminProfile activity
                                        Intent intent = new Intent(ChangePassword.this, AdminProfile.class);
                                        startActivity(intent);
                                        finish(); // Optional: finish the UpdateEmail activity
                                    } else if ("consumer".equals(role)) {
                                        // Navigate to the UserProfile activity
                                        Intent intent = new Intent(ChangePassword.this, UserProfile.class);
                                        startActivity(intent);
                                        finish(); // Optional: finish the UpdateEmail activity
                                    } else {
                                        // Handle other roles or roles not defined
                                        Toast.makeText(ChangePassword.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle database error
                            }
                        });
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}