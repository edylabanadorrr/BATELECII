package com.batelectwo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfile extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private TextInputEditText TextInputEditTextUserPassword;
    private TextView textViewAuthenticated;
    private ProgressBar progressBar;
    private String userPassword;
    private Button buttonReAuthenticate, buttonDeleteUser;
    private static final String TAG = "DeleteProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Delete Profile");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBarShow);
        TextInputEditTextUserPassword = findViewById(R.id.textInputEditTextDeleteUserPassword);
        textViewAuthenticated = findViewById(R.id.textViewDeleteUserAuthenticated);
        buttonDeleteUser = findViewById(R.id.deleteUserButton);
        buttonReAuthenticate = findViewById(R.id.authenticationDeleteUserButton);

        // Disable Delete User Button until user is authenticated
        buttonDeleteUser.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")) {
            Toast.makeText(DeleteProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfile.this, UserProfile.class);
            startActivity(intent);
            finish();
        }
        else {
            reAuthenticateUser(firebaseUser);
        }
    }

    // ReAuthenticate User before changing password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPassword = TextInputEditTextUserPassword.getText().toString();

                if (TextUtils.isEmpty(userPassword)) {
                    TextInputEditTextUserPassword.setError("Please enter your current password to authenticate");
                    TextInputEditTextUserPassword.requestFocus();
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);

                    // ReAuthenticate User now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPassword);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                // Disable TextInputEditText for Password.
                                TextInputEditTextUserPassword.setEnabled(false);

                                // Enable Delete User Button. Disable Authenticate Button
                                buttonReAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                // Set TextView to show User is authenticated or verified
                                textViewAuthenticated.setText("You are authenticated or verified. " + "You can delete your profile and data now");
                                Toast.makeText(DeleteProfile.this, "Password has been verified. " + "You can delete your profile now. Be careful, this action is irreversible", Toast.LENGTH_SHORT).show();

                                // Update color of Change Password Button
                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfile.this, R.color.maroon));
                                textViewAuthenticated.setTextColor(getResources().getColor(R.color.green)); // Set text color
                                textViewAuthenticated.setBackgroundColor(getResources().getColor(R.color.white)); // Set background color
                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showAlertDialog();
                                    }
                                });
                            }
                            else {
                                try {
                                    throw task.getException();
                                }
                                catch (Exception e) {
                                    TextInputEditTextUserPassword.setError("Invalid password");
                                    TextInputEditTextUserPassword.requestFocus();

                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfile.this);
        builder.setTitle("Delete User and Data?");
        builder.setMessage("Do you really want to delete your profile and data? This action is irreversible!");

        // Open Email Apps if user clicks / taps Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData(firebaseUser);
            }
        });

        // Return to User Profile if User presses the cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteProfile.this, DeleteProfile.class);
                startActivity(intent);
                finish();
            }
        });

        // Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        // Change the button color of Continue
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        // Show the Alert Dialog
        alertDialog.show();
    }

    private void deleteUser() {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    authProfile.signOut();
                    Toast.makeText(DeleteProfile.this, "User has been deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteProfile.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    try {
                        throw task.getException();
                    }
                    catch (Exception e) {
                        Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Delete all the data of User
    private void deleteUserData(FirebaseUser firebaseUser) {
        // Delete Display Picture, also check if the user has uploaded any picture before deleting
        if (firebaseUser.getPhotoUrl() != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSuccess: Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Delete data from Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User Data Deleted");

                // Finally delete the user after deleting the data
                deleteUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}