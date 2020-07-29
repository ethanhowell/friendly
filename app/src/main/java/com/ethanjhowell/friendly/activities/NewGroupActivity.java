package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.databinding.ActivityNewGroupBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.parse.ParseUser;

import java.util.Objects;

public class NewGroupActivity extends AppCompatActivity {
    private static final String TAG = NewGroupActivity.class.getCanonicalName();
    ActivityNewGroupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar.toolbar;
        toolbar.setTitle(R.string.activity_new_group_title);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        binding.btCreate.setOnClickListener(this::createGroupOnClick);
    }

    private void createGroupOnClick(View v) {
        String groupName = binding.etGroupName.getText().toString();
        if (groupName.isEmpty()) {
            Toast.makeText(this, R.string.etGroupName_invalid_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loading.clProgress.setVisibility(View.VISIBLE);
        Group group = new Group();
        group.setGroupName(groupName);

        // save the group and wait
        group.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "createGroupOnClick: ", e);
            } else {
                // once the group is saved, save the relation
                Group__User group__user = new Group__User(group, ParseUser.getCurrentUser());
                group__user.saveInBackground(e1 -> {
                    if (e1 != null) {
                        Log.e(TAG, "createGroupOnClick: ", e1);
                    } else {
                        startActivity(ChatActivity.createIntent(this, group));
                        startActivity(GroupDetailsActivity.createIntent(this, group));
                        finish();
                    }
                });
            }
            binding.loading.clProgress.setVisibility(View.GONE);
        });
    }
}