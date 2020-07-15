package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityNewGroupBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.parse.ParseUser;
import com.parse.boltsinternal.Task;

public class NewGroupActivity extends AppCompatActivity {
    private static final String TAG = NewGroupActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNewGroupBinding binding = ActivityNewGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btCreate.setOnClickListener(v -> {
            Group group = new Group();
            // TODO: blank group name edge case?
            group.setGroupName(binding.etGroupName.getText().toString());
            Task<Void> voidTask = group.saveInBackground();

            group.saveInBackground(e -> {
                if (e != null)
                    Log.e(TAG, "onCreate: ", e);
                else {
                    Group__User group__user = new Group__User(group, ParseUser.getCurrentUser());
                    group__user.saveInBackground(e1 -> {
                        startActivity(ChatActivity.createIntent(this, group));
                        finish();
                    });
                }
            });
        });
    }
}