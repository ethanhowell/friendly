package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView tvSignup = binding.tvSignup;
        tvSignup.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}