package com.ethanjhowell.friendly.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ethanjhowell.friendly.databinding.ActivityNewUserBinding;

public class NewUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNewUserBinding binding = ActivityNewUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}