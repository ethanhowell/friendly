package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityLoginBinding;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;

import org.json.JSONException;

import java.util.Collections;

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
        FriendlyParseUser user = FriendlyParseUser.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "onCreate: user already logged in");
            startNextActivity(user);
        } else {
            binding.btFacebookLogin.setOnClickListener(
                    v -> ParseFacebookUtils.logInWithReadPermissionsInBackground(
                            this,
                            Collections.singletonList("email"),
                            this::facebookLoginCallback
                    )
            );
            binding.btLogin.setOnClickListener(this::loginButtonOnClick);
            binding.tvSignup.setOnClickListener(this::registrationOnClick);
        }
    }

    private void updateUserInfoFromFacebook(FriendlyParseUser user) {
        Profile currentProfile = Profile.getCurrentProfile();
        user.setFirstName(currentProfile.getFirstName());
        user.setLastName(currentProfile.getLastName());
        // get the email from the facebook api
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                (object, response) -> {
                    Log.d(TAG, "updateUserInfoFromFacebook: " + response.toString());
                    String email;
                    try {
                        email = object.getString("email");
                    } catch (JSONException e) {
                        Log.e(TAG, "updateUserInfoFromFacebook: ", e);
                        return;
                    }
                    user.setEmail(email);
                    user.saveInBackground(e1 -> {
                        if (e1 != null) {
                            Log.e(TAG, "updateUserInfoFromFacebook: problem saving user profile info ", e1);
                        }
                        startNextActivity(user);
                    });
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
        // TODO: Some sort of intermediate loading bar
    }


    private void facebookLoginCallback(ParseUser parseUser, ParseException e) {
        if (parseUser == null) {
            Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
            Log.e(TAG, "facebookLoginOnClick: ", e);
        } else {
            FriendlyParseUser user = FriendlyParseUser.fromParseUser(parseUser);
            if (user.isNew()) {
                updateUserInfoFromFacebook(user);
                Log.d(TAG, "User signed up and logged in through Facebook!");
            } else {
                Log.d(TAG, "User logged in through Facebook!");
                startNextActivity(user);
            }
        }

    }

    private void startNextActivity(FriendlyParseUser currentUser) {
        if (currentUser.isCompleted())
            startActivity(new Intent(this, GroupActivity.class));
        else
            // still missing fields to fill out
            startActivity(new Intent(this, NewUserActivity.class));
        finish();
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
                        startNextActivity(FriendlyParseUser.fromParseUser(user));
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
        Log.d(TAG, String.format("onActivityResult: requestCode = %d, resultCode = %d", requestCode, resultCode));
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // if registration was a success then we can launch the group activity
            startNextActivity(FriendlyParseUser.getCurrentUser());
        }
    }
}