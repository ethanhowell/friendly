package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityChatBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.ethanjhowell.friendly.models.Message;
import com.ethanjhowell.friendly.proxy.BackgroundManager;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.boltsinternal.Task;

import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getCanonicalName();
    private static final String INTENT_GROUP = "groupId";

    private String groupId;
    private Group group;
    private ActivityChatBinding binding;
    private FriendlyParseUser user = FriendlyParseUser.getCurrentUser();

    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(INTENT_GROUP, group.getObjectId());
        return intent;
    }

    private void loadGroup(BackgroundManager manager) {
        ParseQuery.getQuery(Group.class)
                .whereEqualTo(ParseObject.KEY_OBJECT_ID, groupId)
                // id is unique so we only need to get the first (and only) result
                .getFirstInBackground((g, e) -> {
                    if (e != null) {
                        Log.e(TAG, "loadGroup: ", e);
                        manager.failed(e);
                    } else {
                        group = g;
                        manager.succeeded();
                    }
                });
    }

    private void loadData() {
        // TODO: load all the users in that group
        // TODO: load all the messages in the group
        BackgroundManager backgroundManager = new BackgroundManager(this::onDataLoaded, this::loadGroup);
        backgroundManager.run();
    }

    private void onDataLoaded() {
        binding.tvGroupName.setText(group.getGroupName());
        binding.tvLeave.setOnClickListener(this::leaveGroupOnClick);
        binding.btSend.setOnClickListener(this::sendOnClick);

        // TODO: show that user has left the chat if so
        // TODO: disable sending messages if user has left the chat
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getStringExtra(INTENT_GROUP);
        loadData();
    }

    private void sendOnClick(View v) {
        // TODO: check that message isn't empty
        String body = binding.etMessageBody.getText().toString();
        binding.etMessageBody.setText("");
        Message message = new Message(body, group);
        Task<Void> voidTask = message.saveInBackground();

        message.saveInBackground(e -> Log.e(TAG, "sendOnClick: ", e));
    }

    private void leaveGroupOnClick(View v) {
        Log.i(
                TAG,
                String.format(
                        "leaveGroupOnClick: user %s %s leaving group %s",
                        user.getFirstName(),
                        user.getLastName(),
                        group.getGroupName()
                )
        );

        ParseQuery.getQuery(Group__User.class)
                .whereEqualTo(Group__User.KEY_GROUP, group)
                .whereEqualTo(Group__User.KEY_USER, user.getParseUser())
                .getFirstInBackground((g__u, e) -> {
                    if (e != null)
                        Log.e(TAG, "leaveGroupOnClick: ", e);
                    else {
                        g__u.setDateLeft(new Date());
                        g__u.saveInBackground(e1 -> {
                            // TODO: send some sort of message that "User has left the Group"
                            finish();
                        });
                    }
                });
    }
}