package com.ethanjhowell.friendly.proxy;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.Objects;

public class FriendlyParseUser {
    // class that wraps ParseUser in order to provide convenience methods for setting additional fields
    public final static String KEY_FIRST_NAME = "firstName";
    public final static String KEY_LAST_NAME = "lastName";
    public final static String KEY_PROFILE_PICTURE = "profilePicture";
    public final static String KEY_PHONE_NUMBER = "phoneNumber";
    public final static String KEY_IS_COMPLETED = "isCompleted";
    private static final String TAG = FriendlyParseUser.class.getCanonicalName();

    private ParseUser user;

    private FriendlyParseUser(ParseUser parseUser) {
        user = parseUser;
    }

    public static FriendlyParseUser fromParseUser(ParseUser parseUser) {
        if (parseUser == null)
            return null;
        else {
            try {
                return new FriendlyParseUser(parseUser.fetchIfNeeded());
            } catch (ParseException e) {
                Log.e(TAG, "fromParseUser: problem fetching user", e);
                return null;
            }
        }
    }

    public FriendlyParseUser() {
        this(new ParseUser());
    }

    public static FriendlyParseUser getCurrentUser() {
        return fromParseUser(ParseUser.getCurrentUser());
    }

    public String getFirstName() {
        return user.getString(KEY_FIRST_NAME);
    }

    public void setFirstName(String firstName) {
        user.put(KEY_FIRST_NAME, firstName);
    }

    public String getLastName() {
        return user.getString(KEY_LAST_NAME);
    }

    public void setLastName(String lastName) {
        user.put(KEY_LAST_NAME, lastName);
    }

    public ParseFile getProfilePicture() {
        return user.getParseFile(KEY_PROFILE_PICTURE);
    }

    public void setProfilePicture(ParseFile profilePicture) {
        user.put(KEY_PROFILE_PICTURE, profilePicture);
    }

    public String getPhoneNumber() {
        return user.getString(KEY_PHONE_NUMBER);
    }

    public void setPhoneNumber(String phoneNumber) {
        user.put(KEY_PHONE_NUMBER, phoneNumber);
    }

    public boolean isCompleted() {
        return user.getBoolean(KEY_IS_COMPLETED);
    }

    public void isCompleted(boolean completed) {
        user.put(KEY_IS_COMPLETED, completed);
    }

    public void setEmail(String email) {
        user.setEmail(email);
    }

    public void setUsername(String username) {
        user.setUsername(username);
    }

    public void setPassword(String password) {
        user.setPassword(password);
    }

    public void signUpInBackground(SignUpCallback callback) {
        user.signUpInBackground(callback);
    }

    public boolean isNew() {
        return user.isNew();
    }

    public void saveInBackground(SaveCallback callback) {
        user.saveInBackground(callback);
    }

    public ParseUser getParseUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendlyParseUser that = (FriendlyParseUser) o;
        return user.getObjectId().equals(that.user.getObjectId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getObjectId());
    }
}
