package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityRegisterBinding;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;

public class RegisterActivity extends AppCompatActivity {
    private final static String TAG = RegisterActivity.class.getCanonicalName();
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegisterBinding binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getAllViews(binding);

        btSignup.setOnClickListener(this::registerNewUser);
    }

    private void getAllViews(ActivityRegisterBinding binding) {
        etFirstName = binding.etFirstName;
        etLastName = binding.etLastName;
        etEmail = binding.etEmail;
        etPassword = binding.etPassword;
        etConfirmPassword = binding.etConfirmPassword;
        btSignup = binding.btSignup;
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private void registerNewUser(View button) {
        // TODO: some sort of way to indicate to the user this is happening, maybe freeze the button and have loading circle
        // fill out fields from the views
        FriendlyParseUser user = new FriendlyParseUser();
        // TODO: check first name not empty
        user.setFirstName(etFirstName.getText().toString());
        // TODO: check last name not empty
        user.setLastName(etLastName.getText().toString());
        // TODO: Email validation
        user.setEmail(etEmail.getText().toString());
        user.setUsername(etEmail.getText().toString());
        // TODO: Check that password and confirm password not empty and match
        user.setPassword(etPassword.getText().toString());

        user.signUpInBackground(e -> {
            // if there was an error
            if (e != null) {
                showError(e.getMessage());
                Log.e(TAG, "registerNewUser: something happened", e);
            } else {
                Log.i(TAG, "registerNewUser: registration success!!!");
                // tell the login activity that registration was a success
                setResult(RESULT_OK);
                finish();
            }
        });

    }
}