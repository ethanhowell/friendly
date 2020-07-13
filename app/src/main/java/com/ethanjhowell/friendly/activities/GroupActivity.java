package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityGroupBinding;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.parse.ParseUser;

public class GroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGroupBinding binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FriendlyParseUser user = new FriendlyParseUser(ParseUser.getCurrentUser());

        // TODO: build out the group activity
        binding.textView.setText(String.format("Welcome, %s %s", user.getFirstName(), user.getLastName()));
        binding.button.setOnClickListener(v -> {
            ParseUser.logOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}