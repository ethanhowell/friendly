package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.parse.ParseUser;
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
    public static final int NUM_MESSAGES_BEFORE_SCROLL_BUTTON = 20;

    private final Group group = new Group();
    private Group__User relation;

    private final Handler typingTimer = new Handler();

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
                Log.i(TAG, "loadGroup: " + group.getGroupName());
                manager.succeeded();
            }
        });
    }

    private void loadRelation(BackgroundManager manager) {
        ParseQuery.getQuery(Group__User.class)
                .whereEqualTo(Group__User.KEY_USER, ParseUser.getCurrentUser())
                .whereEqualTo(Group__User.KEY_GROUP, group)
                .getFirstInBackground((r, e) -> {
                    if (e != null) {
                        Log.e(TAG, "loadGroup: ", e);
                        manager.failed(e);
                    } else {
                        relation = r;
                        Log.i(TAG, "loadRelation: " + relation.getObjectId());
                        manager.succeeded();
                    }
                });
    }

    private void scrollToBottomOfMessages(boolean smoothScroll) {
        int size = messages.size();
        if (size > 0) {
            if (smoothScroll) {
                rvMessages.smoothScrollToPosition(size - 1);
            } else {
                rvMessages.scrollToPosition(size - 1);
            }
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

        SubscriptionHandling<Group__User> relationHandling = parseLiveQueryClient.subscribe(ParseQuery.getQuery(Group__User.class)
                .include(Group__User.KEY_USER)
                .whereEqualTo(Group__User.KEY_GROUP, group));
        relationHandling.handleEvent(SubscriptionHandling.Event.UPDATE, (q, relation) -> {
            FriendlyParseUser userTyping = FriendlyParseUser.fromParseUser(relation.getUser());
            Log.i(TAG, String.format(
                    "connectMessageSocket: %s %s is typing",
                    userTyping.getFirstName(),
                    userTyping.getLastName()
            ));

            // if the event comes from another user
            if (!userTyping.equals(FriendlyParseUser.getCurrentUser())) {
                typingTimer.removeCallbacksAndMessages(null);
                runOnUiThread(() -> binding.typingDots.setVisibility(View.VISIBLE));
                typingTimer.postDelayed(() -> runOnUiThread(() ->
                        binding.typingDots.setVisibility(View.GONE)), 1000);
            }
        });


        SubscriptionHandling<Message> messageHandling = parseLiveQueryClient.subscribe(
                ParseQuery.getQuery(Message.class)
                        .whereEqualTo(Message.KEY_GROUP, group)
                        .include(Message.KEY_AUTHOR)
        );

        // Listen for CREATE events
        messageHandling.handleEvent(SubscriptionHandling.Event.CREATE, (q, message) -> {
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
        BackgroundManager backgroundManager = new BackgroundManager(
                // callback
                this::onDataLoaded,
                // tasks to run
                this::loadGroup,
                this::loadRelation,
                this::connectMessageSocket
        );
        backgroundManager.run();
    }

    private void onDataLoaded() {
        loadMessages();
        binding.btSend.setOnClickListener(this::sendOnClick);

        binding.etMessageBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    relation.type(e -> {
                        if (e != null) {
                            Log.e(TAG, "afterTextChanged: ", e);
                        }
                    });
                }
            }
        });

        // TODO: show that user has left the chat if so
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
                        if (currentFocus != null) {
                            inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        }
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

        group.setObjectId(groupId);

        btScrollToBottom = binding.btScrollToBottom;
        btScrollToBottom.setOnClickListener(this::scrollToBottomOnClick);
        setUpRecyclerView();

        loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        typingTimer.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.itLeaveGroup) {
            leaveGroupOnClick();
            return true;
        } else if (item.getItemId() == R.id.itDetails) {
            startActivity(GroupDetails.createIntent(this, group));
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendOnClick(View v) {
        // TODO: check that message isn't empty
        String body = binding.etMessageBody.getText().toString();
        binding.etMessageBody.getText().clear();
        Message message = new Message(body, group);

        message.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "sendOnClick: ", e);
            }
        });
    }

    private void leaveGroupOnClick() {
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
                    if (e != null) {
                        Log.e(TAG, "leaveGroupOnClick: ", e);
                    } else {
                        g__u.setDateLeft(new Date());
                        g__u.saveInBackground(e1 -> {
                            // TODO: send some sort of message that "User has left the Group"
                            finish();
                        });
                    }
                });
    }
}