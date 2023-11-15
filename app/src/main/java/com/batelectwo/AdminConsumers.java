package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminConsumers extends AppCompatActivity {

    private SwipeRefreshLayout swipeContainer;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceProfile;
    private StorageReference storageReference;
    private TextInputEditText TextInputEditTextFirstName, TextInputEditTextLastName, TextInputEditTextBill,
            TextInputEditTextAddress, TextInputEditTextContactNumber, TextInputEditTextUsername;

    private TextView textViewAccountNumber, textViewUserID, textViewEmail;
    private String textFirstName, textLastName, textAddress, textContactNumber, textUsername, textAccountNumber;

    private ProgressBar progressBar;
    private AutoCompleteTextView autoCompleteTextViewSearch;
    private ArrayAdapter<String> searchAdapter;
    private List<Consumer> consumerList;
    private static final String TAG = "AdminConsumers";
    // private static final long MIN_TIME_INTERVAL = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds
    private static final long MIN_TIME_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;
    private ImageView imageViewUploadInvoice, imageViewInvoice;
    private String accountNumber, uid;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_consumers);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Registered Consumers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize UI elements
        progressBar = findViewById(R.id.progressBarShow);
        textViewAccountNumber = findViewById(R.id.textViewShowAccountNumber);
        TextInputEditTextFirstName = findViewById(R.id.textInputEditTextFirstName);
        TextInputEditTextLastName = findViewById(R.id.textInputEditTextLastName);
        TextInputEditTextBill = findViewById(R.id.textInputEditTextBill);
        TextInputEditTextAddress = findViewById(R.id.textInputEditTextAddress);
        TextInputEditTextContactNumber = findViewById(R.id.textInputEditTextContactNumber);
        TextInputEditTextUsername = findViewById(R.id.textInputEditTextUsername);

        swipeToRefresh();

        // Initialize the productList and set up the search functionality
        consumerList = new ArrayList<>();

        setupSearch();

        // Invoice
        imageViewInvoice = findViewById(R.id.imageViewInvoice);
        Button uploadInvoice = findViewById(R.id.uploadInvoiceButton);
        imageViewUploadInvoice = findViewById(R.id.imageViewUploadedInvoice);
        imageViewInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Upload Invoice
        uploadInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Add Consumer Function

        Button buttonAddConsumer = findViewById(R.id.addConsumerButton);
        buttonAddConsumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminConsumers.this, AddConsumer.class);
                startActivity(intent);
            }
        });

        // Update Consumer Function

        Button buttonUpdateConsumer = findViewById(R.id.updateConsumerButton);
        buttonUpdateConsumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateConsumer();
            }
        });

        // Clear Consumer Info

        Button buttonClearConsumer = findViewById(R.id.clearConsumerButton);
        buttonClearConsumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearConsumerInfo();
            }
        });

        // Check the user's role before allowing access to this activity
        checkUserRoleForAccess();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.consumers_bottom);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home_bottom) {
                Intent intent = new Intent(AdminConsumers.this, AdminInterface.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.consumers_bottom) {
                return true;
            } else if (itemId == R.id.videos_bottom) {
                Intent intent = new Intent(AdminConsumers.this, AdminVideos.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.send_email_bottom) {
                Intent intent = new Intent(AdminConsumers.this, AdminSendEmail.class);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.profile_bottom) {
                Intent intent = new Intent(AdminConsumers.this, AdminProfile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    /* private void UploadInvoice() {
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("Uploaded Invoices");
        try {
            if (uriImage != null && authProfile != null && storageReference != null) {
                // Save the image with uid of the currently logged user
                StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "/invoice.jpg");

                Log.d(TAG, "URI Image: " + uriImage);

                // Upload image to Storage
                fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AdminConsumers.this, "Upload Successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AdminConsumers.this, AdminConsumers.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminConsumers.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminConsumers.this, "No File Selected or null references", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AdminConsumers.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    } */

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                uriImage = data.getData();
                imageViewUploadInvoice.setImageURI(uriImage);

                // Remove imageViewInvoice initially
                imageViewInvoice.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception or log it to help diagnose the issue.
        }
    }
    /* private void deleteConsumer(String accountNumberToDelete) {
        try {
            // Find the consumer with the matching account number
            Consumer selectedConsumer = findConsumerByAccountNumber(accountNumberToDelete);

            if (selectedConsumer == null) {
                Toast.makeText(this, "Consumer not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the UID of the consumer you want to delete
            String consumerUid = selectedConsumer.getUserUid();

            // Check if consumerUid is empty or null
            if (consumerUid == null || consumerUid.isEmpty()) {
                Toast.makeText(this, "Consumer UID is empty or null.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Delete the consumer's data from the Realtime Database
            deleteConsumerDataFromDatabase(consumerUid);

            // Delete the user from Firebase Authentication using Admin SDK
            FirebaseAuth.getInstance().deleteUser(consumerUid)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User deleted successfully from Firebase Authentication");
                                Toast.makeText(AdminConsumers.this, "Consumer authentication deleted successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminConsumers.this, "Failed to delete consumer authentication.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Delete from Firebase Authentication failed: " + task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred while deleting the consumer data.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception during consumer deletion: " + e.getMessage());
        }
    }


    private void deleteConsumerDataFromDatabase(String consumerUid) {
        // Construct a database reference to the consumer's data
        DatabaseReference consumerRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(consumerUid);

        // Delete the consumer's data from the Realtime Database
        consumerRef.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Consumer data deleted successfully");
                            Toast.makeText(AdminConsumers.this, "Consumer data deleted successfully.", Toast.LENGTH_SHORT).show();
                            // Optionally, clear the UI fields after successful deletion
                            clearConsumerInfo();
                        } else {
                            Toast.makeText(AdminConsumers.this, "Failed to delete consumer data.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Delete failed: " + task.getException());
                        }
                    }
                });
    } */

    private Consumer findConsumerByAccountNumber(String accountNumberToDelete) {
        for (Consumer consumer : consumerList) {
            if (consumer.getAccountNumber().equals(accountNumberToDelete)) {
                return consumer;
            }
        }
        return null;
    }


    private void clearConsumerInfo() {
        // Clear the text fields and set default values
        textViewAccountNumber.setText("");
        TextInputEditTextFirstName.setText("");
        TextInputEditTextLastName.setText("");
        TextInputEditTextBill.setText("");
        TextInputEditTextAddress.setText("");
        TextInputEditTextContactNumber.setText("");
        TextInputEditTextUsername.setText("");
        textViewAccountNumber.setText("");

        // Clear the profile picture image view
        ImageView profilePictureImageView = findViewById(R.id.imageViewProfileDp);
        profilePictureImageView.setImageResource(R.drawable.no_profile_pic);

        // Clear the invoice image view
        ImageView imageViewInvoice = findViewById(R.id.imageViewUploadedInvoice);
        imageViewInvoice.setImageResource(R.drawable.no_receipt);
    }

    private void checkUserRoleForAccess() {
        DatabaseReference userRolesRef = FirebaseDatabase.getInstance().getReference("Registered Users")
                .child(getCurrentUserUid())
                .child("role");

        String currentUserUid = getCurrentUserUid();
        Log.d(TAG, "Current User UID: " + currentUserUid);

        userRolesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.getValue(String.class);

                    // Log the user's role
                    Log.d(TAG, "User Role: " + role);

                    if ("admin".equals(role)) {
                        // User has the "storeOwner" role, allow access
                        setupSearch();
                    } else {
                        // User does not have the "storeOwner" role, show an error message
                        Toast.makeText(AdminConsumers.this, "Admin can only access this feature.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // User role data does not exist, handle it as needed
                    Toast.makeText(AdminConsumers.this, "User role not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(AdminConsumers.this, "Error checking user role.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error checking user role: " + databaseError.getMessage());
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

    private void setupSearch() {
        // Create an ArrayAdapter with the suggestions
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextViewSearch = findViewById(R.id.autoCompleteTextViewSearch);
        autoCompleteTextViewSearch.setAdapter(searchAdapter);
        autoCompleteTextViewSearch.requestFocus();

        // Initialize an ImageView where you want to display the consumer's image
        ImageView consumerImageView = findViewById(R.id.imageViewProfileDp);
        autoCompleteTextViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedConsumerName = parent.getItemAtPosition(position).toString();

                // Find the corresponding Consumer object in consumerList
                AdminConsumers.Consumer selectedConsumer = null;
                for (AdminConsumers.Consumer consumer : consumerList) {
                    if (consumer.getAccountNumber().equals(selectedConsumerName)) {
                        selectedConsumer = consumer;
                        break; // Exit the loop once the consumer is found
                    }
                }
                // Check if the selected consumer is not null
                if (selectedConsumer != null) {
                    populateConsumerData(selectedConsumer);
                    displayConsumerImage(selectedConsumer.getUserUid(), selectedConsumer.getAccountNumber(), consumerImageView);
                } else {
                    Log.e(TAG, "Selected consumer is null.");
                }
            }
        });

        if (firebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange called");
                    consumerList.clear();
                    searchAdapter.clear();

                    for (DataSnapshot consumerSnapshot : dataSnapshot.getChildren()) {
                        // String profilePictureUrl = consumerSnapshot.child("profilePictureUrl").getValue(String.class);
                        String consumerKey = consumerSnapshot.getKey(); // Get the key of the consumer
                        DataSnapshot roleSnapshot = consumerSnapshot.child("role");
                        DataSnapshot firstNameSnapshot = consumerSnapshot.child("firstName");
                        DataSnapshot lastNameSnapshot = consumerSnapshot.child("lastName");
                        DataSnapshot bilLSnapshot = consumerSnapshot.child("bill");
                        DataSnapshot addressSnapshot = consumerSnapshot.child("address");
                        DataSnapshot contactNumberSnapshot = consumerSnapshot.child("contactNumber");
                        DataSnapshot usernameSnapshot = consumerSnapshot.child("username");
                        DataSnapshot accountNumberSnapshot = consumerSnapshot.child("accountNumber");

                        if (firstNameSnapshot.exists() && lastNameSnapshot.exists() && bilLSnapshot.exists() &&
                                addressSnapshot.exists() &&
                                contactNumberSnapshot.exists() && usernameSnapshot.exists() && accountNumberSnapshot.exists()
                                && roleSnapshot.exists()) {

                            String firstName = firstNameSnapshot.getValue(String.class);
                            String lastName = lastNameSnapshot.getValue(String.class);
                            String bill = bilLSnapshot.getValue(String.class);
                            String address = addressSnapshot.getValue(String.class);
                            String contactNumber = contactNumberSnapshot.getValue(String.class);
                            String username = usernameSnapshot.getValue(String.class);
                            String accountNumber = accountNumberSnapshot.getValue(String.class);
                            String role = roleSnapshot.getValue(String.class);

                            // Log the values for debugging
                            /* Log.d(TAG, "firstName: " + firstName);
                            Log.d(TAG, "lastName: " + lastName);
                            Log.d(TAG, "bill: " + bill);
                            Log.d(TAG, "address: " + address);
                            Log.d(TAG, "contactNumber: " + contactNumber);
                            Log.d(TAG, "username: " + username);
                            Log.d(TAG, "accountNumber: " + accountNumber);
                            Log.d(TAG, "role: " + role); */

                            // Create a new Consumer object and add it to the list
                            AdminConsumers.Consumer consumer = new AdminConsumers.Consumer();
                            consumer.setFirstName(firstName);
                            consumer.setLastName(lastName);
                            consumer.setBill(Double.parseDouble(bill));
                            consumer.setAddress(address);
                            consumer.setContactNumber(contactNumber);
                            consumer.setUsername(username);
                            consumer.setAccountNumber(accountNumber);
                            consumer.setRole(role);

                            // Set the user UID
                            consumer.setUserUid(consumerKey); // Use the consumer's UID as the user UID
                            // consumer.setProfilePictureUrl(profilePictureUrl); // Set the profile picture URL

                            consumerList.add(consumer);
                        } else {
                            Log.e(TAG, "Incomplete data for a consumer.");
                        }
                    }

                    // After adding consumers to the consumerList, update the searchAdapter and notify the AutoCompleteTextView.
                    for (AdminConsumers.Consumer consumer : consumerList) {
                        String accountNumber = consumer.getAccountNumber();
                        searchAdapter.add(consumer.getAccountNumber());
                        Log.d(TAG, "Added accountNumber: " + accountNumber);
                    }
                    searchAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
    private String getCurrentUserUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "";
        }
    }

    private Consumer findConsumerByFirstName(String firstName) {
        for (Consumer consumer : consumerList) {
            if (consumer.getFirstName().equals(firstName)) {
                return consumer;
            }
        }
        return null;
    }

    private void populateConsumerData(Consumer selectedConsumer) {
        double billValue = selectedConsumer.getBill();
        String billString = String.valueOf(billValue);

        TextInputEditTextFirstName.setText(selectedConsumer.getFirstName());
        TextInputEditTextLastName.setText(selectedConsumer.getLastName());
        TextInputEditTextBill.setText(billString);
        TextInputEditTextAddress.setText(selectedConsumer.getAddress());
        TextInputEditTextContactNumber.setText(selectedConsumer.getContactNumber());
        TextInputEditTextUsername.setText(selectedConsumer.getUsername());
        textViewAccountNumber.setText(selectedConsumer.getAccountNumber());

        // Display the invoice image for the selected consumer
        ImageView imageViewInvoice = findViewById(R.id.imageViewInvoice); // Replace with your ImageView ID
        displayInvoiceImage(selectedConsumer.getUserUid(), imageViewInvoice);
    }

    private void displayConsumerImage(String userUid, String accountNumber, ImageView imageView) {
        // Reference to the product image in Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("DisplayPictures")
                .child(userUid)
                .child("profilepicture.jpg"); // Assuming images are in JPG format

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the image using Picasso
                Picasso.with(AdminConsumers.this)
                        .load(uri)
                        .placeholder(R.drawable.no_profile_pic)
                        .error(R.drawable.no_profile_pic)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure to download and display the image
                imageView.setImageResource(R.drawable.no_profile_pic);
                Log.e(TAG, "Error loading product image: " + e.getMessage());
            }
        });
    }

    private void displayInvoiceImage(String userUid, ImageView imageView) {
        // Reference to the invoice image in Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("Uploaded Invoices")
                .child(userUid)
                .child("invoice.jpg"); // Assuming invoice images are in JPG format

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the invoice image using Picasso or any other image loading library
                Picasso.with(AdminConsumers.this)
                        .load(uri)
                        .placeholder(R.drawable.no_receipt) // Placeholder image while loading
                        .error(R.drawable.no_receipt) // Image to display in case of error
                        .into(imageViewUploadInvoice);

                imageViewInvoice.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure to download and display the invoice image
                imageViewInvoice.setImageResource(R.drawable.no_receipt); // Default image for no invoice
                // Set the visibility of the default image to GONE when invoice image is loaded
                imageViewInvoice.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error loading invoice image: " + e.getMessage());
            }
        });
    }

    private void updateConsumer() {
        try {
            Log.d(TAG, "Starting updateConsumer");

            // Initialize Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            // Get a reference to the root of your Firebase Storage
            storageReference = storage.getReference();
            // Get the updated values from the UI elements
            String updatedFirstName = TextInputEditTextFirstName.getText().toString().trim();
            String updatedLastName = TextInputEditTextLastName.getText().toString().trim();
            String updatedBill = TextInputEditTextBill.getText().toString().trim();
            String updatedAddress = TextInputEditTextAddress.getText().toString().trim();
            String updatedContactNumber = TextInputEditTextContactNumber.getText().toString().trim();
            String updatedUsername = TextInputEditTextUsername.getText().toString().trim();

            // Check if any of the fields are empty
            if (TextUtils.isEmpty(updatedFirstName) || TextUtils.isEmpty(updatedLastName) ||
                    TextUtils.isEmpty(updatedBill) && TextUtils.isEmpty(updatedAddress) || TextUtils.isEmpty(updatedContactNumber) ||
                    TextUtils.isEmpty(updatedUsername)) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if it's been long enough since the last bill update for this consumer
            Consumer selectedConsumer = findConsumerByFirstName(updatedFirstName);
            /* SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String consumerKey = selectedConsumer.getUserUid(); // Use a unique identifier for the consumer

            long lastUpdateTimeMillis = sharedPreferences.getLong(consumerKey, 0);
            long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis - lastUpdateTimeMillis < MIN_TIME_INTERVAL) {
                // Calculate the remaining time in hours and minutes
                long timeDifferenceMillis = MIN_TIME_INTERVAL - (currentTimeMillis - lastUpdateTimeMillis);
                long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis - TimeUnit.HOURS.toMillis(hours));

                // Build the toast message with the remaining time
                String toastMessage = "You can update the consumer's data again after " + hours + " hrs and " + minutes + " mins";

                Log.d(TAG, "Time restriction not met for " + consumerKey);
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
                return;
            }


            // After a successful update, update the last update time for this consumer
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(consumerKey, currentTimeMillis);
            editor.apply(); */

            // Check if an image was selected
            if (uriImage != null) {
                // Generate a unique filename using a timestamp
            /* SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String fileName = "invoice_" + timestamp + ".jpg"; */

                String selectedConsumerUid = selectedConsumer.getUserUid();

                // Upload the new image to Firebase Storage
                StorageReference fileReference = storageReference.child("Uploaded Invoices")
                        .child(selectedConsumerUid)
                        .child("invoice.jpg");
                fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the new image URL after successful upload
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri imageUrl) {
                                // Update consumer data with the new image URL
                                updateConsumerData(updatedFirstName, updatedLastName, updatedBill, updatedAddress, updatedContactNumber, updatedUsername, imageUrl.toString());

                                // After a successful update, update the last update time for this consumer
                                /* SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong(consumerKey + "_lastUpdateTimeMillis", currentTimeMillis);
                                editor.apply(); */
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminConsumers.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Hide the progress bar since the update process is complete
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } else {
                // No image selected, update consumer data without modifying the image URL
                updateConsumerData(updatedFirstName, updatedLastName, updatedBill, updatedAddress, updatedContactNumber, updatedUsername, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "An error occurred while updating the consumer: " + e.getMessage());
            Toast.makeText(this, "You cannot change the consumer's first name", Toast.LENGTH_SHORT).show();
        }
    }



    private void updateConsumerData(String updatedFirstName, String updatedLastName, String updatedBill, String updatedAddress, String updatedContactNumber, String updatedUsername, String imageUrl) {

        try {
            Consumer selectedConsumer = findConsumerByFirstName(updatedFirstName);

            if (selectedConsumer == null) {
                Toast.makeText(this, "You cannot change their first name!", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedConsumerUid = selectedConsumer.getUserUid();

            if (selectedConsumerUid == null) {
                Toast.makeText(this, "Selected consumer UID is null.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Debugging: Log values to check if they are correct
            Log.d(TAG, "Selected Consumer UID: " + selectedConsumerUid);
            Log.d(TAG, "Updated First Name: " + updatedFirstName);
            Log.d(TAG, "Updated Last Name: " + updatedLastName);
            Log.d(TAG, "Updated Bill: " + updatedBill);
            Log.d(TAG, "Updated Address: " + updatedAddress);
            Log.d(TAG, "Updated Contact Number: " + updatedContactNumber);
            Log.d(TAG, "Updated Username: " + updatedUsername);

            // Get current timestamp in the desired format
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
            String formattedTimestamp = sdf.format(new Date());

            // Extract month and year from the timestamp
            String[] parts = formattedTimestamp.split(" ");
            String[] dateParts = parts[0].split("/");
            String monthYear = dateParts[0] + "_" + dateParts[2]; // Format: MM_YYYY

            // Update the consumer's details in Firebase
            DatabaseReference consumerRef = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(selectedConsumerUid);

            Map<String, Object> updatedValues = new HashMap<>();
            updatedValues.put("firstName", updatedFirstName);
            updatedValues.put("lastName", updatedLastName);
            updatedValues.put("bill", updatedBill);
            updatedValues.put("address", updatedAddress);
            updatedValues.put("contactNumber", updatedContactNumber);
            updatedValues.put("username", updatedUsername);
            updatedValues.put("timestamp", formattedTimestamp);


            if (imageUrl != null) {
                updatedValues.put("imageUrl", imageUrl); // Update the image URL if an image was uploaded
            }

            consumerRef.updateChildren(updatedValues)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // If the update under "Registered Users" node is successful, also add to "BillHistory" node
                                DatabaseReference billHistoryRef = FirebaseDatabase.getInstance().getReference("BillHistory")
                                        .child(selectedConsumerUid)
                                        .child(monthYear);

                                Map<String, Object> billHistoryValues = new HashMap<>();
                                billHistoryValues.put("firstName", updatedFirstName);
                                billHistoryValues.put("lastName", updatedLastName);
                                billHistoryValues.put("timestamp", formattedTimestamp);
                                billHistoryValues.put("bill", updatedBill);

                                billHistoryRef.setValue(billHistoryValues);

                                progressBar.setVisibility(View.VISIBLE);
                                Toast.makeText(AdminConsumers.this, "Consumer details updated successfully.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AdminConsumers.this, AdminInterface.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Handle the specific error and log it
                                Exception exception = task.getException();
                                if (exception != null) {
                                    Log.e(TAG, "Update failed: " + exception.getMessage());
                                    Toast.makeText(AdminConsumers.this, "Failed to update consumer details: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Update failed with an unknown error.");
                                    Toast.makeText(AdminConsumers.this, "Failed to update consumer details due to an unknown error.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred while updating the consumer.", Toast.LENGTH_SHORT).show();
        }
    }

    private static class Consumer {

        private String userUid;
        private String accountNumber;
        private String firstName;
        private String lastName;
        private double bill;
        private String email;
        private String address;
        private String contactNumber;
        private String username;
        private String role;
        private String profilePictureUrl;


        public Consumer() {
            this.accountNumber = "";
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getProfilePictureUrl() {
            return profilePictureUrl;
        }

        public void setProfilePictureUrl(String profilePictureUrl) {
            this.profilePictureUrl = profilePictureUrl;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public double getBill() {
            return bill;
        }

        public void setBill(double bill) {
            this.bill = bill;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getContactNumber() {
            return contactNumber;
        }

        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
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
                                    Intent adminIntent = new Intent(AdminConsumers.this, AdminInterface.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(AdminConsumers.this, ConsumerInterface.class);
                                    consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(consumerIntent);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(AdminConsumers.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(AdminConsumers.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}

