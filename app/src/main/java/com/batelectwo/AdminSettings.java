package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
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

public class AdminSettings extends AppCompatActivity {
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_settings);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Admin Settings");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // About Function

        RelativeLayout relativeLayoutAbout = findViewById(R.id.relativeLayoutAbout);
        relativeLayoutAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminSettings.this, UserAbout.class);
                startActivity(intent);
                finish();
            }
        });

        // Submitted Tickets Function

        /*
        RelativeLayout relativeLayoutTicket = findViewById(R.id.relativeLayoutTicket);
        relativeLayoutTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminSettings.this, AdminSubmittedTickets.class);
                startActivity(intent);
                finish();
            }
        }); */

        // Sent Feedbacks Function

        /*
        RelativeLayout relativeLayoutFeedback = findViewById(R.id.relativeLayoutFeedback);
        relativeLayoutFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminSettings.this, AdminSentFeedbacks.class);
                startActivity(intent);
                finish();
            }
        }); */

        // Logout Function

        RelativeLayout relativeLayoutLogout = findViewById(R.id.relativeLayoutLogout);
        relativeLayoutLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authProfile = FirebaseAuth.getInstance();
                authProfile.signOut();
                Intent intent = new Intent(AdminSettings.this, Login.class);

                // Clear Stack to prevent user coming back to UserProfile on pressing back button after logging out
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                                if (role.equals("admin")) {
                                    // Handle actions for Admin role
                                    Intent adminIntent = new Intent(AdminSettings.this, AdminInterface.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(AdminSettings.this, ConsumerInterface.class);
                                    consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(consumerIntent);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(AdminSettings.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
            return true;
        } else {
            Toast.makeText(AdminSettings.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}