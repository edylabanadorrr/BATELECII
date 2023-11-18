package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserBillCalculator extends AppCompatActivity {

    private Spinner spinnerSelectAppliance, spinnerWattsKilowatts;
    private TextInputEditText editTextPowerConsumption;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_bill_calculator);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Bill Calculator");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Select Appliance Dropdown

        spinnerSelectAppliance = findViewById(R.id.applianceSpinner);
        editTextPowerConsumption = findViewById(R.id.editTextPowerConsumption);

        // Define the list of categories
        String[] categories = {"-- select --", "Air Conditioner", "Clothes Dryer", "Clothes Iron", "Dishwasher",
                "Electric Kettle", "Fan", "Heater", "Microwave Oven", "Desktop Computer", "Laptop Computer", "Refrigerator",
                "Stereo Receiver", "Television", "Toaster Oven", "Vacuum Cleaner", "Washing Machine", "Water Heater"};

        // Create an ArrayAdapter to populate the Spinner with the categories
        ArrayAdapter<String> adapterAppliance = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Set the dropdown layout style for the Spinner
        adapterAppliance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter on the Spinner
        spinnerSelectAppliance.setAdapter(adapterAppliance);

        // Set the OnItemSelectedListener for the Spinner
        spinnerSelectAppliance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item from the Spinner
                String selectedAppliance = (String) parentView.getItemAtPosition(position);

                // Update the power consumption EditText based on the selected appliance
                int powerConsumption = getPowerConsumptionForAppliance(selectedAppliance);
                editTextPowerConsumption.setText(String.valueOf(powerConsumption));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here if nothing is selected
            }
        });

        // Select Watts or Kilowatts

        spinnerWattsKilowatts = findViewById(R.id.wattsSpinner);

        // Define the list of categories
        String[] categoriesWatts = {"Watts (W)", "Kilowatts (KW)"};

        // Create an ArrayAdapter to populate the Spinner with the categories
        ArrayAdapter<String> adapterWatts = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesWatts);

        // Set the dropdown layout style for the Spinner
        adapterWatts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter on the Spinner
        spinnerWattsKilowatts.setAdapter(adapterWatts);

        // Calculate Button

        TextInputEditText editTextHours = findViewById(R.id.editTextHours);
        TextInputEditText electricityPerDay = findViewById(R.id.editTextElectricityPerDay);
        TextInputEditText electricityPerMonth = findViewById(R.id.editTextElectricityPerMonth);
        TextInputEditText electricityPerYear = findViewById(R.id.editTextElectricityPerYear);
        Button calculateBill = findViewById(R.id.calculateButton);
        calculateBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the selected appliance
                String selectedAppliance = (String) spinnerSelectAppliance.getSelectedItem();

                // Get the power consumption for the selected appliance
                int powerConsumption = getPowerConsumptionForAppliance(selectedAppliance);

                // Get the time of usage in hours from the user-inputted text
                String timeOfUsageText = editTextHours.getText().toString();
                if (!timeOfUsageText.isEmpty()) {
                    int timeOfUsageHours = Integer.parseInt(timeOfUsageText);

                    if (timeOfUsageHours >= 25) {
                        // Show an alert dialog for the error
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle(Html.fromHtml("<font color='#FF0000'>Error</font>"));
                        builder.setMessage("Please enter a valid time of usage");
                        builder.setPositiveButton("OK", null);
                        builder.show();
                        return; // Exit the onClick method to prevent further calculations
                    }

                    // Get the unit selected in the spinnerWattsKilowatts
                    String selectedUnit = (String) spinnerWattsKilowatts.getSelectedItem();

                    // Calculate the total energy consumption in kilowatt-hours or watts
                    double energyConsumptionKWh;
                    if (selectedUnit.equals("Watts (W)")) {
                        energyConsumptionKWh = (double) powerConsumption * timeOfUsageHours / 1000;
                    } else {
                        energyConsumptionKWh = (double) powerConsumption * timeOfUsageHours;
                    }

                    // Display the results in the respective EditText fields
                    /* electricityPerDay.setText(String.valueOf(energyConsumptionKWh));
                    electricityPerMonth.setText(String.valueOf(energyConsumptionKWh * 30));
                    electricityPerYear.setText(String.valueOf(energyConsumptionKWh * 365)); */

                    // Calculate the total electricity cost
                    double costPerDay = energyConsumptionKWh * 12;
                    double costPerMonth = costPerDay * 30; // Assuming an average month has 30 days
                    double costPerYear = costPerDay * 365; // Assuming a year has 365 days

                    // Display the results in the respective EditText fields
                    electricityPerDay.setText(String.valueOf(costPerDay));
                    electricityPerMonth.setText(String.valueOf(costPerMonth));
                    electricityPerYear.setText(String.valueOf(costPerYear));
                }  else {
                    // Show an alert dialog for the error
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(Html.fromHtml("<font color='#FF0000'>Error</font>"));
                    builder.setMessage("Please fill all the required fields");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                    return; // Exit the onClick method to prevent further calculations
                }
            }
        });

        // Reset Function

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset all input fields and result fields
                spinnerSelectAppliance.setSelection(0);
                spinnerWattsKilowatts.setSelection(0);
                editTextHours.setText("");
                editTextPowerConsumption.setText("");
                electricityPerDay.setText("");
                electricityPerMonth.setText("");
                electricityPerYear.setText("");
            }
        });
    }


    // A simple method to get the power consumption based on the selected appliance
    private int getPowerConsumptionForAppliance(String selectedAppliance) {
        // You should implement a logic to get the power consumption for each appliance
        // This is just a placeholder, replace it with your actual logic
        switch (selectedAppliance) {
            case "Air Conditioner":
                return 600;
            case "Clothes Dryer":
                return 3000;
            case "Clothes Iron":
                return 2400;
            case "Dishwasher":
                return 1600;
            case "Electric Kettle":
                return 2000;
            case "Fan":
                return 70;
            case "Heater":
                return 2000;
            case "Microwave Oven":
                return 800;
            case "Desktop Computer":
                return 100;
            case "Laptop Computer":
                return 50;
            case "Refrigerator":
                return 200;
            case "Stereo Receiver":
                return 200;
            case "Television":
                return 70;
            case "Toaster Oven":
                return 1000;
            case "Vacuum Cleaner":
                return 1600;
            case "Washing Machine":
                return 2000;
            case "Water Heater":
                return 4000;
            default:
                return 0; // Default power consumption if no specific case is matched
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
                                    Intent adminIntent = new Intent(UserBillCalculator.this, AdminInterface.class);
                                    startActivity(adminIntent);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(UserBillCalculator.this, BillActivity.class);
                                    startActivity(consumerIntent);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(UserBillCalculator.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
        }
        return super.onOptionsItemSelected(item);
    }
}