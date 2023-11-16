package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserFeedback extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private String userEmail, userUid;
    private TextView textViewEmail, textViewUserID;
    private TextInputEditText TextInputEditTextFeedback;
    private Button submitButton;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_feedback);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Send Feedback");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeToRefresh();

        progressBar = findViewById(R.id.progressBarShow);
        TextInputEditTextFeedback = findViewById(R.id.textInputEditTextFeedback);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // Set Email on Text View
        userEmail = firebaseUser.getEmail();
        TextView textViewEmail = findViewById(R.id.textViewEmail);
        textViewEmail.setText(userEmail);

        // Set User ID on Text View
        userUid = firebaseUser.getUid();
        TextView textViewUserUid = findViewById(R.id.textViewUserID);
        textViewUserUid.setText(userUid);

        Button sendFeedbackButton = findViewById(R.id.sendFeedbackButton);
        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputFeedback = TextInputEditTextFeedback.getText().toString();

                if (TextUtils.isEmpty(inputFeedback)) {
                    TextInputEditTextFeedback.setError("Feedback is required");
                    TextInputEditTextFeedback.requestFocus();
                } else {
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();
                    if (firebaseUser != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        sendFeedbackButton(inputFeedback);
                    } else {
                        Toast.makeText(UserFeedback.this, "You are not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
                    }
                }
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
                                    Intent adminIntent = new Intent(UserFeedback.this, AdminInterface.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(UserFeedback.this, UserSettings.class);
                                    consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(consumerIntent);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(UserFeedback.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(UserFeedback.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

        private void sendFeedbackButton(String inputFeedback) {
            try {
                // Get a reference to the Firebase Database
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Submitted Feedbacks").child(userUid);

                // Create a new ticket object
                SubmittedFeedbacks newTicket = new SubmittedFeedbacks(inputFeedback, userEmail, userUid);

                // Push the new ticket to the Firebase Database
                referenceProfile.setValue(newTicket, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        progressBar.setVisibility(View.GONE);
                        if (databaseError == null) {
                            // Ticket submission successful
                            Toast.makeText(UserFeedback.this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UserFeedback.this, UserSettings.class);
                            startActivity(intent);
                            finish();

                            // Clear input fields
                            TextInputEditTextFeedback.getText().clear();
                        } else {
                            // Ticket submission failed
                            Toast.makeText(UserFeedback.this, "Feedback submission failed. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(UserFeedback.this, "An error occurred while submitting the feedback", Toast.LENGTH_SHORT).show();
            }
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
}