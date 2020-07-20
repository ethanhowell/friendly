package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.databinding.ActivityArchivedChatBinding;
import com.ethanjhowell.friendly.models.Group;

import java.util.Objects;

public class ArchivedChatActivity extends AppCompatActivity {
    private static final String TAG = ArchivedChatActivity.class.getCanonicalName();
    private static final String INTENT_GROUP_ID = "groupId";
    private static final String INTENT_GROUP_NAME = "groupName";
    private final Group group = new Group();

    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, ArchivedChatActivity.class);
        intent.putExtra(INTENT_GROUP_ID, group.getObjectId());
        intent.putExtra(INTENT_GROUP_NAME, group.getGroupName());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityArchivedChatBinding binding = ActivityArchivedChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String groupName = getIntent().getStringExtra(INTENT_GROUP_NAME);
        String groupId = getIntent().getStringExtra(INTENT_GROUP_ID);

        Toolbar toolbar = binding.toolbar.toolbar;
        toolbar.setTitle(String.format(getString(R.string.activity_archived_chat_titleFormat), groupName));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        group.setObjectId(groupId);
        group.setGroupName(groupName);
    }
}