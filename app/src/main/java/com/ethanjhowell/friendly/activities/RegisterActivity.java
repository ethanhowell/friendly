package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegisterBinding binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button btSignup = binding.btSignup;
        btSignup.setOnClickListener(v -> {
            // TODO: create new Parse User
            // TODO: Launch GroupActivity
        });
    }
}