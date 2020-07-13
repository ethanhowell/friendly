package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityRegisterBinding;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;

public class RegisterActivity extends AppCompatActivity {
    private static String TAG = RegisterActivity.class.getCanonicalName();
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhoneNumber;
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
        etPhoneNumber = binding.etPhoneNumber;
        etPassword = binding.etPassword;
        etConfirmPassword = binding.etConfirmPassword;
        btSignup = binding.btSignup;
    }

    private void registerNewUser(View button) {
        // TODO: some sort of way to indicate to the user this is happening, maybe freeze the button and have loading circle
        FriendlyParseUser user = new FriendlyParseUser();
        user.setFirstName(etFirstName.getText().toString());
        user.setLastName(etLastName.getText().toString());
        // TODO: Email validation
        user.setEmail(etEmail.getText().toString());
        user.setUsername(etEmail.getText().toString());
        // TODO: Check that password and confirm password match
        user.setPassword(etPassword.getText().toString());
        // TODO: phone number validation
        user.setPhoneNumber(etPhoneNumber.getText().toString());

        user.signUpInBackground(e -> {
            if (e != null) {
                // TODO: handle exception
                Log.e(TAG, "registerNewUser: something happened", e);
            } else {
                Log.i(TAG, "registerNewUser: registration success!!!");
                // TODO: Launch GroupActivity
                startActivity(new Intent(this, GroupActivity.class));
                finish();
            }
        });

    }
}