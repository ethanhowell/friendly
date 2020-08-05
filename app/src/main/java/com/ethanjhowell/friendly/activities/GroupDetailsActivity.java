package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.adapters.UserAdapter;
import com.ethanjhowell.friendly.databinding.ActivityGroupDetailsBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GroupDetailsActivity extends AppCompatActivity {
    private static final String TAG = GroupDetailsActivity.class.getCanonicalName();
    private static final String INTENT_GROUP_ID = "groupId";
    private static final String INTENT_GROUP_NAME = "groupName";
    private final Group group = new Group();
    private final List<FriendlyParseUser> users = new ArrayList<>();
    private final UserAdapter userAdapter = new UserAdapter(users);
    private TextView tvUsers;

    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, GroupDetailsActivity.class);
        intent.putExtra(INTENT_GROUP_ID, group.getObjectId());
        intent.putExtra(INTENT_GROUP_NAME, group.getGroupName());
        return intent;
    }

    private void loadUsers() {
        ParseQuery.getQuery(Group__User.class)
                .whereEqualTo(Group__User.KEY_GROUP, group)
                .whereDoesNotExist(Group__User.KEY_DATE_LEFT)
                .include(Group__User.KEY_USER)
                .addAscendingOrder(Group__User.KEY_UPDATED_AT)
                .findInBackground((group__users, e) -> {
                    if (e != null) {
                        Log.e(TAG, "loadUsers: ", e);
                    } else {
                        for (Group__User group__user : group__users) {
                            FriendlyParseUser friendlyParseUser = FriendlyParseUser.fromParseUser(group__user.getUser());
                            users.add(friendlyParseUser);
                            Log.d(TAG, "loadUsers: " + friendlyParseUser.getFirstName() + " " + friendlyParseUser.getLastName());
                        }
                        userAdapter.notifyDataSetChanged();
                        if (users.size() > 1) {
                            tvUsers.setText(String.format(Locale.US, getString(R.string.tvUsers_template), users.size()));
                        } else {
                            tvUsers.setText(R.string.tvUsers_single);
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGroupDetailsBinding binding = ActivityGroupDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String groupName = getIntent().getStringExtra(INTENT_GROUP_NAME);
        String groupId = getIntent().getStringExtra(INTENT_GROUP_ID);
        String inviteUrl = "http://friendly-back.herokuapp.com/g/" + groupId;

        group.setObjectId(groupId);
        group.fetchInBackground((object, e) -> {
            if (e != null) {
                Log.e(TAG, "error fetching group: ", e);
            }
        });

        Toolbar toolbar = binding.includeToolbar.toolbar;
        toolbar.setTitle(groupName);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        TextView tvInviteLink = binding.tvInviteLink;
        tvInviteLink.setText(inviteUrl);
        tvInviteLink.setPaintFlags(tvInviteLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        RecyclerView rvUsers = binding.rvUsers;
        rvUsers.setAdapter(userAdapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        tvUsers = binding.tvUsers;

        loadUsers();

        Glide.with(this)
                .load("https://zxing.org/w/chart?cht=qr&chs=350x350&chld=L&choe=UTF-8&chl=" + inviteUrl)
                .into(binding.ivQR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return (super.onOptionsItemSelected(item));
    }
}