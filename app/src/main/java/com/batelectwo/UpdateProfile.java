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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfile extends AppCompatActivity {

    private TextInputEditText TextInputEditTextUpdateFirstName, TextInputEditTextUpdateLastName,
            TextInputEditTextUpdateAddress, TextInputEditTextUpdateContactNumber, TextInputEditTextUpdateUsername;
    private TextView TextViewAccountNumber;
    private String textFirstName, textLastName, textAddress, textContactNumber, textUsername, textAccountNumber;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Update Profile Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Retrieve the accountNumber from the intent's extras
        Intent intent = getIntent();
        if (intent != null) {
            textAccountNumber = intent.getStringExtra("accountNumber");
            // Now, you can use textAccountNumber as needed in this activity
        }

        progressBar = findViewById(R.id.progress_bar);
        TextInputEditTextUpdateFirstName = findViewById(R.id.TextInputEditTextUpdateProfileFirstName);
        TextInputEditTextUpdateLastName = findViewById(R.id.TextInputEditTextUpdateProfileLastName);
        TextInputEditTextUpdateAddress = findViewById(R.id.TextInputEditTextUpdateProfileAddress);
        TextInputEditTextUpdateContactNumber = findViewById(R.id.TextInputEditTextUpdateProfileContactNumber);
        TextInputEditTextUpdateUsername = findViewById(R.id.TextInputEditTextUpdateProfileUsername);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // Show Profile Data
        showProfile(firebaseUser);

        // Update Profile Button
        Button updateProfileButton = findViewById(R.id.profileUpdateProfileButton);
        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }
    private void updateProfile(FirebaseUser firebaseUser) {
        // Validate Mobile Number using Matcher and Pattern
        String mobileRegex = "0[0-9]{10}"; // First no. should be 0 and rest 10 no. can be any no.
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textContactNumber);

        if (TextUtils.isEmpty(textFirstName)) {
            TextInputEditTextUpdateFirstName.setError("First Name is required");
            TextInputEditTextUpdateFirstName.requestFocus();
        } else if (TextUtils.isEmpty(textLastName)) {
            TextInputEditTextUpdateLastName.setError("Last Name is required");
            TextInputEditTextUpdateLastName.requestFocus();
        } else if (TextUtils.isEmpty(textContactNumber)) {
            TextInputEditTextUpdateContactNumber.setError("Contact Number is required");
            TextInputEditTextUpdateContactNumber.requestFocus();
        } else if (textContactNumber.length() != 11) {
            TextInputEditTextUpdateContactNumber.setError("Contact Number must be 11 digits");
            TextInputEditTextUpdateContactNumber.requestFocus();
        } else if (!mobileMatcher.find()) {
            TextInputEditTextUpdateContactNumber.setError("Contact Number is not valid");
            TextInputEditTextUpdateContactNumber.requestFocus();
        } else if (TextUtils.isEmpty(textAddress)) {
            TextInputEditTextUpdateAddress.setError("Address is required");
            TextInputEditTextUpdateAddress.requestFocus();
        } else if (TextUtils.isEmpty(textUsername)) {
            TextInputEditTextUpdateUsername.setError("Username is required");
            TextInputEditTextUpdateUsername.requestFocus();
        } else if (textUsername.length() < 6) {
            TextInputEditTextUpdateUsername.setError("Username should be at least 6 characters");
            TextInputEditTextUpdateUsername.requestFocus();
        } else {
            // Obtain the data entered by the user
            textFirstName = TextInputEditTextUpdateFirstName.getText().toString();
            textLastName = TextInputEditTextUpdateLastName.getText().toString();
            textContactNumber = TextInputEditTextUpdateContactNumber.getText().toString();
            textAddress = TextInputEditTextUpdateAddress.getText().toString();
            textUsername = TextInputEditTextUpdateUsername.getText().toString();

            // Create a map to update only the fields you want to change
            Map<String, Object> updates = new HashMap<>();
            updates.put("firstName", textFirstName);
            updates.put("lastName", textLastName);
            updates.put("contactNumber", textContactNumber);
            updates.put("address", textAddress);
            updates.put("username", textUsername);

            // Extract User reference from Database for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);
            referenceProfile.child(userID).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Setting new first name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFirstName).build();
                        firebaseUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Profile update is successful
                                            Toast.makeText(UpdateProfile.this, "Update Successful", Toast.LENGTH_SHORT).show();

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
                                                            Intent intent = new Intent(UpdateProfile.this, AdminProfile.class);
                                                            startActivity(intent);
                                                            finish(); // Optional: finish the UpdateProfile activity
                                                        } else if ("consumer".equals(role)) {
                                                            // Navigate to the ConsumerProfile activity
                                                            Intent intent = new Intent(UpdateProfile.this, UserProfile.class);
                                                            startActivity(intent);
                                                            finish(); // Optional: finish the UpdateProfile activity
                                                        } else {
                                                            // Handle other roles or roles not defined
                                                            Toast.makeText(UpdateProfile.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(UpdateProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                }
            });
        }
    }

                private void showProfile(FirebaseUser firebaseUser) {
                    String userIDofRegistered = firebaseUser.getUid();

                    // Extracting User Reference from Database for "Registered Users"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    progressBar.setVisibility(View.VISIBLE);

                    referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                            if (readUserDetails != null) {
                                textFirstName = firebaseUser.getDisplayName();
                                textLastName = readUserDetails.lastName;
                                textAddress = readUserDetails.address;
                                textContactNumber = readUserDetails.contactNumber;
                                textUsername = readUserDetails.username;

                                // Set the account number to TextViewAccountNumber
                                TextInputEditTextUpdateFirstName.setText(textFirstName);
                                TextInputEditTextUpdateLastName.setText(textLastName);
                                TextInputEditTextUpdateAddress.setText(textAddress);
                                TextInputEditTextUpdateContactNumber.setText(textContactNumber);
                                TextInputEditTextUpdateUsername.setText(textUsername);
                            } else {
                                Toast.makeText(UpdateProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UpdateProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }