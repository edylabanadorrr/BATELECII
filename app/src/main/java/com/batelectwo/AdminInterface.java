package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminInterface extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;
    private AutoCompleteTextView autoCompleteTextViewSearch;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_interface);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Billing Section Personnel Interface");

        swipeToRefresh();

        VideoView videoClearingDay = findViewById(R.id.national_clearing_day);
        String videoPathClearingDay = "android.resource://" + getPackageName() + "/" + R.raw.national_clearing_day_video;
        Uri uriClearing = Uri.parse(videoPathClearingDay);
        videoClearingDay.setVideoURI(uriClearing);

        MediaController mediaControllerClearing = new MediaController(this);
        videoClearingDay.setMediaController(mediaControllerClearing);

        videoClearingDay.start();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuTips: {
                        Intent intent = new Intent(AdminInterface.this, UserTips.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuAntiElectricity: {
                        Intent intent = new Intent(AdminInterface.this, UserAntiElectricity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuInstallationRequirements: {
                        Intent intent = new Intent(AdminInterface.this, UserInstallationRequirements.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuRates: {
                        Intent intent = new Intent(AdminInterface.this, UserRates.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuAccreditedPaymentCenters: {
                        Intent intent = new Intent(AdminInterface.this, UserPaymentCenters.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuContact: {
                        Intent intent = new Intent(AdminInterface.this, UserContact.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuMainOffice: {
                        Intent intent = new Intent(AdminInterface.this, UserMainOffice.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuBranches: {
                        Intent intent = new Intent(AdminInterface.this, UserBranches.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuUnitOffices: {
                        Intent intent = new Intent(AdminInterface.this, UserUnitOffices.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuRecruitmentProcess: {
                        Intent intent = new Intent(AdminInterface.this, UserRecruitmentProcess.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuPrecautions: {
                        Intent intent = new Intent(AdminInterface.this, UserPrecautions.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuTools: {
                        Intent intent = new Intent(AdminInterface.this, UserTools.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuSettings: {
                        Intent intent = new Intent(AdminInterface.this, AdminSettings.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home_bottom);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home_bottom) {
                // Handle the Home menu item click here
                return true;
            } else if (itemId == R.id.consumers_bottom) {
                Intent intent = new Intent(AdminInterface.this, AdminConsumers.class);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.videos_bottom) {
                Intent intent = new Intent(AdminInterface.this, AdminVideos.class);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.send_email_bottom) {
                Intent intent = new Intent(AdminInterface.this, AdminSendEmail.class);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.profile_bottom) {
                // Handle the Profile menu item click here
                Intent intent = new Intent(AdminInterface.this, AdminProfile.class);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

        private void swipeToRefresh () {
            // Look for the swipe container
            swipeContainer = findViewById(R.id.swipeContainer);

            // Set up Refresh Listener which triggers data loading
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    startActivity(getIntent());
                    finish();
                    overridePendingTransition(0, 0);
                    swipeContainer.setRefreshing(false);
                }
            });

            // Configure refresh colors
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                    android.R.color.holo_orange_light, android.R.color.holo_red_light);
        }

        @Override
        public void onBackPressed () {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }