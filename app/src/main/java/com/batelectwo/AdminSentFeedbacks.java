package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminSentFeedbacks extends AppCompatActivity {
    private ListView listView;
    private List<SentFeedbacks> feedbackList;
    private ArrayAdapter<SentFeedbacks> feedbackAdapter;
    private DatabaseReference databaseReference;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_sent_feedbacks);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Sent Feedbacks");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeToRefresh();

        listView = findViewById(R.id.listView);
        feedbackList = new ArrayList<>();
        feedbackAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, feedbackList);
        listView.setAdapter(feedbackAdapter);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Submitted Feedbacks");

        // Read tickets from Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                feedbackList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SentFeedbacks feedback = snapshot.getValue(SentFeedbacks.class);
                    feedbackList.add(feedback);
                }

                feedbackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Database read error: " + databaseError.getMessage());
            }
        });
    }

    private void swipeToRefresh() {
        // Look up for the swipe container
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup Refresh Listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            // Code to refresh goes here. Make sure to call swipeContainer.setRefresh(false) once the refresh is complete
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
            swipeContainer.setRefreshing(false);
        });

        // Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
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
                                    Intent adminIntent = new Intent(AdminSentFeedbacks.this, AdminSettings.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(AdminSentFeedbacks.this, ConsumerInterface.class);
                                    consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(consumerIntent);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(AdminSentFeedbacks.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(AdminSentFeedbacks.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}