package com.ethanjhowell.friendly.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.databinding.ActivityRegisterBinding;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private final static String TAG = RegisterActivity.class.getCanonicalName();
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btSignup;
    private ConstraintLayout loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegisterBinding binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getAllViews(binding);

        Toolbar toolbar = binding.includeToolbar.toolbar;
        toolbar.setTitle(R.string.activity_register_title);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        btSignup.setOnClickListener(this::registerNewUser);
    }

    private void getAllViews(ActivityRegisterBinding binding) {
        etFirstName = binding.etFirstName;
        etLastName = binding.etLastName;
        etEmail = binding.etEmail;
        etPassword = binding.etPassword;
        etConfirmPassword = binding.etConfirmPassword;
        btSignup = binding.btSignup;

        loading = binding.loading.clProgress;
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private void showError(int errorID) {
        Toast.makeText(this, errorID, Toast.LENGTH_SHORT).show();
    }

    // returns null if there is a problem with the user validation
    private FriendlyParseUser validateUserCreation() {
        // fill out fields from the views
        String firstName = etFirstName.getText().toString();
        if (firstName.isEmpty()) {
            showError(R.string.etFirstName_invalid_toast);
            return null;
        }

        String lastName = etLastName.getText().toString();
        if (lastName.isEmpty()) {
            showError(R.string.etLastName_invalid_toast);
            return null;
        }

        String email = etEmail.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(R.string.etEmail_invalid_toast);
            return null;
        }

        String password = etPassword.getText().toString();
        if (password.isEmpty() || !password.equals(etConfirmPassword.getText().toString())) {
            showError(R.string.etPassword_invalidRegister_toast);
            return null;
        }

        FriendlyParseUser user = new FriendlyParseUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(password);
        return user;
    }

    private void registerNewUser(View button) {
        FriendlyParseUser user = validateUserCreation();
        if (user != null) {
            loading.setVisibility(View.VISIBLE);
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
                loading.setVisibility(View.GONE);
            });
        }
    }
}