package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddConsumer extends AppCompatActivity {

    private TextInputEditText editTextaddFirstName, editTextaddLastName, editTextaddEmail, editTextaddContactNumber, editTextaddAddress, editTextaddUsername, editTextaddPassword, editTextaddConfirmPassword;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "AddConsumer";

    // dsadsajdshaj

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_add_consumer);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Add Consumer");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBarShow);
        editTextaddFirstName = findViewById(R.id.input_firstname);
        editTextaddLastName = findViewById(R.id.input_lastname);
        editTextaddEmail = findViewById(R.id.input_email);
        editTextaddContactNumber = findViewById(R.id.input_contactnumber);
        editTextaddAddress = findViewById(R.id.input_address);
        editTextaddUsername = findViewById(R.id.input_username);
        editTextaddPassword = findViewById(R.id.input_password);
        editTextaddConfirmPassword = findViewById(R.id.confirm_password);

        Button addConsumer = findViewById(R.id.addConsumerButton);
        addConsumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textfirstName = editTextaddFirstName.getText().toString();
                String textlastName = editTextaddLastName.getText().toString();
                String textemail = editTextaddEmail.getText().toString();
                String textcontactNumber = editTextaddContactNumber.getText().toString();
                String textaddress = editTextaddAddress.getText().toString();
                String textusername = editTextaddUsername.getText().toString();
                String textpassword = editTextaddPassword.getText().toString();
                String textconfirmPassword = editTextaddConfirmPassword.getText().toString();

                // Validate Mobile Number using Matcher and Pattern
                String mobileRegex = "0[0-9]{10}"; //First no. should be 0 and rest 10 no. can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textcontactNumber);

                // Generate a random 8-digit account number
                Random random = new Random();
                String textAccountNumber = String.format("%08d", random.nextInt(100000000));

                if (TextUtils.isEmpty(textfirstName)) {
                    editTextaddFirstName.setError("First Name is required");
                    editTextaddFirstName.requestFocus();
                } else if (TextUtils.isEmpty(textlastName)) {
                    editTextaddLastName.setError("Last Name is required");
                    editTextaddLastName.requestFocus();
                } else if (TextUtils.isEmpty(textemail)) {
                    editTextaddEmail.setError("Email is required");
                    editTextaddEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textemail).matches()) {
                    editTextaddEmail.setError("Valid Email is required");
                    editTextaddEmail.requestFocus();
                } else if (TextUtils.isEmpty(textcontactNumber)) {
                    editTextaddContactNumber.setError("Contact Number is required");
                    editTextaddContactNumber.requestFocus();
                } else if (textcontactNumber.length() != 11) {
                    editTextaddContactNumber.setError("Contact Number must be 11 digits");
                    editTextaddContactNumber.requestFocus();
                } else if (!mobileMatcher.find()) {
                    editTextaddContactNumber.setError("Contact Number is not valid");
                    editTextaddContactNumber.requestFocus();
                } else if (TextUtils.isEmpty(textaddress)) {
                    editTextaddAddress.setError("Address is required");
                    editTextaddAddress.requestFocus();
                } else if (TextUtils.isEmpty(textusername)) {
                    editTextaddUsername.setError("Username is required");
                    editTextaddUsername.requestFocus();
                } else if (textusername.length() < 6) {
                    editTextaddUsername.setError("Username should be at least 6 characters");
                    editTextaddUsername.requestFocus();
                } else if (TextUtils.isEmpty(textpassword)) {
                    editTextaddPassword.setError("Password is required");
                    editTextaddPassword.requestFocus();
                } else if (textpassword.length() < 6) {
                    editTextaddPassword.setError("Password too weak");
                    editTextaddPassword.requestFocus();
                } else if (TextUtils.isEmpty(textconfirmPassword)) {
                    editTextaddConfirmPassword.setError("Password confirmation is required");
                    editTextaddConfirmPassword.requestFocus();
                } else if (!textpassword.equals(textconfirmPassword)) {
                    editTextaddConfirmPassword.setError("Mismatch password");
                    editTextaddConfirmPassword.requestFocus();

                    // Clear the entered passwords
                    editTextaddPassword.clearComposingText();
                    editTextaddConfirmPassword.clearComposingText();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(AddConsumer.this, "Please wait for a few seconds", Toast.LENGTH_LONG).show();
                    addConsumer(textfirstName, textlastName, textemail, textcontactNumber, textaddress, textusername, textpassword, textAccountNumber);
                }
            }
        });
    }

    private void addConsumer(String textfirstName, String textlastName, String textemail, String textcontactNumber, String textaddress, String textusername, String textpassword, String accountNumber) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textemail, textpassword).addOnCompleteListener(AddConsumer.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    // Update Display Name of User
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textfirstName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textfirstName, textlastName, textcontactNumber, textaddress, textusername, accountNumber);

                    // Set the user's role to "Consumer" in the Realtime Database
                    writeUserDetails.setRole("consumer");
                    writeUserDetails.setBill("0.00");

                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                // Send Verification Email
                                firebaseUser.sendEmailVerification();

                                Toast.makeText(AddConsumer.this, "Consumer added successfully", Toast.LENGTH_SHORT).show();

                                // Open User Profile after successful registration
                                Intent intent = new Intent(AddConsumer.this, AdminConsumers.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(AddConsumer.this, "User registered failed", Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        editTextaddPassword.setError("Your Password is too Weak");
                        editTextaddPassword.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextaddEmail.setError("Your Email is Invalid or Already Use.");
                        editTextaddEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        editTextaddEmail.setError("User is Already Registered with this Email");
                        editTextaddEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(AddConsumer.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
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
                                    Intent adminIntent = new Intent(AddConsumer.this, AdminConsumers.class);
                                    // adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                    finish();
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(AddConsumer.this, ConsumerInterface.class);
                                    // consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(consumerIntent);
                                    finish();
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(AddConsumer.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });
                return true;
            }
        }
        return false;
    }
}