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
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getCanonicalName();
    private static final String INTENT_GROUP = "groupId";
    private Group group;
    private ActivityChatBinding binding;

    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(INTENT_GROUP, group.getObjectId());
        return intent;
    }

    private void loadGroup(String groupId) {
        ParseQuery.getQuery(Group.class)
                .whereEqualTo(ParseObject.KEY_OBJECT_ID, groupId)
                .getFirstInBackground((g, e) -> {
                    if (e != null)
                        Log.e(TAG, "loadGroup: ", e);
                    else {
                        group = g;
                        onDataLoaded();
                    }
                });
    }

    private void onDataLoaded() {
        binding.tvGroupName.setText(group.getGroupName());
        binding.tvLeave.setOnClickListener(this::leaveGroupOnClick);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String groupId = getIntent().getStringExtra(INTENT_GROUP);
        loadGroup(groupId);
    }

    private void leaveGroupOnClick(View v) {
        Log.i(TAG, String.format("leaveGroupOnClick: user %s %s leaving group %s", FriendlyParseUser.getCurrentUser().getFirstName(), FriendlyParseUser.getCurrentUser().getLastName(), group.getGroupName()));
    }
}