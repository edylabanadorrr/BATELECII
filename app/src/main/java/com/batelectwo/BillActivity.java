package com.batelectwo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class BillActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private TextView textViewBill;
    private String bill;
    private ProgressBar progressBar;
    private ImageView imageViewUploadScreenshot;
    private FirebaseUser firebaseUser;
    private static final String TAG = "BillActivity";
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;
    private Bitmap invoiceBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Bill");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the current date and time
        Date currentDate = new Date();

        // Compare the current date with the deadline (assuming deadline is a Date object)
        Date deadline = getDeadline(); // You need to implement the getDeadline() method
        if (deadline != null && currentDate.after(deadline)) {
            showAlertDialog();

            TextView textViewOverdue = findViewById(R.id.textViewOverdue);
            textViewOverdue.setVisibility(View.VISIBLE);
            textViewOverdue.setTextColor(getResources().getColor(R.color.maroon));

            // If the current date is after the deadline, disable access to the QR code and the "Pay Here" button
            ImageView qrCode = findViewById(R.id.qrCodePayment);
            qrCode.setVisibility(View.GONE);

            TextView textViewScanMe = findViewById(R.id.textViewScanMe);
            textViewScanMe.setVisibility(View.GONE);

            TextView textViewOr = findViewById(R.id.textViewOr);
            textViewOr.setVisibility(View.GONE);

            ImageView paymentIcon = findViewById(R.id.paymentIcon);
            paymentIcon.setClickable(false);

            TextView textViewPayment = findViewById(R.id.textViewPayHere);
            textViewPayment.setClickable(false);
            textViewPayment.setTextColor(getResources().getColor(R.color.maroon));

            TextView textViewScreenshot = findViewById(R.id.textViewScreenshot);
            textViewScreenshot.setText("You are overdue therefore, cannot upload screenshot of your payment.");

            // Disable the "Choose Screenshot" and "Upload Screenshot" buttons
            Button chooseScreenshot = findViewById(R.id.chooseScreenshotButton);
            Button uploadScreenshot = findViewById(R.id.uploadScreenshotButton);
            chooseScreenshot.setClickable(false);
            uploadScreenshot.setClickable(false);
            chooseScreenshot.setEnabled(false);
            uploadScreenshot.setEnabled(false);
        } else {
            ImageView qrCode = findViewById(R.id.qrCodePayment);
            qrCode.setVisibility(View.VISIBLE);

            TextView textViewScanMe = findViewById(R.id.textViewScanMe);
            textViewScanMe.setVisibility(View.VISIBLE);

            TextView textViewOr = findViewById(R.id.textViewOr);
            textViewOr.setVisibility(View.VISIBLE);

            TextView textViewPayment = findViewById(R.id.textViewPayHere);
            textViewPayment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle the click event, e.g., open a web browser with the URL
                    String url = "https://www.justpay.to/batelec";
                    Toast.makeText(BillActivity.this, "You are redirecting to BATELEC II's online payment", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });

            ImageView imageViewPayment = findViewById(R.id.paymentIcon);
            imageViewPayment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle the click event, e.g., open a web browser with the URL
                    String url = "https://www.justpay.to/batelec";
                    Toast.makeText(BillActivity.this, "You are redirecting to BATELEC II's online payment", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });

            // Enable the "Choose Screenshot" and "Upload Screenshot" buttons
            Button chooseScreenshot = findViewById(R.id.chooseScreenshotButton);
            Button uploadScreenshot = findViewById(R.id.uploadScreenshotButton);
            chooseScreenshot.setClickable(true);
            uploadScreenshot.setClickable(true);
            chooseScreenshot.setEnabled(true);
            uploadScreenshot.setEnabled(true);
        }

        ImageView imageViewInvoice = findViewById(R.id.imageViewForReceipt);
        imageViewInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the alert dialog layout
                View dialogView = LayoutInflater.from(BillActivity.this).inflate(R.layout.invoice_alert, null);

                // Find views in the dialog layout
                ImageView imageViewInvoice = dialogView.findViewById(R.id.imageViewInvoice);
                Button printButton = dialogView.findViewById(R.id.printInvoiceButton);
                Button cancelButton = dialogView.findViewById(R.id.cancelInvoiceButton);

                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();

                String userUid = user.getUid();
                // Define a StorageReference to the image in Firebase Storage
                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReference("Uploaded Invoices").child(userUid).child("invoice.jpg");

                // Get the download URL for the image
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Load the image using Picasso
                        Picasso.with(BillActivity.this)
                                .load(uri)
                                .placeholder(R.drawable.no_receipt)  // Placeholder image while loading
                                .error(R.drawable.no_receipt)  // Image to display in case of error
                                .into(imageViewInvoice, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "Image loaded successfully");
                                    }

                                    @Override
                                    public void onError() {
                                        Log.e(TAG, "Error loading image");
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to get download URL for the image: " + e.getMessage());
                    }
                });


                // Create the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(BillActivity.this);
                builder.setView(dialogView);

                // Create and show the dialog
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // Handle print button click
                printButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check if the invoiceBitmap is not null
                        if (invoiceBitmap != null) {
                            // Print the QR code
                            printInvoice(invoiceBitmap);

                            // Dismiss the dialog
                            alertDialog.dismiss();
                        } else {
                            // Handle the case where the invoiceBitmap is null
                            Toast.makeText(BillActivity.this, "Invoice image not available", Toast.LENGTH_SHORT).show();
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

            private void printInvoice(Bitmap bitmap) {
                // Check if printing is supported
                if (!PrintHelper.systemSupportsPrint()) {
                    Toast.makeText(BillActivity.this, "Printing is not supported on this device", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a PrintHelper instance
                PrintHelper printHelper = new PrintHelper(BillActivity.this);
                printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);

                // Print the QR code
                printHelper.printBitmap("My Invoice", bitmap);
            }
        });

        textViewBill = findViewById(R.id.myBill);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(BillActivity.this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_SHORT).show();
        } else {
            showBill(firebaseUser);
        }

        // Upload Screenshot

        Button chooseScreenshot = findViewById(R.id.chooseScreenshotButton);
        Button uploadScreenshot = findViewById(R.id.uploadScreenshotButton);
        progressBar = findViewById(R.id.progressBarShow);
        imageViewUploadScreenshot = findViewById(R.id.imageViewScreenshot);

        firebaseUser = authProfile.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Uploaded Proof of Payment");

        chooseScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Upload Image
        uploadScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                UploadPicture();
            }
        });

        // Consumption Graph

        Button consumptionGraphButton = findViewById(R.id.consumptionGraph);
        consumptionGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inflate the alert dialog layout
                View dialogViewConsumption = LayoutInflater.from(BillActivity.this).inflate(R.layout.monthly_consumption_alert, null);

                // Find the chart view
                LineChartView chart = dialogViewConsumption.findViewById(R.id.chart);

                // Create the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(BillActivity.this);
                builder.setView(dialogViewConsumption);

                // Create and show the dialog
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // Prepare data for the chart
                List<Line> lines = new ArrayList<>();
                List<AxisValue> axisValues = new ArrayList<>();
                List<PointValue> values = new ArrayList<>();

                // Retrieve data from your "Consumption" node
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = auth.getCurrentUser();

                String userId = firebaseUser.getUid();
                DatabaseReference consumptionRef = FirebaseDatabase.getInstance().getReference("Consumption").child(userId);

                consumptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot monthSnapshot : dataSnapshot.getChildren()) {
                                String monthYear = monthSnapshot.getKey(); // Assuming the key is the monthYear format

                                // Check if "totalConsumption" exists under each month
                                if (monthSnapshot.hasChild("totalConsumption")) {
                                    Float totalConsumption = monthSnapshot.child("totalConsumption").getValue(Float.class);

                                    // Check if the "totalConsumption" value is not null and not empty before further processing
                                    if (totalConsumption != null) {
                                        Log.d("BillActivity", "Month: " + monthYear + ", Total Consumption: " + totalConsumption);
                                        // Add data points to the line chart
                                        values.add(new PointValue(values.size(), totalConsumption));

                                        // Add a single axis value for each month
                                        axisValues.add(new AxisValue(axisValues.size()).setLabel(monthYear));
                                    }
                                }
                            }

                            // Customize the chart data and appearance
                            Line line = new Line(values)
                                    .setColor(Color.RED)
                                    .setStrokeWidth(3)
                                    .setPointRadius(6)
                                    .setHasLabels(true)
                                    .setHasPoints(true)
                                    .setCubic(false);

                            lines.add(line);

                            LineChartData data = new LineChartData();
                            data.setLines(lines);

                            // Define the X-axis labels
                            Axis axisX = new Axis();
                            axisX.setTextSize(12);
                            axisX.setValues(axisValues);

                            Axis axisY = new Axis().setHasLines(true);
                            data.setAxisXBottom(axisX);
                            data.setAxisYLeft(axisY);

                            chart.setLineChartData(data);


                            // Customize the viewport and other settings
                            Viewport v = new Viewport(chart.getMaximumViewport());
                            v.top = 1000; // Set the maximum Y value
                            v.bottom = 0; // Set the minimum Y value
                            chart.setMaximumViewport(v);
                            chart.setCurrentViewport(v);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });
            }
        });

        Button bilLHistoryButton = findViewById(R.id.billHistory);
        bilLHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inflate the alert dialog layout
                View dialogViewBillHistory = LayoutInflater.from(BillActivity.this).inflate(R.layout.bill_history_alert, null);

                // Find the chart view
                ColumnChartView billHistoryGraph = dialogViewBillHistory.findViewById(R.id.barGraph);

                // Prepare data for the chart
                List<Column> columns = new ArrayList<>();
                List<AxisValue> axisValues = new ArrayList<>();

                // Retrieve data from your "BillHistory" node (replace "user1" with the actual user ID)
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = auth.getCurrentUser();

                String userId = firebaseUser.getUid();
                DatabaseReference billHistoryRef = FirebaseDatabase.getInstance().getReference("BillHistory").child(userId);

                billHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot monthSnapshot : dataSnapshot.getChildren()) {
                                String monthYear = monthSnapshot.getKey(); // Assuming the key is the monthYear format

                                // Check if "bill" exists under each month
                                if (monthSnapshot.hasChild("bill")) {
                                    String billAmountStr = monthSnapshot.child("bill").getValue(String.class);

                                    // Check if the "bill" value is not null and not empty before further processing
                                    if (billAmountStr != null && !billAmountStr.isEmpty()) {
                                        // Convert the billAmountStr to a Float if needed
                                        Float billAmount = Float.valueOf(billAmountStr);

                                        List<SubcolumnValue> values = new ArrayList<>();

                                        // Set color based on value
                                        if (billAmount <= 500) {
                                            values.add(new SubcolumnValue(billAmount, Color.BLUE));
                                        } else if (billAmount >= 501) {
                                            values.add(new SubcolumnValue(billAmount, Color.RED));
                                        }

                                        Column column = new Column(values);
                                        columns.add(column);

                                        // Add a single axis value for each month
                                        axisValues.add(new AxisValue(axisValues.size()).setLabel(monthYear));
                                    } else {
                                        // Handle the case where "bill" is null or empty, if needed
                                        Log.e("BillActivity", "Bill amount is null or empty for month: " + monthYear);
                                    }
                                }
                            }

                            // Create the data object and set it to the chart
                            ColumnChartData data = new ColumnChartData(columns);

                            // Customize the X-axis labels
                            Axis axisX = new Axis();
                            axisX.setValues(axisValues);
                            data.setAxisXBottom(axisX);

                            // Customize the Y-axis labels
                            Axis axisY = new Axis().setHasLines(true);

                            // Set custom values for Y-axis labels
                            List<AxisValue> yValues = new ArrayList<>();
                            yValues.add(new AxisValue(0).setLabel("0"));
                            yValues.add(new AxisValue(200).setLabel("200"));
                            yValues.add(new AxisValue(400).setLabel("400"));
                            yValues.add(new AxisValue(600).setLabel("600"));
                            yValues.add(new AxisValue(800).setLabel("800"));
                            yValues.add(new AxisValue(1000).setLabel("1000"));

                            axisY.setValues(yValues);
                            data.setAxisYLeft(axisY);

                            // Customize data values inside columns
                            for (Column column : data.getColumns()) {
                                column.setHasLabels(true);
                            }

                            billHistoryGraph.setColumnChartData(data);

                            // Create the alert dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(BillActivity.this);
                            builder.setView(dialogViewBillHistory);

                            // Create and show the dialog
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });
            }
        });

                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                bottomNavigationView.setSelectedItemId(R.id.bill_bottom);

                bottomNavigationView.setOnItemSelectedListener(item -> {
                    int itemId = item.getItemId();

                    if (itemId == R.id.home_bottom) {
                        Intent intent = new Intent(BillActivity.this, ConsumerInterface.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                        return true;
                    } else if (itemId == R.id.bill_bottom) {
                        return true;
                    } else if (itemId == R.id.videos_bottom) {
                        Intent intent = new Intent(BillActivity.this, VideoActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                        return true;
                    } else if (itemId == R.id.profile_bottom) {
                        Intent intent = new Intent(BillActivity.this, UserProfile.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                        return true;
                    }
                    return false;
                });
            }

    // Helper function to get the maximum consumption for setting the Y-axis
    private float getMaxConsumption(float totalConsumption) {
        // You can customize this function based on your requirements
        return totalConsumption + 10; // Adding 10 for some padding
    }

    // Creating Action Bar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu items
        getMenuInflater().inflate(R.menu.user_other_accounts_bill, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Method to generate random bill amounts for demonstration purposes
    private float getRandomBillAmount() {
        return (float) (Math.random() * 1000); // Adjust the range as needed
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BillActivity.this);
        builder.setTitle(Html.fromHtml("<font color='#FF5733'>Bill Notice</font>"));
        builder.setMessage("You cannot use our online payment website since you're overdue or still have payment to pay. Please directly go to any BATELEC II offices near you to pay.\n \nIf you think this is an error on your side, please ignore.");

        // Open Email Apps if user clicks / taps Continue button
        builder.setPositiveButton("Continue", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog
        alertDialog.show();
    }

    private Date getDeadline() {
        // You need to implement how to get the deadline date
        // from your database or any other source.
        // Example code to return a hardcoded date:
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            return sdf.parse("2024-01-05"); // Change this to your actual deadline date
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Return null if parsing fails
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imageViewUploadScreenshot.setImageURI(uriImage);
        }

    }
    private void UploadPicture() {
        if (uriImage != null) {
            // Get the current date and time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            // Create a filename based on the current date and time
            String fileName = "payment_" + timestamp + ".jpg";

            // Create a reference to the unique file
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid()).child(fileName);

            // Upload image to Storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(BillActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(BillActivity.this, BillActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BillActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(BillActivity.this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void showBill(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        // Extracting User Reference from Database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    bill = readUserDetails.bill;

                    textViewBill.setText(bill);

                    // Load and display the invoice image
                    displayInvoiceImage(userID);
                } else {
                    Toast.makeText(BillActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){
                Toast.makeText(BillActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to load and display the invoice image
    private void displayInvoiceImage(String userUid) {
        // Assuming you have a reference to your ImageView
        ImageView imageViewForReceipt = findViewById(R.id.imageViewForReceipt);
        // Reference to the invoice image in Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("Uploaded Invoices")
                .child(userUid)
                .child("invoice.jpg"); // Assuming invoice images are in JPG format

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the invoice image using Picasso or any other image loading library
                Picasso.with(BillActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.no_receipt) // Placeholder image while loading
                        .error(R.drawable.no_receipt) // Image to display in case of error
                        .into(imageViewForReceipt);

                // Assign the loaded bitmap to the invoiceBitmap variable
                Picasso.with(BillActivity.this).load(uri).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        invoiceBitmap = bitmap;
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        // Handle bitmap load failure if needed
                    }
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // Prepare for bitmap load if needed
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure to download and display the invoice image
                imageViewForReceipt.setImageResource(R.drawable.no_receipt); // Default image for no invoice
                Log.e(TAG, "Error loading invoice image: " + e.getMessage());
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
                                    Intent adminIntent = new Intent(BillActivity.this, AdminInterface.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(BillActivity.this, ConsumerInterface.class);
                                    consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(consumerIntent);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(BillActivity.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
        } else if (id == R.id.menuOtherAccounts) {
            Intent intent = new Intent(BillActivity.this, UserOtherAccounts.class);
            startActivity(intent);
            return true;
        } else {
            Toast.makeText(BillActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}