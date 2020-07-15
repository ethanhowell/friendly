package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.ethanjhowell.friendly.databinding.ActivityGroupBinding;
import com.ethanjhowell.friendly.models.Group__User;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class GroupActivity extends AppCompatActivity {
    private final static String TAG = GroupActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGroupBinding binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FriendlyParseUser user = FriendlyParseUser.getCurrentUser();

        // TODO: build out the group activity
        user.getProfilePicture().getFileInBackground((file, e) -> {
            if (e != null)
                Log.e(TAG, "onCreate: ", e);
            Glide.with(this)
                    .load(file)
                    .into(binding.ivProfilePic);
        });
        binding.textView.setText(String.format("Welcome, %s %s", user.getFirstName(), user.getLastName()));
        binding.button.setOnClickListener(v -> {
            ParseUser.logOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        ParseQuery.getQuery(Group__User.class).include(Group__User.KEY_GROUP).whereEqualTo(Group__User.KEY_USER, ParseUser.getCurrentUser()).findInBackground((groups, e) -> {
            for (Group__User g__u : groups) {
                Log.d(TAG, "onCreate: " + g__u.getGroup().getGroupName());
            }
        });
    }
}