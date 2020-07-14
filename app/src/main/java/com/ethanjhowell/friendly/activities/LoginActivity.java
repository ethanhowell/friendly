package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityLoginBinding;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static String TAG = LoginActivity.class.getCanonicalName();
    public static int REGISTER_ACTIVITY_REQUEST_CODE = 1;

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // detect if user is already signed in
        if (ParseUser.getCurrentUser() != null) {
            Log.d(TAG, "onCreate: user already logged in");
            startActivity(new Intent(this, GroupActivity.class));
            finish();
        }

        binding.btFacebookLogin.setOnClickListener(this::facebookLoginOnClick);
        binding.btLogin.setOnClickListener(this::loginButtonOnClick);

        TextView tvSignup = binding.tvSignup;
        tvSignup.setOnClickListener(this::registrationOnClick);
    }

    private void facebookLoginOnClick(View v) {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(
                this,
                Arrays.asList(),
                (user, e) -> {
                    if (user == null) {
                        Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                        Log.e(TAG, "onCreate: ", e);
                    } else {
                        if (user.isNew()) {
                            Log.d(TAG, "User signed up and logged in through Facebook!");
                        }
                        Log.d(TAG, "User logged in through Facebook!");
                        startActivity(new Intent(this, GroupActivity.class));
                        finish();
                    }
                });
    }

    private void loginButtonOnClick(View v) {
        ParseUser.logInInBackground(
                binding.etUsername.getText().toString(),
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
                }
        );
    }

    private void registrationOnClick(View v) {
        startActivityForResult(
                new Intent(this, RegisterActivity.class),
                REGISTER_ACTIVITY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("onActivityResult: requestCode = %d, resultCode = %d", requestCode, resultCode));
        if (requestCode == REGISTER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // if registration was a success then we can launch the group activity
            startActivity(new Intent(this, GroupActivity.class));
            finish();
        }
    }
}