package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityChatBinding;
import com.ethanjhowell.friendly.models.Group;

public class ChatActivity extends AppCompatActivity {
    private static final String INTENT_GROUP_NAME = "groupName";

    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(INTENT_GROUP_NAME, group.getGroupName());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityChatBinding binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvGroupName.setText(getIntent().getStringExtra(INTENT_GROUP_NAME));
    }
}