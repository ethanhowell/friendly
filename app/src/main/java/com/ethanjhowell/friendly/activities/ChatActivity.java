package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.adapters.MessageAdapter;
import com.ethanjhowell.friendly.databinding.ActivityChatBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.ethanjhowell.friendly.models.Message;
import com.ethanjhowell.friendly.proxy.BackgroundManager;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.parse.ParseQuery;
import com.parse.boltsinternal.Task;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getCanonicalName();
    private static final String INTENT_GROUP_ID = "groupId";
    private static final String INTENT_GROUP_NAME = "groupName";
    private static final int NUM_MESSAGES_BEFORE_SCROLL_BUTTON = 20;

    private Group group;

    private final List<Message> messages = new ArrayList<>();
    private RecyclerView rvMessages;
    LinearLayoutManager rvMessagesLayoutManager;
    private MessageAdapter messagesAdapter;
    private Button btScrollToBottom;

    private ActivityChatBinding binding;
    private FriendlyParseUser user = FriendlyParseUser.getCurrentUser();

    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(INTENT_GROUP_ID, group.getObjectId());
        intent.putExtra(INTENT_GROUP_NAME, group.getGroupName());
        return intent;
    }

    private void loadGroup(BackgroundManager manager) {
        group.fetchInBackground((g, e) -> {
            if (e != null) {
                Log.e(TAG, "loadGroup: ", e);
                manager.failed(e);
            } else {
                this.group = (Group) g;
                Log.i(TAG, "loadGroup: " + group.getGroupName());
                manager.succeeded();
            }
        });
    }

    private void scrollToBottomOfMessages(boolean smoothScroll) {
        if (smoothScroll) {
            rvMessages.smoothScrollToPosition(messages.size() - 1);
        } else {
            rvMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void loadMessages() {
        ParseQuery.getQuery(Message.class)
                .whereEqualTo(Message.KEY_GROUP, group)
                .include(Message.KEY_AUTHOR)
                // id is unique so we only need to get the first (and only) result
                .findInBackground((messagesFromServer, e) -> {
                    synchronized (messages) {
                        if (e != null) {
                            Log.e(TAG, "loadMessages: ", e);
                            return;
                        }
                        messages.clear();
                        messages.addAll(messagesFromServer);
                    }
                    for (Message message : messagesFromServer) {
                        Log.d(TAG, "loadMessages: " + message.getBody());
                    }
                    messagesAdapter.notifyDataSetChanged();
                    scrollToBottomOfMessages(false);
                });
    }

    private void connectMessageSocket(BackgroundManager manager) {
        ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class)
                .whereEqualTo(Message.KEY_GROUP, group)
                .include(Message.KEY_AUTHOR);
        SubscriptionHandling<Message> subscriptionHandling = parseLiveQueryClient.subscribe(query);

        // Listen for CREATE events
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (q, message) -> {
            synchronized (messages) {
                messages.add(message);
            }
            Log.d(TAG, "connectMessageSocket: new Message: " + message.getBody());
            int oldLastMessagePos = messages.size() - 2;
            int lastVisiblePosition = rvMessagesLayoutManager.findLastVisibleItemPosition();
            // TODO: add a button to scroll to see latest messages if we aren't looking at the
            //  bottom of the list of messages

            runOnUiThread(() -> {
                messagesAdapter.notifyItemInserted(messages.size() - 1);
                // we only want to scroll to the bottom if the we're at the bottom of the messages
                if (lastVisiblePosition == oldLastMessagePos) {
                    scrollToBottomOfMessages(false);
                }
            });
        });
        manager.succeeded();
    }

    private void loadData() {
        // TODO: load all the users in that group
        BackgroundManager backgroundManager = new BackgroundManager(
                // callback
                this::onDataLoaded,
                // tasks to run
                this::loadGroup,
                this::connectMessageSocket
        );
        backgroundManager.run();
    }

    private void onDataLoaded() {
        loadMessages();
        binding.btSend.setOnClickListener(this::sendOnClick);

        // TODO: show that user has left the chat if so
        // TODO: disable sending messages if user has left the chat
    }

    private void setUpRecyclerView() {
        rvMessages = binding.rvMessages;
        messagesAdapter = new MessageAdapter(messages);
        rvMessages.setAdapter(messagesAdapter);
        rvMessagesLayoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(rvMessagesLayoutManager);

        rvMessages.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                Log.d(TAG, "setUpRecyclerView: scrolling");
                rvMessages.post(() -> scrollToBottomOfMessages(true));
            }
        });

        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0) {
                    InputMethodManager inputManager = (InputMethodManager) ChatActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager != null) {
                        View currentFocus = ChatActivity.this.getCurrentFocus();
                        if (currentFocus != null)
                            inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    }
                }
                int lastVisiblePosition = rvMessagesLayoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition < messages.size() - 1 - NUM_MESSAGES_BEFORE_SCROLL_BUTTON) {
                    btScrollToBottom.setVisibility(View.VISIBLE);
                } else if (lastVisiblePosition == messages.size() - 1) {
                    btScrollToBottom.setVisibility(View.GONE);
                }
            }
        });
    }

    private void scrollToBottomOnClick(View v) {
        btScrollToBottom.setVisibility(View.GONE);
        scrollToBottomOfMessages(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String groupName = getIntent().getStringExtra(INTENT_GROUP_NAME);
        String groupId = getIntent().getStringExtra(INTENT_GROUP_ID);

        Toolbar toolbar = binding.toolbar.toolbar;
        toolbar.setTitle(groupName);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        group = new Group();
        group.setObjectId(groupId);

        btScrollToBottom = binding.btScrollToBottom;
        btScrollToBottom.setOnClickListener(this::scrollToBottomOnClick);
        setUpRecyclerView();

        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
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