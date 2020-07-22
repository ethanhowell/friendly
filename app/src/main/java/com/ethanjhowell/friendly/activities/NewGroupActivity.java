package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityNewGroupBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.parse.ParseUser;

public class NewGroupActivity extends AppCompatActivity {
    private static final String TAG = NewGroupActivity.class.getCanonicalName();
    ActivityNewGroupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btCreate.setOnClickListener(this::createGroupOnClick);
    }

    private void createGroupOnClick(View v) {
        Group group = new Group();
        // TODO: blank group name edge case?
        group.setGroupName(binding.etGroupName.getText().toString());

        // save the group and wait
        group.saveInBackground(e -> {
            if (e != null)
                Log.e(TAG, "createGroupOnClick: ", e);
            else {
                // once the group is saved, save the relation
                Group__User group__user = new Group__User(group, ParseUser.getCurrentUser());
                group__user.saveInBackground(e1 -> {
                    if (e1 != null)
                        Log.e(TAG, "createGroupOnClick: ", e);
                    else {
                        startActivity(ChatActivity.createIntent(this, group));
                        finish();
                    }
                });
            }
        });
    }
}