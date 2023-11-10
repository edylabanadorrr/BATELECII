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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminVideos extends AppCompatActivity {

    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_videos);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("BSP Videos");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        VideoView videoRepublicAct = findViewById(R.id.videoViewRepublicAct);
        VideoView videoReels = findViewById(R.id.videoViewReels);
        VideoView videoCleanUp = findViewById(R.id.videoViewCleanUp);
        VideoView videoPaymentCenters = findViewById(R.id.videoViewPaymentCenters);
        VideoView videoKite = findViewById(R.id.videoViewKite);

        String videoPathRepublicAct = "android.resource://" + getPackageName() + "/" + R.raw.republic_act;
        String videoPathReels = "android.resource://" + getPackageName() + "/" + R.raw.reels;
        String videoPathCleanUp = "android.resource://" + getPackageName() + "/" + R.raw.clean_up_video;
        String videoPathPaymentCenters = "android.resource://" + getPackageName() + "/" + R.raw.accredited_payment_centers;
        String videoPathKite = "android.resource://" + getPackageName() + "/" + R.raw.kite_video;

        Uri uriRepublicAct = Uri.parse(videoPathRepublicAct);
        Uri uriReels = Uri.parse(videoPathReels);
        Uri uriCleanUp = Uri.parse(videoPathCleanUp);
        Uri uriPaymentCenters = Uri.parse(videoPathPaymentCenters);
        Uri uriKite = Uri.parse(videoPathKite);

        videoRepublicAct.setVideoURI(uriRepublicAct);
        videoReels.setVideoURI(uriReels);
        videoCleanUp.setVideoURI(uriCleanUp);
        videoPaymentCenters.setVideoURI(uriPaymentCenters);
        videoKite.setVideoURI(uriKite);

        MediaController mediaControllerRepublicAct = new MediaController(this);
        MediaController mediaControllerReels = new MediaController(this);
        MediaController mediaControllerCleanUp = new MediaController(this);
        MediaController mediaControllerPaymentCenters = new MediaController(this);
        MediaController mediaControllerKite = new MediaController(this);

        videoRepublicAct.setMediaController(mediaControllerRepublicAct);
        videoReels.setMediaController(mediaControllerReels);
        videoCleanUp.setMediaController(mediaControllerCleanUp);
        videoPaymentCenters.setMediaController(mediaControllerPaymentCenters);
        videoKite.setMediaController(mediaControllerKite);

        videoRepublicAct.pause();
        videoReels.pause();
        videoCleanUp.pause();
        videoPaymentCenters.pause();
        videoKite.pause();

        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.videos_bottom);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home_bottom: {
                    startActivity(new Intent(getApplicationContext(), AdminInterface.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                case R.id.consumers_bottom: {
                    startActivity(new Intent(getApplicationContext(), AdminConsumers.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                case R.id.videos_bottom: {
                    return true;
                }
                case R.id.send_email_bottom: {
                    Intent intent = new Intent(AdminVideos.this, AdminSendEmail.class);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    startActivity(intent);
                    finish();
                    return true;
                }
                case R.id.profile_bottom: {
                    startActivity(new Intent(getApplicationContext(), AdminProfile.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
            }
            return false;
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
                                    Intent adminIntent = new Intent(AdminVideos.this, AdminInterface.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                } else if (role.equals("consumer")) {
                                    // Handle actions for Consumer role
                                    Intent consumerIntent = new Intent(AdminVideos.this, ConsumerInterface.class);
                                    consumerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(consumerIntent);
                                } else {
                                    // Handle actions for other roles or roles not defined
                                    Toast.makeText(AdminVideos.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(AdminVideos.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}

