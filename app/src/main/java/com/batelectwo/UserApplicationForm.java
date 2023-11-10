package com.batelectwo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserApplicationForm extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private Bitmap applicationFormBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_application_form);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Application Form");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Load the application form image
        loadApplicationFormImage();

        ImageView applicationForm = findViewById(R.id.applicationFormforServiceConnection);
        applicationForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Inflate the alert dialog layout
                View dialogView = LayoutInflater.from(UserApplicationForm.this).inflate(R.layout.application_form_alert, null);

                // Find views in the dialog layout
                ImageView applicationFormImageView = dialogView.findViewById(R.id.ApplicationFormImageView);
                Button printButton = dialogView.findViewById(R.id.printApplicationForm);
                Button cancelButton = dialogView.findViewById(R.id.cancelApplicationForm);

                // Create the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(UserApplicationForm.this);
                builder.setView(dialogView);

                // Create and show the dialog
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // Handle print button click
                printButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check if the invoiceBitmap is not null
                        if (applicationFormBitmap != null) {
                            // Print the Application Form
                            printApplicationForm(applicationFormBitmap);

                            // Dismiss the dialog
                            alertDialog.dismiss();
                        } else {
                            // Handle the case where the applicationBitmap is null
                            Toast.makeText(UserApplicationForm.this, "Image not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // Handle cancel button click
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dismiss the dialog
                        alertDialog.dismiss();
                    }
                });
            }

            private void printApplicationForm(Bitmap bitmap) {
                // Check if printing is supported
                if (!PrintHelper.systemSupportsPrint()) {
                    Toast.makeText(UserApplicationForm.this, "Printing is not supported on this device", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a PrintHelper instance
                PrintHelper printHelper = new PrintHelper(UserApplicationForm.this);
                printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);

                // Print the QR code
                printHelper.printBitmap("Application Form", bitmap);
            }
        });
    }

    private void loadApplicationFormImage() {
        applicationFormBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.application_form_for_new_connection);
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
                                    Intent adminIntent = new Intent(UserApplicationForm.this, UserInstallationRequirements.class);
                                    startActivity(adminIntent);
                                    finish();
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(UserApplicationForm.this, UserInstallationRequirements.class);
                                    startActivity(consumerIntent);
                                    finish();
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(UserApplicationForm.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(UserApplicationForm.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}