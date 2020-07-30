package com.ethanjhowell.friendly.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.databinding.ActivityNewGroupBinding;
import com.ethanjhowell.friendly.models.Group;
import com.ethanjhowell.friendly.models.Group__User;
import com.parse.ParseUser;

import java.util.Objects;

public class NewGroupActivity extends AppCompatActivity {
    private static final String TAG = NewGroupActivity.class.getCanonicalName();
    ActivityNewGroupBinding binding;
    EditText etGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.includeToolbar.toolbar;
        toolbar.setTitle(R.string.activity_new_group_title);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        etGroupName = binding.etGroupName;
        etGroupName.addTextChangedListener(new GroupTextChange());
        binding.btCreate.setOnClickListener(this::createGroupOnClick);
    }

    private void createGroupOnClick(View v) {
        String groupName = etGroupName.getText().toString();
        if (!groupName.isEmpty()) {

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

    @Override
    protected void onResume() {
        super.onResume();
        if (etGroupName.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etGroupName, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private class GroupTextChange implements TextWatcher {
        public final int COLOR_CONTROL_HIGHLIGHT, COLOR_CONTROL_ACTIVATED;
        public final Drawable background;


        public GroupTextChange() {
            TypedValue typedValue = new TypedValue();
            background = binding.btCreate.getBackground();
            NewGroupActivity.this.getTheme().resolveAttribute(R.attr.colorControlHighlight, typedValue, true);
            COLOR_CONTROL_HIGHLIGHT = typedValue.data;

            NewGroupActivity.this.getTheme().resolveAttribute(R.attr.colorControlActivated, typedValue, true);
            COLOR_CONTROL_ACTIVATED = typedValue.data;

            Log.d(TAG, "GroupTextChange: COLOR_CONTROL_HIGHLIGHT " + COLOR_CONTROL_HIGHLIGHT);
            Log.d(TAG, "GroupTextChange: COLOR_CONTROL_ACTIVATED " + COLOR_CONTROL_ACTIVATED);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            DrawableCompat.setTint(background, editable.length() == 0 ? COLOR_CONTROL_HIGHLIGHT : COLOR_CONTROL_ACTIVATED);
        }
    }
}