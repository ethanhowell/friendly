package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanjhowell.friendly.adapters.GroupAdapter;
import com.ethanjhowell.friendly.databinding.ActivityArchivedGroupBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArchivedGroupActivity extends AppCompatActivity {
    private static final String TAG = ArchivedGroupActivity.class.getCanonicalName();

    private final List<Group> archivedGroups = new ArrayList<>();
    private final GroupAdapter adapter = new GroupAdapter(archivedGroups);

    private void getUserGroupsInBackground() {
        ParseQuery.getQuery(Group__User.class)
                .include(Group__User.KEY_GROUP)
                .whereEqualTo(Group__User.KEY_USER, ParseUser.getCurrentUser())
                // user has left the group
                .whereExists(Group__User.KEY_DATE_LEFT)
                .findInBackground((gs__us, e) -> {
                            if (e != null)
                                Log.e(TAG, "getUserGroupsInBackground: ", e);
                            else {
                                for (Group__User g__u : gs__us) {
                                    Group group = g__u.getGroup();
                                    Log.d(TAG, "getUserGroupsInBackground: " + group.getGroupName());
                                    archivedGroups.add(group);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserGroupsInBackground();

        ActivityArchivedGroupBinding binding = ActivityArchivedGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar.toolbar;
        toolbar.setTitle("Archived Groups");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        RecyclerView rvArchivedGroups = binding.rvArchivedGroups;
        rvArchivedGroups.setAdapter(adapter);
        rvArchivedGroups.setLayoutManager(new LinearLayoutManager(this));

    }
}