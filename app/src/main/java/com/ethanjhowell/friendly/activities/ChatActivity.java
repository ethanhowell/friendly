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
import com.ethanjhowell.friendly.adapters.ActiveMessageAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class ChatActivity extends AppCompatActivity {
    public static final int NUM_MESSAGES_BEFORE_SCROLL_BUTTON = 20;
    private static final String TAG = ChatActivity.class.getCanonicalName();
    private static final String INTENT_GROUP_ID = "groupId";
    private static final String INTENT_GROUP_NAME = "groupName";

    private final Group group = new Group();
    private final Handler typingTimer = new Handler();
    private final List<Message> messages = new ArrayList<>();
    private final HashMap<String, Integer> messagesPos = new HashMap<>();

    private final Object messageMutex = new Object();

    private LinearLayoutManager rvMessagesLayoutManager;
    private Group__User relation;
    private RecyclerView rvMessages;
    private ActiveMessageAdapter messagesAdapter;
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

    private void addMessage(Message m) {
        messagesPos.put(m.getObjectId(), messages.size());
        messages.add(m);
    }

    private void loadMessages() {
        ParseQuery.getQuery(Message.class)
                .whereEqualTo(Message.KEY_GROUP, group)
                .include(Message.KEY_AUTHOR)
                .addAscendingOrder(Message.KEY_CREATED_AT)
                // id is unique so we only need to get the first (and only) result
                .findInBackground((messagesFromServer, e) -> {
                    synchronized (messageMutex) {
                        if (e != null) {
                            Log.e(TAG, "loadMessages: ", e);
                            return;
                        }
                        messages.clear();
                        messagesPos.clear();
                        for (Message message : messagesFromServer) {
                            addMessage(message);
                        }
                    }
                    for (Message message : messagesFromServer) {
                        Log.d(TAG, "loadMessages: " + message.getBody());
                    }
                    messagesAdapter.notifyDataSetChanged();
                    scrollToBottomOfMessages(false);
                    binding.clProgress.setVisibility(View.GONE);
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
            synchronized (messageMutex) {
                addMessage(message);
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

        messageHandling.handleEvent(SubscriptionHandling.Event.UPDATE, (q, message) -> {
            Log.d(TAG, "connectMessageSocket: updated: " + message.getBody());
            Integer position = messagesPos.get(message.getObjectId());
            if (position != null) {
                int pos = position;
                messages.set(pos, message);
                Log.d(TAG, "connectMessageSocket: position: " + pos);
                runOnUiThread(() -> messagesAdapter.notifyItemChanged(pos));
            } else {
                Log.e(TAG, "connectMessageSocket: ", new NullPointerException("Message not found"));
            }
        });

        manager.succeeded();
    }

    private void loadData() {
        binding.clProgress.setVisibility(View.VISIBLE);
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
        messagesAdapter = new ActiveMessageAdapter(messages);
        rvMessages.setAdapter(messagesAdapter);
        rvMessagesLayoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(rvMessagesLayoutManager);
        rvMessages.setItemAnimator(null);


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

        Toolbar toolbar = binding.toolbar;
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
        switch (item.getItemId()) {
            case R.id.itLeaveGroup:
                leaveGroupOnClick();
                return true;
            case R.id.itDetails:
                startActivity(GroupDetailsActivity.createIntent(this, group));
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void sendOnClick(View v) {
        String body = binding.etMessageBody.getText().toString();
        if (!body.isEmpty()) {
            binding.etMessageBody.getText().clear();
            Message message = new Message(body, group);

            message.saveInBackground(e -> {
                if (e != null) {
                    Log.e(TAG, "sendOnClick: ", e);
                }
            });
        }
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