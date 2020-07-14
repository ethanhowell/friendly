package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ethanjhowell.friendly.databinding.ActivityNewUserBinding;
import com.facebook.Profile;

import java.io.File;

public class NewUserActivity extends AppCompatActivity {
    // TODO: change all static constants to final
    private static String TAG = NewUserActivity.class.getCanonicalName();
    private static int ACTION_IMAGE_CAPTURE_REQUEST_CODE = 2;
    private static String PHOTO_FILE_NAME = "photo.png";
    private File photoFile;
    private ActivityNewUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Profile currentProfile = Profile.getCurrentProfile();
        if (currentProfile == null) {
            Log.d(TAG, "onCreate: logged in via parse");
        } else {
            Log.d(TAG, "onCreate: logged in via facebook");
            loadFacebookImage(currentProfile);
        }

        binding.btContinue.setOnClickListener(this::continueOnClick);
        binding.clChooseProfilePic.setOnClickListener(this::launchCamera);
    }

    private File getPhotoFileUri() {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "getPhotoFileUri: failed to create directory");
        }

        return new File(mediaStorageDir.getPath() + File.separator + PHOTO_FILE_NAME);
    }

    private void launchCamera(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri();

        // wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(
                this,
                "com.ethanjhowell.friendly.fileprovider",
                photoFile
        );
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // check that there is a camera app that can handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, ACTION_IMAGE_CAPTURE_REQUEST_CODE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_IMAGE_CAPTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Glide.with(this)
                        .load(photoFile)
                        .circleCrop()
                        .into(binding.ivProfilePic);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadFacebookImage(Profile profile) {
        Glide.with(this)
                .asBitmap()
                .load(profile.getProfilePictureUri(300, 300).toString())
                .circleCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        binding.ivProfilePic.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void continueOnClick(View v) {
        // TODO: check image not empty
        // TODO: validate phone number
        // TODO: save image and phone number
        // TODO: mark user as completed

        // by now user account is completely created, we can navigate to the next activity
        startActivity(new Intent(this, GroupActivity.class));
        finish();
    }


}