package com.batelectwo;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadProfilePicture extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imageViewUploadPicture;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_picture);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellowish)));
        getSupportActionBar().setTitle("Upload Profile Picture");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        authProfile = FirebaseAuth.getInstance();

        Button buttonUploadPictureChoose = findViewById(R.id.uploadPictureChooseButton);
        Button buttonUploadPicture =  findViewById(R.id.uploadPictureButton);
        progressBar = findViewById(R.id.progressBarShow);
        imageViewUploadPicture = findViewById(R.id.imageViewProfilePicture);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPictures");

        Uri uri = firebaseUser.getPhotoUrl();

        // Set User's current DP in ImageView (if uploaded already). We will Picasso since imageViewer setImage
        // Regular URIs
        Picasso.with(UploadProfilePicture.this).load(uri).into(imageViewUploadPicture);

        buttonUploadPictureChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Upload Image
        buttonUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                UploadPicture();
            }
        });
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
            imageViewUploadPicture.setImageURI(uriImage);
        }
    }

    private void UploadPicture() {
        if (uriImage != null) {
            // Save the image with uid of the currently logged user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "/profilepicture.jpg");

            //Upload image to Storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            // Finally set the display image of the user after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates);
                        }
                    });

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicture.this, "Upload Successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent (UploadProfilePicture.this, UserProfile.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicture.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePicture.this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Obtain File Extension of the image
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}