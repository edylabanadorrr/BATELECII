package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserOtherAccountsInterface extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private String userUid, userAccountNumber, userUsername;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_other_accounts_interface);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Other Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // Retrieve extras from Intent
        Intent intent = getIntent();
        if (intent != null) {
            userUid = intent.getStringExtra("USER_UID");
            TextView textViewUserUid = findViewById(R.id.textViewUserID);
            textViewUserUid.setText(userUid);
        }

        if (TextUtils.isEmpty(userUid)) {
            // Handle the case where userUid is not available
            // For example, show an error message and return
            Toast.makeText(this, "User information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use userUid to fetch user information from the database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("Registered Users")
                .child(userUid);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming "accountNumber" is the key for the account number in your database
                    String userAccountNumber = dataSnapshot.child("accountNumber").getValue(String.class);

                    // Set User Account Number on Text View
                    TextView textViewUserAccountNumber = findViewById(R.id.textViewAccountNumber);
                    textViewUserAccountNumber.setText(userAccountNumber);

                    String userBillAmount = dataSnapshot.child("bill").getValue(String.class);

                    // Set User Bill Amount on Text View
                    TextView textViewUserBillAmount = findViewById(R.id.myBill);
                    textViewUserBillAmount.setText(userBillAmount);
                } else {
                    // Handle the case where the data does not exist
                    // For example, set a default value or show an error message
                    Toast.makeText(UserOtherAccountsInterface.this, "User information not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(UserOtherAccountsInterface.this, "Database error", Toast.LENGTH_SHORT).show();
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
                                    intent = new Intent(UserOtherAccountsInterface.this, AdminInterface.class);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    intent = new Intent(UserOtherAccountsInterface.this, UserOtherAccounts.class);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(UserOtherAccountsInterface.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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