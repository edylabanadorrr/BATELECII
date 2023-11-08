package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class UpdateEmail extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String userOldEmail, userNewEmail, userPassword;
    private Button buttonUpdateEmail;
    private TextInputEditText TextInputEditTextNewEmail, TextInputEditTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Update Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBarShow);
        TextInputEditTextPassword = findViewById(R.id.textInputEditTextUpdateEmailVerifyPassword);
        TextInputEditTextNewEmail = findViewById(R.id.textInputEditTextUpdateEmailNew);
        textViewAuthenticated = findViewById(R.id.textViewUpdateEmailAuthenticated);
        buttonUpdateEmail = findViewById(R.id.updateEmailButton);

        buttonUpdateEmail.setEnabled(false); // Make button disabled in the beginning until the user is authenticated
        TextInputEditTextNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // Set old Email ID on Text View
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textViewUpdateEmailOld);
        textViewOldEmail.setText(userOldEmail);

        if (firebaseUser.equals("")) {
            Toast.makeText(UpdateEmail.this, "Something went wrong! User's details not available.", Toast.LENGTH_SHORT).show();
        }
        else {
            reAuthenticate(firebaseUser);
        }
    }
    // ReAuthenticate or Verify user before updating email
    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.authenticationUserButton);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtain password for authentication
                userPassword = TextInputEditTextPassword.getText().toString();

                if (TextUtils.isEmpty(userPassword)) {
                    TextInputEditTextPassword.setError("Password is required");
                    TextInputEditTextPassword.requestFocus();
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPassword);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(UpdateEmail.this, "Password has been verified. " + "You can update your email now.", Toast.LENGTH_SHORT).show();

                                // Set TextView to show that user is authenticated
                                textViewAuthenticated.setText("You are authenticated. You can update your email now.");

                                // Disable TextInputEditText for password and enable TextInputEditText for new Email and Update Email Button
                                TextInputEditTextNewEmail.setEnabled(true);
                                TextInputEditTextPassword.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);

                                // Change color of Update Email Button
                                buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmail.this,R.color.sage_green));
                                textViewAuthenticated.setTextColor(getResources().getColor(R.color.green)); // Set text color
                                textViewAuthenticated.setBackgroundColor(getResources().getColor(R.color.white)); // Set background color

                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = TextInputEditTextNewEmail.getText().toString();
                                        if (TextUtils.isEmpty(userNewEmail)) {
                                            TextInputEditTextNewEmail.setError("Enter new email");
                                            TextInputEditTextNewEmail.requestFocus();
                                        }
                                        else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                            TextInputEditTextNewEmail.setError("Valid email is required");
                                            TextInputEditTextNewEmail.requestFocus();
                                        }
                                        else if (userOldEmail.matches(userNewEmail)) {
                                            TextInputEditTextNewEmail.setError("New email cannot be same as old email");
                                            TextInputEditTextNewEmail.requestFocus();
                                        }
                                        else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            }
                            else {
                                try {
                                    throw task.getException();
                                }
                                catch (Exception e) {
                                    TextInputEditTextPassword.setError("Invalid Password");
                                    TextInputEditTextPassword.requestFocus();

                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {

                    // Verify Email
                    firebaseUser.sendEmailVerification();

                    Toast.makeText(UpdateEmail.this, "Email has been updated. Please verify your new Email", Toast.LENGTH_SHORT).show();

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
                                    Intent intent = new Intent(UpdateEmail.this, AdminProfile.class);
                                    startActivity(intent);
                                    finish(); // Optional: finish the UpdateEmail activity
                                } else if ("consumer".equals(role)) {
                                    // Navigate to the UserProfile activity
                                    Intent intent = new Intent(UpdateEmail.this, UserProfile.class);
                                    startActivity(intent);
                                    finish(); // Optional: finish the UpdateEmail activity
                                } else {
                                    // Handle other roles or roles not defined
                                    Toast.makeText(UpdateEmail.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(UpdateEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}