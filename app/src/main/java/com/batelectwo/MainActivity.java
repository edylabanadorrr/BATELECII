package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("BATELEC II");

        authProfile = FirebaseAuth.getInstance();
        Button button = findViewById(R.id.get_started);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the new activity when the button is clicked
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });
    }
    // Check if user is already logged in. In such case, straightway take the User to the User's Profile
    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = authProfile.getCurrentUser(); // Initialize firebaseUser

        if (authProfile.getCurrentUser() != null) {
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
                                Intent adminIntent = new Intent(MainActivity.this, AdminInterface.class);
                                adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(adminIntent);
                                finish();
                            } else if (role.equals("consumer")) {
                                // Handle actions for Consumer role
                                Intent consumerIntent = new Intent(MainActivity.this, ConsumerInterface.class);
                                consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(consumerIntent);
                                finish();
                            } else {
                                // Handle actions for other roles or roles not defined
                                Toast.makeText(MainActivity.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
    }
}
