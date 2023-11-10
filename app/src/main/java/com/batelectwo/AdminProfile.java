package com.batelectwo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.print.PrintHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class AdminProfile extends AppCompatActivity {
    private TextView textViewWelcome, textViewAccountNumber, textViewFirstName, textViewLastName, textViewEmail, textViewAddress, textViewContactNumber, textViewUsername, textViewUID;
    private ProgressBar progressBar;
    private String accountNumber, firstName, lastName, email, address, contactNumber, username, uid, role;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        Objects.requireNonNull(getSupportActionBar()).setTitle("BSP Profile");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeToRefresh();

        textViewAccountNumber = findViewById(R.id.textViewShowAccountNumber);
        textViewWelcome = findViewById(R.id.textViewShowWelcome);
        textViewFirstName = findViewById(R.id.textViewShowFirstName);
        textViewLastName = findViewById(R.id.textViewShowLastName);
        textViewEmail = findViewById(R.id.textViewShowEmail);
        textViewAddress = findViewById(R.id.textViewShowAddress);
        textViewContactNumber = findViewById(R.id.textViewShowContactNumber);
        textViewUsername = findViewById(R.id.textViewShowUsername);
        textViewUID = findViewById(R.id.textViewShowUserID);
        progressBar = findViewById(R.id.progressBarShow);

        // Set onClickListener on ImageView to Open Upload Profile Picture Activity
        imageView = findViewById(R.id.imageViewProfileDp);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProfile.this, UploadProfilePicture.class);
            startActivity(intent);
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(AdminProfile.this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_SHORT).show();
        } else {
            checkifEmailVerified(firebaseUser);
            progressBar.setVisibility(View.GONE);
            showUserProfile(firebaseUser);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profile_bottom);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home_bottom) {
                Intent intent = new Intent(AdminProfile.this, AdminInterface.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.consumers_bottom) {
                Intent intent = new Intent(AdminProfile.this, AdminConsumers.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.videos_bottom) {
                Intent intent = new Intent(AdminProfile.this, AdminVideos.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.send_email_bottom) {
                Intent intent = new Intent(AdminProfile.this, AdminSendEmail.class);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.profile_bottom) {
                return true;
            }
            return false;
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

    // Users coming to UserProfile after successful register
    private void checkifEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminProfile.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Please verify your email now. You can not login without email verification next time");

        // Open Email Apps if user clicks / taps Continue button
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // To email app in new window and not within our app
            startActivity(intent);
        });

        // Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        // Extracting User Reference from Database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    accountNumber = readUserDetails.accountNumber;
                    firstName = firebaseUser.getDisplayName();
                    lastName = readUserDetails.lastName;
                    email = firebaseUser.getEmail();
                    address = readUserDetails.address;
                    contactNumber = readUserDetails.contactNumber;
                    username = readUserDetails.username;
                    uid = firebaseUser.getUid();

                    textViewWelcome.setText(getString(R.string.welcomeMessage, firstName, lastName));
                    textViewAccountNumber.setText(accountNumber);
                    textViewFirstName.setText(firstName);
                    textViewLastName.setText(lastName);
                    textViewEmail.setText(email);
                    textViewAddress.setText(address);
                    textViewContactNumber.setText(contactNumber);
                    textViewUsername.setText(username);
                    textViewUID.setText(uid);

                    // Set User Profile Picture (After user has uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();

                    // Image Viewer setImageUrl() should not be used with regular URIs. So we are using Picasso
                    Picasso.with(AdminProfile.this).load(uri).into(imageView);
                } else {
                    Toast.makeText(AdminProfile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminProfile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });

        Button buttonGenerateQR = findViewById(R.id.generateQRButton);
        buttonGenerateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected consumer from the TextViews
                String firstName = ((TextView) findViewById(R.id.textViewShowFirstName)).getText().toString();
                String lastName = ((TextView) findViewById(R.id.textViewShowLastName)).getText().toString();
                String address = ((TextView) findViewById(R.id.textViewShowAddress)).getText().toString();
                String contactNumber = ((TextView) findViewById(R.id.textViewShowContactNumber)).getText().toString();

                // Combine the product details into a single text
                String consumerDetails =
                                "First Name: " + firstName + "\n" +
                                "Last Name: " + lastName + "\n" +
                                "Address: " + address + "\n" +
                                "Contact Number: " + contactNumber;

                // Generate the QR code
                generateQRCode(consumerDetails);
            }
        });
    }

    // Creating Action Bar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu items
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                                Intent intent;
                                if (role.equals("admin")) {
                                    // Handle actions for Admin role
                                    intent = new Intent(AdminProfile.this, AdminInterface.class);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    intent = new Intent(AdminProfile.this, ConsumerInterface.class);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(AdminProfile.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
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
        } else if (id == R.id.menuUpdateProfile) {
            Intent intent = new Intent(AdminProfile.this, UpdateProfile.class);
            intent.putExtra("accountNumber", accountNumber);
            intent.putExtra("role", role);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuUpdateEmail) {
            Intent intent = new Intent(AdminProfile.this, UpdateEmail.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuChangePassword) {
            Intent intent = new Intent(AdminProfile.this, ChangePassword.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuDeleteProfile) {
            Intent intent = new Intent(AdminProfile.this, DeleteProfile.class);
            startActivity(intent);
            return true;
        } else {
            Toast.makeText(AdminProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
    }

    private String getAppPackageName() {
        return getApplicationContext().getPackageName();
    }
    private void generateQRCode(String data) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Save the QR code image to a file
            String fileName = "QRCode.png";
            File file = new File(getExternalCacheDir(), fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving QR code image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Inflate the alert dialog layout
            View dialogView = LayoutInflater.from(this).inflate(R.layout.qr_code_alert, null);

            // Find views in the dialog layout
            ImageView qrCodeImageView = dialogView.findViewById(R.id.qrCodeImageView);
            Button printButton = dialogView.findViewById(R.id.printQRButton);
            Button shareButton = dialogView.findViewById(R.id.shareQRButton);
            Button cancelButton = dialogView.findViewById(R.id.cancelQRButton);

            // Set the generated QR code image to the ImageView
            qrCodeImageView.setImageBitmap(bitmap);

            // Create the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            // Create and show the dialog
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            // Handle print button click (implement printing logic here)

            // Handle download button click
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Create a content URI using FileProvider
                    Uri contentUri = FileProvider.getUriForFile(
                            AdminProfile.this,
                            getAppPackageName() + ".fileprovider",
                            file
                    );

                    // Open a share intent to allow the user to download the QR code
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read permission
                    startActivity(Intent.createChooser(intent, "Share QR Code"));
                }
            });


            // Handle print button click
            printButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Print the QR code
                    printQRCode(bitmap);

                    // Dismiss the dialog
                    alertDialog.dismiss();
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

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void printQRCode(Bitmap bitmap) {
        // Check if printing is supported
        if (!PrintHelper.systemSupportsPrint()) {
            Toast.makeText(this, "Printing is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a PrintHelper instance
        PrintHelper printHelper = new PrintHelper(this);
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);

        // Print the QR code
        printHelper.printBitmap("QR Code", bitmap);
    }
}
