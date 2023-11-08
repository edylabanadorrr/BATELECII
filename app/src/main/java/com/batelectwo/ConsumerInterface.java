package com.batelectwo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ConsumerInterface extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consumer_interface);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Consumer Interface");

        swipeToRefresh();

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(ConsumerInterface.this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_SHORT).show();
        } else {
            checkifEmailVerified(firebaseUser);
        }

        // Fixed width for the custom media controller in pixels (e.g., 300dp equivalent in pixels)
        // int mediaControllerWidthPx = 300; // Replace with your desired width in pixels

        VideoView videoClearingDay = findViewById(R.id.national_clearing_day);
        /* VideoView videoCleanUp = findViewById(R.id.clean_up_video);
        VideoView videoRepublicAct = findViewById(R.id.republic_act_video);
        VideoView videoReels = findViewById(R.id.reels_video); */

        String videoPathClearingDay = "android.resource://" + getPackageName() + "/" + R.raw.national_clearing_day_video;
        /* String videoPathCleanUp = "android.resource://" + getPackageName() + "/" + R.raw.clean_up_video;
        String videoPathRepublicAct = "android.resource://" + getPackageName() + "/" + R.raw.republic_act;
        String videoPathReels = "android.resource://" + getPackageName() + "/" + R.raw.reels; */

        Uri uriClearing = Uri.parse(videoPathClearingDay);
        /* Uri uriCleanUp = Uri.parse(videoPathCleanUp);
        Uri uriRepublicAct = Uri.parse(videoPathRepublicAct);
        Uri uriReels = Uri.parse(videoPathReels); */

        videoClearingDay.setVideoURI(uriClearing);
        /* videoCleanUp.setVideoURI(uriCleanUp);
        videoRepublicAct.setVideoURI(uriRepublicAct);
        videoReels.setVideoURI(uriReels); */

        // Create custom media controllers
        MediaController mediaControllerClearing = new MediaController(this);
        /* MediaController mediaControllerCleanUp = new MediaController(this);
        MediaController mediaControllerRepublicAct = new MediaController(this);
        MediaController mediaControllerReels = new MediaController(this); */

        // Set the custom media controller layout
        /* View customMediaControllerClearing = LayoutInflater.from(this).inflate(R.layout.custom_media_controller, null);
        View customMediaControllerCleanUp = LayoutInflater.from(this).inflate(R.layout.custom_media_controller_cleanup, null);
        View customMediaControllerRepublicAct = LayoutInflater.from(this).inflate(R.layout.custom_media_controller_act, null);
        View customMediaControllerReels = LayoutInflater.from(this).inflate(R.layout.custom_media_controller_reels, null); */

        // Set the width of the custom media controllers
        /* ViewGroup.LayoutParams layoutParamsClearing = new ViewGroup.LayoutParams(
                mediaControllerWidthPx,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ViewGroup.LayoutParams layoutParamsCleanUp = new ViewGroup.LayoutParams(
                mediaControllerWidthPx,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ViewGroup.LayoutParams layoutParamsRepublicAct = new ViewGroup.LayoutParams(
                mediaControllerWidthPx,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ViewGroup.LayoutParams layoutParamsReels = new ViewGroup.LayoutParams(
                mediaControllerWidthPx,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        customMediaControllerClearing.setLayoutParams(layoutParamsClearing);
        customMediaControllerCleanUp.setLayoutParams(layoutParamsCleanUp);
        customMediaControllerRepublicAct.setLayoutParams(layoutParamsRepublicAct);
        customMediaControllerReels.setLayoutParams(layoutParamsReels); */

        // Set the anchor views for the custom media controllers
        /* mediaControllerClearing.setAnchorView(customMediaControllerClearing);
        mediaControllerCleanUp.setAnchorView(customMediaControllerCleanUp);
        mediaControllerRepublicAct.setAnchorView(customMediaControllerRepublicAct);
        mediaControllerReels.setAnchorView(customMediaControllerReels); */

        // Set the custom media controllers for the respective VideoViews
        videoClearingDay.setMediaController(mediaControllerClearing);
        /* videoCleanUp.setMediaController(mediaControllerCleanUp);
        videoRepublicAct.setMediaController(mediaControllerRepublicAct);
        videoReels.setMediaController(mediaControllerReels); */

        // Start video playback for all videos
        videoClearingDay.start();
        /* videoCleanUp.start();
        videoRepublicAct.pause();
        videoReels.pause(); */

        // Create a video thumbnail
        // Bitmap thumbnail = createVideoThumbnail(videoPath);

        // Check if the thumbnail was generated successfully
        /* if (thumbnail != null) {
            Drawable drawable = new BitmapDrawable(getResources(), thumbnail);

            // Set the thumbnail as the background of the VideoView
            videoView.setBackground(drawable);
        } else {
            // Handle the case where a thumbnail couldn't be generated
        } */

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
                        Intent intent = new Intent(ConsumerInterface.this, UserTips.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuAntiElectricity: {
                        Intent intent = new Intent(ConsumerInterface.this, UserAntiElectricity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuInstallationRequirements: {
                        Intent intent = new Intent(ConsumerInterface.this, UserInstallationRequirements.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuRates: {
                        Intent intent = new Intent(ConsumerInterface.this, UserRates.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuAccreditedPaymentCenters: {
                        Intent intent = new Intent(ConsumerInterface.this, UserPaymentCenters.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuContact: {
                        Intent intent = new Intent(ConsumerInterface.this, UserContact.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuMainOffice: {
                        Intent intent = new Intent(ConsumerInterface.this, UserMainOffice.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuBranches: {
                        Intent intent = new Intent(ConsumerInterface.this, UserBranches.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuUnitOffices: {
                        Intent intent = new Intent(ConsumerInterface.this, UserUnitOffices.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuRecruitmentProcess: {
                        Intent intent = new Intent(ConsumerInterface.this, UserRecruitmentProcess.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuPrecautions: {
                        Intent intent = new Intent(ConsumerInterface.this, UserPrecautions.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuTools: {
                        Intent intent = new Intent(ConsumerInterface.this, UserTools.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.menuSettings: {
                        Intent intent = new Intent(ConsumerInterface.this, UserSettings.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home_bottom);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home_bottom: {
                    return true;
                }
                case R.id.bill_bottom: {
                    startActivity(new Intent(getApplicationContext(), BillActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                case R.id.videos_bottom: {
                    startActivity(new Intent(getApplicationContext(), VideoActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                case R.id.profile_bottom: {
                    startActivity(new Intent(getApplicationContext(), UserProfile.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
            }
            return false;
        });
    }

    private void checkifEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConsumerInterface.this);
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

    private void swipeToRefresh() {
        // Look for the swipe container
        swipeContainer = findViewById(R.id.swipeContainer);

        // Set up Refresh Listener which triggers data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(getIntent());
                finish();
                overridePendingTransition(0,0);
                swipeContainer.setRefreshing(false);
            }
        });

        // Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
}
