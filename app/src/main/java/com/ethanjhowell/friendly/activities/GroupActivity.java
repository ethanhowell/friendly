package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.adapters.GroupAdapter;
import com.ethanjhowell.friendly.databinding.ActivityGroupBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupActivity extends AppCompatActivity {
    private final static String TAG = GroupActivity.class.getCanonicalName();
    private static final Pattern DEEPLINK_GROUP_PATTERN = Pattern.compile("^http://friendly-back\\.herokuapp.com/g/(.*)$");
    private ArrayList<Group> currentGroups;
    private GroupAdapter groupAdapter;
    private ConstraintLayout progressBar;

    private void getUserGroupsInBackground() {
        assert groupAdapter != null;
        progressBar.setVisibility(View.VISIBLE);
        ParseQuery.getQuery(Group__User.class)
                .include(Group__User.KEY_GROUP)
                .whereEqualTo(Group__User.KEY_USER, ParseUser.getCurrentUser())
                // means user hasn't left the group yet
                .whereDoesNotExist(Group__User.KEY_DATE_LEFT)
                .findInBackground((gs__us, e) -> {
                            if (e != null) {
                                Log.e(TAG, "getUserGroupsInBackground: ", e);
                            } else {
                                // since we run this when the activity resumes, we want to clear the
                                // groups so that we can re-add them
                                currentGroups.clear();
                                for (Group__User g__u : gs__us) {
                                    Group group = g__u.getGroup();
                                    Log.d(TAG, "getUserGroupsInBackground: " + group.getGroupName());
                                    currentGroups.add(group);
                                }
                                groupAdapter.notifyDataSetChanged();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                );
    }

    private void newGroupOnClick(View v) {
        startActivity(new Intent(this, NewGroupActivity.class));
    }

    private void joinGroup(String groupID) {
        progressBar.setVisibility(View.VISIBLE);
        Group group = new Group();
        group.setObjectId(groupID);
        group.fetchInBackground((g, groupException) -> {
            if (groupException != null) {
                Toast.makeText(this, R.string.invalidGroupLink_toast, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "joinGroup: " + groupException);
                progressBar.setVisibility(View.GONE);
            } else {
                ParseQuery.getQuery(Group__User.class)
                        .whereEqualTo(Group__User.KEY_USER, ParseUser.getCurrentUser())
                        .whereEqualTo(Group__User.KEY_GROUP, group)
                        .getFirstInBackground((result, e) -> {
                            // means that the relation doesn't yet exist, so we go and create it
                            if (result == null) {
                                Group__User group__user = new Group__User(group, ParseUser.getCurrentUser());
                                group__user.saveInBackground(e1 -> {
                                    if (e1 != null) {
                                        Log.e(TAG, "joinGroup: ", e1);
                                    } else {
                                        startActivity(ChatActivity.createIntent(this, group));
                                    }
                                    progressBar.setVisibility(View.GONE);
                                });
                            } else {
                                result.remove(Group__User.KEY_DATE_LEFT);
                                result.saveInBackground(e12 -> {
                                    if (e12 != null) {
                                        Log.e(TAG, "joinGroup: problem removing date left", e12);
                                    } else {
                                        startActivity(ChatActivity.createIntent(this, group));
                                    }
                                    progressBar.setVisibility(View.GONE);
                                });
                            }
                        });
            }

        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ActivityGroupBinding binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar.toolbar;
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        progressBar = binding.loading.clProgress;

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            // if user isn't logged in, we redirect them to the login page
            if (ParseUser.getCurrentUser() == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            Matcher matcher = DEEPLINK_GROUP_PATTERN.matcher(data.toString());
            Log.d(TAG, "onCreate: " + data.toString());
            if (matcher.matches()) {
                String groupID = matcher.group(1);
                joinGroup(groupID);
            }
        }

        currentGroups = new ArrayList<>();

        FriendlyParseUser user = FriendlyParseUser.getCurrentUser();
        user.getProfilePicture().getFileInBackground((file, e) -> {
            if (e != null) {
                Log.e(TAG, "onCreate: ", e);
            }
            Glide.with(this)
                    .load(file)
                    .into(binding.ivProfilePic);
        });
        binding.textView.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));

        // set up recycler view for the groups
        RecyclerView rvGroups = binding.rvGroups;
        groupAdapter = new GroupAdapter(currentGroups);
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));

        // click handler for floating action button
        binding.fabNewGroup.setOnClickListener(this::newGroupOnClick);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itArchivedGroups:
                Log.i(TAG, "onOptionsItemSelected: going to see archived chats");
                startActivity(new Intent(this, ArchivedGroupActivity.class));
                return true;
            case R.id.itLogOut:
                ParseUser.logOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: loading list of groups");
        getUserGroupsInBackground();
    }
}