package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.adapters.MessageAdapter;
import com.ethanjhowell.friendly.databinding.ActivityChatBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.ethanjhowell.friendly.models.Message;
import com.ethanjhowell.friendly.proxy.BackgroundManager;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.boltsinternal.Task;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getCanonicalName();
    private static final String INTENT_GROUP = "groupId";

    private String groupId;
    private Group group;
    private List<Message> messages;
    private RecyclerView rvMessages;
    private MessageAdapter adapter;
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

    private void loadMessages(BackgroundManager manager) {
        ParseQuery.getQuery(Message.class)
                .whereMatchesQuery(
                        Message.KEY_GROUP,
                        ParseQuery.getQuery(Group.class)
                                .whereEqualTo(Group.KEY_OBJECT_ID, groupId)
                )
                .include(Message.KEY_AUTHOR)
                // id is unique so we only need to get the first (and only) result
                .findInBackground((messagesFromServer, e) -> {
                    if (e != null) {
                        Log.e(TAG, "loadMessages: ", e);
                        manager.failed(e);
                    } else {
                        messages.addAll(messagesFromServer);
                        for (Message message : messages) {
                            Log.d(TAG, "loadMessages: " + message.getBody());
                        }
                        adapter.notifyDataSetChanged();
                        rvMessages.scrollToPosition(messages.size() - 1);
                        manager.succeeded();
                    }
                });
    }

    private void connectMessageSocket() {
        ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class)
                .whereEqualTo(Message.KEY_GROUP, group)
                .include(Message.KEY_AUTHOR);
        SubscriptionHandling<Message> subscriptionHandling = parseLiveQueryClient.subscribe(query);

        // Listen for CREATE events
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (q, message) -> {
            int oldLastMessagePos = messages.size() > 0 ? messages.size() - 1 : 0;
            messages.add(message);
            Log.d(TAG, "connectMessageSocket: new Message: " + message.getBody());
            runOnUiThread(() -> {
                adapter.notifyItemInserted(messages.size() - 1);
                LinearLayoutManager layoutManager = (LinearLayoutManager) rvMessages.getLayoutManager();
                assert layoutManager != null;
                // we only want to scroll to the bottom if the we're at the bottom of the messages
                if (layoutManager.findLastCompletelyVisibleItemPosition() == oldLastMessagePos) {
                    rvMessages.scrollToPosition(messages.size() - 1);
                }
            });
        });
    }

    private void loadData() {
        // TODO: load all the users in that group
        BackgroundManager backgroundManager = new BackgroundManager(
                // callback
                this::onDataLoaded,
                // tasks to run
                this::loadGroup,
                this::loadMessages
        );
        backgroundManager.run();
    }

    private void onDataLoaded() {
        // TODO: I don't want to lose any messages on connection so I think I need to connect
        //  the socket first but I'm not sure how to properly handle the multithreading to avoid
        //  duplicates and still make sure that the times work. This is quite a dirty solution
        //  but it'll work for now.
        connectMessageSocket();
        binding.tvGroupName.setText(group.getGroupName());
        binding.tvLeave.setOnClickListener(this::leaveGroupOnClick);
        binding.btSend.setOnClickListener(this::sendOnClick);

        // TODO: show that user has left the chat if so
        // TODO: disable sending messages if user has left the chat
    }

    private void setUpRecyclerView() {
        rvMessages = binding.rvMessages;
        adapter = new MessageAdapter(messages);
        rvMessages.setAdapter(adapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        rvMessages.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                Log.d(TAG, "setUpRecyclerView: scrolling");
                rvMessages.post(() -> rvMessages.smoothScrollToPosition(messages.size() - 1));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messages = new ArrayList<>();
        groupId = getIntent().getStringExtra(INTENT_GROUP);

        setUpRecyclerView();

        loadData();
    }

    private void sendOnClick(View v) {
        // TODO: check that message isn't empty
        String body = binding.etMessageBody.getText().toString();
        binding.etMessageBody.setText("");
        Message message = new Message(body, group);
        Task<Void> voidTask = message.saveInBackground();

        message.saveInBackground(e -> {
            if (e != null) Log.e(TAG, "sendOnClick: ", e);
        });
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