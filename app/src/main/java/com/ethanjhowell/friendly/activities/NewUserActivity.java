package com.ethanjhowell.friendly.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ethanjhowell.friendly.R;
import com.ethanjhowell.friendly.databinding.ActivityNewUserBinding;
import com.ethanjhowell.friendly.proxy.FriendlyParseUser;
import com.facebook.Profile;
import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class NewUserActivity extends AppCompatActivity {
    private final static String TAG = NewUserActivity.class.getCanonicalName();
    private final static int ACTION_IMAGE_CAPTURE_REQUEST_CODE = 2;
    private final static String PHOTO_FILE_NAME = "photo.png";
    private File photoFile;
    private ActivityNewUserBinding binding;
    private ParseFile parsePhotoFile;
    private int COLOR_CONTROL_HIGHLIGHT;
    private int COLOR_CONTROL_ACTIVATED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Profile currentProfile = Profile.getCurrentProfile();
        if (currentProfile == null) {
            Log.d(TAG, "onCreate: logged in via parse");
        } else {
            Log.d(TAG, "onCreate: logged in via facebook");
            loadFacebookImage(currentProfile);
        }

        binding.btContinue.setOnClickListener(this::continueOnClick);
        binding.btContinue.setClickable(false);
        loadColors();
        binding.etPhoneNumber.addTextChangedListener(new InputTextWatcher());
        binding.clChooseProfilePic.setOnClickListener(this::launchCamera);
    }

    private void loadColors() {
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(R.attr.colorControlHighlight, typedValue, true);
        COLOR_CONTROL_HIGHLIGHT = typedValue.data;

        this.getTheme().resolveAttribute(R.attr.colorControlActivated, typedValue, true);
        COLOR_CONTROL_ACTIVATED = typedValue.data;
    }

    private File getPhotoFileUri() {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "getPhotoFileUri: failed to create directory");
        }

        return new File(mediaStorageDir.getPath() + File.separator + PHOTO_FILE_NAME);
    }

    private void launchCamera(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri();

        // wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(
                this,
                "com.ethanjhowell.friendly.fileprovider",
                photoFile
        );
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // check that there is a camera app that can handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, ACTION_IMAGE_CAPTURE_REQUEST_CODE);
        }
    }


    private void loadFacebookImage(Profile profile) {
        Glide.with(this)
                .asBitmap()
                .load(profile.getProfilePictureUri(300, 300).toString())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        binding.ivProfilePic.setImageBitmap(resource);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        parsePhotoFile = new ParseFile(stream.toByteArray());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_IMAGE_CAPTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                binding.ivProfilePic.setImageBitmap(
                        BitmapFactory.decodeFile(photoFile.getAbsolutePath())
                );
                parsePhotoFile = new ParseFile(photoFile);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class InputTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String phoneNumber = binding.etPhoneNumber.getText().toString();
            phoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
            if (phoneNumber.isEmpty() || !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
                binding.btContinue.setClickable(false);
                DrawableCompat.setTint(binding.btContinue.getBackground(), COLOR_CONTROL_HIGHLIGHT);
            } else {
                binding.btContinue.setClickable(true);
                DrawableCompat.setTint(binding.btContinue.getBackground(), COLOR_CONTROL_ACTIVATED);
            }
        }
    }

    private void continueOnClick(View v) {
        if (parsePhotoFile != null) {
            String phoneNumber = binding.etPhoneNumber.getText().toString();
            phoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);

            binding.loading.clProgress.setVisibility(View.VISIBLE);
            FriendlyParseUser user = FriendlyParseUser.getCurrentUser();
            user.setProfilePicture(parsePhotoFile);
            user.setPhoneNumber(phoneNumber);
            user.isCompleted(true);
            user.saveInBackground(e -> {
                if (e != null) {
                    Log.e(TAG, "continueOnClick: problem saving user data", e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    // by now user account is completely created, we can navigate to the next activity
                    startActivity(new Intent(this, GroupActivity.class));
                    finish();
                }
                binding.loading.clProgress.setVisibility(View.GONE);
            });
        } else {
            Toast.makeText(this, R.string.selectImage_toast, Toast.LENGTH_SHORT).show();
        }
    }


}