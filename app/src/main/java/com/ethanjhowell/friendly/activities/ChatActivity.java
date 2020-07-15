package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityChatBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getCanonicalName();
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
        binding.tvLeave.setOnClickListener(this::leaveGroupOnClick);
    }

    private void leaveGroupOnClick(View v) {
        Log.i(TAG, String.format("leaveGroupOnClick: user %s %s leaving group", FriendlyParseUser.getCurrentUser().getFirstName(), FriendlyParseUser.getCurrentUser().getLastName()));
    }
}