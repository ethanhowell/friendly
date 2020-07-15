package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
    private ArrayList<Group> groups;
    private GroupAdapter groupAdapter;

    private void getUserGroupsInBackground() {
        assert groupAdapter != null;
        ParseQuery.getQuery(Group__User.class)
                .include(Group__User.KEY_GROUP)
                .whereEqualTo(Group__User.KEY_USER, ParseUser.getCurrentUser())
                .findInBackground((gs__us, e) -> {
                            // since we run this when the activity resumes, we want to clear the
                            // groups so that we can re-add them
                            groups.clear();
                            for (Group__User g__u : gs__us) {
                                Group group = g__u.getGroup();
                                Log.d(TAG, "getUserGroups: " + group.getGroupName());
                                groups.add(group);
                            }
                            groupAdapter.notifyDataSetChanged();
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
        groups = new ArrayList<>();

        FriendlyParseUser user = FriendlyParseUser.getCurrentUser();
        user.getProfilePicture().getFileInBackground((file, e) -> {
            if (e != null)
                Log.e(TAG, "onCreate: ", e);
            Glide.with(this)
                    .load(file)
                    .into(binding.ivProfilePic);
        });
        binding.textView.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
        binding.btLogOut.setOnClickListener(v -> {
            ParseUser.logOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // set up recycler view for the groups
        RecyclerView rvGroups = binding.rvGroups;
        groupAdapter = new GroupAdapter(groups);
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));

        // click handler for floating action button
        binding.fabNewGroup.setOnClickListener(this::newGroupOnClick);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: loading list of groups");
        getUserGroupsInBackground();
    }
}