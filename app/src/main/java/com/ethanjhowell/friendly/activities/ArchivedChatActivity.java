package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.adapters.MessageAdapter;
import com.ethanjhowell.friendly.databinding.ActivityArchivedChatBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Message;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.ethanjhowell.friendly.activities.ChatActivity.NUM_MESSAGES_BEFORE_SCROLL_BUTTON;

public class ArchivedChatActivity extends AppCompatActivity {
    private static final String TAG = ArchivedChatActivity.class.getCanonicalName();
    private static final String INTENT_GROUP_ID = "groupId";
    private static final String INTENT_GROUP_NAME = "groupName";

    private final List<Message> archivedMessages = new ArrayList<>();
    private final MessageAdapter archivedMessagesAdapter = new MessageAdapter(archivedMessages);


    private final Group group = new Group();
    private RecyclerView rvArchivedMessages;
    private Button btScrollToBottom;

    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, ArchivedChatActivity.class);
        intent.putExtra(INTENT_GROUP_ID, group.getObjectId());
        intent.putExtra(INTENT_GROUP_NAME, group.getGroupName());
        return intent;
    }

    private void loadMessages() {
        ParseQuery.getQuery(Message.class)
                .whereEqualTo(Message.KEY_GROUP, group)
                .whereLessThanOrEqualTo(Message.KEY_CREATED_AT, new Date())
                .findInBackground((messages, e) -> {
                    if (e != null) {
                        Log.e(TAG, "problem retrieving archived messages: ", e);
                        return;
                    }
                    for (Message m : messages) {
                        Log.d(TAG, "onCreate: " + m);
                    }
                    archivedMessages.addAll(messages);
                    archivedMessagesAdapter.notifyDataSetChanged();
                });
    }

    private void setUpRecyclerView() {
        rvArchivedMessages.setAdapter(archivedMessagesAdapter);
        LinearLayoutManager rvArchivedMessagesLayoutManager = new LinearLayoutManager(this);
        rvArchivedMessages.setLayoutManager(rvArchivedMessagesLayoutManager);

        rvArchivedMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisiblePosition = rvArchivedMessagesLayoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition < archivedMessages.size() - 1 - NUM_MESSAGES_BEFORE_SCROLL_BUTTON) {
                    btScrollToBottom.setVisibility(View.VISIBLE);
                } else if (lastVisiblePosition == archivedMessages.size() - 1) {
                    btScrollToBottom.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.ethanjhowell.friendly.databinding.ActivityArchivedChatBinding binding = ActivityArchivedChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String groupName = getIntent().getStringExtra(INTENT_GROUP_NAME);
        String groupId = getIntent().getStringExtra(INTENT_GROUP_ID);

        Toolbar toolbar = binding.toolbar.toolbar;
        toolbar.setTitle(String.format(getString(R.string.activity_archived_chat_titleFormat), groupName));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        rvArchivedMessages = binding.rvArchivedMessages;
        setUpRecyclerView();

        btScrollToBottom = binding.btScrollToBottom;

        group.setObjectId(groupId);
        group.setGroupName(groupName);

        loadMessages();
    }
}