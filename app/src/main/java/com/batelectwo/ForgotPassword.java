package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;


public class ForgotPassword extends AppCompatActivity {

    private Button buttonPasswordReset;
    private TextInputEditText TextInputEditTextPasswordResetEmail;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private final static String TAG = "ForgotPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Forgot Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        TextInputEditTextPasswordResetEmail = findViewById(R.id.TextInputEditTextPasswordResetEmail);
        buttonPasswordReset = findViewById(R.id.buttonPasswordReset);
        progressBar = findViewById(R.id.progress_bar);

        buttonPasswordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = TextInputEditTextPasswordResetEmail.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    TextInputEditTextPasswordResetEmail.setError("Email is required");
                    TextInputEditTextPasswordResetEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    TextInputEditTextPasswordResetEmail.setError("Valid email is required");
                    TextInputEditTextPasswordResetEmail.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(email);
                }
            }
        });
    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE); // Hide the progress bar

                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPassword.this, "Please check your inbox for a password reset link", Toast.LENGTH_SHORT).show();

                    // Create a Handler
                    Handler handler = new Handler();

                    // Define the delay duration in milliseconds (e.g., 3000 milliseconds = 3 seconds)
                    long delayMillis = 2000;

                    // Post a delayed action to start the Login activity
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ForgotPassword.this, Login.class);

                            // Clear Stack to prevent the user from coming back to Forgot Password Activity
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Close User Profile
                        }
                    }, delayMillis);
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        TextInputEditTextPasswordResetEmail.setError("User does not exist or is no longer valid. Please register again");
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the up button click (usually, navigate back)
                Intent intent = new Intent(ForgotPassword.this, Login.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}