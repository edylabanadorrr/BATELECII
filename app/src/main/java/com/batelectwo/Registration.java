package com.batelectwo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Registration extends AppCompatActivity {

    private TextInputEditText editTextregFirstName, editTextregLastName, editTextregEmail, editTextregContactNumber, editTextregAddress, editTextregUsername, editTextregPassword, editTextregConfirmPassword;
    private ProgressBar progressBar;
    private CheckBox checkBoxAcceptTerms;
    private TextView textViewAcceptTerms;
    private Button registerButton;
    private CountDownTimer timer;
    private boolean isTimerCompleted = false;


    private static final String TAG = "Registration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBarShow);
        editTextregFirstName = findViewById(R.id.input_firstname);
        editTextregLastName = findViewById(R.id.input_lastname);
        editTextregEmail = findViewById(R.id.input_email);
        editTextregContactNumber = findViewById(R.id.input_contactnumber);
        editTextregAddress = findViewById(R.id.input_address);
        editTextregUsername = findViewById(R.id.input_username);
        editTextregPassword = findViewById(R.id.input_password);
        editTextregConfirmPassword = findViewById(R.id.confirm_password);
        checkBoxAcceptTerms = findViewById(R.id.checkBoxAcceptTerms);
        textViewAcceptTerms = findViewById(R.id.textViewAcceptTerms);
        registerButton = findViewById(R.id.register_button);

        // Initialize a flag to track whether the timer is completed
        isTimerCompleted = false;

        // Create a CountDownTimer for the 5-second timer
        CountDownTimer dialogTimer = new CountDownTimer(30000, 1000) { // 30 seconds (30,000 milliseconds)
            @Override
            public void onTick(long millisUntilFinished) {
                // The timer is still running; do nothing
            }

            @Override
            public void onFinish() {
                // Timer has completed
                isTimerCompleted = true;
            }
        };
        checkBoxAcceptTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isTimerCompleted) {
                    // Display a message to read the terms and conditions first
                    Toast.makeText(Registration.this, "Please read the terms and conditions before checking the box.", Toast.LENGTH_SHORT).show();
                    // Prevent the checkbox from being checked until the timer is complete
                    checkBoxAcceptTerms.setChecked(false);
                } else {
                    // Enable or disable the Register button based on the CheckBox state
                    registerButton.setEnabled(isChecked);
                }
            }
        });

        textViewAcceptTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Inflate the alert dialog layout
                View dialogView = LayoutInflater.from(Registration.this).inflate(R.layout.terms_and_conditions_alert, null);

                // Create the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Registration.this);
                builder.setView(dialogView);

                // Create and show the dialog
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // Get a reference to the dialog's TextView for the timer
                final TextView dialogTextViewTimer = dialogView.findViewById(R.id.dialogTextViewTimer);

                // Create a CountDownTimer for the alert dialog
                CountDownTimer dialogTimer = new CountDownTimer(30000, 1000) { // 30 seconds (30,000 milliseconds)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // Update the timer in the dialog's TextView
                        long secondsRemaining = millisUntilFinished / 1000;
                        // long minutes = secondsRemaining / 60;
                        // long seconds = secondsRemaining % 60;
                        // String timeRemaining = String.format("%d:%02d", minutes, seconds);
                        String text = "Time remaining: " + secondsRemaining;

                        SpannableString spannableString = new SpannableString(text);

                        // Set the color for "Time remaining:"
                        ForegroundColorSpan colorSpan1 = new ForegroundColorSpan(Color.BLUE);
                        spannableString.setSpan(colorSpan1, 0, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Set the color for the time (secondsRemaining)
                        ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(Color.RED);
                        spannableString.setSpan(colorSpan2, 15, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        dialogTextViewTimer.setText(spannableString);

                        // Log.d("CountdownTimer", "Time remaining: " + timeRemaining);
                    }

                    @Override
                    public void onFinish() {
                        // Timer has completed; you can dismiss the dialog or take action

                        alertDialog.dismiss();
                        isTimerCompleted = true;
                        Log.d("CountdownTimer", "Timer finished");
                    }
                };

                // Start the dialog timer
                dialogTimer.start();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textfirstName = editTextregFirstName.getText().toString();
                String textlastName = editTextregLastName.getText().toString();
                String textemail = editTextregEmail.getText().toString();
                String textcontactNumber = editTextregContactNumber.getText().toString();
                String textaddress = editTextregAddress.getText().toString();
                String textusername = editTextregUsername.getText().toString();
                String textpassword = editTextregPassword.getText().toString();
                String textconfirmPassword = editTextregConfirmPassword.getText().toString();

                // Validate Mobile Number using Matcher and Pattern
                String mobileRegex = "0[0-9]{10}"; //First no. should be 0 and rest 10 no. can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textcontactNumber);

                // Generate a random 8-digit account number
                Random random = new Random();
                String textAccountNumber = String.format("%08d", random.nextInt(100000000));

                if (TextUtils.isEmpty(textfirstName)) {
                    editTextregFirstName.setError("First Name is required");
                    editTextregFirstName.requestFocus();
                } else if (TextUtils.isEmpty(textlastName)) {
                    editTextregLastName.setError("Last Name is required");
                    editTextregLastName.requestFocus();
                } else if (TextUtils.isEmpty(textemail)) {
                    editTextregEmail.setError("Email is required");
                    editTextregEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textemail).matches()) {
                    editTextregEmail.setError("Valid Email is required");
                    editTextregEmail.requestFocus();
                } else if (TextUtils.isEmpty(textcontactNumber)) {
                    editTextregContactNumber.setError("Contact Number is required");
                    editTextregContactNumber.requestFocus();
                } else if (textcontactNumber.length() != 11) {
                    editTextregContactNumber.setError("Contact Number must be 11 digits");
                    editTextregContactNumber.requestFocus();
                } else if (!mobileMatcher.find()) {
                    editTextregContactNumber.setError("Contact Number is not valid");
                    editTextregContactNumber.requestFocus();
                } else if (TextUtils.isEmpty(textaddress)) {
                    editTextregAddress.setError("Address is required");
                    editTextregAddress.requestFocus();
                } else if (TextUtils.isEmpty(textusername)) {
                    editTextregUsername.setError("Username is required");
                    editTextregUsername.requestFocus();
                } else if (textusername.length() < 6) {
                    editTextregUsername.setError("Username should be at least 6 characters");
                    editTextregUsername.requestFocus();
                } else if (TextUtils.isEmpty(textpassword)) {
                    editTextregPassword.setError("Password is required");
                    editTextregPassword.requestFocus();
                } else if (textpassword.length() < 6) {
                    editTextregPassword.setError("Password too weak");
                    editTextregPassword.requestFocus();
                } else if (TextUtils.isEmpty(textconfirmPassword)) {
                    editTextregConfirmPassword.setError("Password confirmation is required");
                    editTextregConfirmPassword.requestFocus();
                } else if (!textpassword.equals(textconfirmPassword)) {
                    editTextregConfirmPassword.setError("Mismatch password");
                    editTextregConfirmPassword.requestFocus();

                    // Clear the entered passwords
                    editTextregPassword.clearComposingText();
                    editTextregConfirmPassword.clearComposingText();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(Registration.this, "Please wait for a few seconds", Toast.LENGTH_LONG).show();
                    registerUser(textfirstName, textlastName, textemail, textcontactNumber, textaddress, textusername, textpassword, textAccountNumber);
                }
            }

            //Register User using the credentials given
            private void registerUser(String textfirstName, String textlastName, String textemail, String textcontactNumber, String textaddress, String textusername, String textpassword, String accountNumber) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(textemail, textpassword).addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>() {
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

                                        Toast.makeText(Registration.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                                        // Open User Profile after successful registration
                                        Intent intent = new Intent(Registration.this, ConsumerInterface.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Registration.this, "User registered failed", Toast.LENGTH_LONG).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                editTextregPassword.setError("Your Password is too Weak");
                                editTextregPassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextregEmail.setError("Your Email is Invalid or Already Use.");
                                editTextregEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                editTextregEmail.setError("User is Already Register with this Email");
                                editTextregEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(Registration.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        /* timer = new CountDownTimer(120000, 1000) { // 120,000 milliseconds (2 minutes)
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the timer if needed (e.g., display the remaining time)
                long secondsRemaining = millisUntilFinished / 1000;
                // Update a TextView or any UI element to display the remaining time
                // textViewTimer.setText("Time remaining: " + secondsRemaining + " seconds");
            }

            @Override
            public void onFinish() {
                // Timer has completed; set the flag to true and enable the checkbox
                isTimerCompleted = true;
                checkBoxAcceptTerms.setEnabled(true);
            }
        }; */
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the up button click (usually, navigate back)
                Intent intent = new Intent(Registration.this, Login.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}



