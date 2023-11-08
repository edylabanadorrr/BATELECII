package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
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

public class UserRates extends AppCompatActivity {

    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_rates);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Rates");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                                    Intent adminIntent = new Intent(UserRates.this, AdminInterface.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(UserRates.this, ConsumerInterface.class);
                                    consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(consumerIntent);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(UserRates.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(UserRates.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}