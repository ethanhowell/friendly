package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.ethanjhowell.friendly.databinding.ActivityNewUserBinding;
import com.facebook.Profile;

public class NewUserActivity extends AppCompatActivity {
    private static String TAG = NewUserActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNewUserBinding binding = ActivityNewUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Profile currentProfile = Profile.getCurrentProfile();
        if (currentProfile == null) {
            Log.d(TAG, "onCreate: logged in via parse");
        } else {
            Log.d(TAG, "onCreate: logged in via facebook");
            Glide.with(this)
                    .load(currentProfile.getProfilePictureUri(300, 300).toString())
                    .circleCrop()
                    .into(binding.ivProfilePic);
        }

    }
}