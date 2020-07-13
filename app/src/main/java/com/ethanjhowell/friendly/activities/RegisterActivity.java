package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
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
        // TODO: create new Parse User
        // TODO: Launch GroupActivity

    }
}