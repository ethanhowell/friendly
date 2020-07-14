package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ethanjhowell.friendly.databinding.ActivityNewUserBinding;
import com.facebook.Profile;

public class NewUserActivity extends AppCompatActivity {
    private static String TAG = NewUserActivity.class.getCanonicalName();
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