package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class GroupActivity extends AppCompatActivity {
    private final static String TAG = GroupActivity.class.getCanonicalName();
    private ArrayList<Group> currentGroups;
    private GroupAdapter groupAdapter;

    private void getUserGroupsInBackground() {
        assert groupAdapter != null;
        ParseQuery.getQuery(Group__User.class)
                .include(Group__User.KEY_GROUP)
                .whereEqualTo(Group__User.KEY_USER, ParseUser.getCurrentUser())
                .findInBackground((gs__us, e) -> {
                            if (e != null)
                                Log.e(TAG, "getUserGroupsInBackground: ", e);
                            else {
                                // since we run this when the activity resumes, we want to clear the
                                // groups so that we can re-add them
                                currentGroups.clear();
                                for (Group__User g__u : gs__us) {
                                    Group group = g__u.getGroup();
                                    Log.d(TAG, "getUserGroupsInBackground: " + group.getGroupName());
                                    if (!g__u.hasLeft())
                                        currentGroups.add(group);
                                    else
                                        // TODO: make an archived groups list
                                        Log.i(TAG, "getUserGroupsInBackground: Archived group: " + group.getGroupName());
                                }
                                groupAdapter.notifyDataSetChanged();
                            }
                        }
                );
    }

    private void newGroupOnClick(View v) {
        startActivity(new Intent(this, NewGroupActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGroupBinding binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar.toolbar;
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);


        currentGroups = new ArrayList<>();

        FriendlyParseUser user = FriendlyParseUser.getCurrentUser();
        user.getProfilePicture().getFileInBackground((file, e) -> {
            if (e != null)
                Log.e(TAG, "onCreate: ", e);
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