package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityLoginBinding;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        if (ParseUser.getCurrentUser() != null) {
            Log.d(TAG, "onCreate: user already logged in");
            startGroupActivity();
        }

        binding.btFacebookLogin.setOnClickListener(
                v -> ParseFacebookUtils.logInWithReadPermissionsInBackground(
                        this,
                        Collections.singletonList("email"),
                        this::facebookLoginCallback
                )
        );
        binding.btLogin.setOnClickListener(this::loginButtonOnClick);

        TextView tvSignup = binding.tvSignup;
        tvSignup.setOnClickListener(this::registrationOnClick);
    }

    public ParseFile parseFileFromUrl(Uri uri) throws IOException {
        InputStream inputStream = new URL(uri.toString()).openStream();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int current;

        while ((current = inputStream.read()) != -1) {
            byteStream.write(current);
        }

        return new ParseFile(byteStream.toByteArray());
    }

    private void facebookLoginCallback(ParseUser parseUser, ParseException e) {
        if (parseUser == null) {
            Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
            Log.e(TAG, "facebookLoginOnClick: ", e);
        } else {
            FriendlyParseUser user = new FriendlyParseUser(parseUser);
            if (user.isNew()) {
                Profile currentProfile = Profile.getCurrentProfile();
                user.setFirstName(currentProfile.getFirstName());
                user.setLastName(currentProfile.getLastName());
                try {
                    user.setProfilePicture(parseFileFromUrl(currentProfile.getProfilePictureUri(300, 300)));
                } catch (IOException ex) {
                    Log.e(TAG, "facebookLoginCallback: ", ex);
                }
                parseUser.saveInBackground(e1 -> {
                    if (e1 != null) {
                        Log.e(TAG, "facebookLoginOnClick: problem saving user profile info ", e1);
                    }
                    startGroupActivity();
                });
                // TODO: Some sort of intermediate loading bar
                Log.d(TAG, "User signed up and logged in through Facebook!");
            } else {
                Log.d(TAG, "User logged in through Facebook!");
                startGroupActivity();
            }
        }

    }

    private void startGroupActivity() {
        startActivity(new Intent(this, GroupActivity.class));
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
                        startGroupActivity();
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
            startGroupActivity();
        }
    }
}