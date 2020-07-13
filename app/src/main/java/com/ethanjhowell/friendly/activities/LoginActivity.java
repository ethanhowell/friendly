package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityLoginBinding;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    private static String TAG = LoginActivity.class.getCanonicalName();
    private static int REGISTER_ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // detect if user is already signed in
        if (ParseUser.getCurrentUser() != null) {
            Log.d(TAG, "onCreate: user already logged in");
            startActivity(new Intent(this, GroupActivity.class));
            finish();
        }

        binding.btLogin.setOnClickListener(v -> ParseUser.logInInBackground(binding.etUsername.getText().toString(),
                binding.etPassword.getText().toString(),
                (user, e) -> {
                    // there was a log in problem
                    if (e != null) {
                        Log.e(TAG, "onCreate: log in problem ", e);
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(this, GroupActivity.class));
                        finish();
                    }
                }));

        TextView tvSignup = binding.tvSignup;
        tvSignup.setOnClickListener(v -> startActivityForResult(
                new Intent(this, RegisterActivity.class),
                REGISTER_ACTIVITY_REQUEST_CODE)
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("onActivityResult: requestCode = %d, resultCode = %d", requestCode, resultCode));
        if (requestCode == REGISTER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // if registration was a success then we can launch the group activity
            startActivity(new Intent(this, GroupActivity.class));
            finish();
        }
    }
}