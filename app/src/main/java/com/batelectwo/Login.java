package com.batelectwo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private TextInputEditText TextInputEditTextLoginEmail, TextInputEditTextLoginPassword;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Login");

        TextInputEditTextLoginEmail = findViewById(R.id.login_emailAddress);
        TextInputEditTextLoginPassword = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.progressBarShow);
        authProfile = FirebaseAuth.getInstance();

        // Show Hide Password using Eye Icon
        /* ImageView imageViewShowHidePwd = findViewById(R.id.imageViewShowHidePassword);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_password);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextInputEditTextLoginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    // If Password is Visible then Hide it
                    TextInputEditTextLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    // Change Icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_password);
                } else {
                    TextInputEditTextLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_password);
                }
            }
        }); */

        // Registration Function

        TextView createAccount = findViewById(R.id.registration);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                finish();
            }
        });

        // Forgot Password Function

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });

        // Enable "Click Here" button and "Forgot Password" text by default
        createAccount.setClickable(true);
        forgotPassword.setClickable(true);

        // Login User
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmailAccountNumber = TextInputEditTextLoginEmail.getText().toString();
                String textPassword = TextInputEditTextLoginPassword.getText().toString();

                if (TextUtils.isEmpty(textEmailAccountNumber)) {
                    TextInputEditTextLoginEmail.setError("Email is required");
                    TextInputEditTextLoginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmailAccountNumber).matches()) {
                    TextInputEditTextLoginEmail.setError("Valid Email is required");
                    TextInputEditTextLoginEmail.requestFocus();
                } else if (TextUtils.isEmpty(textPassword)) {
                    TextInputEditTextLoginPassword.setError("Password is required");
                    TextInputEditTextLoginPassword.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmailAccountNumber, textPassword);
                }
            }
        });
    }

    private void loginUser(String emailAccountNumber, String password) {
        // Authenticate the user using Firebase Auth
        authProfile.signInWithEmailAndPassword(emailAccountNumber, password)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE); // Hide the progress bar

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = authProfile.getCurrentUser();
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
                                                startActivity(new Intent(Login.this, AdminInterface.class));
                                            } else if (role.equals("consumer")) {
                                                if (firebaseUser.isEmailVerified()) {
                                                    startActivity(new Intent(Login.this, ConsumerInterface.class));
                                                } else {
                                                    firebaseUser.sendEmailVerification();
                                                    authProfile.signOut(); // Sign out user
                                                    showAlertDialog();
                                                }
                                            } else {
                                                // Handle other roles or roles not defined
                                            }
                                            finish(); // Close login interface
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle database error
                                }
                            });
                        } else {
                            // Handle login failure
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                TextInputEditTextLoginEmail.setError("User does not exist or is no longer valid");
                                TextInputEditTextLoginEmail.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                TextInputEditTextLoginEmail.setError("Invalid credentials");
                                TextInputEditTextLoginEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void showAlertDialog() {
        // Setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Please verify your email now. You can not login without email verification");

        // Open Email Apps if user clicks / taps Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // To email app in new window and not within our app
                startActivity(intent);
            }
        });

        // Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog
        alertDialog.show();
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
                                Intent adminIntent = new Intent(Login.this, AdminInterface.class);
                                adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(adminIntent);
                                finish();
                            } else if (role.equals("consumer")) {
                                // Handle actions for Consumer role
                                Intent consumerIntent = new Intent(Login.this, ConsumerInterface.class);
                                consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(consumerIntent);
                                finish();
                            } else {
                                // Handle actions for other roles or roles not defined
                                Toast.makeText(Login.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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