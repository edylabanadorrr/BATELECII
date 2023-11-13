package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserOtherAccounts extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private TextInputEditText TextInputEditTextLoginAccountNumber, TextInputEditTextLoginUsername;
    private ProgressBar progressBar;
    private FirebaseUser firebaseUser;
    private String userUid, userAccountNumber, userUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_other_accounts);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Input Credentials");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextInputEditTextLoginAccountNumber = findViewById(R.id.loginAccountNumber);
        TextInputEditTextLoginUsername = findViewById(R.id.loginUsername);
        progressBar = findViewById(R.id.progressBarShow);
        authProfile = FirebaseAuth.getInstance();

        // Login User
        /* Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textAccountNumber = TextInputEditTextLoginAccountNumber.getText().toString();
                String textUsername = TextInputEditTextLoginUsername.getText().toString();

                if (TextUtils.isEmpty(textAccountNumber)) {
                    TextInputEditTextLoginAccountNumber.setError("Account Number is required");
                    TextInputEditTextLoginAccountNumber.requestFocus();
                } else if (TextUtils.isEmpty(textUsername)) {
                    TextInputEditTextLoginUsername.setError("Username is required");
                    TextInputEditTextLoginUsername.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textAccountNumber, textUsername);
                }
            }
        }); */

        // Login User
        ImageButton nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textAccountNumber = TextInputEditTextLoginAccountNumber.getText().toString();
                String textUsername = TextInputEditTextLoginUsername.getText().toString();

                if (TextUtils.isEmpty(textAccountNumber)) {
                    TextInputEditTextLoginAccountNumber.setError("Account Number is required");
                    TextInputEditTextLoginAccountNumber.requestFocus();
                } else if (TextUtils.isEmpty(textUsername)) {
                    TextInputEditTextLoginUsername.setError("Username is required");
                    TextInputEditTextLoginUsername.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textAccountNumber, textUsername);
                }
            }
        });
    }

    private void loginUser(String accountNumber, String username) {
        // Authenticate the user using Firebase Auth
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Registered Users");

        usersRef.orderByChild("accountNumber").equalTo(accountNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Account number exists, now check the username
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedUsername = userSnapshot.child("username").getValue(String.class);

                        if (storedUsername != null && storedUsername.equals(username)) {
                            // Username is correct
                            String role = userSnapshot.child("role").getValue(String.class);

                            if (role != null) {
                                // Get the userUid from the snapshot
                                String newUserUid = userSnapshot.getKey();

                                // Pass the newUserUid to UserOtherAccountsInterface
                                Intent intent = new Intent(UserOtherAccounts.this, UserOtherAccountsInterface.class);
                                intent.putExtra("USER_UID", newUserUid);
                                startActivity(intent);
                                finish();
                            /*
                            String role = userSnapshot.child("role").getValue(String.class);

                            if (role != null) {
                                String userId = userSnapshot.getKey();

                                // Sign in with the user ID
                                authProfile.signInWithEmailAndPassword(userId + "@example.com", "") // Use a dummy password or leave it empty
                                        .addOnCompleteListener(UserOtherAccounts.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                    // Authentication successful

                                                    // Start the appropriate activity based on the user's role

                                                    if (role.equals("admin")) {
                                                        Intent intentAdmin = new Intent(UserOtherAccounts.this, AdminInterface.class);
                                                        startActivity(intentAdmin);
                                                    } else if (role.equals("consumer")) {
                                                        Intent intentUser = new Intent(UserOtherAccounts.this, AdminInterface.class);
                                                        startActivity(intentUser);
                                                    } else {
                                                        Toast.makeText(UserOtherAccounts.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                    }

                                                    finish(); // Close login interface
                                                }
                                        });
                            } */
                            } else {
                                // Incorrect username
                                // Handle username mismatch
                                progressBar.setVisibility(View.GONE);
                                TextInputEditTextLoginAccountNumber.setError("Invalid credentials");
                                TextInputEditTextLoginAccountNumber.requestFocus();
                            }
                        } else {
                            // Account number does not exist
                            // Handle invalid account number
                            progressBar.setVisibility(View.GONE);
                            TextInputEditTextLoginAccountNumber.setError("Invalid Credentials");
                            TextInputEditTextLoginAccountNumber.requestFocus();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    // When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Check the user's role here and perform actions accordingly

            authProfile = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = authProfile.getCurrentUser();
            if (firebaseUser != null) {
                DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                        .getReference("Registered Users")
                        .child(firebaseUser.getUid());
                databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String role = dataSnapshot.child("role").getValue(String.class);
                            if (role != null) {
                                Intent intent;
                                if (role.equals("admin")) {
                                    // Handle actions for Admin role
                                    intent = new Intent(UserOtherAccounts.this, AdminInterface.class);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    intent = new Intent(UserOtherAccounts.this, BillActivity.class);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(UserOtherAccounts.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });
            }
        }
        return true;
    }
}